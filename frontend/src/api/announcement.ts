import request from '@/utils/request'

export interface Announcement {
  id?: number
  title: string
  content: string
  type?: number
  publishScope?: number
  classId?: number
  publisherId?: number
  publisherName?: string
  priority?: number
  isTop?: number
  status: number
  viewCount?: number
  publishTime?: string
  expireTime?: string
  createdAt?: string
  updatedAt?: string
}

export interface AnnouncementQuery {
  current?: number
  size?: number
  title?: string
  type?: number
  publishScope?: number
  classId?: number
  isTop?: number
  status?: number
}

// 分页查询公告
export const getAnnouncementPage = (params: AnnouncementQuery) => {
  return request.get('/announcement/page', { params })
}

// 获取公告列表
export const getAnnouncementList = (params?: { type?: number; status?: number; isTop?: number; publishScope?: number; classId?: number }) => {
  return request.get('/announcement/list', { params })
}

// 获取公告详情
export const getAnnouncementById = (id: number) => {
  return request.get(`/announcement/${id}`)
}

// 新增公告
export const addAnnouncement = (data: Announcement) => {
  return request.post('/announcement', data)
}

// 更新公告
export const updateAnnouncement = (data: Announcement) => {
  return request.put('/announcement', data)
}

// 删除公告
export const deleteAnnouncement = (id: number) => {
  return request.delete(`/announcement/${id}`)
}

// 批量删除公告
export const deleteAnnouncementBatch = (ids: number[]) => {
  return request.delete('/announcement/batch', { params: { ids } })
}

// 获取班级公告列表
export const getClassAnnouncements = (classId: number) => {
  return request.get<Announcement[]>(`/announcement/list`, { params: { classId } })
}
