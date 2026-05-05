import axios from 'axios'
import request from '@/utils/request'
import { getScopeFromPath, getScopedToken } from '@/utils/auth'

// 资料查询参数
export interface MaterialQueryParams {
  current?: number
  size?: number
  title?: string
  courseId?: number
  fileType?: string
  category?: string
}

// 资料 DTO
export interface MaterialDTO {
  id?: number
  title: string
  description?: string
  courseId: number
  type: string
  fileUrl: string
  fileSize?: number
  fileName?: string
}

// 资料 VO - 使用 LearningResource 的字段
export interface MaterialVO {
  id: number
  title: string
  description?: string
  courseId: number
  courseName?: string
  category?: string
  type?: string
  fileType?: string
  fileUrl: string
  fileSize?: number
  fileName?: string
  uploadBy?: number
  uploaderName?: string
  downloadCount?: number
  viewCount?: number
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

// 获取资料列表 - 使用 /resource 路径
export const getMaterialList = (params: MaterialQueryParams) => {
  return request.get<PageResult<MaterialVO>>('/resource/page', { params })
}

// 获取课程资料列表
export const getCourseMaterials = (courseId: number) => {
  return request.get<MaterialVO[]>(`/resource/list`, { params: { classId: courseId } })
}

// 获取学生可见的资料列表
export const getStudentMaterials = (params: MaterialQueryParams) => {
  return request.get<PageResult<MaterialVO>>('/resource/page', { params })
}

// 获取资料详情
export const getMaterialById = (id: number) => {
  return request.get<MaterialVO>(`/resource/${id}`)
}

// 预览资料
export const previewMaterial = (id: number) => {
  return request.get<string>(`/resource/${id}/previewUrl`)
}

// 下载资料文件
export const downloadMaterialFile = async (id: number) => {
  const token = getScopedToken(getScopeFromPath(window.location.pathname))
  return axios.get(`/api/resource/${id}/download`, {
    responseType: 'blob',
    headers: token
      ? {
          Authorization: `Bearer ${token}`
        }
      : undefined
  })
}

// 新增资料
export const addMaterial = (data: FormData, onUploadProgress?: (progressEvent: any) => void) => {
  return request.post('/resource/upload', data, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress
  })
}

// 更新资料
export const updateMaterial = (data: MaterialDTO) => {
  return request.put('/resource', data)
}

// 删除资料
export const deleteMaterial = (id: number) => {
  return request.delete(`/resource/${id}`)
}
