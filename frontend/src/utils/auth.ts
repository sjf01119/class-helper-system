export type RoleScope = 'admin' | 'teacher' | 'student'

const ACTIVE_SCOPE_KEY = 'active_role_scope'

const TOKEN_KEY_MAP: Record<RoleScope, string> = {
  admin: 'admin_token',
  teacher: 'teacher_token',
  student: 'student_token'
}

const USER_INFO_KEY_MAP: Record<RoleScope, string> = {
  admin: 'admin_user_info',
  teacher: 'teacher_user_info',
  student: 'student_user_info'
}

const ROLE_SCOPES: RoleScope[] = ['admin', 'teacher', 'student']

const normalizePath = (path?: string | null) => {
  if (!path) return ''
  try {
    if (/^https?:\/\//i.test(path)) {
      return new URL(path).pathname
    }
  } catch {
    return path
  }
  return path
}

export const normalizeRoleScope = (value?: string | null): RoleScope | null => {
  if (!value) return null
  const normalized = String(value).trim().toLowerCase()
  if (ROLE_SCOPES.includes(normalized as RoleScope)) {
    return normalized as RoleScope
  }
  if (normalized.startsWith('role_')) {
    const raw = normalized.slice(5)
    if (ROLE_SCOPES.includes(raw as RoleScope)) {
      return raw as RoleScope
    }
  }
  return null
}

export const getScopeFromPath = (path?: string | null): RoleScope | null => {
  const pathname = normalizePath(path)
  if (pathname.startsWith('/admin')) return 'admin'
  if (pathname.startsWith('/teacher')) return 'teacher'
  if (pathname.startsWith('/student')) return 'student'
  return null
}

export const getTokenStorageKey = (scope: RoleScope) => TOKEN_KEY_MAP[scope]

export const getUserInfoStorageKey = (scope: RoleScope) => USER_INFO_KEY_MAP[scope]

export const getScopedToken = (scope?: RoleScope | null) => {
  if (!scope) return ''
  return localStorage.getItem(getTokenStorageKey(scope)) || ''
}

export const setScopedToken = (scope: RoleScope, token: string) => {
  localStorage.setItem(getTokenStorageKey(scope), token)
}

export const clearScopedToken = (scope?: RoleScope | null) => {
  if (!scope) return
  localStorage.removeItem(getTokenStorageKey(scope))
}

export const getScopedUserInfo = <T = unknown>(scope?: RoleScope | null): T | null => {
  if (!scope) return null
  const raw = localStorage.getItem(getUserInfoStorageKey(scope))
  if (!raw) return null
  try {
    return JSON.parse(raw) as T
  } catch {
    return null
  }
}

export const setScopedUserInfo = (scope: RoleScope, userInfo: unknown) => {
  localStorage.setItem(getUserInfoStorageKey(scope), JSON.stringify(userInfo))
}

export const clearScopedUserInfo = (scope?: RoleScope | null) => {
  if (!scope) return
  localStorage.removeItem(getUserInfoStorageKey(scope))
}

export const getActiveRoleScope = (): RoleScope | null => {
  return normalizeRoleScope(sessionStorage.getItem(ACTIVE_SCOPE_KEY))
}

export const setActiveRoleScope = (scope?: RoleScope | null) => {
  if (!scope) {
    sessionStorage.removeItem(ACTIVE_SCOPE_KEY)
    return
  }
  sessionStorage.setItem(ACTIVE_SCOPE_KEY, scope)
}

export const clearActiveRoleScope = () => {
  sessionStorage.removeItem(ACTIVE_SCOPE_KEY)
}

export const resolveScopeFromRoles = (roles?: string[] | null): RoleScope | null => {
  if (!roles?.length) return null
  for (const role of roles) {
    const scope = normalizeRoleScope(role)
    if (scope) return scope
  }
  return null
}

export const resolveLoginRoleScope = (roles?: string[] | null, redirectPath?: string | null): RoleScope | null => {
  const redirectScope = getScopeFromPath(redirectPath)
  if (redirectScope && roles?.some(role => normalizeRoleScope(role) === redirectScope)) {
    return redirectScope
  }
  return resolveScopeFromRoles(roles)
}

export const getScopedTokenByPath = (path?: string | null, fallbackToActive = true) => {
  const scope = getScopeFromPath(path) || (fallbackToActive ? getActiveRoleScope() : null)
  return getScopedToken(scope)
}

export const getDefaultHomePathByScope = (scope?: RoleScope | null) => {
  if (scope === 'admin') return '/admin/dashboard'
  if (scope === 'teacher') return '/teacher/dashboard'
  if (scope === 'student') return '/student/dashboard'
  return '/'
}
