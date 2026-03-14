<template>
  <div class="characterization-step">
    <StepDescription 
      text="We will present the weight heatmaps of the mean absolute and the mean squared sum for the weight parameters of the auto-learning layer."
    />

    <div class="characterization-content">
      <div v-if="stepStatus === 'pending'" class="action-card">
        <div class="action-icon">
          <img src="@/assets/images/step_by_step/characterization.png" width="100px" height="100px">
        </div>
        <h4 class="action-title">Explanatory Analysis</h4>
        <button class="action-button" @click="handleAnalyze">Explanatory Analyze</button>
      </div>

      <div v-else-if="stepStatus === 'processing'" class="processing-state">
        <div class="spinner"></div>
        <h4 class="processing-title">Explanatory Analyzing...</h4>
      </div>

      <div v-else class="result-container">
        <!-- 修复点1：模板中直接使用 props.weightResult.featureNames -->
        <div class="feature-mapping-card">
          <h5 class="mapping-title">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#003399" stroke-width="2">
              <path d="M4 6h16M4 12h16M4 18h16"/>
            </svg>
            Feature ID - Name Mapping
          </h5>
          <div class="mapping-content">
            <div class="mapping-table">
              <div class="table-header">
                <div class="table-cell">Feature ID</div>
                <div class="table-cell">Feature Name</div>
              </div>
              <div class="table-body">
                <!-- 核心修正：使用 props.weightResult.featureNames，增加空值兜底 -->
                <div 
                  class="table-row" 
                  v-for="(name, index) in (props.weightResult.featureNames || [])" 
                  :key="`feature-${index}`"
                >
                  <div class="table-cell">{{ index + 1 }}</div>
                  <div class="table-cell">{{ name }}</div>
                </div>
                <!-- 空数据提示 -->
                <div v-if="(props.weightResult.featureNames || []).length === 0" class="empty-row">
                  No feature name data available
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 热力图区域 -->
        <div class="result-grid" ref="containerRef">
          <div class="chart-card1">
            <h5 class="chart-title1">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="url(#heatmap-gradient)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <defs>
                  <linearGradient id="heatmap-gradient" x1="0%" y1="0%" x2="100%" y2="0%">
                    <stop offset="0%" stop-color="#003399" />
                    <stop offset="50%" stop-color="#ff9966" />
                    <stop offset="100%" stop-color="#cc0000" />
                  </linearGradient>
                </defs>
                <path d="M12 8v4l3 3" />
                <path d="M8 12a4 4 0 1 0 8 0 4 4 0 0 0-8 0z" />
                <path d="M12 2v2" />
                <path d="M12 16v6" />
              </svg>
              Mean Absolute Weight Heatmap
            </h5>
            <div class="chart-container pie-container" ref="heatmap1Ref"></div>
          </div>

          <div class="chart-card">
            <h5 class="chart-title">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="url(#heatmap-gradient-2)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <defs>
                  <linearGradient id="heatmap-gradient-2" x1="0%" y1="0%" x2="100%" y2="0%">
                    <stop offset="0%" stop-color="#003399" />
                    <stop offset="50%" stop-color="#ff9966" />
                    <stop offset="100%" stop-color="#cc0000" />
                  </linearGradient>
                </defs>
                <path d="M12 8v4l3 3" />
                <path d="M8 12a4 4 0 1 0 8 0 4 4 0 0 0-8 0z" />
                <path d="M12 2v2" />
                <path d="M12 16v6" />
              </svg>
              Mean Squared Sum Weight Heatmap
            </h5>
            <div class="chart-container pie-container" ref="heatmap2Ref"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted, nextTick } from 'vue'; // 引入 nextTick
import * as echarts from 'echarts';
import StepDescription from './StepDescription.vue';

// 1. 定义接收父组件的props
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
  }
});

// 2. 定义emit
const emit = defineEmits(['analyze']);

// 3. 子组件内部引用
const heatmap1Ref = ref(null);
const heatmap2Ref = ref(null);
let chart1 = null;
let chart2 = null;
let resizeHandler = null;

// 4. 子组件暴露给父组件的"触发分析"方法
const handleAnalyze = () => {
  emit('analyze', 5);
};

// 5. 监听props变化，自动重绘热力图（核心修复：增加nextTick + 强校验）
watch(
  () => props.weightResult,
  async (newVal) => {
    // 先校验数据有效性
    if (!newVal || newVal.weightsMean.length === 0 || newVal.weightsSum.length === 0) return;
    // 关键：等待DOM更新完成后再渲染
    await nextTick();
    // 再次校验DOM是否存在
    if (!heatmap1Ref.value || !heatmap2Ref.value) return;
    // 执行渲染
    renderHeatmap(newVal);
  },
  { deep: true, immediate: true } // 深度监听 + 初始化执行
);

// 6. 绘图逻辑（保持不变，增强兜底）
const renderHeatmap = (weightResult) => {
  const { weightsMean, weightsSum, featureNames } = weightResult;
  // 兜底校验
  if (!weightsMean || !weightsSum) return;
  const featureCount = weightsMean.length;
  if (featureCount === 0) return;

  // 销毁旧实例
  if (chart1) chart1.dispose();
  if (chart2) chart2.dispose();

  const featureNamesList = Array.from({ length: featureCount }, (_, i) => `${i + 1}`);

  const getHeatmapOption = (title, data) => {
    const minVal = Math.min(...data);
    const maxVal = Math.max(...data);

    return {
      title: {
        text: title,
        left: 'center',
        textStyle: { fontSize: 16, fontWeight: 'bold', color: '#2d3748' }
      },
      tooltip: {
        trigger: 'item',
        backgroundColor: 'rgba(255,255,255,0.95)',
        textStyle: { color: '#2d3748' },
        borderColor: '#e2e8f0',
        formatter: (params) => {
          const featureIndex = params.name;
          const value = params.value[2];
          const roundedValue = value.toFixed(2);
          const featureName = (featureNames || [])[featureIndex - 1] || 'Unknown Feature';
          return `Feature ID: ${featureIndex}<br/>Feature Name: ${featureName}<br/>Value: ${roundedValue}`;
        }
      },
      visualMap: {
        min: minVal,
        max: maxVal,
        calculable: true,
        orient: 'horizontal',
        left: 'center',
        bottom: '5%',
        width: '60%',
        text: [maxVal.toFixed(3), minVal.toFixed(3)],
        inverse: false,
        textStyle: { fontSize: 10, color: '#4a5568' },
        inRange: {
          color: ['#003399', '#6699ff', '#e6e6e6', '#ff9966', '#cc0000']
        }
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
        data: featureNamesList,
        name: `Feature Dimensions (Total: ${featureCount})`,
        nameLocation: 'center',
        nameGap: 25,
        nameTextStyle: { fontSize: 12, color: '#4a5568' },
        axisLabel: {
          fontSize: 7,
          interval: 2,
          rotate: 0,
          color: '#4a5568',
          margin: 8
        },
        axisLine: { lineStyle: { color: '#cbd5e0' } },
        axisTick: { show: false }
      },
      yAxis: {
        show: false,
        type: 'category',
        data: ['']
      },
      series: [
        {
          name: 'Weight Value',
          type: 'heatmap',
          data: data.map((val, idx) => [idx, 0, val]),
          label: {
            show: false,
          },
          itemStyle: {
            borderWidth: 0.5,
            borderColor: '#fff'
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 6,
              shadowColor: 'rgba(0,0,0,0.15)'
            }
          }
        }
      ]
    };
  };

  // 初始化热力图（最终DOM校验）
  if (heatmap1Ref.value) {
    chart1 = echarts.init(heatmap1Ref.value);
    chart1.setOption(getHeatmapOption(`Mean Absolute Weight (${featureCount} Features)`, weightsMean));
    // 强制 resize
    chart1.resize();
  }
  if (heatmap2Ref.value) {
    chart2 = echarts.init(heatmap2Ref.value);
    chart2.setOption(getHeatmapOption(`Mean Squared Sum (${featureCount} Features)`, weightsSum));
    // 强制 resize
    chart2.resize();
  }

  // 自适应窗口
  if (resizeHandler) window.removeEventListener('resize', resizeHandler);
  resizeHandler = () => {
    chart1?.resize();
    chart2?.resize();
  };
  window.addEventListener('resize', resizeHandler);
};

// 组件卸载清理
onUnmounted(() => {
  if (chart1) chart1.dispose();
  if (chart2) chart2.dispose();
  if (resizeHandler) window.removeEventListener('resize', resizeHandler);
});

onMounted(() => {
  // 初始化渲染（同样增加nextTick）
  nextTick(() => {
    if (props.weightResult.weightsMean.length > 0) {
      renderHeatmap(props.weightResult);
    }
  });
});

// 暴露方法给父组件
defineExpose({
  renderHeatmap
});
</script>

<style scoped>
/* 样式保持不变，无需修改 */
.characterization-step {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
  box-sizing: border-box;
}

.characterization-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.action-card {
  width: 100%;
  max-width: 576px;
  text-align: center;
  background: #fff;
  padding: 64px;
  border-radius: 48px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  box-sizing: border-box;
}

.action-title {
  font-size: 30px;
  font-weight: 900;
  color: #0f172a;
  margin: 0 0 24px 0;
}

.action-button {
  width: 100%;
  padding: 12px 24px;
  background: #007bff;
  color: #fff;
  border: none;
  border-radius: 5px;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.3s;
}

.action-button:hover { background: orangered; }

.processing-state {
  text-align: center;
  padding: 48px 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.spinner {
  width: 80px;
  height: 80px;
  border: 4px solid rgba(0,164,255,0.1);
  border-top-color: #00a4ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 32px;
}

@keyframes spin { to { transform: rotate(360deg); } }

.result-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  max-width: 1200px;
}

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

.result-grid {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  width: 100%;
  max-width: 1200px;
}

.chart-card1, .chart-card {
  background: #fff;
  padding: 20px;
  border-radius: 40px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  width: 100%;
  height: 500px;
  box-sizing: border-box;
}

.chart-title1, .chart-title {
  font-size: 14px;
  font-weight: 900;
  color: #0f172a;
  margin: 0 0 15px 0;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.chart-container {
  flex: 1;
  min-height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pie-container {
  width: 100% !important;
  height: 100% !important;
  min-width: 300px !important;
  min-height: 300px !important;
}
</style>