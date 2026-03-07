<template>
  <div class="content">
    <div class="login-wrapper">
      <div class="left-img">
        <div class="glass">
          <div class="tips">
            <div class="title">
              <img src="@/assets/images/logo1.png" alt="">
            </div>
            <h1>Explore The Platform</h1>
            <span>5 Million+ people have joined our platform.</span>
            <span>we invite you to join the tribe</span>
          </div>
        </div>
      </div>
      <div class="right-login-form">
        <div class="form-wrapper">
          <div class="form-div">
            <div class="reg-content">
              <h1 align="center">Welcome Back</h1>
            </div>

            <table class="right-table">
              <tr>
                <td>Username</td>
                <td class="inputs">
                  <input name="username" type="text" class="login-username-input" v-model="loginForm.username">
                  <br>
                </td>
              </tr>

              <tr>
                <td>Password</td>
                <td class="inputs">
                  <input name="password" type="password" class="login-password-input" v-model="loginForm.password">
                </td>
              </tr>

              <tr class="verification-tr">
                <td>Verification</td>
                <td class="inputs">
                  <input name="checkCode" type="text" class="login-check-code-input" v-model="loginForm.checkCode">
                  <img 
                    :src="codeImageUrl" 
                    alt="点击加载" 
                    @click="refreshCode" 
                    id="checkCodeImg" 
                    title="点击刷新"
                    style="cursor: pointer;"
                  >
                </td>
              </tr>
            </table>
            <div class="buttons">
              <input value="Sign In" type="submit" class="login-submit-btn" @click="handleLogin">
              <input type="button" value="Cancel" class="login-cancel-btn" @click="goHome">
            </div>
            <br class="clear">
            <div class="login-footer">
              <span>Don't have an account?&nbsp;</span>
              <router-link to="/register">Register</router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { loginApi } from '@/api/login';

const router = useRouter();
const route = useRoute();
const loginForm = ref({
  username: '',
  password: '',
  checkCode: ''
});

const codeImageUrl = ref('api/front/codeImage/get');

const refreshCode = () => {
  codeImageUrl.value = `api/front/codeImage/get?_=${Date.now()}`;
};

const handleLogin = async () => {
  try {
    const res = await loginApi(loginForm.value);
    if (String(res.code) === '1') {
      localStorage.setItem('loginUser', JSON.stringify(res.data));
      const redirect = route.query.redirect;
      // 处理登录过期跳转
      if(redirect){
        router.push(redirect);
      }else{
        router.push('/');
      }
    } else {
      ElMessage.error(res.msg);
      refreshCode();
    }
  } catch (error) {
    ElMessage.error('Login failed');
    refreshCode();
  }
};
onMounted(() => {
  refreshCode
});
const goHome = () => {
  loginForm.value = {
    username: '',
    password: '',
    checkCode: ''
  }
  router.push('/');
};
</script>

<style scoped>
* {
  padding: 0;
  margin: 0;
  box-sizing: content-box;
}

.content {
  width: 100vw;
  height: 100vh;
  position: relative;
}

.content .login-wrapper {
  width: 70vw;
  height: 80vh;
  background-color: whitesmoke;
  border-radius: 40px;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  display: flex;
}

.content .login-wrapper .left-img {
  border-radius: 40px;
  flex: 1;
  background: url('@/assets/images/lun2.jpg') no-repeat;
  background-size: cover;
  position: relative;
}

.content .login-wrapper .left-img .glass {
  width: 60%;
  padding: 20px;
  color: #fff;
  position: absolute;
  top: 20%;
  left: 50%;
  transform: translate(-50%, -20%);
  background: rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 15px;
  border: 1px solid rgba(255, 255, 255, 0.48);
}

.right-table {
  display: flex;
  align-items: center;
  flex-direction: column;
  justify-content: center;
}

.right-table tr{
  margin-bottom: 20px;
}

.verification-tr {
  display: flex;
  align-items: center;
  flex-direction: row;
}

.content .login-wrapper .left-img .glass .tips .title img {
  width: 25%;
  font-weight: 600;
  font-size: 15px;
  text-align: center;
  padding: 10px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 15px;
  border: 1px solid rgba(255, 255, 255, 0.48);
}

.content .login-wrapper .left-img .glass .tips h1 {
  margin: 15px 0;
}

.content .login-wrapper .left-img .glass .tips span {
  margin: 5px 0;
  display: block;
  font-weight: 100;
}

.content .login-wrapper .right-login-form {
  flex: 1;
  position: relative;
}

.content .login-wrapper .right-login-form .form-wrapper {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}

.form-div {
  width: 424px;
  height: 500px;
  padding: 30px 0 20px 0px;
  font-size: 16px;
  text-align: left;
}

.reg-content {
  padding: 30px;
  margin: 3px;
}

table {
  border-collapse: collapse;
  border-spacing: 0;
}

td, th {
  padding: 0;
}

.inputs {
  vertical-align: top;
}

.clear {
  clear: both;
}

.clear:before, .clear:after {
  content: "";
  display: table;
}

.clear:after {
  clear: both;
}

.form-div .buttons {
    margin-top: 20px;
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
}

.err_msg {
  color: red;
  padding-right: 170px;
}

.form-div input[type="text"], 
.form-div input[type="password"], 
.form-div input[type="email"] {
  width: 268px;
  margin: 10px;
  line-height: 20px;
  font-size: 16px;
}

.form-div .login-check-code-input {
  width: 180px !important;
}

.form-div input[type="button"], 
.form-div input[type="submit"] {
  margin: 10px 20px 0 0;
}

.form-div table {
  margin: 0 auto;
  text-align: right;
  color: rgba(64, 64, 64, 1.00);
}

.form-div table img {
  vertical-align: middle;
  margin: 0 0 5px 0;
}

.form-div .login-footer {
  padding-left: 100px;
  padding-top: 20px;
}

.form-div .login-footer a {
  text-decoration: none;
  color: #00a4ff;
}

input[type="text"], 
input[type="password"], 
input[type="email"] {
  border-radius: 8px;
  box-shadow: inset 0 2px 5px #eee;
  padding: 10px;
  border: 1px solid #D4D4D4;
  color: #333333;
  margin-top: 5px;
}

input[type="text"]:focus, 
input[type="password"]:focus, 
input[type="email"]:focus {
  border: 1px solid #50afeb;
  outline: none;
}

input[type="button"], 
input[type="submit"] {
  padding: 7px 15px;
  background-color: #00a4ff;
  text-align: center;
  border-radius: 5px;
  overflow: hidden;
  min-width: 20px;
  border: none;
  color: #FFF;
  box-shadow: 1px 1px 1px rgba(75, 75, 75, 0.3);
}

input[type="button"]:hover, 
input[type="submit"]:hover {
  background-color: #bd2c00;
}

input[type="button"]:active, 
input[type="submit"]:active {
  background-color: #5a88c8;
}

.login-submit-btn {
  margin-left: 120px;
width: 80px;
    height: 25px;
  margin-top: 10px;
}

.login-cancel-btn {
width: 80px;
    height: 25px;
  margin-top: 10px;
}

</style>

