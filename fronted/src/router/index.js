import { createRouter, createWebHistory } from 'vue-router';
import { getToken } from '@/utils/auth';
import { ElMessage } from 'element-plus'
const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/introduce',
    name: 'Introduce',
    component: () => import('@/views/Introduce.vue')
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('@/views/About.vue')
  },
  {
    path: '/personnel',
    name: 'Personnel',
    component: () => import('@/views/Personnel.vue')
  },
  {
    path: '/predict',
    name: 'Predict',
    component: () => import('@/views/Predict.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/train',
    name: 'Train',
    component: () => import('@/views/Train.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/train-result',
    name: 'TrainResult',
    component: () => import('@/views/TrainResult.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/train-model-predict',
    name: 'TrainModelPredict',
    component: () => import('@/views/TrainModelPredict.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/historical-record',
    name: 'HistoricalRecord',
    component: () => import('@/views/HistoricalRecord.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/learn/one-click-generation',
    name: 'OneClickGeneration',
    component: () => import('@/views/user/oneClickGeneration.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/learn/step-by-step',
    name: 'StepByStepWorkflow',
    component: () => import('@/views/StepByStepWorkflow.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/networkGraph',
    name: 'NetworkGraph',
    component: () => import('@/components/StepByStepWorkflow/NetworkGraph.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/learn/exampleShow',
    name: 'ExampleShow',
    component: () => import('@/views/ExampleShow.vue'),
    meta: { requiresAuth: true }
  }
];



const router = createRouter({
  history: createWebHistory(),
  routes,
  // 在进行路由切换后，页面滚动到顶部
  scrollBehavior(to, from, savedPosition) {
    // 每次路由切换后滚动到顶部
    return { top: 0 }
  }
});

// 路由守卫 - 简化，只处理未登录拦截
router.beforeEach((to, from, next) => {
  const userInfo = localStorage.getItem('loginUser')
  // 需要登录但未登录（且不是过期跳转来的，过期已由 Axios 处理）
  if (to.meta.requiresAuth && !userInfo) {
    ElMessage.error('Please log in first.')
    next({
      name: 'Login'
    })
  }
  else {
    next()
  }
})
export default router;

