import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

interface UserInfo {
  id: number
  username: string
  realName: string
  phone?: string
  avatarUrl?: string
  roles: string[]
  classId?: number
  className?: string
  isHeadTeacher?: boolean
  status: number
}

export const useUserStore = defineStore('user', () => {
  const normalizeRoles = (roles: unknown): string[] => {
    const source = Array.isArray(roles) ? roles : typeof roles === 'string' ? [roles] : []
    const normalized = source
      .map((role) => String(role || '').trim().toLowerCase())
      .map((role) => (role.startsWith('role_') ? role.slice(5) : role))
      .filter(Boolean)
    return [...new Set(normalized)]
  }

  // State
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.roles?.includes('admin') || false)
  const isTeacher = computed(() => userInfo.value?.roles?.includes('teacher') || false)
  const isStudent = computed(() => userInfo.value?.roles?.includes('student') || false)
  const isHeadTeacher = computed(() => !!userInfo.value?.isHeadTeacher)

  // Actions
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info: UserInfo) => {
    const normalizedInfo = {
      ...info,
      roles: normalizeRoles(info.roles)
    }
    userInfo.value = normalizedInfo
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  const clearUserInfo = () => {
    userInfo.value = null
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    isTeacher,
    isStudent,
    isHeadTeacher,
    setToken,
    setUserInfo,
    clearUserInfo,
    logout,
  }
})
