<template>
  <div class="navbar-component">
    <ul class="navbar-list">
      <li class="navbar-item">
        <router-link to="/">
          <img src="@/assets/images/logo1.png" width="50px" height="50px" class="navbar-logo" alt="logo">
        </router-link>
      </li>
      <li class="navbar-item">
        <router-link 
          to="/introduce" 
          class="navbar-link"
          :class="{ active: activeNavItem === '/introduce' }"
          @click="setActive('/introduce')"
        >
          How It Works
        </router-link>
      </li>
      <li class="navbar-item dropdown" @mouseenter="showLearnDropdown = true" @mouseleave="showLearnDropdown = false">
        <a 
          href="javascript:void(0);" 
          class="navbar-link"
          :class="{ active: activeNavItem === '/learn/one-click-generation' || activeNavItem === '/learn/step-by-step' || activeNavItem === '/user/file-upload' }"
          id="learn"
          style="cursor: pointer;"
        >
          Learn
          <span class="dropdown-arrow">▼</span>
        </a>
        <ul v-show="showLearnDropdown" class="dropdown-menu">
          <li>
            <a 
              href="javascript:void(0);" 
              class="dropdown-item"
              @click="handleLearnNav('/learn/step-by-step')"
            >
              Step-by-Step Workflow
            </a>
          </li>
          <li>
            <a 
              href="javascript:void(0);" 
              class="dropdown-item"
              @click="handleLearnNav('/learn/one-click-generation')"
            >
              One-Click Generation
            </a>
          </li>
        </ul>
      </li>
      <li class="navbar-item">
        <router-link 
          to="/about" 
          class="navbar-link"
          :class="{ active: activeNavItem === '/about' }"
          @click="setActive('/about')"
        >
          About Us
        </router-link>
      </li>
      <li class="navbar-item">
        <a 
          href="javascript:void(0);" 
          class="navbar-link"
          :class="{ active: activeNavItem === '/login' }"
          @click="checkLoginAndNavigate1('/login')"
          id="signup"
        >
          Get Started
        </a>
      </li>
      <li class="navbar-item">
        <a 
          href="javascript:void(0);" 
          class="navbar-link"
          :class="{ active: activeNavItem === '/register' }"
          @click="checkLoginAndNavigate2('/register')"
          id="register"
        >
          Register
        </a>
      </li>
      <li class="navbar-item">
        <router-link 
          to="/personnel" 
          class="navbar-link"
          :class="{ active: activeNavItem === '/personnel' }"
          @click="setActive('/personnel')"
        >
          Personal Center
        </router-link>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessageBox } from 'element-plus';

const route = useRoute();
const router = useRouter();
const activeNavItem = ref(null);
const showLearnDropdown = ref(false);

onMounted(() => {
  activeNavItem.value = route.path;
});

watch(() => route.path, (newPath) => {
  activeNavItem.value = newPath;
});

const setActive = (item) => {
  activeNavItem.value = item;
};

const handleLearnNav = (item) => {
  showLearnDropdown.value = false;
  checkLoginAndNavigate(item);
};

const checkLoginAndNavigate = (item) => {
  const userInfo = localStorage.getItem('loginUser');
  if (userInfo) {
    // 用户已登录，允许跳转
    setActive(item);
    router.push(item);
  } else {
    // 用户未登录，显示警告并跳转到登录页面
    ElMessageBox.confirm('Please log in to access this page.', 'Warning', {
      confirmButtonText: 'OK',
      cancelButtonText: 'Cancel',
      type: 'warning'
    }).then(() => {
      router.push('/login');
    }).catch(() => {
      // 用户点击取消时不执行任何操作
    });
  }
};

const checkLoginAndNavigate1 = (item) => {
  const userInfo = localStorage.getItem('loginUser');
  if (userInfo) {
    ElMessageBox.confirm('You have already logged in.', 'Warning', {
      confirmButtonText: 'OK',
      showCancelButton: false,
      type: 'warning'
    });
  } else {
    setActive(item);
    router.push(item);
  }
};

const checkLoginAndNavigate2 = (item) => {
  const userInfo = localStorage.getItem('loginUser');
  if (userInfo) {
    ElMessageBox.confirm('You have already logged in.', 'Warning', {
      confirmButtonText: 'OK',
      showCancelButton: false,
      type: 'warning'
    });
  } else {
    setActive(item);
    router.push(item);
  }
};
</script>

<style scoped>
.navbar-component {
  position: fixed;
  top: 0px;
  width: 100%;
  height: 70px;
  background-color: #fff;
  z-index: 10;
  box-sizing: border-box;
}

.navbar-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: block;
}

.navbar-item {
  float: left;
  margin-left: 70px;
  list-style: none;
}

.navbar-link {
  display: block;
  padding: 0;
  height: 42px;
  line-height: 60px;
  font-size: 20px;
  color: #14235A;
  text-decoration: none;
  box-sizing: border-box;
}

.navbar-link:hover {
  color: #00a4ff;
  border-bottom: 2px solid #00a4ff;
}

.navbar-link.active {
  color: #00a4ff;
  border-bottom: 2px solid #00a4ff;
}

.navbar-logo {
  margin-top: 10px;
  display: block;
}

.dropdown {
  position: relative;
}

.dropdown-arrow {
  font-size: 10px;
  margin-left: 5px;
  vertical-align: middle;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  min-width: 220px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  border-radius: 4px;
  list-style: none;
  /* margin: -5px 0 0 0; */
  padding: 5px 0;
  z-index: 1000;
  margin-top: 4px;
}

.dropdown-menu li {
  margin: 0;
  float: none;
}

.dropdown-item {
  display: block;
  padding: 10px 20px;
  color: #14235A;
  text-decoration: none;
  font-size: 16px;
  line-height: normal;
  height: auto;
  border-bottom: none;
  transition: background-color 0.2s;
}

.dropdown-item:hover {
  background-color: #f0f0f0;
  color: #00a4ff;
  border-bottom: none;
}
</style>

