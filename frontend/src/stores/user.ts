import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  clearActiveRoleScope,
  clearScopedToken,
  clearScopedUserInfo,
  getScopedUserInfo,
  getScopedTokenByPath,
  getActiveRoleScope,
  resolveScopeFromRoles,
  setActiveRoleScope,
  setScopedToken,
  setScopedUserInfo,
  type RoleScope
} from '@/utils/auth'

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
  const initialScope = getActiveRoleScope() || resolveScopeFromRoles([])
  const token = ref<string>(getScopedTokenByPath(window.location.pathname))
  const userInfo = ref<UserInfo | null>(getScopedUserInfo<UserInfo>(initialScope))

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.roles?.includes('admin') || false)
  const isTeacher = computed(() => userInfo.value?.roles?.includes('teacher') || false)
  const isStudent = computed(() => userInfo.value?.roles?.includes('student') || false)
  const isHeadTeacher = computed(() => !!userInfo.value?.isHeadTeacher)

  // Actions
  const setToken = (newToken: string, scope?: RoleScope | null) => {
    const resolvedScope = scope || getActiveRoleScope() || resolveScopeFromRoles(userInfo.value?.roles)
    if (!resolvedScope) {
      token.value = newToken
      return
    }
    setScopedToken(resolvedScope, newToken)
    setActiveRoleScope(resolvedScope)
    token.value = newToken
  }

  const setUserInfo = (info: UserInfo) => {
    const normalizedInfo = {
      ...info,
      roles: normalizeRoles(info.roles)
    }
    const resolvedScope = resolveScopeFromRoles(normalizedInfo.roles) || getActiveRoleScope()
    userInfo.value = normalizedInfo
    if (resolvedScope) {
      setActiveRoleScope(resolvedScope)
      setScopedUserInfo(resolvedScope, normalizedInfo)
    }
  }

  const syncToken = (path?: string) => {
    token.value = getScopedTokenByPath(path || window.location.pathname)
    const resolvedScope = getActiveRoleScope()
    userInfo.value = getScopedUserInfo<UserInfo>(resolvedScope)
  }

  const logout = (scope?: RoleScope | null) => {
    const resolvedScope = scope || getActiveRoleScope() || resolveScopeFromRoles(userInfo.value?.roles)
    clearScopedToken(resolvedScope)
    clearScopedUserInfo(resolvedScope)
    if (!scope || resolvedScope === getActiveRoleScope()) {
      clearActiveRoleScope()
    }
    token.value = getScopedTokenByPath(window.location.pathname, false)
    userInfo.value = null
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
    syncToken,
    logout,
  }
})
