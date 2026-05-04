<template>
  <div class="my-class-page">
    <el-card class="hero-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="section-title">
            <span class="title-icon">
              <el-icon><Reading /></el-icon>
            </span>
            <div>
              <div class="title-text">我的班级</div>
              <div class="title-tip">查看当前班级信息、公告和成员动态</div>
            </div>
          </div>
          <el-button v-if="!classInfo" type="primary" @click="showJoinDialog = true">
            <el-icon><Plus /></el-icon>
            加入班级
          </el-button>
        </div>
      </template>

      <div v-if="classLoading" class="hero-skeleton">
        <el-skeleton animated>
          <template #template>
            <div class="hero-skeleton__top">
              <div>
                <el-skeleton-item variant="h1" style="width: 220px; height: 34px" />
                <el-skeleton-item variant="text" style="width: 420px; margin-top: 12px" />
                <el-skeleton-item variant="text" style="width: 360px; margin-top: 8px" />
              </div>
              <el-skeleton-item variant="rect" style="width: 260px; height: 116px; border-radius: 20px" />
            </div>
            <div class="hero-skeleton__grid">
              <el-skeleton-item v-for="item in 4" :key="item" variant="rect" style="width: 100%; height: 110px; border-radius: 18px" />
            </div>
          </template>
        </el-skeleton>
      </div>

      <div v-else-if="classInfo" class="hero-content">
        <div class="hero-top">
          <div class="hero-main">
            <div class="class-name-row">
              <div class="class-name-group">
                <h2>{{ classInfo.className }}</h2>
                <div class="class-subline">
                  <el-tag
                    class="status-tag"
                    :type="classInfo.status === 1 ? 'success' : 'danger'"
                    effect="light"
                  >
                    {{ classInfo.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                  <span class="class-subline__tip">当前班级信息、成员和公告动态一目了然</span>
                </div>
              </div>
            </div>
            <div class="description-panel">
              <div class="description-panel__label">班级简介</div>
              <p class="class-description">
                {{ classDescription }}
              </p>
            </div>
          </div>

          <div class="invite-box">
            <div class="invite-box__header">
              <span class="invite-label">班级邀请码</span>
              <span class="invite-tip">分享邀请码给同学加入班级</span>
            </div>
            <div class="invite-content">
              <span class="invite-code">{{ classInfo.inviteCode || '暂未生成' }}</span>
              <el-button
                type="primary"
                plain
                size="small"
                :disabled="!classInfo.inviteCode"
                aria-label="复制班级邀请码"
                @click="handleCopyInviteCode"
              >
                <el-icon><CopyDocument /></el-icon>
                {{ copyButtonText }}
              </el-button>
            </div>
          </div>
        </div>

        <div class="info-grid">
          <div class="info-item">
            <div class="info-icon info-icon--primary">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="info-label">班主任</div>
            <div class="info-value">{{ classInfo.headTeacherName || '暂未分配' }}</div>
            <div class="info-helper">负责班级日常事务与协同管理</div>
          </div>
          <div class="info-item">
            <div class="info-icon info-icon--indigo">
              <el-icon><CollectionTag /></el-icon>
            </div>
            <div class="info-label">授课教师</div>
            <div class="info-value">{{ classInfo.teacherNames || '暂未安排' }}</div>
            <div class="info-helper">当前班级相关课程的任课教师</div>
          </div>
          <div class="info-item">
            <div class="info-icon info-icon--emerald">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="info-label">班级成员</div>
            <div class="info-value">{{ classInfo.currentCount || 0 }} 人</div>
            <div class="info-helper">包含你当前所在班级的全部成员</div>
          </div>
          <div class="info-item">
            <div class="info-icon info-icon--amber">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="info-label">创建时间</div>
            <div class="info-value">{{ formatDate(classInfo.createdAt) }}</div>
            <div class="info-helper">班级建立与维护的起始时间</div>
          </div>
        </div>
      </div>

      <div v-else class="no-class">
        <el-empty description="您还没有加入任何班级">
          <el-button type="primary" @click="showJoinDialog = true">立即加入</el-button>
        </el-empty>
      </div>
    </el-card>

    <div v-if="classInfo" class="content-grid">
      <el-card class="panel-card" shadow="never">
        <template #header>
          <div class="card-header">
            <div class="section-title compact">
              <span class="title-icon subtle danger">
                <el-icon><Bell /></el-icon>
              </span>
              <div class="title-text">班级公告</div>
            </div>
            <div class="panel-header-actions">
              <el-button text size="small" @click="toggleAnnouncementExpanded">
                {{ showAllAnnouncements ? '收起公告' : '查看全部公告' }}
              </el-button>
              <el-button type="primary" plain size="small" @click="loadAnnouncements">
                <el-icon><RefreshRight /></el-icon>
                刷新
              </el-button>
            </div>
          </div>
        </template>

        <div class="panel-body">
          <div v-if="announcementLoading" class="panel-skeleton">
            <el-skeleton animated>
              <template #template>
                <div class="announcement-skeleton-list">
                  <el-skeleton-item v-for="item in 4" :key="item" variant="rect" style="width: 100%; height: 112px; border-radius: 18px" />
                </div>
              </template>
            </el-skeleton>
          </div>
          <el-scrollbar v-else-if="displayAnnouncementList.length" class="panel-scroll">
            <div class="announcement-list">
              <button
                v-for="item in displayAnnouncementList"
                :key="item.id"
                type="button"
                class="announcement-item"
                :class="{ 'announcement-item--read': isAnnouncementRead(item.id), 'announcement-item--unread': !isAnnouncementRead(item.id) }"
                @click="toggleAnnouncementCard(item)"
              >
                <div class="announcement-title-row">
                  <span v-if="!isAnnouncementRead(item.id)" class="announcement-dot" />
                  <el-tag
                    v-if="item.priority === 1"
                    type="danger"
                    size="small"
                    effect="dark"
                  >
                    置顶
                  </el-tag>
                  <span class="announcement-title" :class="{ 'announcement-title--unread': !isAnnouncementRead(item.id) }" :title="item.title">
                    {{ item.title }}
                  </span>
                </div>
                <div class="announcement-preview">
                  {{ getAnnouncementPreview(item) }}
                </div>
                <div class="announcement-meta">
                  <span>{{ formatDate(item.publishTime) }}</span>
                  <span class="meta-with-icon">
                    <el-icon><View /></el-icon>
                    {{ item.viewCount || 0 }}
                  </span>
                </div>
                <div v-if="expandedAnnouncementIds.includes(item.id || -1)" class="announcement-expand">
                  <div class="announcement-expand__content" v-html="item.content || '暂无公告内容'" />
                  <div class="announcement-expand__actions">
                    <el-button text type="primary" size="small" @click.stop="showAnnouncementDetail(item)">
                      查看完整公告
                    </el-button>
                    <el-button text size="small" @click.stop="toggleAnnouncementCard(item)">
                      收起
                    </el-button>
                  </div>
                </div>
                <div v-else class="announcement-inline-action">
                  <span>点击展开全文</span>
                </div>
              </button>
            </div>
          </el-scrollbar>
          <div v-else class="panel-empty">
            <el-empty description="暂无班级公告" />
          </div>
        </div>
      </el-card>

      <el-card class="panel-card" shadow="never">
        <template #header>
          <div class="card-header member-header">
            <div class="section-title compact">
              <span class="title-icon subtle primary">
                <el-icon><UserFilled /></el-icon>
              </span>
              <div class="title-text">班级成员</div>
            </div>
            <div class="member-tools">
              <el-select v-model="memberRoleFilter" class="role-filter" size="default">
                <el-option label="全部" value="all" />
                <el-option label="教师" value="teacher" />
                <el-option label="学生" value="student" />
              </el-select>
              <el-input
                v-model="studentSearchKeyword"
                placeholder="搜索学生姓名或学号"
                clearable
                class="member-search"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
          </div>
        </template>

        <div class="panel-body">
          <div v-if="studentLoading" class="panel-skeleton">
            <el-skeleton animated>
              <template #template>
                <div class="member-skeleton-list">
                  <el-skeleton-item v-for="item in 5" :key="item" variant="rect" style="width: 100%; height: 92px; border-radius: 18px" />
                </div>
              </template>
            </el-skeleton>
          </div>
          <el-scrollbar v-else-if="displayMemberList.length" class="panel-scroll">
            <div class="student-list">
              <div
                v-for="member in displayMemberList"
                :key="member.key"
                class="student-item"
                :class="`student-item--${member.roleType}`"
              >
                <el-avatar :size="46" :src="member.avatarUrl || defaultAvatar">
                  {{ member.name?.charAt(0) || member.account?.charAt(0) }}
                </el-avatar>
                <div class="student-main">
                  <div class="student-name-row">
                    <span class="student-name" v-html="highlightKeyword(member.name)" />
                    <el-tag size="small" effect="light" :type="member.roleType === 'teacher' ? 'warning' : 'success'">
                      {{ member.roleLabel }}
                    </el-tag>
                    <el-tag
                      v-if="member.userId === currentUserId"
                      size="small"
                      effect="plain"
                      type="primary"
                    >
                      我
                    </el-tag>
                  </div>
                  <div class="student-meta-line">
                    <span class="student-meta-chip">{{ member.roleType === 'teacher' ? '工号' : '学号' }}：</span>
                    <span v-html="highlightKeyword(member.account)" />
                  </div>
                </div>
                <div class="student-joined">
                  <span class="joined-label">{{ member.roleType === 'teacher' ? '职责' : '加入时间' }}</span>
                  <span class="joined-value">{{ member.roleType === 'teacher' ? member.joinedText : formatDate(member.joinedAt) }}</span>
                </div>
              </div>
            </div>
          </el-scrollbar>
          <div v-else class="panel-empty">
            <el-empty :description="memberEmptyDescription" />
          </div>
        </div>
      </el-card>
    </div>

    <el-dialog
      v-model="showJoinDialog"
      title="加入班级"
      width="420px"
      :close-on-click-modal="false"
    >
      <el-form ref="joinFormRef" :model="joinForm" :rules="joinRules" label-width="90px">
        <el-form-item label="邀请码" prop="inviteCode">
          <el-input
            v-model="joinForm.inviteCode"
            placeholder="请输入班级邀请码"
            maxlength="10"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showJoinDialog = false">取消</el-button>
        <el-button type="primary" :loading="joinLoading" @click="handleJoinClass">
          确认加入
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showAnnouncementDialog" title="公告详情" width="680px">
      <div v-if="currentAnnouncement" class="announcement-detail">
        <div class="detail-header">
          <div class="detail-title-row">
            <h3 class="detail-title">{{ currentAnnouncement.title }}</h3>
            <el-tag
              v-if="currentAnnouncement.priority === 1"
              type="danger"
              size="small"
              effect="dark"
            >
              置顶
            </el-tag>
          </div>
          <div class="detail-meta">
            <span>发布时间：{{ formatDate(currentAnnouncement.publishTime) }}</span>
            <span>发布教师：{{ currentAnnouncement.publisherName || '系统发布' }}</span>
            <span>浏览量：{{ currentAnnouncement.viewCount || 0 }}</span>
          </div>
        </div>
        <el-divider />
        <div class="detail-content" v-html="currentAnnouncement.content || '暂无公告内容'" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getMyStudentClass, joinClassByInviteCode, type ClassVO } from '@/api/class'
import { getAnnouncementById, getClassAnnouncements, type Announcement } from '@/api/announcement'
import { getClassStudents, type UserVO } from '@/api/user'
import {
  Bell,
  Calendar,
  CollectionTag,
  CopyDocument,
  Plus,
  Reading,
  RefreshRight,
  Search,
  UserFilled,
  View
} from '@element-plus/icons-vue'

const userStore = useUserStore()

type MemberRoleFilter = 'all' | 'student' | 'teacher'

interface DisplayMemberItem {
  key: string
  userId?: number
  roleType: 'teacher' | 'student'
  roleLabel: string
  name: string
  account: string
  avatarUrl?: string
  joinedAt?: string
  joinedText: string
}

const classInfo = ref<ClassVO>()
const classLoading = ref(false)
const announcementLoading = ref(false)
const studentLoading = ref(false)

const announcementList = ref<Announcement[]>([])
const studentList = ref<UserVO[]>([])
const studentSearchKeyword = ref('')
const memberRoleFilter = ref<MemberRoleFilter>('all')
const copyButtonText = ref('复制')
const showAllAnnouncements = ref(false)
const expandedAnnouncementIds = ref<number[]>([])
const readAnnouncementIds = ref<number[]>([])
let copyResetTimer: number | undefined

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const showJoinDialog = ref(false)
const joinLoading = ref(false)
const joinFormRef = ref<FormInstance>()
const joinForm = reactive({
  inviteCode: ''
})
const joinRules: FormRules = {
  inviteCode: [
    { required: true, message: '请输入邀请码', trigger: 'blur' },
    { min: 4, max: 10, message: '邀请码长度为4-10位', trigger: 'blur' }
  ]
}

const showAnnouncementDialog = ref(false)
const currentAnnouncement = ref<Announcement>()

const currentUserId = computed(() => userStore.userInfo?.id)
const announcementStorageKey = computed(() => `class-announcement-read:${currentUserId.value || 'guest'}:${classInfo.value?.id || 0}`)

const classDescription = computed(() => {
  const description = classInfo.value?.description?.trim()
  return description || '暂无班级简介，可联系班主任补充'
})

const teacherMemberList = computed<DisplayMemberItem[]>(() => {
  if (!classInfo.value) {
    return []
  }
  const teacherNames = (classInfo.value.teacherNames || '')
    .split(/[、,，]/)
    .map((item) => item.trim())
    .filter(Boolean)
  const result: DisplayMemberItem[] = []
  if (classInfo.value.headTeacherName) {
    result.push({
      key: `head-${classInfo.value.headTeacherName}`,
      roleType: 'teacher',
      roleLabel: '班主任',
      name: classInfo.value.headTeacherName,
      account: '班级管理教师',
      joinedText: '负责班级管理'
    })
  }
  teacherNames.forEach((name) => {
    if (name && name !== classInfo.value?.headTeacherName) {
      result.push({
        key: `teacher-${name}`,
        roleType: 'teacher',
        roleLabel: '任课教师',
        name,
        account: '授课教师',
        joinedText: '负责课程教学'
      })
    }
  })
  return result
})

const sortedStudentList = computed(() => {
  return [...studentList.value].sort((a, b) => {
    return getTime(b.createdAt) - getTime(a.createdAt)
  })
})

const filteredStudentList = computed<DisplayMemberItem[]>(() => {
  const keyword = studentSearchKeyword.value.trim().toLowerCase()
  const baseStudents = sortedStudentList.value.map<DisplayMemberItem>((student) => ({
    key: `student-${student.id}`,
    userId: student.id,
    roleType: 'student',
    roleLabel: '学生',
    name: student.realName || '未命名学生',
    account: student.username || '暂无数据',
    avatarUrl: student.avatarUrl,
    joinedAt: student.createdAt,
    joinedText: '班级成员'
  }))
  const allMembers = [...teacherMemberList.value, ...baseStudents]
  const filteredByRole = allMembers.filter((member) => {
    if (memberRoleFilter.value === 'all') return true
    return member.roleType === memberRoleFilter.value
  })
  if (!keyword) {
    return filteredByRole
  }
  return filteredByRole.filter((member) => {
    return member.name.toLowerCase().includes(keyword) || member.account.toLowerCase().includes(keyword)
  })
})

const hasOnlySelf = computed(() => {
  return (
    studentList.value.length === 1 &&
    !!currentUserId.value &&
    studentList.value[0]?.id === currentUserId.value
  )
})

const displayMemberList = computed(() => {
  const studentOnlyCurrentUser = memberRoleFilter.value !== 'teacher' && hasOnlySelf.value
  if (studentOnlyCurrentUser && teacherMemberList.value.length === 0) {
    return []
  }
  return filteredStudentList.value
})

const displayAnnouncementList = computed(() => {
  if (showAllAnnouncements.value) {
    return announcementList.value
  }
  return announcementList.value.slice(0, 4)
})

const memberEmptyDescription = computed(() => {
  if (memberRoleFilter.value === 'teacher') {
    return '当前班级暂无教师信息展示'
  }
  if (hasOnlySelf.value && teacherMemberList.value.length === 0) {
    return '暂无其他班级成员，邀请同学后会展示在这里'
  }
  if (studentSearchKeyword.value.trim()) {
    return '未找到匹配的成员，请换个姓名或学号再试试'
  }
  return '当前班级暂无成员信息'
})

const getTime = (date?: string) => {
  if (!date) {
    return 0
  }
  const time = new Date(date).getTime()
  return Number.isNaN(time) ? 0 : time
}

const formatDate = (date?: string) => {
  if (!date) {
    return '暂无数据'
  }
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const resetPageStateForClass = () => {
  copyButtonText.value = '复制'
  showAllAnnouncements.value = false
  expandedAnnouncementIds.value = []
  currentAnnouncement.value = undefined
}

const loadClassInfo = async () => {
  classLoading.value = true
  try {
    const res = await getMyStudentClass()
    classInfo.value = res
    resetPageStateForClass()
    restoreReadAnnouncements()
    if (res?.id) {
      await Promise.all([loadAnnouncements(), loadStudents()])
    } else {
      announcementList.value = []
      studentList.value = []
      readAnnouncementIds.value = []
    }
  } catch (error) {
    console.error('加载班级信息失败', error)
  } finally {
    classLoading.value = false
  }
}

const loadAnnouncements = async () => {
  if (!classInfo.value?.id) {
    announcementList.value = []
    return
  }
  announcementLoading.value = true
  try {
    const res = await getClassAnnouncements(classInfo.value.id)
    announcementList.value = [...(res || [])].sort((a, b) => {
      if ((a.priority || 0) === (b.priority || 0)) {
        return getTime(b.publishTime) - getTime(a.publishTime)
      }
      return (b.priority || 0) - (a.priority || 0)
    })
    expandedAnnouncementIds.value = expandedAnnouncementIds.value.filter((id) =>
      announcementList.value.some((item) => item.id === id)
    )
  } catch (error) {
    console.error('加载公告失败', error)
  } finally {
    announcementLoading.value = false
  }
}

const loadStudents = async () => {
  if (!classInfo.value?.id) {
    studentList.value = []
    return
  }
  studentLoading.value = true
  try {
    const res = await getClassStudents(classInfo.value.id)
    studentList.value = res || []
  } catch (error) {
    console.error('加载学生列表失败', error)
  } finally {
    studentLoading.value = false
  }
}

const handleCopyInviteCode = async () => {
  if (!classInfo.value?.inviteCode) {
    return
  }
  try {
    await navigator.clipboard.writeText(classInfo.value.inviteCode)
    ElMessage.success('邀请码已复制')
    copyButtonText.value = '已复制'
    if (copyResetTimer) {
      window.clearTimeout(copyResetTimer)
    }
    copyResetTimer = window.setTimeout(() => {
      copyButtonText.value = '复制'
      copyResetTimer = undefined
    }, 1800)
  } catch (error) {
    console.error('复制邀请码失败', error)
    ElMessage.error('复制失败，请手动复制邀请码')
  }
}

const handleJoinClass = async () => {
  if (!joinFormRef.value) {
    return
  }
  const valid = await joinFormRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  joinLoading.value = true
  try {
    await joinClassByInviteCode(joinForm.inviteCode)
    ElMessage.success('加入班级成功')
    showJoinDialog.value = false
    joinForm.inviteCode = ''
    await loadClassInfo()
  } catch (error: any) {
    ElMessage.error(error?.message || '加入班级失败')
  } finally {
    joinLoading.value = false
  }
}

const showAnnouncementDetail = async (item: Announcement) => {
  markAnnouncementRead(item.id)
  if (!item.id) {
    currentAnnouncement.value = item
    showAnnouncementDialog.value = true
    return
  }

  try {
    const detail = await getAnnouncementById(item.id)
    currentAnnouncement.value = detail || item
    if (detail) {
      announcementList.value = announcementList.value.map((announcement) =>
        announcement.id === detail.id ? { ...announcement, ...detail } : announcement
      )
    }
    showAnnouncementDialog.value = true
  } catch (error) {
    console.error('加载公告详情失败', error)
    currentAnnouncement.value = item
    showAnnouncementDialog.value = true
  }
}

const escapeHtml = (value: string) => {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

const highlightKeyword = (value?: string) => {
  const text = value || '暂无数据'
  const keyword = studentSearchKeyword.value.trim()
  if (!keyword) {
    return escapeHtml(text)
  }
  const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regexp = new RegExp(`(${escapedKeyword})`, 'ig')
  return escapeHtml(text).replace(regexp, '<mark>$1</mark>')
}

const getAnnouncementPreview = (item: Announcement) => {
  const content = (item.content || '').replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
  if (!content) {
    return '暂无公告摘要，点击查看完整内容'
  }
  return content.length > 72 ? `${content.slice(0, 72)}...` : content
}

const toggleAnnouncementExpanded = () => {
  showAllAnnouncements.value = !showAllAnnouncements.value
}

const toggleAnnouncementCard = (item: Announcement) => {
  if (!item.id) {
    showAnnouncementDetail(item)
    return
  }
  markAnnouncementRead(item.id)
  const announcementId = item.id
  if (expandedAnnouncementIds.value.includes(announcementId)) {
    expandedAnnouncementIds.value = expandedAnnouncementIds.value.filter((id) => id !== announcementId)
    return
  }
  expandedAnnouncementIds.value = [...expandedAnnouncementIds.value, announcementId]
}

const restoreReadAnnouncements = () => {
  try {
    const raw = window.localStorage.getItem(announcementStorageKey.value)
    readAnnouncementIds.value = raw ? JSON.parse(raw) : []
  } catch {
    readAnnouncementIds.value = []
  }
}

const persistReadAnnouncements = () => {
  window.localStorage.setItem(announcementStorageKey.value, JSON.stringify(readAnnouncementIds.value))
}

const markAnnouncementRead = (id?: number) => {
  if (!id || readAnnouncementIds.value.includes(id)) {
    return
  }
  readAnnouncementIds.value = [...readAnnouncementIds.value, id]
  persistReadAnnouncements()
}

const isAnnouncementRead = (id?: number) => {
  if (!id) {
    return true
  }
  return readAnnouncementIds.value.includes(id)
}

onMounted(() => {
  loadClassInfo()
})

onBeforeUnmount(() => {
  if (copyResetTimer) {
    window.clearTimeout(copyResetTimer)
  }
})
</script>

<style scoped lang="scss">
.my-class-page {
  padding: 20px;
  background: linear-gradient(180deg, #f5f8ff 0%, #f9fbff 100%);
}

.hero-card,
.panel-card {
  border: 1px solid #e7eefb;
  border-radius: 20px;
  box-shadow: 0 16px 40px rgba(25, 55, 109, 0.08);
  background: rgba(255, 255, 255, 0.96);
}

.hero-card {
  margin-bottom: 20px;
}

.hero-skeleton__top,
.hero-skeleton__grid {
  display: grid;
  gap: 18px;
}

.hero-skeleton__top {
  grid-template-columns: minmax(0, 1fr) 260px;
  align-items: start;
}

.hero-skeleton__grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
  margin-top: 22px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;

  &.compact {
    gap: 10px;
  }
}

.title-icon {
  width: 40px;
  height: 40px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #1d4ed8;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.16), rgba(96, 165, 250, 0.28));
  font-size: 18px;

  &.subtle {
    width: 34px;
    height: 34px;
    border-radius: 12px;
    font-size: 16px;
  }

  &.danger {
    color: #dc2626;
    background: rgba(239, 68, 68, 0.12);
  }

  &.primary {
    color: #2563eb;
    background: rgba(37, 99, 235, 0.12);
  }
}

.title-text {
  font-size: 17px;
  font-weight: 700;
  color: #1f2a44;
}

.title-tip {
  margin-top: 4px;
  font-size: 13px;
  color: #7b8aac;
}

.hero-content {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.hero-top {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 20px;
  align-items: start;
}

.hero-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.class-name-row {
  display: flex;
  align-items: center;
  gap: 12px;

  h2 {
    margin: 0;
    font-size: 28px;
    line-height: 1.2;
    color: #1f2a44;
  }
}

.class-name-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.class-subline {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px 12px;
}

.class-subline__tip {
  color: #7b8aac;
  font-size: 13px;
}

.status-tag {
  border-radius: 999px;
  padding: 0 12px;
}

.description-panel {
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid #e7eefb;
  background: linear-gradient(180deg, #fcfdff 0%, #f7faff 100%);
}

.description-panel__label {
  margin-bottom: 8px;
  color: #6f7f9f;
  font-size: 13px;
  font-weight: 600;
}

.class-description {
  margin: 0;
  color: #5f6f92;
  font-size: 14px;
  line-height: 1.75;
  white-space: pre-wrap;
}

.invite-box {
  min-width: 240px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid #d9e6ff;
  background: linear-gradient(135deg, #f8fbff 0%, #edf4ff 100%);
  box-shadow: 0 12px 28px rgba(37, 99, 235, 0.08);
}

.invite-box__header {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.invite-label {
  display: block;
  color: #6b7a99;
  font-size: 13px;
  font-weight: 600;
}

.invite-tip {
  color: #8a99b6;
  font-size: 12px;
}

.invite-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 12px;
}

.invite-code {
  display: inline-flex;
  align-items: center;
  min-height: 38px;
  padding: 0 14px;
  border-radius: 12px;
  background: #fff;
  border: 1px dashed #b8c9ea;
  color: #1f2a44;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 16px;
}

.info-item {
  min-height: 96px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid #edf2fb;
  background: #fbfcff;
  box-shadow: 0 8px 18px rgba(31, 52, 87, 0.04);
}

.info-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  margin-bottom: 12px;
  border-radius: 14px;
  font-size: 18px;
}

.info-icon--primary {
  color: #2563eb;
  background: rgba(37, 99, 235, 0.12);
}

.info-icon--indigo {
  color: #4f46e5;
  background: rgba(79, 70, 229, 0.12);
}

.info-icon--emerald {
  color: #059669;
  background: rgba(5, 150, 105, 0.12);
}

.info-icon--amber {
  color: #d97706;
  background: rgba(217, 119, 6, 0.12);
}

.info-label {
  margin-bottom: 8px;
  color: #6f7f9f;
  font-size: 13px;
  font-weight: 600;
}

.info-value {
  color: #1f2a44;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.6;
  word-break: break-word;
}

.info-helper {
  margin-top: 8px;
  color: #8c99b0;
  font-size: 12px;
  line-height: 1.6;
}

.no-class {
  padding: 36px 12px;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
  align-items: stretch;
}

.panel-card {
  height: 540px;

  :deep(.el-card__body) {
    height: calc(100% - 73px);
    padding: 0 20px 20px;
  }
}

.member-header {
  align-items: center;
}

.panel-header-actions,
.member-tools {
  display: flex;
  align-items: center;
  gap: 10px;
}

.role-filter {
  width: 112px;
}

.member-search {
  width: 220px;
}

.panel-body {
  height: 100%;
  min-height: 0;
}

.panel-scroll {
  height: 100%;
}

.panel-empty {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.panel-skeleton {
  padding-top: 6px;
}

.announcement-skeleton-list,
.member-skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.announcement-list,
.student-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.announcement-item {
  width: 100%;
  padding: 16px;
  border: 1px solid #edf1f8;
  border-radius: 16px;
  background: #fff;
  appearance: none;
  color: inherit;
  font: inherit;
  text-align: left;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease;

  &:hover {
    transform: translateY(-1px);
    border-color: #c9dcff;
    box-shadow: 0 10px 24px rgba(43, 86, 173, 0.08);
  }

  &:focus-visible {
    outline: 0;
    border-color: #2563eb;
    box-shadow:
      0 0 0 3px rgba(37, 99, 235, 0.14),
      0 10px 24px rgba(43, 86, 173, 0.08);
  }
}

.announcement-item--unread {
  border-color: #d7e4ff;
  background: linear-gradient(180deg, #fbfdff 0%, #f5f9ff 100%);
}

.announcement-item--read {
  opacity: 0.92;
}

.announcement-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.announcement-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ef4444;
  flex-shrink: 0;
}

.announcement-title {
  flex: 1;
  min-width: 0;
  color: #1f2a44;
  font-size: 15px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.announcement-title--unread {
  font-weight: 700;
}

.announcement-preview {
  color: #60708f;
  font-size: 13px;
  line-height: 1.75;
  display: -webkit-box;
  overflow: hidden;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.announcement-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
  color: #7f8ca8;
  font-size: 13px;
}

.meta-with-icon {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.announcement-inline-action {
  margin-top: 10px;
  color: #8c99b0;
  font-size: 12px;
}

.announcement-expand {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eef3fb;
}

.announcement-expand__content {
  color: #4c5a74;
  font-size: 13px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;

  :deep(p) {
    margin: 0 0 12px;
  }

  :deep(p:last-child) {
    margin-bottom: 0;
  }
}

.announcement-expand__actions {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
  margin-top: 10px;
}

.student-item {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border: 1px solid #edf1f8;
  border-radius: 16px;
  background: #fff;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease;
}

.student-item:hover {
  transform: translateY(-1px);
  border-color: #d5e0f4;
  box-shadow: 0 10px 24px rgba(43, 86, 173, 0.07);
}

.student-item--teacher {
  background: linear-gradient(180deg, #fffaf3 0%, #fffdf8 100%);
  border-color: #f2e1bf;
}

.student-item--student {
  background: linear-gradient(180deg, #fcfdff 0%, #f8fbff 100%);
}

.student-main {
  min-width: 0;
}

.student-name-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 6px;
}

.student-name {
  color: #1f2a44;
  font-size: 15px;
  font-weight: 600;
}

.student-meta-line {
  color: #7f8ca8;
  font-size: 13px;
}

.student-meta-chip {
  color: #95a2b8;
}

.student-joined {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  text-align: right;
}

.joined-label {
  color: #8b97b2;
  font-size: 12px;
}

.joined-value {
  color: #3a4a68;
  font-size: 13px;
  font-weight: 500;
}

.announcement-detail {
  .detail-header {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .detail-title-row {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .detail-title {
    margin: 0;
    color: #1f2a44;
    font-size: 22px;
    font-weight: 700;
  }

  .detail-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 12px 20px;
    color: #7f8ca8;
    font-size: 13px;
  }

  .detail-content {
    color: #44506a;
    line-height: 1.85;
    white-space: pre-wrap;
    word-break: break-word;
  }
}

:deep(mark) {
  padding: 0 2px;
  border-radius: 4px;
  background: rgba(255, 222, 89, 0.45);
  color: inherit;
}

@media (max-width: 992px) {
  .hero-skeleton__top,
  .hero-skeleton__grid,
  .hero-top,
  .content-grid,
  .info-grid {
    grid-template-columns: 1fr;
  }

  .panel-card {
    height: 500px;
  }
}

@media (max-width: 768px) {
  .my-class-page {
    padding: 16px;
  }

  .card-header,
  .member-header,
  .member-tools,
  .panel-header-actions {
    align-items: flex-start;
    flex-direction: column;
  }

  .member-tools,
  .panel-header-actions {
    width: 100%;
  }

  .role-filter,
  .member-search {
    width: 100%;
  }

  .student-item {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .student-joined {
    grid-column: 1 / -1;
    align-items: flex-start;
    text-align: left;
    padding-left: 56px;
  }

  .invite-box {
    min-width: 100%;
  }

  .announcement-meta {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (max-width: 576px) {
  .my-class-page {
    padding: 12px;
  }

  .panel-card,
  .hero-card {
    border-radius: 18px;
  }

  .class-name-row h2 {
    font-size: 24px;
  }

  .invite-content {
    align-items: stretch;
    flex-direction: column;
  }

  .invite-code,
  .invite-content :deep(.el-button) {
    width: 100%;
    justify-content: center;
  }

  .announcement-item,
  .student-item {
    padding: 14px;
  }

  .announcement-expand__actions {
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .student-joined {
    padding-left: 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .announcement-item,
  .student-item {
    transition: none;
  }
}
</style>
