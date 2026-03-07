package com.auto.vo;

import com.opencsv.CSVWriter;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 返回网络
 */
@Data
public class NetworkResponse {
    private String taskId;
    private Integer nodeCount;
    private Integer edgeCount;
    private Map<String, String> idMapping; // 整数ID -> 原始user_id
    private List<EdgeDTO> edges;
    private List<String> timeRange; // 应该是为了之后的窗口划分
    @Data
    public static class EdgeDTO {
        private Integer source;     // 编码后的源节点ID
        private Integer target;     // 编码后的目标节点ID
        private String type;        // "repost", "comment", "thumb", "original"
        private String timestamp;   // 原始时间字符串。注意：根据数据中的时间格式做更改
        private Long timestampMs;   // 转换为毫秒的时间戳（用于排序和计算） 注意：如果原始数据本身就是这种格式，则不需要转换
    }

}
