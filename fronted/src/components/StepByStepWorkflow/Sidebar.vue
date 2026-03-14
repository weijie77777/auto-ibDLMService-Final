<template>
  <aside class="workflow-sidebar">
    <div class="sidebar-header">
      <div class="logo-container">
        <div class="logo-icon">
          <img src="@/assets/images/step_by_step/steps.png" width="40px" height="40px" class="logo-icon" alt="logo">
        </div>
        <div class="logo-text">
          <h1 class="logo-title">STEPS</h1>
        </div>
      </div>
    </div>

    <nav class="sidebar-nav">
      <button
        v-for="step in steps"
        :key="step.id"
        :class="['nav-item', {
          'nav-item-active': currentStep === step.id,
          'nav-item-locked': stepStatuses[step.id - 1] === 'locked',
          'nav-item-completed': stepStatuses[step.id - 1] === 'completed'
        }]"
        :disabled="stepStatuses[step.id - 1] === 'locked' && mode === 'guided'"
        @click="$emit('step-change', step.id)"
      >
        <span class="nav-item-text">{{ step.name }}</span>
        <img 
          v-if="stepStatuses[step.id - 1] === 'completed'" 
          src="@/assets/images/step_by_step/对.png" 
          alt="completed" 
          class="nav-item-check"
        />
      </button>
    </nav>

    <div class="sidebar-footer">
      <button class="reset-button" @click="$emit('reset-project')">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <polyline points="23 4 23 10 17 10"></polyline>
          <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
        </svg>
        Reset
      </button>
    </div>
  </aside>
</template>

<script setup>
defineProps({
  currentStep: {
    type: Number,
    required: true
  },
  stepStatuses: {
    type: Array,
    required: true
  },
  mode: {
    type: String,
    default: 'guided'
  }
});

defineEmits(['step-change', 'mode-change', 'reset-project']);

const steps = [
  { id: 1, name: 'Data Collection' },
  { id: 2, name: 'Dynamic Interaction Network' },
  { id: 3, name: 'Characterization' },
  { id: 4, name: 'Model Pre-training' },
  { id: 5, name: 'Model Fine-tuning' },
  { id: 6, name: 'Prediction Module' }
];
</script>

<style scoped>
.workflow-sidebar {
  width: 320px;
  min-height: 100%;
  background-color: #f8fafc;
  border-right: 1px solid #f1f5f9;
  display: flex;
  flex-direction: column;
  padding: 32px;
  flex-shrink: 0;
}

.sidebar-header {
  margin-bottom: 40px;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-title {
  font-size: 20px;
  font-weight: 900;
  color: #0f172a;
  line-height: 1;
  margin: 0;
  letter-spacing: -0.5px;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.nav-item {
  width: 100%;
  text-align: left;
  padding: 16px 20px;
  border-radius: 12px;
  border: none;
  background-color: transparent;
  color: #56575b;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
}

.nav-item:hover:not(.nav-item-locked) {
  color: #0f172a;
  background-color: rgba(226, 232, 240, 0.5);
}

.nav-item-active {
  color: #007bff;
  background-color: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.nav-item-locked {
  opacity: 0.5;
  cursor: not-allowed;
}

.nav-item-check {
  width: 20px;
  height: 20px;
  object-fit: contain;
  flex-shrink: 0;
}

.nav-item-text {
  flex: 1;
}

.sidebar-footer {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.reset-button {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  border: none;
  background-color: transparent;
  color: #94a3b8;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  transition: color 0.2s;
}

.reset-button:hover {
  color: #ef4444;
}
</style>
