package com.auto.controller;

import com.auto.common.BaseContext;
import com.auto.common.CustomException;
import com.auto.common.R;
import com.auto.constant.MessageConstant;
import com.auto.constant.OssConstant;
import com.auto.constant.RedisConstant;
import com.auto.dto.AnalysisRequest;
import com.auto.dto.PredictionResultDTO;
import com.auto.dto.TrainResultDTO;
import com.auto.entity.PretrainForm;
import com.auto.service.PredictResultService;
import com.auto.service.impl.NetworkProcessingService;
import com.auto.service.impl.SnapshotAnalysisService;
import com.auto.util.AliOssUtil;
import com.auto.vo.AnalysisResponse;
import com.auto.vo.NetworkResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/network")
public class NetworkController {
    @Autowired
    private NetworkProcessingService networkService;
    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private SnapshotAnalysisService analysisService;
    @Autowired
    private PredictResultService predictResultService;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${python.service.url}")
    private String pythonServiceUrl;

    @Autowired
    @Qualifier("taskExecutor")  // 指定 Bean 名称
    private ThreadPoolTaskExecutor executor;

    /**
     * 上传原始数据并构建网络
     * @param file
     * @param timestampFormat
     * @return
     */
    @PostMapping("/upload")
    public R<NetworkResponse> uploadAndBuild(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "timestampFormat", defaultValue = "yyyy-MM-dd HH:mm:ssXXX") String timestampFormat) {
        NetworkResponse response = null;
        try {
            response = networkService.processUpload(file, timestampFormat);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("构建网络失败");
        }
        return R.success(response);
    }

    /**
     * 下载样例原始数据
     * @param response
     */
    @GetMapping("/downloadSample")
    public void downSample(HttpServletResponse response) {
        try {
            aliOssUtil.download(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/downloadEdgeList")
    public void downloadEdgeList(HttpServletResponse response) {
        long userId = BaseContext.getCurrentId();
        String fileUrl = stringRedisTemplate.opsForValue().get(RedisConstant.USER_EDGELIST_RESULT+userId);
        if(fileUrl!=null){
            int index = fileUrl.indexOf('/', 8); // 从第8位开始找（跳过 https://）

            // 提取 key（第三个 / 之后的内容）
            String fileUrlKey = fileUrl.substring(index + 1);
            try {
                aliOssUtil.downloadEdgeList(response, fileUrlKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            throw new CustomException("没有找到对应数据");
        }
    }

    @GetMapping("/analyze")
    public R<AnalysisResponse> analyzeSnapshots(@RequestParam(defaultValue = "30m") String windowSize) {
        try {
            AnalysisResponse response = analysisService.analyzeSnapshots(windowSize);
            return R.success(response);
        } catch (Exception e){
            return R.error("提取特征错误");
        }
    }

    /**
     * 下载节点层面特征数据
     * @param response
     */
    @GetMapping("/downloadNode")
    public void downloadNode(HttpServletResponse response) {
        long userId = BaseContext.getCurrentId();
        String fileUrl = stringRedisTemplate.opsForValue().get(RedisConstant.USER_FEATURES_X_RESULT+userId);
        if(fileUrl!=null){
            int index = fileUrl.indexOf('/', 8); // 从第8位开始找（跳过 https://）

            // 提取 key（第三个 / 之后的内容）
            String fileUrlKey = fileUrl.substring(index + 1);
            try {
                aliOssUtil.downloadEdgeList(response, fileUrlKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            throw new CustomException("没有找到对应数据");
        }
    }

    /**
     * 下载网络层面特征数据
     * @param response
     */
    @GetMapping("/downloadNetwork")
    public void downloadNetwork(HttpServletResponse response) {
        long userId = BaseContext.getCurrentId();
        String fileUrl = stringRedisTemplate.opsForValue().get(RedisConstant.USER_FEATURES_GLOBAL_RESULT+userId);
        if(fileUrl!=null){
            int index = fileUrl.indexOf('/', 8); // 从第8位开始找（跳过 https://）

            // 提取 key（第三个 / 之后的内容）
            String fileUrlKey = fileUrl.substring(index + 1);
            try {
                aliOssUtil.downloadEdgeList(response, fileUrlKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            throw new CustomException("没有找到对应数据");
        }
    }

    /**
     * 用于预训练模型
     * @param pretrainForm
     * @return
     */
    @PostMapping("/pretrain")
    public R<String> preTrain(@RequestBody PretrainForm pretrainForm){
        long userId = BaseContext.getCurrentId();
        String fileXUrl = stringRedisTemplate.opsForValue().get(RedisConstant.USER_FEATURES_X_RESULT+userId);
        String fileGlobalUrl = stringRedisTemplate.opsForValue().get(RedisConstant.USER_FEATURES_GLOBAL_RESULT+userId);
        log.info("fileXUrl: {}",fileXUrl);
        log.info("fileGlobalUrl: {}", fileGlobalUrl);
        log.info("window_size {}", pretrainForm.getWindowSize());
        log.info("num_epochs {}", pretrainForm.getEpochs());
        // 2. 调用Flask服务
        Map<String, Object> pyRequest = new HashMap<>();
        pyRequest.put("file_x_url", fileXUrl);
        pyRequest.put("file_global_url", fileGlobalUrl);
        pyRequest.put("window_size", pretrainForm.getWindowSize());
        pyRequest.put("epochs", pretrainForm.getEpochs());
        // 这里暂时使用restTemplate进行服务间的调用看看效果
        try{
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    pythonServiceUrl + "/pretrain",
                    pyRequest,
                    Map.class
            );
            // 打印返回结果（便于调试）
            log.info("python服务返回结果：{}", response.getBody());

            // 解析返回结果（关键步骤）
            if (response.getBody() == null) {
                log.error("Python服务返回结果为空");
                return R.error("调用Python服务失败：返回结果为空");
            }

            // 先校验返回码
            Integer code = (Integer) response.getBody().get("code");
            if (code == null || code != 200) {
                String message = (String) response.getBody().get("message");
                log.error("Python服务返回异常：code={}, message={}", code, message);
                return R.error("调用Python服务失败：" + (message != null ? message : "未知错误"));
            }

            // 提取data字段（直接取字符串，而非数组）
            String pretrain_url = (String) response.getBody().get("data");

            // 校验URL是否为空
            if (pretrain_url == null || pretrain_url.trim().isEmpty()) {
                log.error("Python服务返回的data字段为空");
                return R.error("调用Python服务失败：返回的模型URL为空");
            }
            // 存储模型地址
            String preTrainUrlKey = RedisConstant.USER_PRETRAIN_MODEL+userId;

            // 获取旧任务 URL（旧值）原子获取旧值并设置新值
            String oldOssUrl = stringRedisTemplate.opsForValue().getAndSet(preTrainUrlKey, pretrain_url);

            // 异步删除旧 OSS 文件（不阻塞新任务）并做对应的判断 避免出现线程安全问题
            if (oldOssUrl != null && !oldOssUrl.equals(pretrain_url)) {
                executor.execute(() -> {
                    try {
                        aliOssUtil.deleteByUrl(oldOssUrl);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            // 存储之前设置的windowSize
            String windowSizeKey = RedisConstant.USER_WINDOW_SIZE+userId;
            stringRedisTemplate.opsForValue().getAndSet(windowSizeKey, String.valueOf(pretrainForm.getWindowSize()));
            // 返回成功结果
            return R.success("保存结果成功：" + pretrain_url);
        }catch (Exception e) {
            // 6. 异常捕获（避免程序崩溃）
            log.error("调用Python服务/解析结果失败", e);
            return R.error("调用Python服务失败：" + e.getMessage());
        }
    }

    /**
     * 下载预训练的模型
     * @param response
     */
    @GetMapping("/downloadPreTrain")
    public void downloadPretrain(HttpServletResponse response) {
        long userId = BaseContext.getCurrentId();
        String fileUrl = stringRedisTemplate.opsForValue().get(RedisConstant.USER_PRETRAIN_MODEL+userId);
        if(fileUrl!=null){
            int index = fileUrl.indexOf('/', 8); // 从第8位开始找（跳过 https://）

            // 提取 key（第三个 / 之后的内容）
            String fileUrlKey = fileUrl.substring(index + 1);
            try {
                aliOssUtil.downloadEdgeList(response, fileUrlKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            throw new CustomException("没有找到对应数据");
        }
    }


    /**
     * 用于训练模型
     * @param epochs
     * @return
     */
    @PostMapping("/train")
    public R<String> Train(@RequestBody Integer epochs){
        long userId = BaseContext.getCurrentId();
        String fileXUrl = stringRedisTemplate.opsForValue().get(RedisConstant.USER_FEATURES_X_RESULT+userId);
        String fileGlobalUrl = stringRedisTemplate.opsForValue().get(RedisConstant.USER_FEATURES_GLOBAL_RESULT+userId);
        Integer windowSize = Integer.parseInt(stringRedisTemplate.opsForValue().get(RedisConstant.USER_WINDOW_SIZE+userId));
        String feature_selector_url = stringRedisTemplate.opsForValue().get(RedisConstant.USER_PRETRAIN_MODEL+userId);
        log.info("epochs: {}", epochs);
        log.info("fileXUrl: {}",fileXUrl);
        log.info("fileGlobalUrl: {}", fileGlobalUrl);
        // 2. 调用Flask服务
        Map<String, Object> pyRequest = new HashMap<>();
        pyRequest.put("file_x_url", fileXUrl);
        pyRequest.put("file_global_url", fileGlobalUrl);
        pyRequest.put("feature_selector_url", feature_selector_url);

        pyRequest.put("window_size", windowSize);
        pyRequest.put("epochs", epochs);
        // 这里暂时使用restTemplate进行服务间的调用看看效果
        try{
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    pythonServiceUrl + "/train",
                    pyRequest,
                    Map.class
            );
            // 打印返回结果（便于调试）
            log.info("python服务返回结果：{}", response.getBody());

            // 解析返回结果（关键步骤）
            if (response.getBody() == null) {
                log.error("Python服务返回结果为空");
                return R.error("调用Python服务失败：返回结果为空");
            }

            // 先校验返回码
            Integer code = (Integer) response.getBody().get("code");
            if (code == null || code != 200) {
                String message = (String) response.getBody().get("message");
                log.error("Python服务返回异常：code={}, message={}", code, message);
                return R.error("调用Python服务失败：" + (message != null ? message : "未知错误"));
            }

            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            TrainResultDTO result = TrainResultDTO.fromMap(data);
            String featureSelectorKey = RedisConstant.USER_FEATURE_SELECTOR+userId;
            String gruKey = RedisConstant.USER_MODEL+userId;
            // 获取旧任务 URL（旧值）原子获取旧值并设置新值
            String oldFeatureSelectorUrl = stringRedisTemplate.opsForValue().getAndSet(featureSelectorKey, result.getFeature_selector_url());
            String oldGruUrl = stringRedisTemplate.opsForValue().getAndSet(gruKey, result.getModel_url());

            // 异步删除旧 OSS 文件（不阻塞新任务）并做对应的判断 避免出现线程安全问题
            if (oldFeatureSelectorUrl != null && !oldFeatureSelectorUrl.equals(result.getFeature_selector_url())) {
                executor.execute(() -> {
                    try {
                        aliOssUtil.deleteByUrl(oldFeatureSelectorUrl);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            if (oldGruUrl != null && !oldGruUrl.equals(result.getModel_url())) {
                executor.execute(() -> {
                    try {
                        aliOssUtil.deleteByUrl(oldGruUrl);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            // 返回成功结果
            return R.success("保存结果成功：" + result);
        }catch (Exception e) {
            // 6. 异常捕获（避免程序崩溃）
            log.error("调用Python服务/解析结果失败", e);
            return R.error("调用Python服务失败：" + e.getMessage());
        }
    }


    /**
     * 下载训练的模型
     * @param response
     */
    @GetMapping("/downloadTrain")
    public void downloadTrain(HttpServletResponse response) {
        long userId = BaseContext.getCurrentId();
        String fileUrl = stringRedisTemplate.opsForValue().get(RedisConstant.USER_FEATURE_SELECTOR+userId);
        if(fileUrl!=null){
            int index = fileUrl.indexOf('/', 8); // 从第8位开始找（跳过 https://）

            // 提取 key（第三个 / 之后的内容）
            String fileUrlKey = fileUrl.substring(index + 1);
            try {
                aliOssUtil.downloadEdgeList(response, fileUrlKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            throw new CustomException("没有找到对应数据");
        }
    }

    /**
     * 下载预训练的模型
     * @param response
     */
    @GetMapping("/downloadTrainModel")
    public void downloadTrainModel(HttpServletResponse response) {
        long userId = BaseContext.getCurrentId();
        String fileUrl = stringRedisTemplate.opsForValue().get(RedisConstant.USER_MODEL+userId);
        if(fileUrl!=null){
            int index = fileUrl.indexOf('/', 8); // 从第8位开始找（跳过 https://）

            // 提取 key（第三个 / 之后的内容）
            String fileUrlKey = fileUrl.substring(index + 1);
            try {
                aliOssUtil.downloadEdgeList(response, fileUrlKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            throw new CustomException("没有找到对应数据");
        }
    }


    @PostMapping("/finalPredict")
    public R<String> predict(@RequestBody Integer threshold) {
        long userId = BaseContext.getCurrentId();
        String fileXUrl = stringRedisTemplate.opsForValue().get(RedisConstant.USER_FEATURES_X_RESULT+userId);
        String fileGlobalUrl = stringRedisTemplate.opsForValue().get(RedisConstant.USER_FEATURES_GLOBAL_RESULT+userId);
        Integer windowSize = Integer.parseInt(stringRedisTemplate.opsForValue().get(RedisConstant.USER_WINDOW_SIZE+userId));
        String feature_selector_url = stringRedisTemplate.opsForValue().get(RedisConstant.USER_FEATURE_SELECTOR +userId);
        String gru_url = stringRedisTemplate.opsForValue().get(RedisConstant.USER_MODEL +userId);
        log.info("threshold: {}", threshold);
        log.info("fileXUrl: {}",fileXUrl);
        log.info("fileGlobalUrl: {}", fileGlobalUrl);


        // 2. 调用Flask服务
        Map<String, Object> pyRequest = new HashMap<>();
        pyRequest.put("file_x_url", fileXUrl);
        pyRequest.put("file_global_url", fileGlobalUrl);
        pyRequest.put("feature_selector_url", feature_selector_url.substring(feature_selector_url.lastIndexOf('/')+1));
        pyRequest.put("model_url", gru_url.substring(gru_url.lastIndexOf('/')+1));
        log.info("feature_selector_url{}", feature_selector_url.substring(feature_selector_url.lastIndexOf('/')+1));
        log.info("model_url{}", gru_url.substring(gru_url.lastIndexOf('/')+1));
        pyRequest.put("window_size", windowSize);
        pyRequest.put("threshold", threshold);
        try {
            // 这里暂时使用restTemplate进行服务间的调用看看效果
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    pythonServiceUrl + "/finalPredict",
                    pyRequest,
                    Map.class
            );
            log.info("python服务返回结果：{}", response.getBody());
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            PredictionResultDTO result = PredictionResultDTO.fromMap(data);
            result.setThreshold(threshold);
            log.info("result: {}", result);
            // 3. 保存结果到数据库
            predictResultService.saveResult(result);
            return R.success("保存结果成功");
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(MessageConstant.SERVER_ERROR);
        }
    }






}
