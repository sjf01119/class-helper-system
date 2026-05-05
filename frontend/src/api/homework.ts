import axios from 'axios'
import request from '@/utils/request'
import { getScopeFromPath, getScopedToken } from '@/utils/auth'

export interface AttachmentItem {
  name: string
  url: string
  size?: number
}

export interface HomeworkQueryParams {
  current?: number
  size?: number
  title?: string
  courseId?: number
  status?: number
}

export interface HomeworkDTO {
  id?: number
  title: string
  content: string
  courseId: number
  deadline: string
  totalScore: number
  attachmentList?: AttachmentItem[]
}

export interface HomeworkVO {
  id: number
  title: string
  content: string
  courseId: number
  classId?: number
  className?: string
  courseName?: string
  teacherId?: number
  teacherName?: string
  deadline: string
  totalScore?: number
  maxScore?: number
  attachments?: string
  attachmentList?: AttachmentItem[]
  submissionAttachments?: string
  submissionAttachmentList?: AttachmentItem[]
  status: number // 0-待提交 1-已提交
  rawStatus?: number
  statusText?: string
  submitTime?: string
  score?: number
  feedback?: string
  pendingCount?: number
  submittedCount?: number
  totalCount?: number
  createdAt?: string
}

export interface SubmitHomeworkDTO {
  homeworkId: number
  content?: string
  attachments?: string
}

export interface GradeHomeworkDTO {
  submissionId: number
  score: number
  feedback?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  pages: number
  current: number
  size: number
}

export const getHomeworkList = (params: HomeworkQueryParams) => {
  return request.get<PageResult<HomeworkVO>>('/assignment/page', { params })
}

export const getHomeworkListByClassId = (classId: number) => {
  return request.get<HomeworkVO[]>(`/homework/listByClass/${classId}`)
}

export const getStudentHomeworkList = (status?: number) => {
  return request.get<HomeworkVO[]>('/assignment/student/list', { params: { status } })
}

export const getHomeworkById = (id: number) => {
  return request.get<HomeworkVO>(`/assignment/${id}`)
}

export const submitHomework = (data: SubmitHomeworkDTO) => {
  return request.post('/assignment/submit', data)
}

export const getSubmissionDetail = (homeworkId: number) => {
  return request.get(`/assignment/submission/${homeworkId}`)
}

export const downloadAttachment = (url: string) => {
  return request.get(url, { responseType: 'blob' })
}

export const publishHomework = (data: HomeworkDTO) => {
  return request.post('/assignment', data)
}

export const updateHomework = (data: HomeworkDTO) => {
  return request.put('/assignment', data)
}

export const deleteHomework = (id: number) => {
  return request.delete(`/assignment/${id}`)
}

export interface HomeworkSubmissionVO {
  id: number
  assignmentId: number
  studentId: number
  studentName: string
  studentNo: string
  content?: string
  submitTime?: string
  score?: number
  comment?: string
  status?: number
  attachmentList?: AttachmentItem[]
}

export interface AssignmentStatsVO {
  totalStudents: number
  submittedCount: number
  gradedCount: number
  avgScore: string
  passRate: string
  submitRate: string
}

export const getHomeworkSubmissions = (homeworkId: number, params?: { current?: number; size?: number }) => {
  return request.get<PageResult<HomeworkSubmissionVO>>(`/assignment/${homeworkId}/submissions`, { params })
}

export const getHomeworkStats = (homeworkId: number) => {
  return request.get<AssignmentStatsVO>(`/assignment/${homeworkId}/stats`)
}

export const gradeHomework = (data: GradeHomeworkDTO) => {
  return request.put(`/submission/${data.submissionId}/grade`, {
    score: data.score,
    feedback: data.feedback
  })
}

export const submitAssignment = (data: {
  assignmentId: number
  studentId: number
  classId: number
  content: string
  attachments?: string
}) => {
  return request.post('/submission', data)
}

export const getMySubmission = (assignmentId: number, studentId: number) => {
  return request.get('/submission/my-submission', {
    params: { assignmentId, studentId }
  })
}

export const uploadHomeworkAttachments = (data: FormData, onUploadProgress?: (progressEvent: any) => void) => {
  return request.post<AttachmentItem[]>('/assignment/upload', data, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress
  })
}

export const deleteHomeworkAttachment = (fileUrl: string) => {
  return request.delete('/assignment/upload', {
    params: { fileUrl }
  })
}

export const getHomeworkAttachmentAccessUrl = (params: {
  fileUrl: string
  fileName?: string
  download?: boolean
}) => {
  return request.get<string>('/assignment/attachment/access-url', { params })
}

export const downloadHomeworkAttachmentByUrl = async (accessUrl: string) => {
  const token = getScopedToken(getScopeFromPath(window.location.pathname))
  return axios.get(accessUrl, {
    responseType: 'blob',
    headers: token
      ? {
          Authorization: `Bearer ${token}`
        }
      : undefined
  })
}
