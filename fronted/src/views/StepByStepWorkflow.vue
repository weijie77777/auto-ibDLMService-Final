<template>
  <div class="step-workflow-page">
    <NavBar />
    <div class="workflow-container">
      <!-- Sidebar -->
      <StepWorkflowSidebar 
        :current-step="currentStep"
        :step-statuses="stepStatuses"
        :mode="mode"
        @step-change="handleStepChange"
        @mode-change="handleModeChange"
        @reset-project="handleResetProject"
      />

      <!-- Main Content -->
      <main class="workflow-main">
        <!-- Header -->
        <header class="workflow-header">
          <h2 class="workflow-title">
            {{ currentStepNumber }}. {{ stepTitles[currentStep - 1] }}
          </h2>
        </header>

        <!-- Step Content -->
        <div class="workflow-content">
          <AcquisitionStep 
            v-if="currentStep === 1"
            :files="files"
            :step-status="stepStatuses[0]"
            @file-upload="handleFileUpload"
            @file-remove="handleFileRemove"
            @clear-all="handleClearAll"
          />

          <SynthesisStep 
            v-if="currentStep === 2"
            :step-status="stepStatuses[1]"
            :data="graphData"
            @synthesize="() => handleStepAction(1)"
          />

          <CharacterizationStep 
            v-if="currentStep === 3" 
            :step-status="stepStatuses[2]"
            :feature-data="featureData"
            @analyze="() => handleStepAction(2)"
          />

          <EmbeddingStep 
            v-if="currentStep === 4"
            :step-status="stepStatuses[3]"
            @pretrain="handlePreTrain"
          />

            <EmbeddingStep1 
            v-if="currentStep === 5"
            :step-status="stepStatuses[4]"
            @train="handleTrain"
          />

          <AlignmentStep 
            v-if="currentStep === 6"
            :step-status="stepStatuses[5]"
            @finalize="handlePredict"
          />
        </div>

        <!-- Footer Navigation -->
        <footer class="workflow-footer">
          <button 
            class="nav-button back-button"
            :disabled="currentStep === 1"
            @click="handleBack">
            <span class="chevron-left">‹</span> Back
          </button>
          
          <button 
            class="nav-button proceed-button"
            :disabled="currentStep === 6 || stepStatuses[currentStep - 1] !== 'completed'"
            @click="handleProceed"
          >
            Next <span class="chevron-right">›</span>
          </button>
        </footer>
      </main>
    </div>
    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import NavBar from '@/components/NavBar.vue';
import Footer from '@/components/Footer.vue';
import StepWorkflowSidebar from '@/components/StepByStepWorkflow/Sidebar.vue';
import AcquisitionStep from '@/components/StepByStepWorkflow/AcquisitionStep.vue';
import SynthesisStep from '@/components/StepByStepWorkflow/SynthesisStep.vue';
import CharacterizationStep from '@/components/StepByStepWorkflow/CharacterizationStep.vue';
import EmbeddingStep from '@/components/StepByStepWorkflow/EmbeddingStep.vue';
import EmbeddingStep1 from '@/components/StepByStepWorkflow/EmbeddingStep1.vue';
import AlignmentStep from '@/components/StepByStepWorkflow/AlignmentStep.vue';
import NetworkGraph from  '@/components/StepByStepWorkflow/NetworkGraph.vue'
import request from '@/utils/request';
import { useRouter } from 'vue-router'

const STORAGE_KEY = 'step-by-step-workflow-state';

// 初始化状态
const currentStep = ref(1);
const mode = ref('guided'); // 'guided' or 'express'
const files = ref([]);
const stepStatuses = ref(['pending', 'locked', 'locked', 'locked', 'locked', 'locked']); // pending, processing, completed, locked
// 接收图数据
const graphData = ref({
  taskId: '',
  nodeCount: 0,
  edgeCount: 0,
  idMapping: {},
  edges: [],
  timeRange: ['', '']
})
// 接收特征数据
const featureData = ref({
  taskId: '',
  windowSizeMs: 0,
  snapshots: []
});

// 从 localStorage 恢复状态
const loadState = () => {
  try {
    const savedState = localStorage.getItem(STORAGE_KEY);
    if (savedState) {
      const state = JSON.parse(savedState);
      currentStep.value = state.currentStep || 1;
      mode.value = state.mode || 'guided';
      files.value = state.files || [];
      stepStatuses.value = state.stepStatuses || ['pending', 'locked', 'locked', 'locked', 'locked', 'locked'];
      graphData.value = state.graphData || {
        taskId: '',
        nodeCount: 0,
        edgeCount: 0,
        idMapping: {},
        edges: [],
        timeRange: ['', '']
      };
      featureData.value = state.featureData || {
        taskId: '',
        windowSizeMs: 0,
        snapshots: []
      };
    }
  } catch (error) {
    console.error('Failed to load workflow state:', error);
  }
};

// 保存状态到 localStorage
const saveState = () => {
  try {
    const state = {
      currentStep: currentStep.value,
      mode: mode.value,
      files: files.value,
      stepStatuses: stepStatuses.value,
      graphData: graphData.value,
      featureData: featureData.value // 记得要保存数据 保证数据的持久化
    };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  } catch (error) {
    console.error('Failed to save workflow state:', error);
  }
};

// 清除保存的状态
const clearState = () => {
  try {
    localStorage.removeItem(STORAGE_KEY);
  } catch (error) {
    console.error('Failed to clear workflow state:', error);
  }
};

// 监听状态变化并自动保存
watch([currentStep, mode, files, stepStatuses, graphData], () => {
  saveState();
}, { deep: true });

watch(currentStep, ()=>{
  window.scrollTo({ top: 0, behavior: 'smooth' })
})

// 组件挂载时恢复状态
onMounted(() => {
  loadState();
  handleClearAll();
});

const stepTitles = [
  'Data Collection',
  'Dynamic Interaction Network',
  'Characterization',
  'Model Pre-training',
  'Model Fine-tuning',
  'Prediction Module'
];

const currentStepNumber = computed(() => currentStep.value);

const handleStepChange = (step) => {
  if (stepStatuses.value[step - 1] !== 'locked') {
    currentStep.value = step;
  }
};

const handleModeChange = (newMode) => {
  mode.value = newMode;
};

const handleResetProject = () => {
  if (confirm('Reset current project? All progress will be cleared.')) {
    files.value = [];
    currentStep.value = 1;
    stepStatuses.value = ['pending', 'locked', 'locked', 'locked', 'locked', 'locked'];
    clearState(); // 清除保存的状态
  }
};

const handleFileUpload = (fileList) => {
  files.value = [...files.value, ...fileList];
  // stepStatuses.value[0] = 'completed';
  // if (stepStatuses.value[1] === 'locked') {
  // 这里的逻辑改为只要重新上传文件，则除了第一步外重新开始
  stepStatuses.value = ['completed', 'pending', 'locked', 'locked', 'locked', 'locked'];
  // }
};

const handleFileRemove = (fileId) => {
  files.value = files.value.filter(f => f.id !== fileId);
};

const handleClearAll = () => {
  files.value = [];
};

const handleStepAction = async (stepIndex) => {
  stepStatuses.value[stepIndex] = 'processing';

  let delayTime = 1500; // 默认延迟

  // 第3步（索引2）有请求，计算实际耗时
  if (stepIndex === 2) {
    const startTime = Date.now(); // 记录开始时间
    
    try {
      const res = await request.get('/network/analyze');
      featureData.value = res.data;
      console.log("特征数据", res.data);
      // 计算实际耗时，至少保留最小动画时间（如300ms）
      const actualTime = Date.now() - startTime;
      delayTime = Math.max(actualTime, 300);
      
    } catch (err) {
      ElMessage.error(err.message);
      // 请求失败仍使用默认时间或更短
      delayTime = 800;
    }
  }

  // 使用计算后的延迟时间
  setTimeout(() => {
    stepStatuses.value[stepIndex] = 'completed';
    if (stepIndex < 5) {
      stepStatuses.value[stepIndex + 1] = 'pending';
    }
  }, delayTime);

};

const handlePreTrain = async (stepIndex, windowSize, epochs) => {

  stepStatuses.value[stepIndex] = 'processing';

  let delayTime = 1500; // 默认延迟

  const startTime = Date.now(); // 记录开始时间
    
  try {
    const res = await request.post('/network/pretrain', {
      windowSize: windowSize,
      epochs: epochs
    });
    // 计算实际耗时，至少保留最小动画时间（如300ms）
    const actualTime = Date.now() - startTime;
    delayTime = Math.max(actualTime, 300);
      
    } catch (err) {
    ElMessage.error(err.message);
    // 请求失败仍使用默认时间或更短
    delayTime = 800;
    }
  

  // 使用计算后的延迟时间
  setTimeout(() => {
    stepStatuses.value[stepIndex] = 'completed';
    if (stepIndex < 5) {
      stepStatuses.value[stepIndex + 1] = 'pending';
    }
  }, delayTime);
  
};

const handleTrain = async (stepIndex, epochs) => {

  stepStatuses.value[stepIndex] = 'processing';

  console.log('epochs', epochs)
  let delayTime = 1500; // 默认延迟

  const startTime = Date.now(); // 记录开始时间
    
  try {
    const res = await request.post('/network/train', JSON.stringify(epochs), {
    headers: {
      'Content-Type': 'application/json' // 必须声明JSON格式
    }
  });
    // 计算实际耗时，至少保留最小动画时间（如300ms）
    const actualTime = Date.now() - startTime;
    delayTime = Math.max(actualTime, 300);
      
    } catch (err) {
    ElMessage.error(err.message);
    // 请求失败仍使用默认时间或更短
    delayTime = 800;
    }
  

  // 使用计算后的延迟时间
  setTimeout(() => {
    stepStatuses.value[stepIndex] = 'completed';
    if (stepIndex < 5) {
      stepStatuses.value[stepIndex + 1] = 'pending';
    }
  }, delayTime);
  
};

const router = useRouter()
// 跳转函数
const jumpToPage = () => {
  router.push('/historical-record') // 跳转Vue路由
  // 外部链接：window.location.href = 'https://xxx.com'
  return {} // 必须返回空对象，避免style报错
}

const handlePredict = async (stepIndex, threshold) => {

  stepStatuses.value[stepIndex] = 'processing';

  console.log('epochs', threshold)
  let delayTime = 1500; // 默认延迟

  const startTime = Date.now(); // 记录开始时间
    
  try {
    const res = await request.post('/network/finalPredict', JSON.stringify(threshold), {
    headers: {
      'Content-Type': 'application/json' // 必须声明JSON格式
    }
  });
    // 计算实际耗时，至少保留最小动画时间（如300ms）
    const actualTime = Date.now() - startTime;
    delayTime = Math.max(actualTime, 300);
      
    } catch (err) {
    ElMessage.error(err.message);
    // 请求失败仍使用默认时间或更短
    delayTime = 800;
    }

  // 使用计算后的延迟时间
  setTimeout(() => {
    stepStatuses.value[stepIndex] = 'completed';
    jumpToPage();
    stepStatuses.value[stepIndex] = 'pending';
    if (stepIndex < 5) {
      stepStatuses.value[stepIndex + 1] = 'pending';
    }
  }, delayTime);
  

};

const handleBack = () => {
  if (currentStep.value > 1) {
    currentStep.value--;
  }
};

const handleProceed = async() => {
  // 处理数据上传 当前步骤为1 且状态为completed 并且上传了文件才处理
  if (currentStep.value === 1 && stepStatuses.value[0] === 'completed' && files.value.length >0) {
    // 校验
    if (files.value.length > 1) {
      ElMessage.error('You have uploaded too many files. Please check carefully and upload only one valid file.')
      return;
    }
    // 构建 FormData
    const formData = new FormData();
    // 添加所有文件
    files.value.forEach(file => {
      console.log('file 类型:', typeof file);           // 应该是 "object"
      console.log('file 构造函数:', file.constructor?.name);  // 必须是 "File"
      console.log('file instanceof File:', file instanceof File);  // 必须是 true
      console.log('file 的 keys:', Object.keys(file));   // 如果是 ["id", "name", "file"] 说明被包装了
      console.log('Appending file:', file.name, file.size, file.type)
      formData.append('file', file.raw);
    });
    try {
    const response = await request.post('/network/upload', formData);
    console.log('Metwork Construction Result:', response);
    graphData.value = response.data;
    handleClearAll();
    if(!response.code === '1'){
      ElMessage.error(response.msg);
      return;
    }
    } catch (error) {
      console.error('Network Constrcutin failed:', error);
      ElMessage.error('Network Constrcution failed, please check your dataset and try again');
    }
  }
  if (currentStep.value < 6 && stepStatuses.value[currentStep.value - 1] === 'completed') {
    currentStep.value++;
  }
};
</script>

<style scoped>
.step-workflow-page {
  width: 100%;
  min-height: 100vh;
  padding-top: 70px;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.step-workflow-page > .workflow-container {
  flex: 1;
}

.workflow-container {
  display: flex;
  width: 100%;
  flex: 1;
  min-height: calc(100vh - 70px);
  align-items: stretch;
  overflow: hidden;
}

.workflow-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
  overflow: hidden;
}

.workflow-header {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  border-bottom: 1px solid #f1f5f9;
  background-color: #ffffff;
}

.workflow-title {
  font-size: 24px;
  font-weight: 900;
  color: #333333;
  letter-spacing: -0.5px;
  margin: 0;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.workflow-content {
  flex: 1;
  overflow-y: auto;
  padding: 32px;
}

.workflow-footer {
  height: 96px;
  border-top: 1px solid #f1f5f9;
  padding: 0 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #ffffff;
  flex-shrink: 0;
}

.nav-button {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 24px;
  border: none;
  border-radius: 5px;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.back-button {
  background-color: transparent;
  color: #007bff;
  text-decoration: none;
}

.back-button:hover:not(:disabled) {
  color: orangered;
}

.back-button:disabled {
  opacity: 0.8;
  cursor: not-allowed;
}

.proceed-button {
  background-color: #007bff;
  color: #ffffff;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.proceed-button:hover:not(:disabled) {
  background-color: orangered;
}

.proceed-button:disabled {
  opacity: 0.2;
  cursor: not-allowed;
}

.chevron-left,
.chevron-right {
  font-size: 28px;
  line-height: 1;
  margin-bottom: 5px;
}
</style>
