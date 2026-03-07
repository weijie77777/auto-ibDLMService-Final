<template>
  <div class="train-result-page">
    <NavBar />
    <div class="container">
      <router-link to="/train" class="back-to-home">Go Back</router-link>
      <h1>The following is the predicted result</h1>
      <div class="extra-content">
        <h2>Additional Information</h2>
        <p>This is some extra content to make the page more informative and engaging.</p>
        <p class="p2">
          The y-axis yt in the graph represents the number of new nodes (i.e., people)
          added to the interaction network over a period of time. The ori curve represents the
          actual number of new additions, while the pre curve represents the predictions made
          by the model being utilized. The accuracy of the predictions can be determined by the
          overlap between the two curves.
          If you want to download the visualization result, please click download.
        </p>
        <span class="train-result-classification">If you are satisfied with the training results, you can click the button to save the model:</span>
        <input value="SaveModel" type="submit" class="train-result-save-btn button" @click="saveModel">
      </div>
      <div class="table">
        <el-table :data="fileList" stripe style="width: 100%">
          <el-table-column prop="fileName" label="fileName" width="180" align="center"></el-table-column>
          <el-table-column prop="type" label="Type" width="180" align="center"></el-table-column>
          <el-table-column prop="data" label="Visualization Results" width="300" align="center">
            <template #default>
              <img src="@/assets/images/loss.png" alt="" width="300px" height="300px">
            </template>
          </el-table-column>
          <el-table-column prop="modifyDate" label="Date" align="center"></el-table-column>
          <el-table-column label="Operation" align="center">
            <template #default="scope">
              <el-button size="small" type="text" @click="handleDownload(scope.row)">download</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="page-container">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[10, 50, 100]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total">
          </el-pagination>
        </div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import NavBar from '@/components/NavBar.vue';
import Footer from '@/components/Footer.vue';
import { save } from '@/api/train';

const fileList = ref([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

onMounted(() => {
  getFileList();
});

const getFileList = () => {
  const currentDate = new Date().toLocaleString();
  axios.get(`api/fileList/${pageNum.value}/${pageSize.value}?parentPath=D:/SSM资料库/train`)
    .then(response => {
      response.data.data.forEach(file => {
        file.modifyDate = currentDate;
      });
      fileList.value = response.data.data;
      total.value = response.data.size;
    })
    .catch(error => {
      console.error(error);
    });
};

const handleDownload = (row) => {
  window.location = `api/download/${row.fileName}`;
};

const handleSizeChange = () => {};

const handleCurrentChange = (e) => {
  pageNum.value = e;
  getFileList();
};

const saveModel = async () => {
  try {
    const res = await save();
    if (res === 1) {
      ElMessage.success('保存成功');
    } else {
      ElMessage.error('保存失败');
    }
  } catch (error) {
    ElMessage.error('保存失败');
  }
};
</script>

<style scoped>
.train-result-page {
  width: 100%;
  background-color: white;
}

.train-result-page .container {
  max-width: 1000px;
  margin: 50px auto;
  padding: 30px;
  background-color: #ffffff;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.table {
  margin-top: 50px;
  width: 900px;
}

.page-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.back-to-home {
  margin-bottom: 20px;
  margin-left: -700px;
  text-align: left;
  z-index: 1;
  position: relative;
  text-decoration: none;
  color: #007bff;
  padding: 10px 20px;
  border-radius: 5px;
  transition: background-color 0.3s;
}

.back-to-home:hover {
  color: orangered;
}

h1 {
  margin-top: 10px;
  font-size: 24px;
  color: #333333;
  position: relative;
  z-index: 1;
}

.extra-content {
  margin-top: 40px;
  text-align: left;
  z-index: 1;
}

.extra-content p {
  margin: 10px 0;
}

.p2 {
  color: #00a4ff;
}

#reg_btn1 {
  padding: 12px 24px;
  margin-top: 20px;
  font-size: 18px;
  background-color: #007bff;
  color: #ffffff;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  z-index: 1;
}

#reg_btn1:hover {
  background-color: orangered;
}

#classfication {
  color: orangered;
}
</style>

