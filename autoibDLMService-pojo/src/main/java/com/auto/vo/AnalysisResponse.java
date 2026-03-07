package com.auto.vo;
import lombok.Data;

import java.util.List;

/**
 * 特征提取后的响应
 */
@Data
public class AnalysisResponse {
    private String taskId;
    private Long windowSizeMs;
    private List<SnapshotMetrics> snapshots;

    @Data
    public static class SnapshotMetrics {
        private Long windowStart;           // 时间窗口起始毫秒
        private String windowStartISO;      // ISO格式时间
        private GraphMetrics graphLevel;    // 网络层面指标
        private NodeMetrics nodeLevel;      // 节点层面
        // TODO 社区层面 暂时不考虑 因为节点层面特征加网络层面特征已经够用
        // private ComMetrics comLevel;
        public SnapshotMetrics subtract(SnapshotMetrics other) {
            SnapshotMetrics diff = new SnapshotMetrics();
            diff.setWindowStart(this.windowStart);
            diff.setWindowStartISO(this.windowStartISO);
            if (this.graphLevel != null) {
                diff.setGraphLevel(this.graphLevel.subtract(other != null ? other.graphLevel : null));
            }
            if (this.nodeLevel != null) {
                diff.setNodeLevel(this.nodeLevel.subtract(other != null ? other.nodeLevel : null));
            }
            return diff;
        }

        // 图全局层面上的特征指标
        @Data
        public static class GraphMetrics {
            private int nodeCount;
            private int edgeCount;
            private double density;
            private double diameter;
            private double averagedistance;
            /**
             * 计算当前指标对象与另一个指标对象的差值（this - other）
             * @param other 另一个时间点的指标
             * @return 差值指标对象
             */
            public GraphMetrics subtract(GraphMetrics other) {
                // 因为我们计算的是快照之间的差值 因此当另一个快照对象的对应指标为null时 设置为0
                if (other == null) {
                    return this.cloneWithZeroDelta();
                }
                GraphMetrics diff = new GraphMetrics();
                diff.setNodeCount(this.nodeCount - other.nodeCount);
                diff.setEdgeCount(this.edgeCount - other.edgeCount);
                diff.setDensity(this.density - other.density);
                diff.setDiameter(this.diameter - other.diameter);
                diff.setAveragedistance(this.averagedistance - other.averagedistance);
                return diff;
            }
            /**
             * 克隆当前对象，但所有数值字段设为0（表示无变化）
             **/
            private GraphMetrics cloneWithZeroDelta() {
                GraphMetrics zero = new GraphMetrics();
                zero.setNodeCount(0);
                zero.setEdgeCount(0);
                zero.setDensity(0.0);
                zero.setDiameter(0.0);
                zero.setAveragedistance(0.0);
                return zero;
            }
        }

        // 图观层面上的特征指标
        @Data
        public static class NodeMetrics {
            private double mean_inD;
            private double mean_outD;
            private double mean_inEXTD;
            private double mean_outEXTD;
            private double mean_inACCD;
            private double mean_outACCD;
            private double mean_inNM;
            private double mean_outNM;
            private double mean_inCE;
            private double mean_outCE;
            private double mean_inDE;
            private double mean_outDE;
            private double mean_inLCC;
            private double mean_outLCC;
            private double mean_inCOREDC;
            private double mean_outCOREDC;
            private double mean_inCOREDJ;
            private double mean_outCOREDJ;
            private double mean_inCOREDP;
            private double mean_outCOREDP;
            private double mean_inCOREDPA;
            private double mean_outCOREDPA;
            private double std_inD;
            private double std_outD;
            private double std_inEXTD;
            private double std_outEXTD;
            private double std_inACCD;
            private double std_outACCD;
            private double std_inNM;
            private double std_outNM;
            private double std_inCE;
            private double std_outCE;
            private double std_inDE;
            private double std_outDE;
            private double std_inLCC;
            private double std_outLCC;
            private double std_inCOREDC;
            private double std_outCOREDC;
            private double std_inCOREDJ;
            private double std_outCOREDJ;
            private double std_inCOREDP;
            private double std_outCOREDP;
            private double std_inCOREDPA;
            private double std_outCOREDPA;

            /**
             * 计算当前指标对象与另一个指标对象的差值（this - other）
             * @param other 另一个时间点的指标
             * @return 差值指标对象
             */
            public NodeMetrics subtract(NodeMetrics other) {
                if (other == null) {
                    return this.cloneWithZeroDelta();
                }
                NodeMetrics diff = new NodeMetrics();
                diff.setMean_inD(this.mean_inD - other.mean_inD);
                diff.setMean_outD(this.mean_outD - other.mean_outD);
                diff.setMean_inEXTD(this.mean_inEXTD - other.mean_inEXTD);
                diff.setMean_outEXTD(this.mean_outEXTD - other.mean_outEXTD);
                diff.setMean_inACCD(this.mean_inACCD - other.mean_inACCD);
                diff.setMean_outACCD(this.mean_outACCD - other.mean_outACCD);
                diff.setMean_inNM(this.mean_inNM - other.mean_inNM);
                diff.setMean_outNM(this.mean_outNM - other.mean_outNM);
                diff.setMean_inCE(this.mean_inCE - other.mean_inCE);
                diff.setMean_outCE(this.mean_outCE - other.mean_outCE);
                diff.setMean_inDE(this.mean_inDE - other.mean_inDE);
                diff.setMean_outDE(this.mean_outDE - other.mean_outDE);
                diff.setMean_inLCC(this.mean_inLCC - other.mean_inLCC);
                diff.setMean_outLCC(this.mean_outLCC - other.mean_outLCC);
                diff.setMean_inCOREDC(this.mean_inCOREDC - other.mean_inCOREDC);
                diff.setMean_outCOREDC(this.mean_outCOREDC - other.mean_outCOREDC);
                diff.setMean_inCOREDJ(this.mean_inCOREDJ - other.mean_inCOREDJ);
                diff.setMean_outCOREDJ(this.mean_outCOREDJ - other.mean_outCOREDJ);
                diff.setMean_inCOREDP(this.mean_inCOREDP - other.mean_inCOREDP);
                diff.setMean_outCOREDP(this.mean_outCOREDP - other.mean_outCOREDP);
                diff.setMean_inCOREDPA(this.mean_inCOREDPA - other.mean_inCOREDPA);
                diff.setMean_outCOREDPA(this.mean_outCOREDPA - other.mean_outCOREDPA);
                diff.setStd_inD(this.std_inD - other.std_inD);
                diff.setStd_outD(this.std_outD - other.std_outD);
                diff.setStd_inEXTD(this.std_inEXTD - other.std_inEXTD);
                diff.setStd_outEXTD(this.std_outEXTD - other.std_outEXTD);
                diff.setStd_inACCD(this.std_inACCD - other.std_inACCD);
                diff.setStd_outACCD(this.std_outACCD - other.std_outACCD);
                diff.setStd_inNM(this.std_inNM - other.std_inNM);
                diff.setStd_outNM(this.std_outNM - other.std_outNM);
                diff.setStd_inCE(this.std_inCE - other.std_inCE);
                diff.setStd_outCE(this.std_outCE - other.std_outCE);
                diff.setStd_inDE(this.std_inDE - other.std_inDE);
                diff.setStd_outDE(this.std_outDE - other.std_outDE);
                diff.setStd_inLCC(this.std_inLCC - other.std_inLCC);
                diff.setStd_outLCC(this.std_outLCC - other.std_outLCC);
                diff.setStd_inCOREDC(this.std_inCOREDC - other.std_inCOREDC);
                diff.setStd_outCOREDC(this.std_outCOREDC - other.std_outCOREDC);
                diff.setStd_inCOREDJ(this.std_inCOREDJ - other.std_inCOREDJ);
                diff.setStd_outCOREDJ(this.std_outCOREDJ - other.std_outCOREDJ);
                diff.setStd_inCOREDP(this.std_inCOREDP - other.std_inCOREDP);
                diff.setStd_outCOREDP(this.std_outCOREDP - other.std_outCOREDP);
                diff.setStd_inCOREDPA(this.std_inCOREDPA - other.std_inCOREDPA);
                diff.setStd_outCOREDPA(this.std_outCOREDPA - other.std_outCOREDPA);
                return diff;
            }
            /**
             * 克隆当前对象，但所有数值字段设为0（表示无变化）
             **/
            private NodeMetrics cloneWithZeroDelta() {
                NodeMetrics zero = new NodeMetrics();
                zero.setMean_inD(0.0);
                zero.setMean_outD(0.0);
                zero.setMean_inEXTD(0.0);
                zero.setMean_outEXTD(0.0);
                zero.setMean_inACCD(0.0);
                zero.setMean_outACCD(0.0);
                zero.setMean_inNM(0.0);
                zero.setMean_outNM(0.0);
                zero.setMean_inCE(0.0);
                zero.setMean_outCE(0.0);
                zero.setMean_inDE(0.0);
                zero.setMean_outDE(0.0);
                zero.setMean_inLCC(0.0);
                zero.setMean_outLCC(0.0);
                zero.setMean_inCOREDC(0.0);
                zero.setMean_outCOREDC(0.0);
                zero.setMean_inCOREDJ(0.0);
                zero.setMean_outCOREDJ(0.0);
                zero.setMean_inCOREDP(0.0);
                zero.setMean_outCOREDP(0.0);
                zero.setMean_inCOREDPA(0.0);
                zero.setMean_outCOREDPA(0.0);
                zero.setStd_inD(0.0);
                zero.setStd_outD(0.0);
                zero.setStd_inEXTD(0.0);
                zero.setStd_outEXTD(0.0);
                zero.setStd_inACCD(0.0);
                zero.setStd_outACCD(0.0);
                zero.setStd_inNM(0.0);
                zero.setStd_outNM(0.0);
                zero.setStd_inCE(0.0);
                zero.setStd_outCE(0.0);
                zero.setStd_inDE(0.0);
                zero.setStd_outDE(0.0);
                zero.setStd_inLCC(0.0);
                zero.setStd_outLCC(0.0);
                zero.setStd_inCOREDC(0.0);
                zero.setStd_outCOREDC(0.0);
                zero.setStd_inCOREDJ(0.0);
                zero.setStd_outCOREDJ(0.0);
                zero.setStd_inCOREDP(0.0);
                zero.setStd_outCOREDP(0.0);
                zero.setStd_inCOREDPA(0.0);
                zero.setStd_outCOREDPA(0.0);
                return zero;
            }
        }
        // TODO 社区层面的特征暂时不计算 因为已经够用
//        @Data
//        public static class ComMetrics {
//            //to do...
//        }
    }
}
