<template>
  <div class="acquisition-step">
    <div class="acquisition-content">
      <div class="upload-zone">
        <label class="upload-label">
          <div class="upload-icon">
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          </div>
          <h4 class="upload-title">Click to upload dataset</h4>
          <p class="upload-subtitle">Select data files related to public events</p>
          <input
            type="file"
            multiple
            class="upload-input"
            @change="handleFileChange"
          />
        </label>
      </div>

      <BufferedFiles
        :files="files"
        @remove="handleRemove"
        @clear-all="handleClearAll"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { UploadFilled } from '@element-plus/icons-vue';
import BufferedFiles from './BufferedFiles.vue';

const props = defineProps({
  files: {
    type: Array,
    default: () => []
  },
});

const emit = defineEmits(['file-upload', 'file-remove', 'clear-all', 'threshold-change']);

const handleFileChange = (event) => {
  const fileList = Array.from(event.target.files || []).map((file) => ({
    id: Math.random().toString(36).substr(2, 9),
    name: file.name,
    size: file.size,
    type: file.type,
    lastModified: file.lastModified,
    raw: file // 需要保留真正的file对象 才能将文件正确传递给前端
  }));
  emit('file-upload', fileList);
  event.target.value = '';
};

const handleRemove = (fileId) => {
  emit('file-remove', fileId);
};

const handleClearAll = () => {
  emit('clear-all');
};

</script>

<style scoped>
.acquisition-step {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding-bottom: 24px;
}

.acquisition-content {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  min-height: 0;
}

.upload-zone {
  background-color: #f8fafc;
  border: 2px dashed #cbd5e1;
  border-radius: 32px;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  transition: all 0.3s;
  cursor: pointer;
}

.upload-zone:hover {
  border-color: rgba(0, 164, 255, 0.4);
}

.upload-label {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  text-align: center;
}

.upload-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  transition: transform 0.3s;
}

.el-icon--upload {
  font-size: 67px;
  color: #C0C4CC;
  line-height: 50px;
}

.upload-zone:hover .upload-icon {
  transform: scale(1.05);
}

.upload-title {
  font-size: 24px;
  font-weight: 900;
  color: #0f172a;
  margin: 0 0 4px 0;
}

.upload-subtitle {
  font-size: 18px;
  color: #94a3b8;
  font-weight: 500;
  margin: 0;
}

.upload-input {
  display: none;
}

.threshold-panel {
  margin-top: 32px;
  width: 100%;
}

.threshold-container {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 24px 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.threshold-header {
  margin-bottom: 16px;
}

.threshold-title-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-icon {
  color: #007bff;
  flex-shrink: 0;
}

.threshold-title {
  font-size: 20px;
  font-weight: 900;
  color: #0f172a;
  margin: 0;
  letter-spacing: -0.3px;
}

.threshold-description {
  margin-bottom: 20px;
}

.threshold-description p {
  font-size: 16px;
  color: #475569;
  line-height: 1.6;
  margin: 0;
}

.threshold-input-section {
  width: 100%;
}

.threshold-input-group {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.threshold-input {
  flex: 1;
  min-width: 200px;
  padding: 12px 16px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 16px;
  color: #0f172a;
  background-color: #ffffff;
  transition: border-color 0.2s;
}

.threshold-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.threshold-save-btn {
  padding: 12px 24px;
  background-color: #007bff;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.3s ease;
  white-space: nowrap;
}

.threshold-save-btn:hover {
  background-color: #0056b3;
}

.threshold-save-btn:active {
  transform: scale(0.98);
}

.threshold-default-hint {
  font-size: 14px;
  color: #94a3b8;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .threshold-input-group {
    flex-direction: column;
    align-items: stretch;
  }

  .threshold-input {
    min-width: 100%;
  }

  .threshold-save-btn {
    width: 100%;
  }
}
</style>
