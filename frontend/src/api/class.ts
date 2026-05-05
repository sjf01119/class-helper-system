import request from '@/utils/request'

// 班级查询参数
export interface ClassQueryParams {
  current?: number
  size?: number
  keyword?: string
  status?: number
}

// 班级 DTO
export interface ClassDTO {
  id?: number
  className: string
  description?: string
  inviteCode?: string
  teacherId?: number | null
  status?: number
  forceReplaceHeadTeacher?: boolean
  confirmClearHeadTeacher?: boolean
}

// 班级 VO
export interface ClassVO {
  id: number
  className: string
  description?: string
  inviteCode?: string
  teacherId?: number | null
  headTeacherName?: string
  teacherNames?: string
  status: number
  currentCount?: number
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

// 获取班级列表
export const getClassList = (params: ClassQueryParams) => {
  return request.get<PageResult<ClassVO>>('/class/list', { params })
}

// 新增班级
export const addClass = (data: ClassDTO) => {
  return request.post('/class/add', data)
}

// 编辑班级
export const updateClass = (data: ClassDTO) => {
  return request.put('/class/update', data)
}

// 删除班级
export const deleteClass = (id: number) => {
  return request.delete(`/class/${id}`)
}

// 获取班级详情
export const getClassById = (id: number) => {
  return request.get<ClassVO>(`/class/${id}`)
}

// 获取教师管理的班级
export const getMyTeacherClasses = () => {
  return request.get<ClassVO[]>('/class/my-teacher')
}

// 获取班主任负责的班级
export const getMyHeadTeacherClasses = () => {
  return request.get<ClassVO[]>('/class/my-head-teacher')
}

export const getHeadTeacherStatus = () => {
  return request.get<boolean>('/class/head-teacher/status')
}

// 获取学生所在班级
export const getMyStudentClass = () => {
  return request.get<ClassVO>('/class/my-student')
}

// 获取所有班级列表（用于下拉选择）
export const getAllClasses = () => {
  return request.get<ClassVO[]>('/class/all')
}

// 通过邀请码加入班级
export const joinClassByInviteCode = (inviteCode: string) => {
  return request.post('/class/join', { inviteCode })
}

export const resetInviteCode = (id: number) => {
  return request.put<ClassVO>(`/class/${id}/reset-invite-code`)
}
