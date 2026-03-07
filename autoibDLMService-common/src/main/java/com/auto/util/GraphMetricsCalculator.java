package com.auto.util;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

public class GraphMetricsCalculator {
    /**
     * 通过 BFS 获取图的最大连通分量（子图）
     * @param nbgraph 邻接表表示的图，Map<节点ID, List<邻居ID>>
     * @return 最大连通分量的节点集合
     */
    public static Set<Integer> getLargestConnectedComponent(Map<Integer, List<Integer>> nbgraph) {
        if (nbgraph == null || nbgraph.isEmpty()) {
            return new HashSet<>();
        }

        Set<Integer> visited = new HashSet<>();
        Set<Integer> largestComponent = new HashSet<>();

        for (Integer startNode : nbgraph.keySet()) {
            if (!visited.contains(startNode)) {
                Set<Integer> currentComponent = bfs(startNode, nbgraph);
                visited.addAll(currentComponent);

                if (currentComponent.size() > largestComponent.size()) {
                    largestComponent = currentComponent;
                }
            }
        }
        return largestComponent;
    }

    /**
     * 从指定起始节点进行 BFS，返回该节点所在的连通分量
     */
    private static Set<Integer> bfs(Integer start, Map<Integer, List<Integer>> nbgraph) {
        Set<Integer> component = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(start);
        component.add(start);

        while (!queue.isEmpty()) {
            Integer current = queue.poll();
            for (Integer neighbor : nbgraph.getOrDefault(current, new ArrayList<>())) {
                if (!component.contains(neighbor)) {
                    component.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }
        return component;
    }

    /**
     * 计算图的直径（基于最大连通分量）
     * 原理：对分量中每个节点做 BFS，记录最远距离，所有最远距离中的最大值即为直径
     * @param component 最大连通分量的节点集合
     * @param nbgraph 完整的邻接表
     * @return 直径（如果分量大小<2，返回0）
     */
    public static double calculateDiameter(Set<Integer> component, Map<Integer, List<Integer>> nbgraph) {
        if (component.size() <= 1) return 0.0;

        double diameter = 0.0;
        List<Integer> nodeList = new ArrayList<>(component);

        for (Integer source : nodeList) {
            double maxDistanceFromSource = bfsMaxDistance(source, nbgraph, component);
            if (maxDistanceFromSource > diameter) {
                diameter = maxDistanceFromSource;
            }
        }
        return diameter;
    }

    /**
     * 计算图的平均最短路径长度（基于最大连通分量）
     * @param component 最大连通分量的节点集合
     * @param nbgraph 完整的邻接表
     * @return 平均最短路径长度
     */
    public static double calculateAveragePathLength(Set<Integer> component, Map<Integer, List<Integer>> nbgraph) {
        int n = component.size();
        if (n <= 1) return 0.0;

        double totalPathLength = 0.0;
        long totalPairs = 0L;
        List<Integer> nodeList = new ArrayList<>(component);

        for (int i = 0; i < nodeList.size(); i++) {
            Integer source = nodeList.get(i);
            Map<Integer, Integer> distances = bfsAllDistances(source, nbgraph, component);

            // 累加从 source 到所有其他可达节点的距离
            for (int j = i + 1; j < nodeList.size(); j++) {
                Integer target = nodeList.get(j);
                Integer dist = distances.get(target);
                if (dist != null) {
                    totalPathLength += dist;
                    totalPairs++;
                }
            }
        }
        // 平均路径长度 = 所有节点对距离之和 / 节点对数量
        return totalPairs > 0 ? totalPathLength / totalPairs : 0.0;
    }

    /**
     * 单源 BFS，返回从源节点到分量内所有其他节点的最远距离
     */
    private static double bfsMaxDistance(Integer source, Map<Integer, List<Integer>> nbgraph, Set<Integer> component) {
        Map<Integer, Integer> distances = bfsAllDistances(source, nbgraph, component);
        int maxDist = 0;
        for (int dist : distances.values()) {
            if (dist > maxDist) maxDist = dist;
        }
        return maxDist;
    }

    /**
     * 单源 BFS，返回从源节点到分量内所有其他节点的距离映射
     * 这是最核心的 BFS 函数
     */
    private static Map<Integer, Integer> bfsAllDistances(Integer source,
                                                         Map<Integer, List<Integer>> nbgraph,
                                                         Set<Integer> component) {
        Map<Integer, Integer> distances = new HashMap<>();
        Queue<Integer> queue = new LinkedList<>();

        distances.put(source, 0);
        queue.offer(source);

        while (!queue.isEmpty()) {
            Integer current = queue.poll();
            int currentDist = distances.get(current);

            for (Integer neighbor : nbgraph.getOrDefault(current, new ArrayList<>())) {
                // 只考虑在目标分量内的邻居
                if (component.contains(neighbor) && !distances.containsKey(neighbor)) {
                    distances.put(neighbor, currentDist + 1);
                    queue.offer(neighbor);
                }
            }
        }
        return distances;
    }

    /**
     * 将 JGraphT 的 Graph 对象转换为邻接表，供我们的 BFS 函数使用
     */
    public static Map<Integer, List<Integer>> convertToAdjacencyList(Graph<Integer, DefaultEdge> jgraphtGraph) {
        Map<Integer, List<Integer>> adjList = new HashMap<>();

        // 添加所有顶点（即使有些顶点可能没有边）
        for (Integer vertex : jgraphtGraph.vertexSet()) {
            adjList.putIfAbsent(vertex, new ArrayList<>());
        }

        // 添加边
        for (DefaultEdge edge : jgraphtGraph.edgeSet()) {
            Integer source = jgraphtGraph.getEdgeSource(edge);
            Integer target = jgraphtGraph.getEdgeTarget(edge);

            adjList.get(source).add(target);
        }

        return adjList;
    }
}
