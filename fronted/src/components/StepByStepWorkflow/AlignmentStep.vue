<template>
  <div class="alignment-step">
    <StepDescription 
      text="Predict public events using the trained ibDLM model. The model predicts the increment of nodes in the dynamic interaction network to determine if a public event will occur."
    />

    <div class="alignment-content">
      <div v-if="stepStatus === 'pending'" class="action-card">
        <div class="action-icon">
          <img src="@/assets/images/step_by_step/prediction.png" width="100px" height="100px">
        </div>
        <h4 class="action-title">Event Prediction</h4>
        <!-- 新增输入项 -->
        <div class="input-group">
          <label for="threshold">threshold</label>
          <input
            id="threshold"
            v-model.number="threshold"
            type="number"
            :min="min_threshold"
            :max="max_threshold"
            placeholder="Please input event threshold"
            required
          >
        </div>
        <button class="action-button" @click="$emit('finalize', 5, threshold)">
          Start Prediction
        </button>
      </div>

      <div v-else-if="stepStatus === 'processing'" class="processing-state">
        <div class="spinner"></div>
        <h4 class="processing-title">Predicting Events...</h4>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import StepDescription from './StepDescription.vue';
import { useRouter } from 'vue-router'

const props = defineProps({
  stepStatus: {
    type: String,
    default: 'pending'
  }
});

const emit = defineEmits(['finalize']);

const metrics = ref([
  { label: 'Prediction Accuracy', value: '97.2%', colorClass: 'metric-emerald' },
  { label: 'Model Performance', value: '98.5%', colorClass: 'metric-brand' },
  { label: 'Event Detection Rate', value: '96.8%', colorClass: 'metric-blue' }
]);

const min_threshold = ref(1);
const max_threshold = ref(30000);



const router = useRouter()

// 跳转函数
const jumpToPage = () => {
  router.push('/historical-record') // 跳转Vue路由
  // 外部链接：window.location.href = 'https://xxx.com'
  return {} // 必须返回空对象，避免style报错
}
</script>

<style scoped>

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

.alignment-step {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.alignment-content {
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
  font-size: 36px;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
}

.result-grid {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  min-height: 0;
}

.radar-card {
  background-color: #ffffff;
  padding: 48px;
  border-radius: 48px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.radar-title {
  font-size: 20px;
  font-weight: 900;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.2em;
  margin: 0 0 40px 0;
}

.radar-container {
  flex: 1;
  min-height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mock-radar {
  width: 100%;
  aspect-ratio: 1;
  max-width: 400px;
  position: relative;
}

.radar-center {
  width: 100%;
  height: 100%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.radar-ring {
  position: absolute;
  border: 1px solid #f1f5f9;
  border-radius: 50%;
}

.ring-1 {
  width: 33%;
  height: 33%;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.ring-2 {
  width: 66%;
  height: 66%;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.ring-3 {
  width: 100%;
  height: 100%;
}

.radar-lines {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
}

.radar-line {
  position: absolute;
  width: 1px;
  height: 100%;
  background-color: #f1f5f9;
  top: 0;
  left: 50%;
  transform-origin: center;
}

.radar-polygon {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
}

.radar-point {
  position: absolute;
  width: 8px;
  height: 8px;
  background-color: #00a4ff;
  border-radius: 50%;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) rotate(var(--angle)) translateY(calc(-50% * var(--distance)));
  box-shadow: 0 0 10px rgba(0, 164, 255, 0.5);
}

.metrics-column {
  display: flex;
  flex-direction: column;
  gap: 24px;
  overflow: hidden;
}

.metric-card {
  background-color: #f8fafc;
  padding: 32px;
  border-radius: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #f1f5f9;
  flex: 1;
}

.metric-label {
  font-size: 20px;
  font-weight: 700;
  color: #475569;
}

.metric-value {
  font-size: 36px;
  font-weight: 900;
}

.metric-emerald {
  color: #10b981;
}

.metric-brand {
  color: #00a4ff;
}

.metric-blue {
  color: #3b82f6;
}

.export-button {
  margin-top: 16px;
  width: 100%;
  background-color: #0f172a;
  color: #ffffff;
  padding: 32px;
  border-radius: 32px;
  border: none;
  font-size: 28px;
  font-weight: 900;
  cursor: pointer;
  transition: background-color 0.2s;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
}

.export-button:hover {
  background-color: #000000;
}

.export-button svg {
  color: #00a4ff;
}
</style>
