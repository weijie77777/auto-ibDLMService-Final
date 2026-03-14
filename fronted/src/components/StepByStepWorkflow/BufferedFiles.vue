<template>
  <div class="buffered-files">
    <div class="buffered-header">
      <span class="buffered-title">Dataset Files ({{ files.length }})</span>
      <button
        v-if="files.length > 0"
        class="clear-button"
        @click="$emit('clear-all')">
        Clear All
      </button>
    </div>

    <div class="buffered-list">
      <div v-if="files.length === 0" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
          <polyline points="14 2 14 8 20 8"></polyline>
          <line x1="16" y1="13" x2="8" y2="13"></line>
          <line x1="16" y1="17" x2="8" y2="17"></line>
          <polyline points="10 9 9 9 8 9"></polyline>
        </svg>
        <p class="empty-text">No dataset files</p>
      </div>

      <div v-else class="file-list">
        <div
          v-for="file in files"
          :key="file.id"
          class="file-item"
        >
          <div class="file-info">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="file-icon">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
            </svg>
            <span class="file-name">{{ file.name }}</span>
          </div>
          <button class="remove-button" @click="$emit('remove', file.id)">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  files: {
    type: Array,
    default: () => []
  }
});

defineEmits(['remove', 'clear-all']);
</script>

<style scoped>
.buffered-files {
  background-color: #ffffff;
  border: 1px solid #f1f5f9;
  border-radius: 32px;
  padding: 32px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  min-height: 300px;
  overflow: hidden;
}

.buffered-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
}

.buffered-title {
  font-size: 20px;
  font-weight: 900;
  color: #0f172a;
}

.clear-button {
  padding: 6px 12px;
  border: none;
  background-color: transparent;
  color: #ef4444;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.clear-button:hover {
  background-color: #fef2f2;
}

.buffered-list {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
}

.empty-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0.2;
}

.empty-text {
  font-size: 18px;
  font-weight: 700;
  font-style: italic;
  color: #64748b;
  margin-top: 16px;
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.file-item {
  padding: 16px;
  background-color: rgba(248, 250, 252, 0.5);
  border: 1px solid #f1f5f9;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.2s;
}

.file-item:hover {
  border-color: rgba(0, 164, 255, 0.2);
}

.file-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.file-icon {
  color: #00a4ff;
  flex-shrink: 0;
}

.file-name {
  font-size: 18px;
  font-weight: 700;
  color: #475569;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 280px;
}

.remove-button {
  background-color: transparent;
  border: none;
  color: #cbd5e1;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;
}

.remove-button:hover {
  color: #ef4444;
}
</style>
