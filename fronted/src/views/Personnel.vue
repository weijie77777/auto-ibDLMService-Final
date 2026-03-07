<template>
  <div class="personnel-page">
    <NavBar />
    <div class="personnel-container">
      <div class="personnel-user-info">
        <img src="@/assets/images/logo1.png" alt="">
        <h2 class="personnel-login"></h2>
        <p class="personnel-username-display"></p>
        <p class="personnel-phone-display"></p>
        <p class="personnel-gender-display">Sex： </p>
        <router-link to="/historical-record" class="personnel-historical" v-if="isLoggedIn">historical prediction record</router-link>
        <img 
          src="../assets/images/btn_close@2x.png" 
          class="personnel-logout" 
          alt="退出" 
          @click="logout" 
          title="log out"
          v-if="isLoggedIn"
        />
        <div class="personnel-not-logged-in" v-if="!isLoggedIn">
          <p>Not logged in,Please <router-link to="/login">Log In</router-link> </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import NavBar from '@/components/NavBar.vue';
import { logoutApi } from '@/api/login';

const router = useRouter();
const isLoggedIn = ref(false);
const userInfo = ref(null);

onMounted(() => {
  const userInfoStr = localStorage.getItem('loginUser');
  console.log(userInfoStr)
  if (userInfoStr) {
    isLoggedIn.value = true;
    userInfo.value = JSON.parse(userInfoStr);
    document.querySelector('.personnel-login').innerText = 'Personal Information';
    document.querySelector('.personnel-username-display').innerText = 'Username：' + userInfo.value.username;
    document.querySelector('.personnel-phone-display').innerText = 'Phone：' + userInfo.value.phone;
    const genderDisplay = document.querySelector('.personnel-gender-display');
    if (userInfo.value.sex === '1') {
      genderDisplay.innerHTML += 'Male';
    } else if (userInfo.value.sex === '0') {
      genderDisplay.innerHTML += 'Female';
    }
  } else {
    document.querySelector('.personnel-login').innerText = 'Not Logged In';
    document.querySelector('.personnel-username-display').innerText = 'UserName：';
    document.querySelector('.personnel-phone-display').innerText = 'Phone：';
    document.querySelector('.personnel-gender-display').innerText = 'Sex：';
  }
});

const logout = async () => {
  try {
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
.personnel-page {
  width: 100%;
  background-color: white;
}

.personnel-container {
  max-width: 600px;
  height: 600px;
  margin: 50px auto;
  padding: 30px;
  background-color: #fff;
  text-align: center;
}

.personnel-container h2 {
  color: #333;
  text-align: center;
}

.personnel-container p {
  margin: 50px 0;
}

.personnel-user-info {
  display: block;
  max-width: 400px;
  height: 400px;
  margin: 50px auto;
  padding: 20px;
  background-color: #fff;
  border-radius: 5px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.personnel-user-info img:first-child {
  margin-top: -70px;
  width: 70px;
  height: 70px;
  display: block;
  margin-left: auto;
  margin-right: auto;
}

.personnel-not-logged-in p {
  color: #FF5733;
}

.personnel-not-logged-in a {
  text-decoration: none;
  color: #007bff;
  border-radius: 5px;
  transition: background-color 0.3s;
}

.personnel-container a:hover {
  color: orangered;
}

.personnel-historical {
  display: block;
  text-align: center;
  text-decoration: none;
  color: #007bff;
  border-radius: 5px;
  transition: background-color 0.3s;
  margin-top: 20px;
  margin-bottom: 0;
}

.personnel-historical:hover {
  color: orangered;
}

.personnel-logout {
  width: 50px;
  height: 50px;
  /* padding-top: 50px;
  margin-left: 20px; */
  cursor: pointer;
}

.personnel-logout:hover {
  cursor: pointer;
  filter: brightness(150%);
}
</style>

