import request from '@/utils/request'

// 用户查询参数
export interface UserQueryParams {
  current?: number
  size?: number
  keyword?: string
  role?: string
  classId?: number
  status?: number
}

// 用户 DTO
export interface UserDTO {
  id?: number
  username: string
  password?: string
  realName: string
  email?: string
  phone?: string
  roles: string[]
  classId?: number | null
  classIds?: number[]
  status?: number
}

// 用户 VO
export interface UserVO {
  id: number
  username: string
  realName: string
  nickname?: string
  email?: string
  phone?: string
  avatarUrl?: string
  gender?: number
  roles: string[]
  classId?: number | null
  className?: string
  classIds?: number[]
  isHeadTeacher?: boolean
  headTeacherClassId?: number
  headTeacherClassName?: string
  status: number
  createdAt?: string
}

// 分页结果
export interface PageResult<T> {
  records: T[]
  total: number
  pages: number
  current: number
  size: number
}

// 获取用户列表
export const getUserList = (params: UserQueryParams) => {
  return request.get<PageResult<UserVO>>('/user/list', { params })
}

// 新增用户
export const addUser = (data: UserDTO) => {
  return request.post('/user/add', data)
}

// 编辑用户
export const updateUser = (data: UserDTO) => {
  return request.put('/user/update', data)
}

// 删除用户
export const deleteUser = (id: number) => {
  return request.delete(`/user/${id}`)
}

export const deleteUserBatch = (ids: number[]) => {
  const params = new URLSearchParams()
  ids.forEach((id) => params.append('ids', String(id)))
  return request.delete(`/user/batch?${params.toString()}`)
}

// 获取用户详情
export const getUserById = (id: number) => {
  return request.get<UserVO>(`/user/${id}`)
}

// 重置密码
export const resetPassword = (id: number) => {
  return request.post(`/user/resetPwd/${id}`)
}

// 获取教师管理的学生列表
export const getMyStudents = () => {
  return request.get<UserVO[]>('/user/my-students')
}

// 获取当前登录用户信息
export const getCurrentUserInfo = () => {
  return request.get<UserVO>('/user/info')
}

// 获取班级学生列表
export const getClassStudents = (classId: number) => {
  return request.get<UserVO[]>(`/user/class-students/${classId}`)
}

// 修改密码
export const changePassword = (data: { oldPassword: string; newPassword: string }) => {
  return request.post('/user/change-password', data)
}

// 修改当前登录用户个人信息
export const updateMyProfile = (data: {
  realName: string
  email?: string
  phone?: string
  avatarUrl?: string
  nickname?: string
  gender?: number
}) => {
  return request.put('/user/profile', data)
}

export const uploadMyAvatar = (data: FormData) => {
  return request.post<string>('/user/profile/avatar', data, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
