<template>
  <div class="synthesis-step">
    <StepDescription 
      text="Create interactive network based on user interactions. Each participant is abstracted as a node, and their interaction behaviors are described as edges connecting the nodes.
            Due to constraints on the number of nodes and edges, we select only the most influential node (marked in red), along with its associated 100 edges and corresponding nodes for visualization. To access the complete dataset, please click to download the edge list file."
    />

    <div class="synthesis-content">
      <!-- v-if="stepStatus === 'pending'" -->
      <div v-if="stepStatus === 'pending'" class="action-card">
        <div class="action-icon">
          <img src="@/assets/images/step_by_step/network.png" width="100px" height="100px">
        </div>
        <h4 class="action-title">Create Interactive Network</h4>
        <button class="action-button" @click="$emit('synthesize', 1)">
          Build Network
        </button>
      </div>
      <!-- v-else-if="stepStatus === 'processing'" -->
      <div v-else-if="stepStatus === 'processing'" class="processing-state">
        <div class="spinner"></div>
        <h4 class="processing-title">Building Network...</h4>
      </div>
      <!-- v-else -->
      <div v-else class="result-state">
        <div class="result-container" v-if="isShow==false">
          <div class="network-icon">
            <svg width="100" height="100" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <circle cx="12" cy="12" r="2"></circle>
              <circle cx="6" cy="6" r="2"></circle>
              <circle cx="18" cy="6" r="2"></circle>
              <circle cx="6" cy="18" r="2"></circle>
              <circle cx="18" cy="18" r="2"></circle>
              <line x1="12" y1="12" x2="6" y2="6"></line>
              <line x1="12" y1="12" x2="18" y2="6"></line>
              <line x1="12" y1="12" x2="6" y2="18"></line>
              <line x1="12" y1="12" x2="18" y2="18"></line>
            </svg>
          </div>
          <h3 class="result-title">Network Created</h3>
          <p class="result-description">
            Interactive network construction completed. The dynamic interaction network has been successfully generated based on participant interactions.
          </p>
          <button class="result-button" @click="isShowned()">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
              <polyline points="15 3 21 3 21 9"></polyline>
              <line x1="10" y1="14" x2="21" y2="3"></line>
            </svg>
            View Network Visualization
          </button>
        </div>
        <div class="result-realcontainer" v-if="isShow==true">
          <p class="data-stats">
          <span class="label">graphData</span>
          <span class="divider">(</span>
          <span class="stat">
            <el-icon class="icon"><Aim /></el-icon>
            nodeCount: <b>{{ graphData.nodeCount }}</b>
          </span>
          <span class="divider">,</span>
          <span class="stat">
            <el-icon class="icon"><Right /></el-icon>
            edgeCount: <b>{{ graphData.edgeCount }}</b>
          </span>
          <span class="divider">)</span>
          <a href="javascript:voide(0)" class="downloadedge" @click="downloadEdgeList"> download edgeList <el-icon class="downicon"><Download /></el-icon> </a>
          </p>
          <NetworkGraph :data="graphData" class="networkGraph"/>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import StepDescription from './StepDescription.vue';
import NetworkGraph from './NetworkGraph.vue';
import { ElMessage } from 'element-plus';
import request from '@/utils/request';
const isShow = ref(false);
// 接收父组件数据
const props = defineProps({
  stepStatus: {
    type: String,
    default: 'pending'
  },
  data: {
    type: Object,
    default: () => ({})
  }
});

const graphData = computed(() => {
  // 关键：如果是复杂对象，深拷贝
  // 简单对象可直接返回：return props.data;
  return JSON.parse(JSON.stringify(props.data)); 
});

// stepStatus 同理，用 computed 同步
const stepStatus = computed(() => props.stepStatus);

watch(
  () => props.data,
  (newData) => {
    console.log('子组件监听到 data 更新:', newData);
    console.log('子组件 graphData 同步更新:', graphData.value); 
  },
  { deep: true, immediate: true } // 深度监听 + 首次加载立即执行
);

defineEmits(['synthesize']);
const isShowned = ()=>{
  if(props.data===null){
    ElMessage.error('Create Network Failed, Please Try Again Later');
    return;
  }
  isShow.value = true;
}

const downloadEdgeList = async() => {
try {
  const response = await request({
    url: '/network/downloadEdgeList',
    method: 'GET',
    responseType: 'blob'  // 关键：指定响应类型为二进制流
  })

  // 创建下载链接
  const blob = new Blob([response.data], { type: 'text/csv' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = 'edgeList.csv'  // 下载文件名
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
.synthesis-step {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.synthesis-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
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
}

.spinner {
  width: 80px;
  height: 80px;
  border: 4px solid rgba(0, 164, 255, 0.1);
  border-top-color: #00a4ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 32px;
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

.result-state {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.result-container {
  background-color: #0f172a;
  border-radius: 48px;
  padding: 48px;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.result-container::before {
  content: '';
  position: absolute;
  inset: 0;
  opacity: 0.1;
  pointer-events: none;
  background-image: radial-gradient(#00a4ff 1px, transparent 1px);
  background-size: 60px 60px;
}

.result-realcontainer {
  width: 100%;
  height: 100%;
  border-radius: 12px;
}

.data-stats {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  font-family: 'SF Mono', Monaco, monospace;
  font-size: 14px;
  /* box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4); */
  margin: 0;
  width: 100%;
}

.data-stats .label {
  font-weight: 600;
  /* color: #ffd700; */
}

.data-stats .divider {
  color: rgba(255, 255, 255, 0.6);
}

.data-stats .stat {
  display: flex;
  align-items: center;
  gap: 4px;
}

.data-stats .stat b {
  color: #fff;
  font-size: 16px;
  min-width: 30px;
  text-align: center;
}

.data-stats .icon {
  color: #ffd700;
}
.data-stats .downloadedge { 
  margin-left: 30%;
  cursor: pointer;
  color: #fff;
}
.data-stats .downloadedge:hover {
  color: #ffd700;
}
.data-stats .downloadedge .downicon {
  font-size: 15px;
}

@keyframes gridMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(50px, 50px); }
}
.network-icon {
  color: #00a4ff;
  margin-bottom: 32px;
  position: relative;
  z-index: 1;
}

.result-title {
  font-size: 36px;
  font-weight: 900;
  color: #ffffff;
  margin: 0 0 24px 0;
  letter-spacing: -0.5px;
  position: relative;
  z-index: 1;
}

.result-description {
  font-size: 20px;
  color: #94a3b8;
  max-width: 768px;
  margin: 0 0 40px 0;
  line-height: 1.6;
  font-weight: 500;
  position: relative;
  z-index: 1;
}

.result-button {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 48px;
  background-color: rgba(255, 255, 255, 0.1);
  color: #ffffff;
  border: none;
  border-radius: 24px;
  font-size: 18px;
  font-weight: 900;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  z-index: 1;
}

.result-button:hover {
  background-color: #ffffff;
  color: #0f172a;
}
</style>
