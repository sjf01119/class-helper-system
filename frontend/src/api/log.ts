import request from '@/utils/request'

export interface LoginLog {
  id: number
  userId?: number
  username: string
  ip: string
  location?: string
  browser?: string
  os?: string
  loginStatus: number
  msg?: string
  createdAt: string
}

export interface OperationLog {
  id: number
  userId?: number
  username: string
  operation: string
  method: string
  params?: string
  ip: string
  spendTime?: number
  status: number
  errorMsg?: string
  createdAt: string
}

export interface LoginLogQuery {
  current?: number
  size?: number
  username?: string
  loginStatus?: number
}

export interface OperationLogQuery {
  current?: number
  size?: number
  username?: string
  operation?: string
  status?: number
}

// ========== 登录日志 ==========

// 分页查询登录日志
export const getLoginLogPage = (params: LoginLogQuery) => {
  return request.get('/log/login/page', { params })
}

// 获取登录日志列表
export const getLoginLogList = (userId?: number) => {
  return request.get('/log/login/list', { params: { userId } })
}

// 删除登录日志
export const deleteLoginLog = (id: number) => {
  return request.delete(`/log/login/${id}`)
}

// 批量删除登录日志
export const deleteLoginLogBatch = (ids: number[]) => {
  return request.delete('/log/login/batch', { params: { ids } })
}

// ========== 操作日志 ==========

// 分页查询操作日志
export const getOperationLogPage = (params: OperationLogQuery) => {
  return request.get('/log/operation/page', { params })
}

// 获取操作日志列表
export const getOperationLogList = (userId?: number) => {
  return request.get('/log/operation/list', { params: { userId } })
}

// 删除操作日志
export const deleteOperationLog = (id: number) => {
  return request.delete(`/log/operation/${id}`)
}

// 批量删除操作日志
export const deleteOperationLogBatch = (ids: number[]) => {
  return request.delete('/log/operation/batch', { params: { ids } })
}
