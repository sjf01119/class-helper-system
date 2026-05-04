import request from '@/utils/request'
import type { Announcement } from '@/api/announcement'
import type { LoginLog } from '@/api/log'
import type { ClassVO } from '@/api/class'
import type { CourseVO } from '@/api/course'

export interface DashboardOverview {
  teacherCount: number
  studentCount: number
  classCount: number
  announcementCount: number
}

export interface UserGrowthItem {
  month: string
  value: number
}

export interface ClassDistributionItem {
  name: string
  value: number
}

export interface TeacherDashboardOverview {
  studentCount: number
  courseCount: number
  homeworkCount: number
  materialCount: number
  classCount: number
  pendingReviewCount: number
  announcementCount: number
}

export interface TeacherPendingHomeworkItem {
  id: number
  title: string
  courseName: string
  deadline?: string
  pendingCount: number
}

export interface StudentDashboardStats {
  courseCount: number
  pendingHomework: number
  submittedHomework: number
  announcementCount: number
}

export interface StudentPendingHomeworkItem {
  id: number
  title: string
  courseId?: number
  courseName: string
  deadline?: string
  deadlineStatus: '待提交' | '已截止'
}

export interface StudentDashboardOverview {
  stats: StudentDashboardStats
  myClass: ClassVO | null
  courseList: CourseVO[]
  pendingHomeworkList: StudentPendingHomeworkItem[]
  announcementList: Announcement[]
}

export interface StudentGradeStats {
  completedRate: number
  avgScore: number
  submittedCount: number
  gradedCount: number
  passRate: number
  rank: number
}

export interface StudentCourseGradeStat {
  courseId?: number
  courseName: string
  gradedCount: number
  avgScore: string
  maxScore: number
  minScore: number
}

export interface StudentScoreTrend {
  dates: string[]
  scores: number[]
}

export interface StudentReportOverview {
  stats: StudentGradeStats
  scoreTrend: StudentScoreTrend
  weakPoints: {
    indicators: Array<Record<string, any>>
    values: number[]
  }
  courseStats: StudentCourseGradeStat[]
}

export const getDashboardOverview = () => {
  return request.get<DashboardOverview>('/admin/dashboard/overview')
}

export const getUserGrowthTrend = () => {
  return request.get<UserGrowthItem[]>('/admin/dashboard/user-growth')
}

export const getClassDistribution = () => {
  return request.get<ClassDistributionItem[]>('/admin/dashboard/class-distribution')
}

export const getLatestAnnouncements = (limit = 2) => {
  return request.get<Announcement[]>('/admin/dashboard/latest-announcements', { params: { limit } })
}

export const getRecentLoginLogs = (limit = 5) => {
  return request.get<LoginLog[]>('/admin/dashboard/recent-login-logs', { params: { limit } })
}

export const getTeacherDashboardOverview = () => {
  return request.get<TeacherDashboardOverview>('/teacher/dashboard/overview')
}

export const getTeacherDashboardClasses = () => {
  return request.get<ClassVO[]>('/teacher/dashboard/classes')
}

export const getTeacherPendingHomework = (limit = 5) => {
  return request.get<TeacherPendingHomeworkItem[]>('/teacher/dashboard/pending-homework', { params: { limit } })
}

export const getTeacherRecentAnnouncements = (limit = 5) => {
  return request.get<Announcement[]>('/teacher/dashboard/recent-announcements', { params: { limit } })
}

export const getTeacherDashboardCourses = () => {
  return request.get<CourseVO[]>('/teacher/dashboard/my-courses')
}

export const getStudentDashboardOverview = (params?: { pendingLimit?: number; announcementLimit?: number }) => {
  return request.get<StudentDashboardOverview>('/student/dashboard/overview', { params })
}

export const getStudentReportOverview = (params?: { period?: string }) => {
  return request.get<StudentReportOverview>('/student/report', { params })
}
