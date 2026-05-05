import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  clearActiveRoleScope,
  clearScopedToken,
  getActiveRoleScope,
  getDefaultHomePathByScope,
  getScopeFromPath,
  getScopedToken,
  normalizeRoleScope,
  setActiveRoleScope,
  type RoleScope
} from '@/utils/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'HomePage',
      component: () => import('@/views/HomePage.vue'),
      meta: { public: true }
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { public: true }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue'),
      meta: { public: true }
    },
    // 管理员布局
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true, roles: ['admin'] },
      children: [
        {
          path: '',
          redirect: '/admin/dashboard'
        },
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/dashboard.vue'),
          meta: { title: '系统首页', icon: 'HomeFilled' }
        },
        {
          path: 'teacher-manage',
          name: 'TeacherManage',
          component: () => import('@/views/admin/TeacherManage.vue'),
          meta: { title: '教师管理', icon: 'UserFilled' }
        },
        {
          path: 'student-manage',
          name: 'StudentManage',
          component: () => import('@/views/admin/StudentManage.vue'),
          meta: { title: '学生管理', icon: 'User' }
        },
        {
          path: 'class-manage',
          name: 'ClassManage',
          component: () => import('@/views/admin/ClassManage.vue'),
          meta: { title: '班级管理', icon: 'School' }
        },
        {
          path: 'announcement-manage',
          name: 'AnnouncementManage',
          component: () => import('@/views/admin/AnnouncementManage.vue'),
          meta: { title: '系统公告', icon: 'Bell' }
        },
        {
          path: 'login-log',
          name: 'LoginLog',
          component: () => import('@/views/admin/LoginLog.vue'),
          meta: { title: '登录日志', icon: 'Document' }
        },
        {
          path: 'operation-log',
          name: 'OperationLog',
          component: () => import('@/views/admin/OperationLog.vue'),
          meta: { title: '操作日志', icon: 'DocumentChecked' }
        }
      ]
    },
    // 教师布局
    {
      path: '/teacher',
      component: () => import('@/layouts/TeacherLayout.vue'),
      meta: { requiresAuth: true, roles: ['teacher'] },
      children: [
        {
          path: '',
          redirect: '/teacher/dashboard'
        },
        {
          path: 'dashboard',
          name: 'TeacherDashboard',
          component: () => import('@/views/teacher/dashboard.vue'),
          meta: { title: '教师首页', icon: 'HomeFilled' }
        },
        {
          path: 'my-class',
          name: 'TeacherMyClass',
          component: () => import('@/views/teacher/MyClass.vue'),
          meta: { title: '我的班级', icon: 'School', requiresHeadTeacher: true }
        },
        {
          path: 'my-course',
          name: 'TeacherMyCourse',
          component: () => import('@/views/teacher/MyCourse.vue'),
          meta: { title: '我的课程', icon: 'Reading' }
        },
        {
          path: 'my-course/:id',
          name: 'TeacherCourseDetail',
          component: () => import('@/views/teacher/CourseDetail.vue'),
          meta: { title: '课程详情', icon: 'Reading' }
        },
        {
          path: 'homework',
          name: 'HomeworkManage',
          component: () => import('@/views/teacher/HomeworkManage.vue'),
          meta: { title: '作业管理', icon: 'EditPen' }
        },
        {
          path: 'material',
          name: 'MaterialManage',
          component: () => import('@/views/teacher/MaterialManage.vue'),
          meta: { title: '学习资料', icon: 'Folder' }
        },
        {
          path: 'announcement',
          name: 'TeacherAnnouncement',
          component: () => import('@/views/teacher/AnnouncementManage.vue'),
          meta: { title: '公告管理', icon: 'Bell' }
        },
        {
          path: 'grade',
          name: 'TeacherGradeManage',
          component: () => import('@/views/teacher/GradeManage.vue'),
          meta: { title: '成绩管理', icon: 'DataLine' }
        },
        {
          path: 'profile',
          name: 'TeacherProfile',
          component: () => import('@/views/teacher/Profile.vue'),
          meta: { title: '个人中心', icon: 'UserFilled' }
        }
      ]
    },
    // 学生布局
    {
      path: '/student',
      component: () => import('@/layouts/StudentLayout.vue'),
      meta: { requiresAuth: true, roles: ['student'] },
      children: [
        {
          path: '',
          redirect: '/student/dashboard'
        },
        {
          path: 'dashboard',
          name: 'StudentDashboard',
          component: () => import('@/views/student/dashboard.vue'),
          meta: { title: '首页', icon: 'HomeFilled' }
        },
        {
          path: 'my-class',
          name: 'MyClass',
          component: () => import('@/views/student/MyClass.vue'),
          meta: { title: '我的班级', icon: 'School' }
        },
        {
          path: 'my-course',
          name: 'StudentMyCourse',
          component: () => import('@/views/student/MyCourse.vue'),
          meta: { title: '我的课程', icon: 'Reading' }
        },
        {
          path: 'course-detail/:courseId',
          name: 'StudentCourseDetail',
          component: () => import('@/views/student/course/CourseDetail.vue'),
          meta: { title: '课程详情', icon: 'Reading' }
        },
        {
          path: 'homework',
          name: 'StudentHomework',
          component: () => import('@/views/student/HomeworkCenter.vue'),
          meta: { title: '作业中心', icon: 'EditPen' }
        },
        {
          path: 'material',
          name: 'StudentMaterial',
          component: () => import('@/views/student/Material.vue'),
          meta: { title: '学习资料', icon: 'Folder' }
        },
        {
          path: 'announcement',
          name: 'StudentAnnouncement',
          component: () => import('@/views/student/AnnouncementNotice.vue'),
          meta: { title: '公告通知', icon: 'Bell' }
        },
        {
          path: 'grade',
          name: 'StudentGrade',
          component: () => import('@/views/student/GradeQuery.vue'),
          meta: { title: '成绩查询', icon: 'DataLine' }
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/student/Profile.vue'),
          meta: { title: '个人中心', icon: 'UserFilled' }
        }
      ]
    },
    // 全局 404 兜底（必须放在最后）
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/404.vue')
    }
  ]
})

const PUBLIC_PATHS = ['/', '/login', '/register']
const AUTH_PAGE_PATHS = ['/login', '/register']

const isPublicRoute = (path: string) => PUBLIC_PATHS.includes(path)

const decodeTokenPayload = (token: string) => {
  try {
    const payload = token.split('.')[1]
    if (!payload) return null
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=')
    const decoded = atob(padded)
    return JSON.parse(decoded)
  } catch {
    return null
  }
}

const normalizeRoles = (roles: unknown): string[] => {
  const source = Array.isArray(roles) ? roles : typeof roles === 'string' ? [roles] : []
  return [...new Set(
    source
      .map((role) => String(role || '').trim().toLowerCase())
      .map((role) => (role.startsWith('role_') ? role.slice(5) : role))
      .filter(Boolean)
  )]
}

const getTokenRoles = (token: string): string[] => {
  const payload = decodeTokenPayload(token)
  return normalizeRoles(payload?.roles)
}

const getRedirectPath = (value: unknown) => {
  return typeof value === 'string' ? value : ''
}

const resolveCurrentScope = (path: string, redirectPath?: string): RoleScope | null => {
  return getScopeFromPath(path) || getScopeFromPath(redirectPath) || getActiveRoleScope()
}

// 全局前置守卫：
// 1. 公开页面（/、/login、/register）直接放行
// 2. 未登录访问 requiresAuth 页面时，跳转到 /login 并记录 redirect
// 3. 已登录访问 /login 或 /register 时，强制跳回首页 /
// 4. 已登录访问受保护页面时，同时校验 token 和 meta.roles，禁止串后台
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  const redirectPath = getRedirectPath(to.query.redirect)
  const currentScope = resolveCurrentScope(to.path, redirectPath)
  const token = currentScope ? getScopedToken(currentScope) : ''
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const requiredRoles = to.matched
    .flatMap(record => {
      const roles = record.meta.roles
      return Array.isArray(roles) ? roles : []
    })
  const currentRoles = getTokenRoles(token)

  if (getScopeFromPath(to.path)) {
    setActiveRoleScope(getScopeFromPath(to.path))
  }
  userStore.syncToken(to.path)

  // 已登录用户不允许重复进入认证页面
  if (AUTH_PAGE_PATHS.includes(to.path) && token && currentScope) {
    next(redirectPath || getDefaultHomePathByScope(currentScope))
    return
  }

  // 首页、登录页、注册页保持公开访问
  if (isPublicRoute(to.path)) {
    next()
    return
  }

  // 未登录访问受保护页面时，自动跳登录页并记录原目标地址
  if (requiresAuth && !token) {
    next({
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    })
    return
  }

  // token 存在但无法解析出合法角色时，视为无效登录态
  if (requiresAuth && currentRoles.length === 0) {
    clearScopedToken(currentScope)
    clearActiveRoleScope()
    next({
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    })
    return
  }

  // 已登录后只能访问自己角色对应的后台页面，禁止越权和串后台
  if (requiredRoles.length > 0) {
    const hasPermission = requiredRoles.some(role => currentRoles.includes(normalizeRoleScope(role) || role))
    if (!hasPermission) {
      next('/')
      return
    }
  }

  next()
})

export default router
