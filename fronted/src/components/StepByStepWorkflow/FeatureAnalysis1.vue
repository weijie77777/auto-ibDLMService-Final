<template>
  <div class="characterization-step">
    <StepDescription 
      text="We will present the weight heatmaps of the mean absolute and the mean squared sum for the weight parameters of the auto-learning layer."
    />

    <div class="characterization-content">
      <!-- 初始状态 -->
      <div v-if="stepStatus === 'pending'" class="action-card">
        <div class="action-icon">
          <img src="@/assets/images/step_by_step/characterization.png" width="100px" height="100px" alt="icon">
        </div>
        <h4 class="action-title">Explanatory Analysis</h4>
        <button class="action-button" @click="handleAnalyze">Explanatory Analyze</button>
      </div>

      <!-- 加载状态 -->
      <div v-else-if="stepStatus === 'processing'" class="processing-state">
        <div class="spinner"></div>
        <h4 class="processing-title">Explanatory Analyzing...</h4>
      </div>

      <!-- 结果状态 -->
      <div v-else class="result-wrapper">
        <!-- 特征映射表 -->
        <div class="feature-mapping-card">
          <h5 class="mapping-title">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#003399" stroke-width="2">
              <path d="M4 6h16M4 12h16M4 18h16"/>
            </svg>
            Feature ID - Name Mapping
          </h5>
          <div class="mapping-table">
            <div class="table-header">
              <div class="table-cell">Feature ID</div>
              <div class="table-cell">Feature Name</div>
            </div>
            <div class="table-body">
              <div 
                class="table-row" 
                v-for="(name, index) in props.weightResult.featureNames" 
                :key="`feat-${index}`"
              >
                <div class="table-cell">{{ index + 1 }}</div>
                <div class="table-cell">{{ name }}</div>
              </div>
              <div v-if="!props.weightResult.featureNames.length" class="empty-row">No data</div>
            </div>
          </div>
        </div>

        <!-- 热力图区域：双卡片布局（无套壳子） -->
        <div class="charts-wrapper">
          <!-- 图表1 -->
          <div class="chart-card">
            <h5 class="chart-title">Mean Absolute Weight Heatmap</h5>
            <div class="chart-container" ref="heatmap1Ref"></div>
          </div>

          <!-- 图表2 -->
          <div class="chart-card">
            <h5 class="chart-title">Mean Squared Sum Heatmap</h5>
            <div class="chart-container" ref="heatmap2Ref"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted, nextTick } from 'vue';
import * as echarts from 'echarts';
import StepDescription from './StepDescription.vue';

// Props定义
const props = defineProps({
  stepStatus: {
    type: String,
    required: true,
    default: 'pending'
  },
  weightResult: {
    type: Object,
    required: true,
    default: () => ({
      weightsMean: [],
      weightsSum: [],
      featureNames: []
    })
  },
  isStepCollapsed: {
    type: Boolean,
    default: true
  }
});

// 事件派发
const emit = defineEmits(['analyze']);
const handleAnalyze = () => emit('analyze', 5);

// 核心引用与实例
const heatmap1Ref = ref(null);
const heatmap2Ref = ref(null);
let chart1 = null;
let chart2 = null;

// 核心渲染函数（自动触发，无手动按钮）
const renderCharts = () => {
  const { weightsMean, weightsSum, featureNames } = props.weightResult;
  
  // 基础校验
  if (weightsMean.length === 0 || !heatmap1Ref.value || !heatmap2Ref.value) return;
  if (heatmap1Ref.value.offsetWidth === 0 || heatmap1Ref.value.offsetHeight === 0) return;

  // 销毁旧实例
  if (chart1) chart1.dispose();
  if (chart2) chart2.dispose();

  const featureCount = weightsMean.length;
  const xAxisData = Array.from({ length: featureCount }, (_, i) => `${i + 1}`);

  // 核心配置：移除ECharts内部背景/边框，无套壳子
    // 核心配置：更舒展的布局
    const getCleanOption = (data) => {
    const min = Math.min(...data);
    const max = Math.max(...data);
    return {
        backgroundColor: 'transparent',
        tooltip: {
        trigger: 'item',
        formatter: (p) => {
            const idx = p.data[0];
            return `ID: ${idx + 1}<br/>Name: ${featureNames[idx] || 'Unknown'}<br/>Value: ${p.data[2].toFixed(2)}`;
        }
        },
        visualMap: {
        min, max,
        calculable: true,
        orient: 'horizontal',
        left: 'center',
        bottom: '5%', // 增加底部间距
        width: '60%',    // 颜色条更宽一点
        text: [max.toFixed(3), min.toFixed(3)],
        textStyle: { fontSize: 10, color: '#4a5568' }, 
        inRange: {
            color: ['#003399', '#6699ff', '#e6e6e6', '#ff9966', '#cc0000']
        },
        backgroundColor: 'transparent'
        },
        grid: {
        left: '3%',
        right: '3%',
        top: '15%',
        bottom: '30%',
        containLabel: true
        },
        xAxis: {
        type: 'category',
        data: xAxisData,
        name: `Feature Dimensions (Total: ${featureCount})`,
        nameLocation: 'center',
        nameGap: 25, 
        nameTextStyle: { fontSize: 12, color: '#4a5568' },
        axisLabel: {
            fontSize: 7, 
            interval: 2,
            color: '#666',
            margin: 8
        },
        axisLine: { show: false },
        axisTick: { show: false }
        },
        yAxis: {
        type: 'category',
        data: [''],
        show: false
        },
        series: [{
        type: 'heatmap',
        data: data.map((val, idx) => [idx, 0, val]),
        symbolSize: [18, 250], // 增加色块高度，更舒展
        itemStyle: {
            borderWidth: 0.4,
            borderColor: '#fff'
        }
        }]
    };
    };

  // 初始化图表
  chart1 = echarts.init(heatmap1Ref.value, null, {
    width: heatmap1Ref.value.clientWidth,
    height: heatmap1Ref.value.clientHeight
  });
  chart1.setOption(getCleanOption(weightsMean), true);

  chart2 = echarts.init(heatmap2Ref.value, null, {
    width: heatmap2Ref.value.clientWidth,
    height: heatmap2Ref.value.clientHeight
  });
  chart2.setOption(getCleanOption(weightsSum), true);

  // 自适应调整
  const resizeCharts = () => {
    chart1?.resize();
    chart2?.resize();
  };
  window.addEventListener('resize', resizeCharts);
  onUnmounted(() => window.removeEventListener('resize', resizeCharts));
};

// 监听折叠状态：展开时自动渲染
watch(() => props.isStepCollapsed, (newVal, oldVal) => {
  if (oldVal === true && newVal === false) {
    nextTick(() => {
      if (props.weightResult.weightsMean.length > 0) {
        renderCharts();
      }
    });
  }
});

// 监听数据变化：数据更新时自动渲染
watch(
  () => [props.weightResult.weightsMean, props.weightResult.weightsSum],
  () => {
    if (props.weightResult.weightsMean.length > 0 && !props.isStepCollapsed) {
      nextTick(() => renderCharts());
    }
  },
  { deep: true, immediate: true }
);

// 挂载时触发
onMounted(() => {
  if (props.weightResult.weightsMean.length > 0 && !props.isStepCollapsed) {
    renderCharts();
  }
});

// 卸载清理
onUnmounted(() => {
  chart1?.dispose();
  chart2?.dispose();
});
</script>

<style scoped>
/* 基础布局 */
.characterization-step {
  padding: 20px;
  box-sizing: border-box;
}

.characterization-content {
  margin-top: 20px;
}

/* 初始按钮样式 */
.action-card {
  text-align: center;
  padding: 40px;
  border: 1px solid #eee;
  border-radius: 20px;
  max-width: 500px;
  margin: 0 auto;
}

.action-icon {
  margin-bottom: 20px;
}

.action-title {
  font-size: 24px;
  margin: 20px 0;
}

.action-button {
  padding: 10px 20px;
  background: #007bff;
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

/* 加载状态 */
.processing-state {
  text-align: center;
  padding: 40px 0;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #eee;
  border-top: 4px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.processing-title {
  font-size: 18px;
  color: #333;
}

/* 结果区域 */
.result-wrapper {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

/* 特征映射表 */
.feature-mapping-card {
  background: #fff;
  padding: 20px;
  border-radius: 20px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  width: 100%;
  max-height: 200px;
  overflow-y: auto;
}

.mapping-title {
  font-size: 14px;
  font-weight: 900;
  color: #0f172a;
  margin: 0 0 15px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.mapping-content {
  width: 100%;
}

.mapping-table {
  width: 100%;
  border-collapse: collapse;
}

.table-header {
  display: flex;
  background-color: #f8fafc;
  border-radius: 8px 8px 0 0;
}

.table-cell {
  flex: 1;
  padding: 8px 12px;
  font-size: 12px;
  font-weight: 600;
  color: #4a5568;
  text-align: left;
}

.table-body {
  max-height: 120px;
  overflow-y: auto;
}

.table-row {
  display: flex;
  border-bottom: 1px solid #e2e8f0;
  transition: background-color 0.2s;
}

.table-row:hover {
  background-color: #f8fafc;
}

.empty-row {
  padding: 12px;
  font-size: 12px;
  color: #94a3b8;
  text-align: center;
}



/* 热力图区域 */
.charts-wrapper {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

/* 核心：无套壳子的卡片样式 */
.chart-card {
  flex: 1;
  min-width: 400px;
  background: #fff;
  border: 1px solid #f1f5f9;
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  padding: 0; /* 无内边距，热力图直接顶边 */
  overflow: hidden;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 15px 0 0 0;
  text-align: center;
}

.chart-container {
  width: 90%;
  height: 400px;
  min-height: 400px;
  box-sizing: border-box;
  margin-left: 5%;
}
</style>