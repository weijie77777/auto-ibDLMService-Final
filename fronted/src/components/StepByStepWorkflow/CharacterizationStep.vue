<template>
  <div class="characterization-step">
    <StepDescription 
      text="Extract profile characterizations from the latent space. We provide high-fidelity visual insights into skill magnitudes and categorical distributions."
    />

    <div class="characterization-content">
      <div v-if="stepStatus === 'pending'" class="action-card">
        <div class="action-icon">
          <img src="@/assets/images/step_by_step/characterization.png" width="100px" height="100px">
        </div>
        <h4 class="action-title">Event Characterization</h4>
        <button class="action-button" @click="$emit('analyze', 2)">
          Extract Features
        </button>
      </div>

      <div v-else-if="stepStatus === 'processing'" class="processing-state">
        <div class="spinner"></div>
        <h4 class="processing-title">Extracting Features...</h4>
      </div>

      <div v-else class="result-grid" ref="containerRef">

        <div class="chart-card1">
          <h5 class="chart-title1">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="20" x2="18" y2="10"></line>
              <line x1="12" y1="20" x2="12" y2="4"></line>
              <line x1="6" y1="20" x2="6" y2="14"></line>
            </svg>
            Node Increments
          </h5>
          <div class="chart-container pie-container" ref="chartRef" style="width: 400px; height: 400px;">
            
          </div>
        </div>

        <div class="chart-card">
          <h5 class="chart-title">
            Features Characterization download
          </h5>
          <div class="chart-container">
            <div class="mock-chart">
              <div class="bar-item">
                <span class="bar-label">Node-Level Features</span>
                <div class="bar-wrapper">
                  <el-button type="primary" class="downloadButton" @click="downloadNode">
                    download<el-icon class="el-icon--right"><Download /></el-icon>
                  </el-button>
                </div>
              </div>
              <div class="bar-item">
                <span class="bar-label">Network-Level Features</span>
                <div class="bar-wrapper">
                  <el-button type="primary" class="downloadButton" @click="downloadNetwork">
                    download<el-icon class="el-icon--right"><Download /></el-icon>
                  </el-button>
                </div>
              </div>

            </div>
          </div> 
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import StepDescription from './StepDescription.vue';
import * as echarts from 'echarts';
import request from '@/utils/request';

// ========== Props要在Emits之前定义==========
const props = defineProps({
  stepStatus: {
    type: String,
    default: 'pending'
  },
  featureData: {
    type: Object,
    default: () => ({
      taskId: '',
      windowSizeMs: 0,
      snapshots: []
    })
  }
});

const emit = defineEmits(['analyze']);

// ========== 关键：新增 v-else 容器的 ref ==========
const containerRef = ref(null); // 对应 v-else 的 .result-grid 否则数据在Dom 渲染完成之前无法获取到图表容器
const chartRef = ref(null);
let myChart = null;
let resizeHandler = null;

// ========== 计算属性 ==========
const hasData = computed(() => {
  const snapshots = props.featureData?.snapshots;
  return Array.isArray(snapshots) && snapshots.length > 0 && !!snapshots[0]?.windowStartISO;
});

// ========== 核心：适配 v-else 的图表初始化 ==========
// 安全获取图表容器
const getChartContainer = () => {
  // 优先用 chartRef
  if (chartRef.value) return chartRef.value;
  // 兜底：通过 id 获取
  const dom = document.getElementById('node-increment-chart');
  return dom || null;
};

// 初始化图表（适配 v-else 渲染时机）
const initChart = async () => {
  // 第一步：等待 v-else 容器渲染完成
  if (!containerRef.value) {
    console.warn('v-else 容器还未渲染，等待后重试');
    await nextTick(); // 等待 Vue 更新 DOM
  }

  // 第二步：等待图表容器渲染完成
  let container = getChartContainer();
  if (!container) {
    // 延迟重试（应对 DOM 渲染延迟）
    setTimeout(initChart, 200);
    return;
  }

  // 第三步：校验数据
  if (!hasData.value) {
    console.warn('无数据，跳过图表初始化');
    return;
  }

  // 第四步：初始化 ECharts
  if (myChart) myChart.dispose(); // 销毁旧实例
  myChart = echarts.init(container);
  myChart.resize(); // 强制刷新尺寸

  // 更新图表数据
  const snapshots = props.featureData.snapshots;
  const xData = snapshots.map(s => s.windowStartISO || '未知时间');
  const yData = snapshots.map(s => s.graphLevel?.nodeCount || 0);

  myChart.setOption({
    title: { text: 'Node Increments Curve', left: 'center' },
    xAxis: { type: 'category', data: xData, name:'Window Start Time', axisLabel: { rotate: 30, 
      // 格式化标签：保留日期+小时，且补充“Start”（可选）
    formatter: (value) => {
      // 示例：把 2026-03-05T12:00:00 转为 2026-03-05 12:00 (Start)
      const formattedTime = value.replace('T', ' ').slice(0, 16);
      return `${formattedTime}`; 
    }
    }},
    yAxis: { type: 'value', name: 'Node Increments' },
    series: [{
      type: 'line',
      data: yData,
      smooth: true,
      areaStyle: {},
      name: '节点数'
    }],
    tooltip: { trigger: 'axis' }
  });

  // 监听窗口大小变化
  resizeHandler = () => myChart?.resize();
  window.addEventListener('resize', resizeHandler);
  console.log('图表初始化成功');
};

// ========== 监听逻辑（核心适配 v-else） ==========
// 1. 监听 v-else 容器的渲染状态（通过 featureData 间接监听，因为 v-else 显示通常和数据相关）
watch([() => props.featureData, () => containerRef.value], async () => {
  if (hasData.value) {
    await nextTick(); // 等待 DOM 更新
    initChart();
  } else {
    // 无数据时销毁图表
    if (myChart) {
      myChart.dispose();
      myChart = null;
    }
  }
}, { deep: true, immediate: false });

// 2. 组件挂载后检查
onMounted(async () => {
  await nextTick(); // 等待初始 DOM 渲染
  if (hasData.value && containerRef.value) {
    initChart();
  }
});

// 3. 组件卸载时清理
onUnmounted(() => {
  if (resizeHandler) window.removeEventListener('resize', resizeHandler);
  if (myChart) myChart.dispose();
});

// 设置点击事件逻辑
const downloadNode = async() => {
try {
  const response = await request({
    url: '/network/downloadNode',
    method: 'GET',
    responseType: 'blob'  // 关键：指定响应类型为二进制流
  })

  // 创建下载链接
  const blob = new Blob([response.data], { type: 'text/csv' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = 'node_x.csv'  // 下载文件名
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(link.href)

} catch (error) {
  console.error('下载失败:', error)
  ElMessage.error('download failed');
}
};

const downloadNetwork = async() => {
try {
  const response = await request({
    url: '/network/downloadNetwork',
    method: 'GET',
    responseType: 'blob'  // 关键：指定响应类型为二进制流
  })

  // 创建下载链接
  const blob = new Blob([response.data], { type: 'text/csv' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = 'network_global.csv'  // 下载文件名
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(link.href)

} catch (error) {
  console.error('下载失败:', error)
  ElMessage.error('download failed');
}
};

</script>

<style scoped>
.characterization-step {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.characterization-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.action-card {
  width: 100%;
  max-width: 576px;
  text-align: center;
  background-color: #ffffff;
  padding: 64px;
  border-radius: 48px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.action-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 32px;
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
  background-color: #007bff;
  color: #ffffff;
  border: none;
  border-radius: 5px;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.3s ease;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.action-button:hover {
  background-color: orangered;
}

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
  border: 4px solid rgba(0, 164, 255, 0.1);
  border-top-color: #00a4ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 32px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.processing-title {
  font-size: 30px;
  font-weight: 900;
  color: #0f172a;
  margin: 0;
}

.result-grid {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  min-height: 0;
}

.chart-card1 {
  background-color: #ffffff;
  padding: 40px;
  border-radius: 40px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  width: 550px;
  height: 400px;
}

.chart-card {
  background-color: #ffffff;
  padding: 40px;
  border-radius: 40px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chart-title1 {
  font-size: 20px;
  font-weight: 900;
  color: #0f172a;
  margin: 0 0 40px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.chart-title {
  font-size: 15px;
  font-weight: 900;
  color: #0f172a;
  margin: 0 0 40px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.chart-title1 svg {
  color: #00a4ff;
}

.chart-container {
  flex: 1;
  min-height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mock-chart {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.bar-item {
  display: flex;
  /* align-items: center; */
  gap: 16px;
}

.bar-label {
  width: 150px;
  font-size: 10px;
  font-weight: 700;
  color: #94a3b8;
  /* text-align: right; */
}

.bar-wrapper {
  flex: 1;
  height: 50px;
  /* background-color: #f1f5f9;
  border-radius: 12px 12px 0 0;
  overflow: hidden; */
}

/* .bar-fill {
  height: 100%;
  background-color: #00a4ff;
  border-radius: 12px 12px 0 0;
  transition: width 0.3s;
}

.bar-fill.secondary {
  background-color: #e2e8f0;
} */

.bar-value {
  width: 50px;
  font-size: 14px;
  font-weight: 700;
  color: #64748b;
  text-align: left;
}

.pie-container {
  flex-direction: column;
  gap: 32px;
}

.mock-pie {
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: conic-gradient(
    #00a4ff 0deg 158deg,
    #7dd3fc 158deg 277deg,
    #f1f5f9 277deg 360deg
  );
  position: relative;
}

.pie-legend {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 700;
  color: #64748b;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
}

.legend-color.software {
  background-color: #00a4ff;
}

.legend-color.finance {
  background-color: #7dd3fc;
}

.legend-color.rd {
  background-color: #f1f5f9;
}
</style>
