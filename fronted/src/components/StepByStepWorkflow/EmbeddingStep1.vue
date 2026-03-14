<template>
  <div class="embedding-step" v-if="isabled===false">
    <StepDescription 
      text="Pre-training the auto-learning layer to suppress noisy or irrelevant feature
           components."
    />

    <div class="embedding-content">
      <div v-if="stepStatus === 'pending'" class="action-card">
        <div class="action-icon">
          <img src="@/assets/images/step_by_step/model-training.png" width="100px" height="100px">
        </div>
        <h4 class="action-title">Pre-training</h4>
        <!-- 新增输入项 -->
        <div class="input-group">
          <label for="windowSize">Window Size</label>
          <input
            id="windowSize"
            v-model.number="windowSize"
            type="number"
            :min="min_WindowSize"
            :max="max_WindowSize"
            required
          >
        </div>

        <div class="input-group">
          <label for="epochs">Pre-raining Epochs</label>
          <input
            id="epochs"
            v-model.number="epochs"
            type="number"
            :min="min_epochs"
            :max="max_epochs"
            placeholder="Please input train epochs"
            required
          >
        </div>
        

        <button class="action-button" @click="$emit('pretrain', 3, windowSize, epochs)" >
          Start Pre-training
        </button>
      </div>

      <div v-else-if="stepStatus === 'processing'" class="processing-state">
        <div class="spinner"></div>
        <h4 class="processing-title">Pre-training auto-learning layer...</h4>
        <div class="progress-bar">
          <div class="progress-fill"></div>
        </div>
      </div>

      <div v-else class="result-state">
        <div class="result-container">
          <div class="result-content">
            <h3 class="result-title">Pre-training<br/>Completed</h3>
            <p class="result-description">
              The auto-learning layer has been successfully pre-trained. The layer is ready to participate in the jointly optimized.
            </p>
            <button class="result-button" @click="downloadPretrain">
              to jointly optimized
            </button>
          </div>
          <div class="neural-icon-container">
            <svg width="100" height="100" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="neural-icon">
              <rect x="2" y="2" width="20" height="20" rx="2.18" ry="2.18"></rect>
              <line x1="7" y1="2" x2="7" y2="22"></line>
              <line x1="17" y1="2" x2="17" y2="22"></line>
              <line x1="2" y1="12" x2="22" y2="12"></line>
            </svg>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div class="embedding-step" v-if="isabled===true">
    <StepDescription 
      text="The entire model—including both the auto-learning layer and the GRU-based prediction module—is
            jointly optimized to convert raw feature vectors into a compact, robust, and
            informative latent representation."/>

    <div class="embedding-content">
      <div v-if="stepStatus1 === 'pending'" class="action-card">
        <div class="action-icon">
          <img src="@/assets/images/step_by_step/model-training.png" width="100px" height="100px">
        </div>
        <h4 class="action-title">Jointly Optimizing</h4>
        <!-- 新增输入项 -->

        <div class="input-group">
          <label for="epochs">Training Epochs</label>
          <input
            id="epochs"
            v-model.number="epochs1"
            type="number"
            :min="min_epochs"
            :max="max_epochs"
            placeholder="Please input train epochs"
            required
          >
        </div>
        

        <button class="action-button" @click="$emit('train', 3, epochs1)" >
          Start Jonintly Optimizing
        </button>
      </div>

      <div v-else-if="stepStatus1 === 'processing'" class="processing-state">
        <div class="spinner"></div>
        <h4 class="processing-title">Jonintly Optimizing...</h4>
        <div class="progress-bar">
          <div class="progress-fill"></div>
        </div>
      </div>

      <div v-else-if="stepStatus1 === 'completed'" class="result-state">
        <div class="result-container1">
          <div class="result-content">
            <h3 class="result-title">Jointly Optimizing<br/>Completed</h3>
            <p class="result-description">
              The auto-ibDLM model has been successfully optimized. The model is ready to predict the development of public events.
            </p>
            <button class="result-button" @click="downloadTrain">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="7 10 12 15 17 10"></polyline>
                <line x1="12" y1="15" x2="12" y2="3"></line>
              </svg>
              Export Latent Representation
            </button>
          </div>
          <div class="neural-icon-container">
            <svg width="100" height="100" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="neural-icon">
              <rect x="2" y="2" width="20" height="20" rx="2.18" ry="2.18"></rect>
              <line x1="7" y1="2" x2="7" y2="22"></line>
              <line x1="17" y1="2" x2="17" y2="22"></line>
              <line x1="2" y1="12" x2="22" y2="12"></line>
            </svg>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed }  from 'vue';
import StepDescription from './StepDescription.vue';
import request from '@/utils/request';


const props = defineProps({
  stepStatus: {
    type: String,
    default: 'pending'
  },
  stepStatus1: {
    type: String,
    default: 'locked'
  },
  isabled: {
    type: Boolean,
    default: false
  }
});

const emit =defineEmits(['pretrain, train', 'update:isabled']);

const min_WindowSize = ref(1);
const max_WindowSize = ref(7);
const min_epochs = ref(100);
const max_epochs = ref(3000);

const windowSize = ref(7);
const STORAGE_KEY = 'step-by-step-workflow-state';
onMounted(() => {
  const state = JSON.parse(localStorage.getItem(STORAGE_KEY));
  if (state) {
    max_WindowSize.value = state.featureData.snapshots.length-1;
  }
});

// 子组件方法：触发事件，通知父组件修改值
const downloadPretrain = async() => {
  emit('update:isabled', true); 
};

const downloadTrain = async() => {
try {
  const response = await request({
    url: '/network/downloadExampleTrain',
    method: 'GET',
    responseType: 'blob'  // 关键：指定响应类型为二进制流
  })

  // 创建下载链接
  const blob = new Blob([response.data], { type: 'text/csv/pth' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = 'Latent_Representation.csv'  // 下载文件名
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
.embedding-step {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.embedding-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.input-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 20px;
  width: 100%;
}

.input-group label {
  font-size: 14px;
  color: #666;
  margin-bottom: 6px;
}

.input-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
}

.input-group input:focus {
  outline: none;
  border-color: #2563EB;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.2);
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
  width: 100%;
  max-width: 768px;
  margin: 0 auto;
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
  font-size: 36px;
  font-weight: 900;
  color: #0f172a;
  margin: 0 0 32px 0;
}

.progress-bar {
  width: 100%;
  height: 12px;
  background-color: #f1f5f9;
  border-radius: 9999px;
  overflow: hidden;
  padding: 2px;
}

.progress-fill {
  height: 100%;
  width: 75%;
  background-color: #00a4ff;
  border-radius: 9999px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.result-state {
  width: 100%;
  flex: 1;
}

.result-container {
  background-color: #0f172a;
  border-radius: 64px;
  padding: 64px;
  height: 400px;
  color: #ffffff;
  position: relative;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 64px;
  text-align: justify;
}

.result-container::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 400px;
  height: 300px;
  background-color: rgba(0, 164, 255, 0.2);
  border-radius: 50%;
  filter: blur(150px);
}

.result-container1 {
  background-color: #0f172a;
  border-radius: 64px;
  padding: 64px;
  height: 500px;
  color: #ffffff;
  position: relative;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 64px;
  text-align: justify;
}

.result-container1::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 400px;
  height: 300px;
  background-color: rgba(0, 164, 255, 0.2);
  border-radius: 50%;
  filter: blur(150px);
}


.result-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 32px;
  position: relative;
  z-index: 10;
}

.result-title {
  font-size: 40px;
  font-weight: 900;
  letter-spacing: -1px;
  line-height: 1.1;
  margin: 0;
}

.result-description {
  font-size: 20px;
  color: #94a3b8;
  line-height: 1.6;
  font-weight: 500;
  margin: 0;
}

.result-button {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 48px;
  background-color: #00a4ff;
  color: #ffffff;
  border: none;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 900;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 10px 30px rgba(0, 164, 255, 0.3);
  width: fit-content;
}

.result-button:hover {
  background-color: #0088cc;
}

.neural-icon-container {
  width: 200px;
  aspect-ratio: 1;
  background-color: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 10;
}

.neural-icon {
  color: #00a4ff;
  animation: pulse 2s infinite;
  filter: drop-shadow(0 0 15px rgba(0, 164, 255, 0.7));
}
</style>
