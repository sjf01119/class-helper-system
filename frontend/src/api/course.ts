import request from '@/utils/request'

// 课程查询参数
export interface CourseQueryParams {
  current?: number
  size?: number
  keyword?: string
  teacherId?: number
  classId?: number
  status?: number
  sortOrder?: 'asc' | 'desc'
}

// 课程 DTO
export interface CourseDTO {
  id?: number
  courseName: string
  teacherId?: number
  classId?: number
  credit?: number
  courseHours?: number
  description?: string
  coverUrl?: string
  status?: number
  startTime?: string
  endTime?: string
}

// 课程 VO
export interface CourseVO {
  id: number
  courseName: string
  teacherId: number
  teacherName?: string
  classId: number
  className?: string
  credit?: number
  courseHours?: number
  description?: string
  coverUrl?: string
  status: number
  startTime?: string
  endTime?: string
  createTime?: string
  createdAt?: string
  updatedAt?: string
  studentCount?: number
  assignmentCount?: number
  activeAssignmentCount?: number
  completionRate?: number
}

// 分页结果
export interface PageResult<T> {
  records: T[]
  total: number
  pages: number
  current: number
  size: number
}

// 获取课程列表
export const getCourseList = (params: CourseQueryParams) => {
  return request.get<PageResult<CourseVO>>('/course/list', { params })
}

// 新增课程
export const addCourse = (data: CourseDTO) => {
  return request.post('/course/add', data)
}

// 编辑课程
export const updateCourse = (data: CourseDTO) => {
  return request.put('/course/update', data)
}

// 删除课程
export const deleteCourse = (id: number) => {
  return request.delete(`/course/${id}`)
}

// 获取课程详情
export const getCourseById = (id: number) => {
  return request.get<CourseVO>(`/course/${id}`)
}

export const uploadCourseCover = (data: FormData) => {
  return request.post<string>('/course/cover', data, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取教师授课的课程
export const getMyTeacherCourses = () => {
  return request.get<CourseVO[]>('/course/my-teacher')
}

// 获取学生班级课程
export const getMyStudentCourses = () => {
  return request.get<CourseVO[]>('/course/my-student')
}

// 根据班级ID获取课程列表
export const getCoursesByClassId = (classId: number) => {
  return request.get<CourseVO[]>(`/course/by-class/${classId}`)
}

export const batchUpdateCourseStatus = (data: { ids: number[]; status: number }) => {
  return request.put('/course/batch/status', data)
}

export const batchDeleteCourses = (ids: number[]) => {
  return request.post('/course/batch-delete', ids)
}
