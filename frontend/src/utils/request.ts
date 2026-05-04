import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp?: number
}

class RequestError extends Error {
  code?: number
  status?: number

  constructor(message: string, options?: { code?: number; status?: number }) {
    super(message)
    this.name = 'RequestError'
    this.code = options?.code
    this.status = options?.status
  }
}

interface RequestClient {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>
}

// 创建 axios 实例
const instance: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    const token = userStore.token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

const redirectToLogin = () => {
  const userStore = useUserStore()
  const currentPath = `${window.location.pathname}${window.location.search}${window.location.hash}`
  userStore.logout()
  if (window.location.pathname !== '/login') {
    const redirect = encodeURIComponent(currentPath)
    window.location.href = `/login?redirect=${redirect}`
  }
}

// 响应拦截器
instance.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { data } = response
    if (data.code === 200 || data.code === 0) {
      return data.data
    }

    const message = data.message || '请求失败'
    ElMessage.error(message)
    if (data.code === 401) {
      redirectToLogin()
    }
    return Promise.reject(new RequestError(message, { code: data.code, status: response.status }))
  },
  (error) => {
    const { response } = error
    if (response) {
      const { status, data } = response
      const message = data?.message || getHttpErrorMessage(status)
      switch (status) {
        case 401:
          ElMessage.error(message)
          redirectToLogin()
          break
        case 403:
        case 404:
        case 500:
          ElMessage.error(message)
          break
        default:
          ElMessage.error(message)
      }
      return Promise.reject(new RequestError(message, { code: data?.code, status }))
    } else {
      ElMessage.error('网络连接失败')
      return Promise.reject(new RequestError('网络连接失败'))
    }
  }
)

const getHttpErrorMessage = (status: number) => {
  switch (status) {
    case 401:
      return '登录已过期，请重新登录'
    case 403:
      return '没有权限访问'
    case 404:
      return '请求的资源不存在'
    case 500:
      return '服务器内部错误'
    default:
      return '网络错误'
  }
}

const request: RequestClient = {
  get: <T = any>(url: string, config?: AxiosRequestConfig) => {
    return instance.get<any, T>(url, config)
  },
  post: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) => {
    return instance.post<any, T>(url, data, config)
  },
  put: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) => {
    return instance.put<any, T>(url, data, config)
  },
  delete: <T = any>(url: string, config?: AxiosRequestConfig) => {
    return instance.delete<any, T>(url, config)
  }
}

export default request
