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
        <!-- 新增：输入框+单位选择容器 -->
        <div class="input-group" style="margin: 10px 0; display: flex; align-items: center; gap: 8px;">
          <label style="font-size: 14px; color: #333; font-weight: 500;">Interval:</label>
          <!-- 数值输入框 -->
          <input 
            type="number" 
            v-model.number="timeValue" 
            min="1"
            style="padding: 8px; width: 200px; border: 1px solid #ddd; border-radius: 4px;"
          >
          <!-- 单位下拉框 -->
          <select 
            v-model="timeUnit" 
            style="padding: 8px; border: 1px solid #ddd; border-radius: 4px; background: #fff;"
          >
            <option value="m">minute (m)</option>
            <option value="h">hour (h)</option>
            <option value="d">day (d)</option>
          </select>
        </div>

        <button class="action-button" @click="handleAnalyze">
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
          <!-- 修复：强制指定容器尺寸，禁用布局干扰 -->
          <div 
            ref="chartRef" 
            class="chart-container pie-container" 
            style="width: 400px; height: 300px;"
          >
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
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import StepDescription from './StepDescription.vue';
import * as echarts from 'echarts';
import { ElMessage } from 'element-plus';
import { Aim, Right, Download } from '@element-plus/icons-vue';
import request from '@/utils/request';

// ========== Props定义 ==========
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

// ========== 时间参数 ==========
const timeValue = ref(30)  // 数值默认30
const timeUnit = ref('m')  // 单位默认m → 组合就是30m

// ========== 事件派发 ==========
const emit = defineEmits(['analyze']);
const handleAnalyze = () => {
  const timeParam = `${timeValue.value}${timeUnit.value}`
  emit('analyze', 2, timeParam)
};

// ========== DOM引用 ==========
const containerRef = ref(null); 
const chartRef = ref(null);
let myChart = null;
let resizeHandler = null;

// ========== 数据校验 ==========
const hasData = computed(() => {
  const snapshots = props.featureData?.snapshots;
  return Array.isArray(snapshots) && snapshots.length > 0 && !!snapshots[0]?.windowStartISO;
});

// ========== 核心：图表初始化（终极修复版） ==========
const initChart = () => {
  // 延迟300ms，确保DOM和布局完全渲染
  setTimeout(() => {
    // 1. 校验容器
    if (!chartRef.value) {
      console.warn('图表容器不存在');
      return;
    }

    // 2. 强制设置容器尺寸（兜底）
    chartRef.value.style.width = '400px';
    chartRef.value.style.height = '300px';
    const rect = chartRef.value.getBoundingClientRect();
    console.log('图表容器最终尺寸：', rect.width, rect.height);

    // 3. 销毁旧实例
    if (myChart) {
      myChart.dispose();
      myChart = null;
    }

    // 4. 初始化ECharts（强制指定尺寸和渲染器）
    myChart = echarts.init(chartRef.value, null, {
      renderer: 'canvas', // 强制canvas渲染，避免svg尺寸问题
      width: 400,
      height: 300
    });

    // 5. 无数据则退出
    if (!hasData.value) {
      console.warn('无有效数据，跳过图表渲染');
      return;
    }

    // 6. 处理图表数据
    const snapshots = props.featureData.snapshots;
    const xData = snapshots.map(s => s.windowStartISO || '未知时间');
    const yData = snapshots.map(s => s.graphLevel?.nodeCount || 0);

    // 7. 设置图表配置项
    myChart.setOption({
      title: { text: 'Node Increments Curve', left: 'center' },
      xAxis: { 
        type: 'category', 
        data: xData, 
        name: 'Window Start Time', 
        axisLabel: { 
          rotate: 30, 
          formatter: (value) => {
            const formattedTime = value.replace('T', ' ').slice(0, 16);
            return `${formattedTime}`; 
          }
        }
      },
      yAxis: { type: 'value', name: 'Node Increments' },
      series: [{
        type: 'line',
        data: yData,
        smooth: true,
        areaStyle: {},
        name: '节点数'
      }],
      tooltip: { trigger: 'axis' },
      // 强制适配容器，避免标签溢出
    });

    // 8. 强制刷新尺寸
    myChart.resize({ width: 400, height: 300 });
    console.log('ECharts初始化完成，尺寸已强制设置为400x300');

  }, 300);
};

// ========== 监听逻辑（简化版） ==========
// 只监听状态切换，确保图表在completed时渲染
watch(() => props.stepStatus, (newStatus) => {
  if (newStatus === 'completed' && hasData.value) {
    initChart();
  } else {
    // 其他状态销毁图表，避免内存泄漏
    if (myChart) {
      myChart.dispose();
      myChart = null;
    }
  }
}, { immediate: true });

// ========== 生命周期 ==========
onMounted(() => {
  // 组件挂载时检查状态，若已完成则初始化图表
  if (props.stepStatus === 'completed' && hasData.value) {
    initChart();
  }

  // 监听窗口resize
  resizeHandler = () => {
    if (myChart) {
      myChart.resize({ width: 400, height: 300 });
    }
  };
  window.addEventListener('resize', resizeHandler);
});

onUnmounted(() => {
  // 清理资源
  if (resizeHandler) window.removeEventListener('resize', resizeHandler);
  if (myChart) myChart.dispose();
});

// ========== 下载逻辑 ==========
const downloadNode = async() => {
  try {
    const response = await request({
      url: '/network/downloadExampleNode',
      method: 'GET',
      responseType: 'blob'
    });

    const blob = new Blob([response.data], { type: 'text/csv' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'node_x.csv';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(link.href);

  } catch (error) {
    console.error('Node文件下载失败:', error);
    ElMessage.error('download failed');
  }
};

const downloadNetwork = async() => {
  try {
    const response = await request({
      url: '/network/downloadExampleNetwork',
      method: 'GET',
      responseType: 'blob'
    });

    const blob = new Blob([response.data], { type: 'text/csv' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'network_global.csv';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(link.href);

  } catch (error) {
    console.error('Network文件下载失败:', error);
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
