package com.auto.controller;

import com.auto.common.R;
import com.auto.constant.MessageConstant;
import com.auto.constant.OssConstant;
import com.auto.dto.PredictionResultDTO;
import com.auto.entity.PythonResponse;
import com.auto.service.PredictResultService;
import com.auto.util.AliOssUtil;
import com.auto.vo.PredictionResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class PredictController {

    @Value("${python.service.url}")
    private String pythonServiceUrl;
    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private PredictResultService predictResultService;
    /**
     * 注意 @RequestBody不能处理二进制流数据
     * @param files
     * @param threshold
     * @return
     */
    @PostMapping("/predict")
    public R<String> predict(@RequestBody @RequestParam("files") MultipartFile[] files,
                                         @RequestParam("threshold") String threshold) {
        MultipartFile xFile = null; // 微观特征文件
        MultipartFile addFile = null; // 社区行为文件
        MultipartFile globalFile = null; // 全局特征文件
        for(MultipartFile file : files){
            String fileName = file.getOriginalFilename();
            if(fileName.contains("_x")){
                xFile = file;
            }else if(fileName.contains("_add")){
                addFile = file;
            }else if(fileName.contains("_global")){
                globalFile = file;
            }
        }
        String fileXUrl = null;
        String fileAddUrl = null;
        String fileGlobalUrl = null;

        try {
            fileXUrl = aliOssUtil.upload(xFile.getBytes(), xFile.getOriginalFilename(), OssConstant.DATA_PARENT_PATH);
            fileAddUrl = aliOssUtil.upload(addFile.getBytes(), addFile.getOriginalFilename(), OssConstant.DATA_PARENT_PATH);
            fileGlobalUrl = aliOssUtil.upload(globalFile.getBytes(), globalFile.getOriginalFilename(), OssConstant.DATA_PARENT_PATH);
            if(fileXUrl == null || fileAddUrl == null || fileGlobalUrl == null){
                return R.error(MessageConstant.UPLOAD_FAILED);
            }
            int passThreshold = Integer.parseInt(threshold);
            // 2. 调用Flask服务
            Map<String, Object> pyRequest = new HashMap<>();
            pyRequest.put("file_x_url", fileXUrl);
            pyRequest.put("file_add_url", fileAddUrl);
            pyRequest.put("file_global_url", fileGlobalUrl);
            pyRequest.put("threshold", passThreshold);
            pyRequest.put("window_size", 7);
            // 这里暂时使用restTemplate进行服务间的调用看看效果
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    pythonServiceUrl + "/predict",
                    pyRequest,
                    Map.class
            );
            log.info("python服务返回结果：{}", response.getBody());
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            PredictionResultDTO result = PredictionResultDTO.fromMap(data);
            result.setThreshold(passThreshold);
            log.info("result: {}", result);
            // 3. 保存结果到数据库
            predictResultService.saveResult(result);
            return R.success("保存结果成功");
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(MessageConstant.SERVER_ERROR);
        } finally {
            try {
                aliOssUtil.deleteByUrl(fileXUrl);
                aliOssUtil.deleteByUrl(fileAddUrl);
                aliOssUtil.deleteByUrl(fileGlobalUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
