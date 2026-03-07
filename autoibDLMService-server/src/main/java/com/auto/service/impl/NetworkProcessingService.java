package com.auto.service.impl;

import com.auto.common.BaseContext;
import com.auto.common.CustomException;
import com.auto.constant.OssConstant;
import com.auto.constant.RedisConstant;
import com.auto.util.AliOssUtil;
import com.auto.vo.NetworkResponse;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class NetworkProcessingService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private AliOssUtil aliOssUtil;

    @Autowired
    @Qualifier("taskExecutor")  // 指定 Bean 名称
    private ThreadPoolTaskExecutor executor;

    // 处理上传文件，构建网络
    public NetworkResponse processUpload(MultipartFile file, String timestampFormat) throws Exception {
        // 1. 读取CSV
        CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()));
        // 按行读取
        List<String[]> rows = reader.readAll();

        // 2. 初始化数据结构
        Map<String, Integer> userToIdMap = new HashMap<>(); // 用户ID -> 整数ID(重新映射)
        Map<String, String> weiboTouserMap = new HashMap<>(); // 微博ID与用户ID的映射 为了之后重新将整数ID与用户ID做映射
        List<NetworkResponse.EdgeDTO> edges = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(timestampFormat); //注意：根据实际的时间格式做更改

        // 3. 第一遍扫描：收集所有用户ID
        for (int i = 1; i < rows.size(); i++) { //跳过第一行，因为第一行是表头
            String[] row = rows.get(i);
            String userId = row[4]; // 注意：此处假设第4列是user_id，请根据实际情况做更改
            if (!userToIdMap.containsKey(userId)) {
                userToIdMap.put(userId, userToIdMap.size());
            }
            String weiboId = row[3]; //注意：此处假设第3列是weibo_id，请根据实际情况做更改
            if (!weiboTouserMap.containsKey(weiboId)) {
                weiboTouserMap.put(weiboId, userId);
            }
        }

        // 4. 第二遍扫描：构建边
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            String interactionType = row[0]; // 当前交互行为的类型
            String sourceUserId = row[4]; // 当前行为的user作为边的源节点
            // 根据交互的原weiboID找到对应的UserId
            String targetUserId = determineTargetUser(row[1], weiboTouserMap); //通过iteraction_id获取到被交互的微博/评论的用户id。此处假设第一列是interaction_id
            if (targetUserId == null) {
                //print 日志
                throw new CustomException("无法确定目标用户ID, 您上传的文件有误");
            }
            String timestampStr = row[6];

            // 转换为整数ID 注意这里有bug 就是当targetUserId是"00"的时候 即根节点
            // 这里将最大节点数作为其映射
            Integer sourceId = userToIdMap.get(sourceUserId);

            Integer targetId = userToIdMap.get(targetUserId);
            if(targetId==null){
                targetId = userToIdMap.size();
            }

            // 转换为毫秒时间戳。注意：需根据实际情况做更改
            Long timestampMs = convertToTimestamp(timestampStr, dtf);

            // 构建边对象
            NetworkResponse.EdgeDTO edge = new NetworkResponse.EdgeDTO();
            edge.setSource(sourceId);
            edge.setTarget(targetId);
            edge.setType(interactionType);
            edge.setTimestamp(timestampStr);
            edge.setTimestampMs(timestampMs);
            edges.add(edge);
        }

        // 5. 按时间排序
        edges.sort(Comparator.comparing(NetworkResponse.EdgeDTO::getTimestampMs));

        // 6. 构建响应
        NetworkResponse response = new NetworkResponse();
        String taskId = "task_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        response.setTaskId(taskId);
        response.setNodeCount(userToIdMap.size());
        response.setEdgeCount(edges.size());

        // 构建ID映射（整数->原始）
        Map<String, String> idMapping = new HashMap<>();
        for (Map.Entry<String, Integer> entry : userToIdMap.entrySet()) {
            idMapping.put(entry.getValue().toString(), entry.getKey());
        }
        // 增加根节点的映射
        idMapping.put(""+userToIdMap.size(), "00");
        response.setIdMapping(idMapping);
        response.setEdges(edges);

        // 时间范围
        if (!edges.isEmpty()) {
            response.setTimeRange(Arrays.asList(
                    edges.get(0).getTimestamp(),
                    edges.get(edges.size() - 1).getTimestamp() // 这里设置时间范围应该是为了之后的窗口划分
            ));
        }

        // 7. 存储到缓存
        // NetworkDataStorage.storeNetworkData(taskId, userToIdMap, edges);

        // TODO 存储当前的TaskId
        long userId = BaseContext.getCurrentId();
        String redis_TaskIdKey = RedisConstant.USER_TASKID_RESULT + userId;
        stringRedisTemplate.opsForValue().getAndSet(redis_TaskIdKey, taskId);
        // TODO 存储到edgeList
        String filePath= exportEdgesToCsv(edges,"edgeList.csv", OssConstant.EdgeList_PARENT_PATH + "/"+ BaseContext.getCurrentId() + "/" + taskId);


        String redis_EdgeListKey = RedisConstant.USER_EDGELIST_RESULT + userId;

        // 7.1. 获取旧任务 URL（旧值）原子获取旧值并设置新值
        String oldOssUrl = stringRedisTemplate.opsForValue().getAndSet(redis_EdgeListKey, filePath);

        // 7.2. 异步删除旧 OSS 文件（不阻塞新任务）并做对应的判断 避免出现线程安全问题
        if (oldOssUrl != null && !oldOssUrl.equals(filePath)) {
            executor.execute(() -> {
                try {
                    aliOssUtil.deleteByUrl(oldOssUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        return response;
    }

    // 通过交互的weibo_id确定target user
    private String determineTargetUser(String interactionId, Map<String, String> weiboTouserMap) {
        // 1. 如果是原创微博，返回 "00"
        if (interactionId == null || interactionId.trim().isEmpty()) {
            return "00"; // "00作为根节点"
        }
        // 2. 如果是转发/评论，通过interaction_id找到原微博作者
        return weiboTouserMap.get(interactionId);
    }
    //注意：根据实际的时间格式选择使用或更改
    private Long convertToTimestamp(String timestampStr, DateTimeFormatter dtf) {
        try {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(timestampStr, dtf);
            // 直接转时间戳（自动转换为 UTC 毫秒）
            return offsetDateTime.toInstant().toEpochMilli();
        } catch (Exception e) {
            // return System.currentTimeMillis(); // 失败时返回当前时间 不正确 对于后期的处理
            throw new CustomException("时间转换失败, 请检查数据的时间格式");
        }
    }

    // 导出 edges 到 CSV 的方法
    private String exportEdgesToCsv(List<NetworkResponse.EdgeDTO>edges, String originalFilename, String parentPath) throws Exception {
        // 2. 生成 CSV 内容到内存（不写本地磁盘）
        byte[] csvContent = generateCsvBytes(edges);
        String filePath = aliOssUtil.upload(csvContent, originalFilename, parentPath);
        return filePath;
    }

    // 生成对应的边表csv字节数组 方便后续上传至OSS中
    private byte[] generateCsvBytes(List<NetworkResponse.EdgeDTO>edges) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (Writer writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer,
                     CSVWriter.DEFAULT_SEPARATOR,
                     CSVWriter.NO_QUOTE_CHARACTER,
                     CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                     CSVWriter.DEFAULT_LINE_END)) {

            // 表头
            String[] header = {"source", "target", "type", "timestamp", "timestampMs"};
            csvWriter.writeNext(header);

            // 数据行（假设 edges 是当前类的成员变量或通过参数传入）
            for (NetworkResponse.EdgeDTO edge : edges) {
                String[] row = {
                        String.valueOf(edge.getSource()),
                        String.valueOf(edge.getTarget()),
                        String.valueOf(edge.getType()),
                        String.valueOf(edge.getTimestamp()),
                        String.valueOf(edge.getTimestampMs())
                };
                csvWriter.writeNext(row);
            }
        }

        return baos.toByteArray();
    }
}
