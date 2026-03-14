<template>
  <div class="predict-page">
    <NavBar />
    <div class="container">
      <router-link to="/user/file-upload-2" class="back-to-home">Go Back</router-link>
      <h1>Click the button below to make a prediction</h1>

      <div class="network">
        <img src="@/assets/images/simple-network.gif" alt="Network Animation" width="250">
      </div>

      <div class="buttons">
        <input value="Predict" type="submit" class="predict-submit-btn" @click="handlePredict">
      </div>
      <span class="predict-span1"></span>
      <div class="spinner predict-spinner" v-show="loading"></div>
      <span class="predict-span2"></span>

      <div class="extra-content">
        <h2>Additional Information</h2>
        <p>This is some extra content to make the page more informative and engaging.</p>
        <p class="p2">Please confirm that you have submitted the dataset to be predicted on the previous page. If you haven't submitted it yet, please go back and submit the dataset. If you have confirmed the submission, please click the button to proceed with event prediction.</p>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import NavBar from '@/components/NavBar.vue';
import Footer from '@/components/Footer.vue';
import { predict } from '@/api/predict';

const router = useRouter();
const loading = ref(false);
const span1 = ref('');
const span2 = ref('');

const handlePredict = async () => {
  loading.value = true;
  span1.value = 'In progress, please wait...';
  
  try {
    const res = await predict();
    if (res === 1) {
      span1.value = '';
      router.push('/user/file-list');
    } else {
      span1.value = '';
      span2.value = 'You did not input any dataset';
    }
  } catch (error) {
    span1.value = '';
    span2.value = 'Prediction failed';
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.predict-page {
  width: 100%;
  background-color: white;
}

.container {
  position: relative;
  text-align: center;
  width: 90%;
  max-width: 600px;
  margin: 70px auto;
  padding: 30px;
  background-color: #ffffff;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

h1 {
  margin-top: 40px;
  font-size: 24px;
  color: #333333;
  position: relative;
  z-index: 1;
}

.back-to-home {
  position: absolute;
  left: 10px;
  margin-top: 0px;
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

.buttons input {
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

.buttons input:hover {
  background-color: orangered;
}

.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-left-color: #fff;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 20px auto;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

#span1, #span2 {
  display: block;
  margin-top: 10px;
  font-size: 16px;
  color: red;
  z-index: 1;
}

.network img {
  max-width: 100%;
  height: auto;
  margin-top: 30px;
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
</style>

