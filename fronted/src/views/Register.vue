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
              <h1 align="center">Create An Account</h1>
            </div>

            <table class="right-table">
              <tr>
                <td>Username</td>
                <td class="inputs">
                  <input name="username" type="text" class="register-username-input" v-model="ruleForm.username">
                </td>
              </tr>

              <tr>
                <td>Password</td>
                <td class="inputs">
                  <input name="password" type="password" class="register-password-input" v-model="ruleForm.password">
                </td>
              </tr>

              <tr>
                <td>Telephone</td>
                <td class="inputs">
                  <input name="phone" type="text" class="register-phone-input" v-model="ruleForm.phone">
                </td>
              </tr>

              <tr class="sex-tr">
                <td style="padding-right: 80px;">Sex</td>
                <td class="sex">
                  <el-radio-group v-model="ruleForm.sex">
                    <el-radio label="男"></el-radio>
                    <span style="width: 60px;"></span>
                    <el-radio label="女"></el-radio>
                    <span style="width: 60px;"></span>
                  </el-radio-group>
                </td>
              </tr>

              <tr>
                <td>Verification</td>
                <td class="inputs">
                  <input name="checkCode" type="text" class="register-check-code-input" v-model="ruleForm.checkCode">
                  <img 
                    :src="codeImageUrl" 
                    alt="点击加载" 
                    @click="refreshCode" 
                    class="register-check-code-img" 
                    title="点击刷新"
                    style="cursor: pointer;"
                  >
                </td>
              </tr>
            </table>
            <div class="buttons">
              <input value="Register" type="submit" class="register-submit-btn" @click="submitForm">
            </div>
            <br class="clear">
            <div class="register-footer">
              <span>Already have an account?&nbsp;</span>
              <router-link to="/login">Sign In</router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { addEmployee } from '@/api/register';

const router = useRouter();

const ruleForm = ref({
  username: '',
  password: '',
  phone: '',
  sex: '男',
  checkCode: ''
});

const codeImageUrl = ref('api/front/codeImage/get');

const refreshCode = () => {
  codeImageUrl.value = `api/front/codeImage/get?_=${Date.now()}`;
};

const submitForm = () => {
  ruleForm.value.sex === '女' ? '0' : '1'
  addEmployee(ruleForm.value).then(res => {
    if (res.code === 1) {
      ElMessage.success('员工添加成功！');
      router.push('/login');
    } else {
      ElMessage.error(res.msg || '操作失败');
      refreshCode();
    }
  }).catch(err => {
    ElMessage.error('请求出错了：' + err);
    refreshCode();
  });
};
onMounted(() => {
  refreshCode();
});
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
  height: 90vh;
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

.reg-content {
  padding: 30px;
  margin: 3px;
}

.right-table {
  display: flex;
  flex-direction: column;
  align-items: center;
}

table {
  border-collapse: collapse;
  border-spacing: 0;
}

td, th {
  padding: 0;
  height: 70px;
}

.inputs {
  vertical-align: top;
}

.form-div {
  width: 424px;
  height: 600px;
  padding: 30px 0 20px 0px;
  font-size: 16px;
  text-align: left;
}

.form-div input[type="text"], 
.form-div input[type="password"] {
  width: 268px;
  margin: 10px;
  line-height: 20px;
  font-size: 16px;
}

.form-div input[type="radio"] {
  width: 70px;
}

.form-div input[type="button"], 
.form-div input[type="submit"] {
  margin: 10px 20px 0 0;
}

.form-div table {
  margin: 0 auto;
  color: rgba(64, 64, 64, 1.00);
}

.form-div table img {
  vertical-align: middle;
  margin: 0 0 5px 0;
}

.form-div .buttons {
  margin-top: 20px;
  margin-left: 50px;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
}

.form-div .register-footer {
  padding-left: 120px;
  /* padding-top: 10px; */
}

.form-div .register-footer a {
  text-decoration: none;
  color: #00a4ff;
}

input[type="text"], 
input[type="password"] {
  border-radius: 8px;
  box-shadow: inset 0 2px 5px #eee;
  padding: 10px;
  border: 1px solid #D4D4D4;
  color: #333333;
  margin-top: 5px;
}

input[type="text"]:focus, 
input[type="password"]:focus {
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
  min-width: 80px;
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

.err_msg {
  color: red;
  font-size: 14px;
  height: 10px;
}

.register-submit-btn {
  margin-right: 50px; 
  width: 240px; 
  height: 40px; 
  margin-top: 10px;
}

.register-check-code-input {
  width: 180px !important;
}
</style>

