<template>
  <div class="historical-record-page">
    <NavBar />
    <div class="container">
      <div class="head">
        <h1>Your historical forecast records are as follows</h1>
        <el-button type="success" @click="exportAllData" class="exportAll">ExportAllResults</el-button>
        <div class="extra-content">
          <h2>Additional Information</h2>
          <p class="p2">
            The table below presents the prediction results 
            under the specified event triggering threshold, 
            where eventNum denotes the actual number of 
            public events and predictNum denotes the predicted 
            number of public events. To view a clearer 
            comparison chart of the prediction results, 
            please click the download button within the row; 
            to access more detailed prediction result data, 
            please click the export button within the row; 
            to view all prediction results, please click 
            the button at the top of the page.
          </p>
        </div>
      </div>
      <div>
        <el-table :data="tableData" border style="width: 95%; margin: 0 auto;">
          <el-table-column label="username" align="center" width="100px">{{ username }}</el-table-column>
          <el-table-column prop="threshold" label="threshold" align="center" width="100px">
            <template #default="scope">
              {{ scope.row.threshold  }}
            </template>
          </el-table-column>
          <el-table-column label="visualization results" align="center">
            <template #default="scope">
              <img :src="scope.row.imageUrl" style="max-width:100px; max-height: 100px;"/>
            </template>
          </el-table-column>
          <el-table-column prop="eventNum" label="eventNum" align="center" width="100px">
            <template #default="scope">
              {{ scope.row.eventNum  }}
            </template>
          </el-table-column>
          <el-table-column prop="eventPredictNum" label="PredictNum" align="center" width="120px">
            <template #default="scope">
              {{ scope.row.eventPredictNum  }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="predictTime" align="center" width="140px">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="download" align="center" width="100px">
            <template #default="scope">
              <a :href="scope.row.imageUrl" download>
                <el-icon style="font-size: 20px;"><Download /></el-icon>
              </a>
            </template>
          </el-table-column>

          <el-table-column label="exportData" align="center" width="120px">
            <template #default="scope">
              <el-button type="success" @click="exportRowData(scope.row)">export</el-button>
            </template>
          </el-table-column>
          <el-table-column label="delete" align="center" width="100px">
            <template #default="scope">
              <el-button type="danger" @click="showDeleteConfirm(scope.row)">delete</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 30, 40]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          style="margin-left: 50%; margin-top: 2%">
        </el-pagination>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage,ElMessageBox } from 'element-plus';
import { Download } from '@element-plus/icons-vue';
import axios from 'axios';
import NavBar from '@/components/NavBar.vue';
import request from '@/utils/request';
const tableData = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const username = ref('');
onMounted(() => {
  loadData();
});

const formatDate = (dateTimeStr) => {
  const dateTime = new Date(dateTimeStr);
  if (isNaN(dateTime.getTime())) {
    return '';
  } else {
    return dateTime.toLocaleString();
  }
};

const loadData = () => {
  const loginUser = JSON.parse(localStorage.getItem('loginUser'))
  username.value = loginUser.username;
  console.log(loginUser.username)
  request.get('/result', {
    params: {
      page: currentPage.value,
      size: pageSize.value
    }
  })
    .then(response => {
      tableData.value = response.records;
      total.value = response.total;
    })
    .catch(error => {
      console.error('获取数据时出错:', error);
    });
};

const handleDelete = (row) => {
  const imagePath = row.imageUrl;
  request.delete('/result', {
    params: {
      imagePath: imagePath,
      page: currentPage.value,
      size: pageSize.value
    }
  })
    .then(response => {
      loadData();
      ElMessage.success('Delete Success');
    })
    .catch(error => {
      ElMessage.error('Delete Failed')
      console.error('删除数据时出错:', error);
    });
};

const handleSizeChange = (val) => {
  pageSize.value = val;
  loadData();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  loadData();
};

const showDeleteConfirm = (row) => {
  ElMessageBox.confirm('Are you sure to delete this record?', 'Warning', {
    confirmButtonText: 'Delete',
    cancelButtonText: 'Cancel',
    type: 'warning'
  }).then(() => {
    handleDelete(row);
  }).catch(() => {
    // 用户点击取消时不执行任何操作
  });
};
const exportData = ref(
  {
    'username' : '',
    'createTime' : ''
  }
)
const exportRowData = async (row) => { 
  try {
    exportData.value.username = JSON.parse(localStorage.getItem('loginUser')).username;
    exportData.value.createTime = row.createTime;
    
    // 注意：现在返回的是完整 response，不是直接的 blob
    const response = await request.post('/export', exportData.value, { 
      responseType: 'blob' 
    });
    
    // 从 response 中提取 blob 数据
    const blob = response.data;
    
    // 现在 blob 就是后端生成的原始 Excel 二进制数据，直接使用！
    // 不需要再 new Blob([blob]) 包装，直接创建下载链接
    const url = window.URL.createObjectURL(blob);
    
    const link = document.createElement('a');
    link.href = url;
    
    // 从后端设置的 header 中获取文件名（如果后端设置了的话）
    const contentDisposition = response.headers['content-disposition'];
    let filename = '预测数据报表.xlsx';
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename\*?=(?:UTF-8'')?(.+)/);
      if (filenameMatch) {
        filename = decodeURIComponent(filenameMatch[1].replace(/['"]/g, ''));
      }
    }
    link.download = filename;
    
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    ElMessage.success('Export Success');
  } catch (error) {
    // 错误处理：如果导出失败，后端返回的可能是 JSON 错误信息
    if (error.response?.data instanceof Blob) {
      const text = await error.response.data.text();
      try {
        const errData = JSON.parse(text);
        ElMessage.error(errData.message || 'Export Failed');
      } catch {
        ElMessage.error('Export Failed');
      }
    } else {
      ElMessage.error('Export Failed');
      console.error('Export Data Failed:', error);
    }
  }
};

const exportAllData = async () => { 
  try {
    const username = JSON.parse(localStorage.getItem('loginUser')).username;
    
    // 注意：现在返回的是完整 response，不是直接的 blob
    const response = await request.get('/exportAll', { 
      params: {
        username: username
      },
      responseType: 'blob' 
    });
    
    // 从 response 中提取 blob 数据
    const blob = response.data;
    
    // 现在 blob 就是后端生成的原始 Excel 二进制数据，直接使用！
    // 不需要再 new Blob([blob]) 包装，直接创建下载链接
    const url = window.URL.createObjectURL(blob);
    
    const link = document.createElement('a');
    link.href = url;
    
    // 从后端设置的 header 中获取文件名（如果后端设置了的话）
    const contentDisposition = response.headers['content-disposition'];
    let filename = '预测数据报表.xlsx';
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename\*?=(?:UTF-8'')?(.+)/);
      if (filenameMatch) {
        filename = decodeURIComponent(filenameMatch[1].replace(/['"]/g, ''));
      }
    }
    link.download = filename;
    
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    ElMessage.success('Export Success');
  } catch (error) {
    // 错误处理：如果导出失败，后端返回的可能是 JSON 错误信息
    if (error.response?.data instanceof Blob) {
      const text = await error.response.data.text();
      try {
        const errData = JSON.parse(text);
        ElMessage.error(errData.message || 'Export Failed');
      } catch {
        ElMessage.error('Export Failed');
      }
    } else {
      ElMessage.error('Export Failed');
      console.error('Export Data Failed:', error);
    }
  }
};
</script>

<style scoped>
.historical-record-page {
  width: 100%;
  background-color: white;
}

.container {
  text-align: center;
  width: 90%;
  margin: 70px auto;
  padding: 30px;
  background-color: #ffffff;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.head h1 {
  margin-bottom: 20px;
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

.back-to-home {
  position: absolute;
  left: 10px;
  margin-top: -20px;
  text-decoration: none;
  color: #007bff;
  background-color: transparent;
  padding: 10px 20px;
  border-radius: 5px;
  transition: color 0.3s;
  z-index: 1;
}

.back-to-home:hover {
  color: orangered;
}

.exportAll {
  position: absolute;
  right: 10%;
  margin-top: -20px;
  border-radius: 10px;
  height: 50px;
}
</style>

