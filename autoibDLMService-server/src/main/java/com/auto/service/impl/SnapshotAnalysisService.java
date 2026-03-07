package com.auto.service.impl;

import com.auto.common.BaseContext;
import com.auto.common.CustomException;
import com.auto.constant.FeatureConstant;
import com.auto.constant.FeatureTypeConstant;
import com.auto.constant.OssConstant;
import com.auto.constant.RedisConstant;
import com.auto.entity.MetricResult;
import com.auto.util.AliOssUtil;
import com.auto.util.GraphMetricsCalculator;
import com.auto.vo.AnalysisResponse;
import com.auto.vo.NetworkResponse;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class SnapshotAnalysisService {
    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    @Qualifier("taskExecutor")  // 指定 Bean 名称
    private ThreadPoolTaskExecutor executor;

    public AnalysisResponse analyzeSnapshots(String windowSize) throws Exception {
        // 1. 读取边列表数据 从redis缓存中读取对应的边列表文件
        long userId = BaseContext.getCurrentId();

        String taskId = stringRedisTemplate.opsForValue().get(RedisConstant.USER_TASKID_RESULT+userId);


        String fileUrl = stringRedisTemplate.opsForValue().get(RedisConstant.USER_EDGELIST_RESULT+userId);

        if(fileUrl==null || fileUrl.equals("")){
            throw new CustomException("当前任务已过期: ");
        }
        List<NetworkResponse.EdgeDTO> edges = aliOssUtil.readEdgesFromCsv(fileUrl);

        // 2. 转换时间窗口为毫秒。注意：根据实际时间决定是否转换以及如何转换
        long windowSizeMs = parseWindowSize(windowSize);

        // 3. 按时间窗口分组
        Map<Long, List<NetworkResponse.EdgeDTO>> snapshots = groupEdgesByWindow(edges, windowSizeMs);

        // 4. 为每个快照计算指标
        Graph<Integer, DefaultEdge> cumulativeGraph = createEmptyGraph(); //累加图
        List<AnalysisResponse.SnapshotMetrics> snapshotMetrics = new ArrayList<>();
        AnalysisResponse.SnapshotMetrics previousMetrics = null;
        for (Map.Entry<Long, List<NetworkResponse.EdgeDTO>> entry : snapshots.entrySet()) {
            Long windowStart = entry.getKey();
            List<NetworkResponse.EdgeDTO> snapshotEdges = entry.getValue();

            // 将新增加边添加到累加图中
            addEdgesToGraph(cumulativeGraph, snapshotEdges);

            // 计算网络层面指标
            AnalysisResponse.SnapshotMetrics.GraphMetrics graphMetrics = calculateGraphMetrics(cumulativeGraph);

            // 计算节点级指标
            AnalysisResponse.SnapshotMetrics.NodeMetrics nodeMetrics = calculateNodeMetrics(cumulativeGraph);

            //TODO 计算社区级指标
            //List<AnalysisResponse.SnapshotMetrics.ComMetrics> comMetrics = calculateComMetrics(cumulativeGraph);

            // 构建快照指标
            AnalysisResponse.SnapshotMetrics currentMetrics = new AnalysisResponse.SnapshotMetrics();
            currentMetrics.setWindowStart(windowStart);
            currentMetrics.setWindowStartISO(new Date(windowStart).toInstant().toString());
            currentMetrics.setGraphLevel(graphMetrics);
            currentMetrics.setNodeLevel(nodeMetrics);
            AnalysisResponse.SnapshotMetrics diffMetrics;

            diffMetrics = currentMetrics.subtract(previousMetrics);

            snapshotMetrics.add(diffMetrics);
            System.out.println(snapshotMetrics);
            previousMetrics = currentMetrics;
        }
        // 5. 构建响应
        AnalysisResponse response = new AnalysisResponse();
        response.setTaskId(taskId);
        response.setWindowSizeMs(windowSizeMs);
        response.setSnapshots(snapshotMetrics);

        // 6. 存储计算出的特征属性

        // TODO 存储节点层面属性
        String fileXPath= exportFeaturesToCsv(snapshotMetrics,"_x.csv", OssConstant.FEATURES_PARENT_PATH + "/"+ userId + "/" + taskId, FeatureTypeConstant.FEATURES_X);

        String redis_features_x_Key = RedisConstant.USER_FEATURES_X_RESULT + userId;

        String oldXUrl = stringRedisTemplate.opsForValue().getAndSet(redis_features_x_Key, fileXPath);

        // 7.2. 异步删除旧 OSS 文件（不阻塞新任务）
        if (oldXUrl != null && !oldXUrl.equals(fileXPath)) {
            executor.execute(() -> {
                try {
                    aliOssUtil.deleteByUrl(oldXUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // TODO 存储网络层面属性 这里先重复 后面再提取公共方法
        String fileGlobalPath= exportFeaturesToCsv(snapshotMetrics,"_global.csv", OssConstant.FEATURES_PARENT_PATH + "/"+ userId + "/" + taskId, FeatureTypeConstant.FEATURES_GLOBAL);

        String redis_features_global_Key = RedisConstant.USER_FEATURES_GLOBAL_RESULT + userId;

        String oldGlobalUrl = stringRedisTemplate.opsForValue().getAndSet(redis_features_global_Key, fileGlobalPath);

        // 7.2. 异步删除旧 OSS 文件（不阻塞新任务）
        if (oldGlobalUrl != null && !oldGlobalUrl.equals(fileGlobalPath)) {
            executor.execute(() -> {
                try {
                    aliOssUtil.deleteByUrl(oldGlobalUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        return response;
    }

    /**
     * 前端提供划分窗口 这里进行对应时间窗口的转换
     * @param windowSize
     * @return
     */
    private long parseWindowSize(String windowSize) {
        if (windowSize.endsWith("m")) {
            return Long.parseLong(windowSize.substring(0, windowSize.length() - 1)) * 60 * 1000;
        } else if (windowSize.endsWith("h")) {
            return Long.parseLong(windowSize.substring(0, windowSize.length() - 1)) * 60 * 60 * 1000;
        } else if (windowSize.endsWith("d")) {
            return Long.parseLong(windowSize.substring(0, windowSize.length() - 1)) * 24 * 60 * 60 * 1000;
        }
        return 30 * 60 * 1000; // 默认30分钟
    }

    /**
     * 将对应的边表数据按照划分窗口进行组合 代表一个完整的切片数据(代表一个快照)
     * @param edges
     * @param windowSizeMs
     * @return 返回值 键为时间窗口起点 值为该窗口内的边列表
     */
    private Map<Long, List<NetworkResponse.EdgeDTO>> groupEdgesByWindow(
            List<NetworkResponse.EdgeDTO> edges, long windowSizeMs) {
        // 使用TreeMap进行数据的存储 直接按照键值进行排序
        Map<Long, List<NetworkResponse.EdgeDTO>> snapshots = new TreeMap<>();

        for (NetworkResponse.EdgeDTO edge : edges) {
            long windowStart = (edge.getTimestampMs() / windowSizeMs) * windowSizeMs;
            snapshots.computeIfAbsent(windowStart, k -> new ArrayList<>()).add(edge);
        }

        return snapshots;
    }

    /**
     * 创建一个与原始图类型相同的空图
     */
    private Graph<Integer, DefaultEdge> createEmptyGraph() {
        return new DirectedMultigraph<>(DefaultEdge.class);  // 有向图，允许重复边，禁止自环
    }

    /**
     * 将边列表添加到图中
     */
    private void addEdgesToGraph(Graph<Integer, DefaultEdge> graph,
                                 List<NetworkResponse.EdgeDTO> edges) {
        for (NetworkResponse.EdgeDTO edge : edges) {
            //TODO 跳过自环边 不处理自环边
            if (edge.getSource().equals(edge.getTarget())) {
                continue;
            }
            // 确保节点存在
            graph.addVertex(edge.getSource());
            graph.addVertex(edge.getTarget());
            graph.addEdge(edge.getSource(), edge.getTarget());
        }
    }

    private AnalysisResponse.SnapshotMetrics.GraphMetrics calculateGraphMetrics(Graph<Integer, DefaultEdge> graph) {
        AnalysisResponse.SnapshotMetrics.GraphMetrics metrics = new AnalysisResponse.SnapshotMetrics.GraphMetrics();

        int nodeCount = graph.vertexSet().size();
        int edgeCount = graph.edgeSet().size();

        metrics.setNodeCount(nodeCount);
        metrics.setEdgeCount(edgeCount);

        // 计算密度、直径和平均最短路径
        if (nodeCount > 1) {
            double density = (2.0 * edgeCount) / (nodeCount * (nodeCount - 1)); //！！！未考虑重复边的上限，对于重复边的图，该值会稍大。
            metrics.setDensity(density);
            //1) 转换为邻接表，用于我们的 BFS 计算
            Map<Integer, List<Integer>> adjList = GraphMetricsCalculator.convertToAdjacencyList(graph);
            //2) 获取最大连通分量
            Set<Integer> largestComponent = GraphMetricsCalculator.getLargestConnectedComponent(adjList);
            //3). 计算直径和平均路径长度（基于最大连通分量）
            if (largestComponent.size() > 1) {
                double diameter = GraphMetricsCalculator.calculateDiameter(largestComponent, adjList);
                double avgPathLength = GraphMetricsCalculator.calculateAveragePathLength(largestComponent, adjList);
                metrics.setDiameter(diameter);
                metrics.setAveragedistance(avgPathLength);
            } else {
                // 分量大小不足 2
                metrics.setDiameter(0.0);
                metrics.setAveragedistance(0.0);
            }
        } else {
            metrics.setDensity(0.0);
            metrics.setDiameter(0.0);
            metrics.setAveragedistance(0.0);
        }
        return metrics;
    }

    private AnalysisResponse.SnapshotMetrics.NodeMetrics calculateNodeMetrics(
            Graph<Integer, DefaultEdge> graph) {
        AnalysisResponse.SnapshotMetrics.NodeMetrics nodeMetrics = new AnalysisResponse.SnapshotMetrics.NodeMetrics();
        try {
            // 1. 将Graph写入edges.txt
            String edgeFile = "edges.txt";
            writeGraphToEdgeFile(graph, edgeFile);
            // 2. 构建所有计算任务
            List<CompletableFuture<MetricResult>> futures = new ArrayList<>();

            for (String direction : FeatureConstant.DIRECTION) {
                for (String feature : FeatureConstant.FEATURES) {
                    // 每个组合创建一个异步任务 允许多核cpu并行运行
                    CompletableFuture<MetricResult> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            String outputFile = "result_" + direction + "_" + feature + ".txt";

                            List<String> command = Arrays.asList(
                                    "D:\\Download\\4first2steps_in_webserver\\4first2steps_in_webserver\\directed_centrality.exe",
                                    feature,
                                    direction,
                                    edgeFile,
                                    "***",
                                    outputFile
                            );

                            executeCommand(command);
                            Double[] result = parseResultFile(outputFile);

                            log.info("完成计算: {}_{} - mean: {}, std: {}",
                                    direction, feature, result[0], result[1]);

                            return new MetricResult(direction, feature, result[0], result[1]);

                        } catch (Exception e) {
                            log.error("计算失败: {}_{}", direction, feature, e);
                            throw new RuntimeException(e);
                        }
                    });

                    futures.add(future);
                }
            }

            // 3. 等待所有任务完成并收集结果
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 4. 设置结果到 nodeMetrics
            for (CompletableFuture<MetricResult> future : futures) {
                MetricResult mr = future.get();
                setMetricValue(nodeMetrics, mr.getDirection(), mr.getFeature(), mr.getMean(), mr.getStd());
            }

        } catch (Exception e) {
            log.error("计算节点指标失败", e);
            throw new RuntimeException("计算节点指标失败", e);
        }

        return nodeMetrics;
    }

    /**
     * 将Graph写入边列表文件
     */
    private void writeGraphToEdgeFile(Graph<Integer, DefaultEdge> graph, String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) { // 采用覆盖模式写入
            for (DefaultEdge edge : graph.edgeSet()) {
                Integer source = graph.getEdgeSource(edge);
                Integer target = graph.getEdgeTarget(edge);
                writer.write(source + " " + target);
                writer.newLine();
            }
        }
        log.info("图已写入文件: {}, 节点数: {}, 边数: {}",
                filename, graph.vertexSet().size(), graph.edgeSet().size());
    }

    /**
     * 执行系统命令
     */
    private void executeCommand(List<String> command) throws IOException, InterruptedException {
        String cmdStr = String.join(" ", command);
        log.info("执行命令: {}", cmdStr);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File("."));  // 设置工作目录
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // 打印C++程序输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("[C++] {}", line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("命令执行失败，退出码: " + exitCode + ", 命令: " + cmdStr);
        }

        log.info("命令执行成功");
    }

    /**
     * 解析处理结果文件（mean std格式）
     */
    private Double[] parseResultFile(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine();
            if (line == null || line.trim().isEmpty()) {
                throw new IOException("结果文件为空: " + filename);
            }

            String[] parts = line.trim().split("\\s+");
            if (parts.length != 2) {
                throw new IOException("结果文件格式错误，期望'mean std'，实际: " + line);
            }

            Double mean = Double.parseDouble(parts[0]);
            Double std = Double.parseDouble(parts[1]);

            return new Double[]{mean, std};
        }
    }

    /**
     * 根据 direction 和 feature 设置对应的属性值
     * 这里可以使用反射 但是最终我没有使用反射 因为这会破坏类的封装 我觉得应该慎用
     */
    private void setMetricValue(AnalysisResponse.SnapshotMetrics.NodeMetrics nodeMetrics,
                                String direction, String feature,
                                Double mean, Double std) {

        String key = direction + "_" + feature;  // 例如: "i_D", "o_CE"

        switch (key) {
            // 度中心性 D
            case "i_D":
                nodeMetrics.setMean_inD(mean);
                nodeMetrics.setStd_inD(std);
                break;
            case "o_D":
                nodeMetrics.setMean_outD(mean);
                nodeMetrics.setStd_outD(std);
                break;
            case "i_CE":
                nodeMetrics.setMean_inCE(mean);
                nodeMetrics.setStd_inCE(std);
                break;
            case "o_CE":
                nodeMetrics.setMean_outCE(mean);
                nodeMetrics.setStd_outCE(std);
                break;
            case "i_DE":
                nodeMetrics.setMean_inDE(mean);
                nodeMetrics.setStd_inDE(std);
                break;
            case "o_DE":
                nodeMetrics.setMean_outDE(mean);
                nodeMetrics.setStd_outDE(std);
                break;
            case "i_LCC":
                nodeMetrics.setMean_inLCC(mean);
                nodeMetrics.setStd_inLCC(std);
                break;
            case "o_LCC":
                nodeMetrics.setMean_outLCC(mean);
                nodeMetrics.setStd_outLCC(std);
                break;
            case "i_EXTD":
                nodeMetrics.setMean_inEXTD(mean);
                nodeMetrics.setStd_inEXTD(std);
                break;
            case "o_EXTD":
                nodeMetrics.setMean_outEXTD(mean);
                nodeMetrics.setStd_outEXTD(std);
                break;
            case "i_ACCD":
                nodeMetrics.setMean_inACCD(mean);
                nodeMetrics.setStd_inACCD(std);
                break;
            case "o_ACCD":
                nodeMetrics.setMean_outACCD(mean);
                nodeMetrics.setStd_outACCD(std);
                break;
            case "i_NM":
                nodeMetrics.setMean_inNM(mean);
                nodeMetrics.setStd_inNM(std);
                break;
            case "o_NM":
                nodeMetrics.setMean_outNM(mean);
                nodeMetrics.setStd_outNM(std);
                break;
            case "i_COREDJ":
                nodeMetrics.setMean_inCOREDJ(mean);
                nodeMetrics.setStd_inCOREDJ(std);
                break;
            case "o_COREDJ":
                nodeMetrics.setMean_outCOREDJ(mean);
                nodeMetrics.setStd_outCOREDJ(std);
                break;
            case "i_COREDC":
                nodeMetrics.setMean_inCOREDC(mean);
                nodeMetrics.setStd_inCOREDC(std);
                break;
            case "o_COREDC":
                nodeMetrics.setMean_outCOREDC(mean);
                nodeMetrics.setStd_outCOREDC(std);
                break;
            case "i_COREDP":
                nodeMetrics.setMean_inCOREDP(mean);
                nodeMetrics.setStd_inCOREDP(std);
                break;
            case "o_COREDP":
                nodeMetrics.setMean_outCOREDP(mean);
                nodeMetrics.setStd_outCOREDP(std);
                break;
            case "i_COREDPA":
                nodeMetrics.setMean_inCOREDPA(mean);
                nodeMetrics.setStd_inCOREDPA(std);
                break;
            case "o_COREDPA":
                nodeMetrics.setMean_outCOREDPA(mean);
                nodeMetrics.setStd_outCOREDPA(std);
                break;

            default:
                log.warn("未处理的组合: direction={}, feature={}", direction, feature);
        }

    }

    private String exportFeaturesToCsv(List<AnalysisResponse.SnapshotMetrics> snapshotMetrics, String originalFilename, String parentPath, String featureType) throws Exception {
        // 2. 生成 CSV 内容到内存（不写本地磁盘）
        byte[] csvContent = generateCsvBytes(snapshotMetrics, featureType);
        // 新增：校验生成的字节数组是否为空，避免上传0字节文件
        if (csvContent.length == 0) {
            throw new IllegalArgumentException("生成的CSV内容为空，请检查featureType或snapshotMetrics数据");
        }
        String filePath = aliOssUtil.upload(csvContent, originalFilename, parentPath);
        return filePath;
    }

    // 生成对应的features csv字节数组 方便后续上传至OSS中
    private byte[] generateCsvBytes(List<AnalysisResponse.SnapshotMetrics> snapshotMetrics, String featureType) throws IOException {
        // 提前校验：空列表直接返回有提示的空内容，而非纯0字节
        if (snapshotMetrics == null || snapshotMetrics.isEmpty()) {
            return "无可用数据".getBytes(StandardCharsets.UTF_8);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (Writer writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer,
                     CSVWriter.DEFAULT_SEPARATOR,
                     CSVWriter.NO_QUOTE_CHARACTER,
                     CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                     CSVWriter.DEFAULT_LINE_END)) {

            if (featureType.equals(FeatureTypeConstant.FEATURES_X)) {
                // 表头
                String[] header = {"mean_inD", "mean_outD", "mean_inEXTD", "mean_outEXTD", "mean_inACCD",
                        "mean_outACCD", "mean_inNM", "mean_outNM", "mean_inCE", "mean_outCE",
                        "mean_inDE", "mean_outDE", "mean_inLCC", "mean_outLCC", "mean_inCOREDC",
                        "mean_outCOREDC", "mean_inCOREDJ", "mean_outCOREDJ", "mean_inCOREDP", "mean_outCOREDP",
                        "mean_inCOREDPA", "mean_outCOREDPA", "std_inD", "std_outD", "std_inEXTD",
                        "std_outEXTD", "std_inACCD", "std_outACCD", "std_inNM", "std_outNM",
                        "std_inCE", "std_outCE", "std_inDE", "std_outDE", "std_inLCC",
                        "std_outLCC", "std_inCOREDC", "std_outCOREDC", "std_inCOREDJ", "std_outCOREDJ",
                        "std_inCOREDP", "std_outCOREDP", "std_inCOREDPA", "std_outCOREDPA"};
                csvWriter.writeNext(header);

                // 数据行
                for (AnalysisResponse.SnapshotMetrics snapshotMetric : snapshotMetrics) {
                    String[] row = {
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_inD()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_outD()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_inEXTD()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_outEXTD()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_inACCD()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_outACCD()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_inNM()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_outNM()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_inCE()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_outCE()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_inDE()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_outDE()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_inLCC()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_outLCC()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_inCOREDC()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_outCOREDC()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_inCOREDJ()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_outCOREDJ()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_inCOREDP()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_outCOREDP()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_inCOREDPA()),
                            String.valueOf(snapshotMetric.getNodeLevel().getMean_outCOREDPA()),

                            String.valueOf(snapshotMetric.getNodeLevel().getStd_inD()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_outD()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_inEXTD()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_outEXTD()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_inACCD()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_outACCD()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_inNM()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_outNM()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_inCE()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_outCE()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_inDE()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_outDE()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_inLCC()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_outLCC()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_inCOREDC()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_outCOREDC()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_inCOREDJ()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_outCOREDJ()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_inCOREDP()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_outCOREDP()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_inCOREDPA()),
                            String.valueOf(snapshotMetric.getNodeLevel().getStd_outCOREDPA()),
                    };
                    csvWriter.writeNext(row);
                }
            } else if (featureType.equals(FeatureTypeConstant.FEATURES_GLOBAL)) {
                // 修复：表头重复问题，且匹配实际字段含义
                String[] header = {"node_count", "edge_count", "density", "diameter", "average_path_length"};
                csvWriter.writeNext(header);

                // 数据行
                for (AnalysisResponse.SnapshotMetrics snapshotMetric : snapshotMetrics) {
                    String[] row = {
                            String.valueOf(snapshotMetric.getGraphLevel().getNodeCount()),
                            String.valueOf(snapshotMetric.getGraphLevel().getEdgeCount()),
                            String.valueOf(snapshotMetric.getGraphLevel().getDensity()),
                            String.valueOf(snapshotMetric.getGraphLevel().getDiameter()),
                            String.valueOf(snapshotMetric.getGraphLevel().getAveragedistance()),
                    };
                    csvWriter.writeNext(row);
                }
            } else {
                // 新增：处理未知featureType，避免无数据写入
                csvWriter.writeNext(new String[]{"错误：未知的featureType", featureType});
            }

            // 核心修复：强制刷新CSVWriter的缓存到Writer，再刷新Writer到ByteArrayOutputStream
            csvWriter.flush();
            writer.flush();

        } // try-with-resources 会自动关闭csvWriter和writer
        byte[] result = baos.toByteArray();
        return result;
    }


}
