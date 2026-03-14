<template>
  <div class="home-page">
    <NavBar />
    <!--轮播图-->
    <div class="home-container">
      <div class="home-slide">
        <div 
          v-for="(item, index) in carouselItems" 
          :key="index"
          class="item" 
          :style="{ backgroundImage: `url(${item.image})` }"
        >
          <div class="content">
            <div class="name">
              Dynamic Interaction Network
            </div>
            <div class="des">
              Modeling the Evolution of Dynamic Interaction
              Networks for Public Event Prediction
            </div>
            <button @click="goToIntroduce">See more</button>
          </div>
        </div>
      </div>
      <div class="buttons">
        <div class="s_button" @click="prevSlide">&lt;</div>
        <div class="s_button" @click="nextSlide">&gt;</div>
      </div>
    </div>
    
    <!--简介-->
    <div class="introduce">
      <div class="introduction">        
        <h1>
            Modeling the Evolution of
            Dynamic Interaction
            Networks
            for Public Event Prediction 
        </h1>
        <p>
          Effective forecasting of public events is a critical
          capability for intelligent service systems, enabling
          proactive risk mitigation, adaptive resource allocation,
          and informed decision-making in urban and social 
          service platforms. In many real-world services,
          public events emerge from complex and evolving
          interactions among participants, making accurate and
          explainable prediction particularly challenging. We
          proposes <strong>auto-ibDLM</strong>, a novel deep learning framework for
          public event forecasting services that predicts event
          occurrence by modeling the evolution of dynamic participant
          interaction networks
        </p>
      </div>
      <!--网络动态图-->
      <div class="network">
        <img src="@/assets/images/simple-network.gif" alt="" >
      </div>
    </div>
    
    <!--搜索-->
    <div class="search-box">
      <input 
        class="home-search-txt search-txt" 
        type="text" 
        placeholder="Type to search" 
        v-model="searchText"
        @keyup.enter="handleSearch"
      >
      <a class="search-btn" @click="handleSearch">
        <i class="fas fa-search home-search-btn"></i>
      </a>
    </div>
    <div>
      <el-dialog v-model="dialogVisible" title="Search Result" width="500px" @close="dialogVisible = false">
        <!-- <img src="@/assets/images/logo1.png" alt="" width="50px" height="50px" class="dialog-logo"> -->
        <ul class="home-search-results-list search-result">
          <li v-for="(result, index) in searchResults" :key="index" class="result-item">
            <img :src="result.imageUrl" alt="">
            <div class="text-content">
              <h3>{{ result.title }}</h3>
              <p>{{ result.comdescription }}</p>
            </div>
          </li>
        </ul>
      </el-dialog>
    </div>
    
    <!--相关信息展示部分 -->
    <div class="displayout">
      <p>Why Event Characterization?</p>
      <hr>
      <div v-for="box in boxes" :key="box.id" class="box">
        <div class="box-content">
          <img :src="box.imageUrl" alt="box.image" class="box-image">
          <p>{{ box.title }}</p>
        </div>
        <div class="box-description">{{ box.description }}</div>
      </div>
    </div>
    
    <!--底部-->
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { ElMessageBox, ElMessage } from 'element-plus';
import NavBar from '@/components/NavBar.vue';
import Footer from '@/components/Footer.vue';

const router = useRouter();
const searchText = ref('');
const dialogVisible = ref(false);
const searchResults = ref([]);
const boxes = ref([]);
import lun1 from '@/assets/images/lun1.jpg';
import lun2 from '@/assets/images/lun2.jpg';
import lun3 from '@/assets/images/lun3.jpg';
import lun4 from '@/assets/images/lun4.jpg';
import lun5 from '@/assets/images/lun5.jpg';
import lun6 from '@/assets/images/lun6.jpg';

const carouselItems = ref([
  { image: lun1 },
  { image: lun2 },
  { image: lun3 },
  { image: lun4 },
  { image: lun5 },
  { image: lun6 }
]);

let timer = null;
let direction = true;

onMounted(() => {
  fetchData();
  startCarousel();
});

onUnmounted(() => {
  if (timer) {
    clearInterval(timer);
  }
});

const fetchData = async () => {
  try {
    const response = await axios.get('/api/displayout');
    boxes.value = response.data;
  } catch (error) {
    console.error('Error fetching data:', error);
  }
};

const handleSearch = async () => {
  if (!searchText.value.trim()) return;
  
  try {
    const response = await fetch('api/searchByTitle', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(searchText.value)
    });
    const data = await response.json();
    if (data!=null && data.length > 0) {
      searchResults.value = data;
      dialogVisible.value = true;
    }
    else{
      ElMessage({
        message: 'No results found.',
        type: 'Warning',
      })
    }
  } catch (error) {
    console.error('Error:', error);
  }
};

const goToIntroduce = () => {
  router.push('/introduce');
};

const moveSlide = () => {
  const slide = document.querySelector('.home-slide');
  const items = document.querySelectorAll('.item');
  if (direction) {
    slide.appendChild(items[0]);
  } else {
    slide.insertBefore(items[items.length - 1], items[0]);
  }
};

const startCarousel = () => {
  timer = setInterval(moveSlide, 2000);
};

const prevSlide = () => {
  direction = false;
  moveSlide();
};

const nextSlide = () => {
  direction = true;
  moveSlide();
};

const logout = async () => {
  try {
    const { logoutApi } = await import('@/api/login');
    const res = await logoutApi();
    if (res.code === 1) {
      localStorage.removeItem('loginUser');
      router.push('/');
    }
  } catch (error) {
    console.error(error);
  }
};
</script>

<style scoped>
.home-page {
  width: 100%;
  background-color: white;
}

.home-container {
  margin-top: 400px;
  margin-left: 600px;
  width: 1000px;
  height: 600px;
  /* width: 1000px;
  height: 600px; */
  /* position: absolute; */
  /* top: 55%;
  left: 50%; */
  transform: translate(-50%, -50%);
  background-color: #f5f5f5;
  padding: 50px;
  box-shadow: 0 30px 50px;
}

.home-slide {
  width: max-content;
  margin-top: 50px;
}

.item {
  width: 200px;
  height: 300px;
  background-position: 50%, 50%;
  display: inline-block;
  background-size: cover;
  position: absolute;
  top: 50%;
  margin-top: -150px;
  border-radius: 20px;
  box-shadow: 0 30px 50px #505050;
  transition: 0.5s ease-in-out;
}

.item:nth-child(1),
.item:nth-child(2) {
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  margin-top: 0;
  border-radius: 0;
  box-shadow: none;
}

.item:nth-child(3) {
  left: 50%;
}

.item:nth-child(4) {
  left: calc(50% + 220px);
}

.item:nth-child(5) {
  left: calc(50% + 440px);
}

.item:nth-child(n+6) {
  left: calc(50% + 660px);
  opacity: 0;
}

.item .content {
  width: 300px;
  position: absolute;
  left: 100px;
  top: 50%;
  transform: translateY(-50%);
  font-family: system-ui;
  color: #eee;
  display: none;
}

.item:nth-child(2) .content {
  display: block;
}

.item .name {
  font-size: 40px;
  font-weight: bold;
  opacity: 0;
  animation: showcontent 1s ease-in-out 1 forwards;
}

.item .des {
  margin: 20px 0;
  opacity: 0;
  animation: showcontent 1s ease-in-out 0.3s 1 forwards;
}

.item button {
  padding: 10px 20px;
  border: none;
  opacity: 0;
  animation: showcontent 1s ease-in-out 0.6s 1 forwards;
}

@keyframes showcontent {
  from {
    opacity: 0;
    transform: translateY(100px);
    filter: blur(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
    filter: blur(0);
  }
}

.buttons {
  width: 100%;
  position: absolute;
  bottom: 50px;
  margin-left: -50px;
  text-align: center;
  z-index: 100;
}

.s_button {
  display: inline-block;
  width: 50px;
  height: 50px;
  line-height: 50px;
  text-align: center;
  color: gray;
  background-color: #fff;
  font-size: 25px;
  border-radius: 50%;
  font-weight: bold;
  border: 1px solid #555;
  margin: 0 25px;
  transition: 0.5s;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.s_button:hover {
  background-color: #ccc;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.introduce {
  margin-top: -200px;
  margin-left: 100px;
  display: flex;          /* 关键：启用弹性布局 */
  flex-direction: row;    /* 横向排列（默认，可省略） */
  width: 100%;
}
.introduction{
  width: 50%;
  text-align: justify;
}
.introduce .introduction h1 {
  font-size: 40px;
  line-height: 50px;
  letter-spacing: 0.01em;
  color: #14235A;
}

.introduce .introduction p {
  margin-top: 30px;
  font-size: 25px;
  color: #868e96;
}

.network {
  margin-left: 20px;
  width: 50%;
}

.search-box {
  position: absolute; 
  /* top: 1530px; 
  left: 150px; */
  margin-top: 10px;
  margin-left: 100px;
  /* transform: translate(-50%, -50%); */
  background: #2f3640;
  height: 60px;
  border-radius: 40px;
  padding: 10px;
}

.search-box:hover > .search-txt {
  width: 240px;
  padding: 0 6px;
}

.search-box:hover > .search-btn {
  background: white;
}

.search-btn {
  color: #e84118;
  float: right;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #2f3640;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

.search-txt {
  border: none;
  background: none;
  outline: none;
  float: left;
  padding: 0;
  color: white;
  font-size: 16px;
  transition: 0.4s;
  line-height: 40px;
  width: 0;
}

.displayout {
  margin-top: 70px;
  height: 700px;
  width: 100%;
}

.displayout p {
  margin-left: 100px;
  font-size: 35px;
  font-weight: bold;
}

.displayout hr {
  margin-top: 20px;
}

.displayout .box {
  padding-left: 100px;
  padding-top: 30px;
  height: 300px;
  /* width: 400px; */
  width: 30%;
  float: left;
  display: flex;
  flex-direction: column;
}

.box-content {
  display: flex;
  align-items: center;
}

.box-content p {
  font-size: 30px;
  font-weight: bolder;
  margin-left: 10px;
}

.box-image {
  border-radius: 30px;
  width: 50px;
  height: 50px;
}

.box-description {
  margin-top: 10px;
  margin-left: 20px;
  font-size: 20px;
  line-height: 25px;
  color: darkgray;
}

/* .dialog-logo {
  margin-left: 200px;
  margin-top: -100px;
} */

.search-result {
  list-style-type: none;
  padding: 0;
}

.result-item {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: flex-start;
}

.result-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.search-result img {
  max-width: 100px;
  max-height: 100px;
  margin-left: 10px;
  border-radius: 5px;
}

.search-result h3 {
  margin-top: 20px;
  margin-left: 10px;
  font-size: 18px;
  color: #333;
}

.search-result p {
  margin-top: 40px;
  margin-left: -60px;
  font-size: 14px;
  color: darkgray;
  text-align: justify;
}
</style>

