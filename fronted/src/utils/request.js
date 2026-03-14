import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

//创建axios实例对象
const request = axios.create({
  baseURL: '/api',
  timeout: 600000
})

//axios的请求 request 拦截器, 每次请求获取localStorage中的loginUser, 从中获取到token, 在请求头token中携带到服务端
request.interceptors.request.use(
  (config) => {
    let loginUser = JSON.parse(localStorage.getItem('loginUser'))
    console.log(localStorage.getItem('loginUser'))
    if (loginUser) {
      config.headers['authentication'] = loginUser.token
    }
    return config
  }
)

//axios的响应 response 拦截器
request.interceptors.response.use(
  (response) => { //成功回调
    // 如果是下载文件，返回完整 response，让业务层能拿到 headers
    if (response.config.responseType === 'blob') {
      return response
    }
    return response.data
  },
  (error) => { //失败回调
    if (error.response.status === 401) {
      ElMessage.error('Login expired. Please log in again.')
      // 只清除状态，不跳转 跳转操作交给路由守卫
      localStorage.removeItem('loginUser')
      // 强制刷新并携带当前路径，用于登录后回跳
      const currentPath = router.currentRoute.value.fullPath
      console.log("currentPath",currentPath)
      // 获取当前路径（从 error.config 或 router）
      // 让路由守卫处理跳转逻辑
      router.push({
        path: '/login',
        query: {
          expired: '1',
          redirect: currentPath
        }
      })
      // 后面的跳转逻辑在登录完成之后再处理
    }else{
      ElMessage.error('Interface access exception.')
    }
    return Promise.reject(error)
  }
)

export default request