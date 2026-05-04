<template>
  <div class="student-profile">
    <el-row :gutter="20" class="profile-layout">
      <el-col :xs="24" :sm="24" :md="10" :lg="9">
        <el-card class="profile-card" shadow="never">
          <div class="profile-header">
            <AvatarUploadCard
              v-model="avatarUrl"
              :default-avatar="defaultAvatar"
              :fallback-text="userInfo?.realName?.charAt(0) || '?'"
              :upload-handler="uploadMyAvatar"
              :disabled="avatarLoading"
              @success="handleAvatarUploaded"
            />
            <div class="avatar-tip">点击头像即可更换，上传成功后会同步更新顶部头像</div>
            <h3 class="profile-name">{{ userInfo?.realName || '暂无数据' }}</h3>
            <p class="profile-role">
              <el-tag type="success">学生</el-tag>
            </p>
          </div>
          <el-divider />
          <div class="profile-info">
            <div class="info-item">
              <span class="info-label">学号</span>
              <span class="info-value">{{ userInfo?.username || '暂无数据' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">班级</span>
              <span class="info-value">{{ userInfo?.className || '暂无数据' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">手机号</span>
              <span class="info-value">{{ userInfo?.phone || '暂无数据' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">账号状态</span>
              <span class="info-value">
                <el-tag :type="userInfo?.status === 1 ? 'success' : 'danger'" size="small">
                  {{ userInfo?.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">注册时间</span>
              <span class="info-value">{{ formatDate(userInfo?.createdAt) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="14" :lg="15">
        <el-card class="password-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>修改密码</span>
            </div>
          </template>
          <div class="password-intro">
            <div class="password-intro__title">账户安全设置</div>
            <div class="password-intro__text">建议定期更新密码，并避免与其他平台使用相同口令。</div>
          </div>
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="120px"
            class="password-form"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入原密码"
                show-password
                class="password-input"
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码（6-20位）"
                show-password
                class="password-input"
              />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
                class="password-input"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">
                确认修改
              </el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getCurrentUserInfo, changePassword, uploadMyAvatar, type UserVO } from '@/api/user'
import AvatarUploadCard from '@/components/profile/AvatarUploadCard.vue'

const router = useRouter()
const userStore = useUserStore()

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 用户信息
const userInfo = ref<UserVO>()
const avatarUrl = ref('')
const avatarLoading = ref(false)

// 密码表单
const passwordFormRef = ref<FormInstance>()
const passwordLoading = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 自定义确认密码校验
const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await getCurrentUserInfo()
    userInfo.value = res
    avatarUrl.value = res.avatarUrl || ''
    userStore.setUserInfo(res)
  } catch (error) {
    console.error('加载用户信息失败', error)
    ElMessage.error('加载用户信息失败')
  }
}

const handleAvatarUploaded = async () => {
  avatarLoading.value = true
  try {
    await loadUserInfo()
  } finally {
    avatarLoading.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        await changePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        ElMessage.success('密码修改成功，请重新登录')
        resetPasswordForm()
        setTimeout(() => {
          userStore.logout()
          router.push('/login')
        }, 1500)
      } catch (error: any) {
        ElMessage.error(error.message || '密码修改失败')
      } finally {
        passwordLoading.value = false
      }
    }
  })
}

// 重置密码表单
const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.resetFields()
}

// 格式化日期
const formatDate = (date: string | undefined) => {
  if (!date) return '暂无数据'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.student-profile {
  padding: 20px;
  background: linear-gradient(180deg, #f6f9ff 0%, #fbfcff 100%);
}

.profile-layout {
  align-items: stretch;
}

.avatar-tip {
  color: #7f8ca8;
  font-size: 12px;
  line-height: 1.7;
}

.profile-card {
  height: 100%;
  border-radius: 20px;
  border: 1px solid #e7eefb;
  box-shadow: 0 16px 38px rgba(31, 52, 87, 0.08);

  .profile-header {
    text-align: center;
    padding: 20px 0 18px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;

    .profile-name {
      margin: 0;
      font-size: 20px;
      color: #303133;
    }

    .profile-role {
      margin: 0;
    }
  }

  .profile-info {
    .info-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 18px;
      padding: 12px 0;
      border-bottom: 1px solid #ebeef5;

      &:last-child {
        border-bottom: none;
      }

      .info-label {
        color: #909399;
        font-size: 14px;
      }

      .info-value {
        color: #303133;
        font-size: 14px;
        font-weight: 500;
        text-align: right;
        word-break: break-word;
      }
    }
  }
}

.password-card {
  height: 100%;
  border-radius: 20px;
  border: 1px solid #e7eefb;
  box-shadow: 0 16px 38px rgba(31, 52, 87, 0.08);

  .card-header {
    font-size: 16px;
    font-weight: bold;
    color: #1f2a44;
  }
}

.password-intro {
  margin-bottom: 6px;
  padding: 14px 16px;
  border: 1px solid #e7eefb;
  border-radius: 16px;
  background: linear-gradient(180deg, #fbfdff 0%, #f5f8ff 100%);
}

.password-intro__title {
  color: #1f2a44;
  font-size: 15px;
  font-weight: 600;
}

.password-intro__text {
  margin-top: 6px;
  color: #7f8ca8;
  font-size: 13px;
  line-height: 1.7;
}

.password-form {
  max-width: 520px;
  padding: 18px 0 6px;
}

.password-input {
  max-width: 360px;
}

@media (max-width: 992px) {
  .student-profile {
    padding: 16px;
  }

  .password-form {
    max-width: none;
  }
}

@media (max-width: 768px) {
  .student-profile {
    padding: 12px;
  }

  .profile-card,
  .password-card {
    border-radius: 18px;
  }

  .profile-card .profile-info .info-item {
    align-items: flex-start;
    flex-direction: column;
  }

  .profile-card .profile-info .info-item .info-value {
    text-align: left;
  }

  .password-form {
    padding-top: 16px;
  }

  .password-input {
    max-width: none;
    width: 100%;
  }
}
</style>
