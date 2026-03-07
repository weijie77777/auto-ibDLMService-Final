<template>
  <div class="file-upload-page">
    <NavBar />
    <div class="container">
      <div class="title">
        <img src="@/assets/images/step_by_step/steps.png" width="60px" height="60px" class="logo-icon" alt="logo">
        <h1>Please upload your Dataset</h1>
      </div>
      <div class="extra-content">
        <h2>Additional Information</h2>
        <p class="p2">
          To invoke the event prediction model provided by the website, 
          please complete the following steps sequentially: first, 
          upload the file containing <span>node-level features</span>; second, 
          upload the file related to <span>community evolution behavior features</span>; 
          and finally, upload the file encompassing <span>network-level features</span>.
          Subsequently, set the <span>threshold</span> for the number of participants 
          triggering the event according to your reasonable judgment, 
          and click the "Predict" button to initiate the prediction.
        </p>
      </div>

       <!-- Step Content -->
      <div class="workflow-content">
          <FeaturesUpload
          :files="files"
          @file-upload="handleFileUpload"
          @file-remove="handleFileRemove"
          @clear-all="handleClearAll"
          />
          <span>Please enter the threshold for the number of participants 
          triggering the event : </span>
          <input type="text" class="threshold" v-model="threshold">
          <input value="Predict" type="button" class="file-upload-next-btn" @click="handlePredict">
      </div>
      <div class="spinner" id="spinner"></div>
    </div>
    <Footer />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import NavBar from '@/components/NavBar.vue';
import Footer from '@/components/Footer.vue';
import FeaturesUpload from '../../components/StepByStepWorkflow/FeaturesUpload.vue';
import { useRouter } from 'vue-router';
import { ElMessageBox, ElMessage } from 'element-plus';
import request from '@/utils/request.js';
const router = useRouter();
const threshold = ref(1000)
const files = ref([]);
const handleFileUpload = (fileList) => {
  // 拼接文件列表
  files.value = [...files.value, ...fileList];
};

const handleFileRemove = (fileId) => {
  // 从文件列表中删除指定ID的文件
  files.value = files.value.filter(f => f.id !== fileId);
};

const handleClearAll = () => {
  // 清空文件列表
  files.value = [];
};
// 预测按钮事件
const handlePredict = async() => {
  spinner.style.display = "block";
  // 校验
  if (files.value.length === 0) {
    ElMessage.error('Please upload your files first.')
    return;
  }
  if (!threshold.value || isNaN(threshold.value)) {
    ElMessage.error('Please enter a valid threshold.')
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
    formData.append('files', file.raw);
  });
  // 添加阈值
  formData.append('threshold', threshold.value); 
  try {
    const response = await request.post('/predict', formData);
    console.log('Prediction result:', response);
    if(!response.code === '1'){
      ElMessage.error(response.msg);
      return;
    }
    ElMessage.success('Prediction successful!');
    spinner.style.display = "none";
    router.push('/historical-record');
    } catch (error) {
      console.error('Prediction failed:', error);
      ElMessage.error('Prediction failed, please check your dataset and try again');
      spinner.style.display = "none";
    }
}
  
</script>

<style scoped>
.container {
  width: 95%;
  margin-top: 70px;
  margin-left: 2.5%;
  background-color: #ffffff;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.title {
  margin-top: 10px;
  margin-left: 40%;
  text-align: center;
  display: flex;
  flex-direction: row;
}

.title h1 {
  margin-top: 10px;
  font-size: 24px;
  color: #333333;
  z-index: 1;
}

.file-upload-next-btn {
  padding: 12px 24px;
  margin-left: 100px;
  font-size: 18px;
  background-color: #007bff;
  color: #ffffff;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  z-index: 1;
}

.file-upload-next-btn:hover {
  background-color: orangered;
}

.extra-content {
  width: 90%;
  margin-top: 2%;
  margin-left: 5%;
  text-align: justify;
  z-index: 1;
}

.p2 {
  color: #00a4ff;
}

.p2 span {
  color: red;
}
.workflow-content {
  flex: 1;
  overflow-y: auto;
  padding: 32px;
}
.workflow-content span{
   color: #00a4ff;
}
.threshold{
  height: 40px;
}
.spinner {
  border: 4px solid #00a4ff;;
  border-left-color: #fff;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  display: none; /* 默认隐藏 */
  margin: 20px auto;
}
</style>

