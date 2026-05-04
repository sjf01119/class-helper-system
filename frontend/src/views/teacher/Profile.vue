<template>
  <div class="teacher-profile">
    <el-row :gutter="16">
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-title">个人信息</div>
          </template>
          <div class="avatar-section">
            <AvatarUploadCard
              v-model="avatarUrl"
              :default-avatar="defaultAvatar"
              :fallback-text="profileForm.realName?.charAt(0) || 'T'"
              :upload-handler="uploadMyAvatar"
              :disabled="avatarLoading"
              @success="handleAvatarUploaded"
            />
            <div class="avatar-tip">点击头像即可更换，上传成功后会同步更新顶部头像</div>
          </div>
          <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="90px">
            <el-form-item label="账号">
              <el-input :model-value="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="profileForm.realName" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="性别">
              <el-select v-model="profileForm.gender" style="width: 100%">
                <el-option label="保密" :value="2" />
                <el-option label="男" :value="1" />
                <el-option label="女" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="profileLoading" @click="handleSaveProfile">保存资料</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-title">修改密码</div>
          </template>
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="110px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">修改密码</el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { changePassword, getCurrentUserInfo, updateMyProfile, uploadMyAvatar, type UserVO } from '@/api/user'
import AvatarUploadCard from '@/components/profile/AvatarUploadCard.vue'

const router = useRouter()
const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const profileLoading = ref(false)
const passwordLoading = ref(false)
const avatarLoading = ref(false)
const avatarUrl = ref('')

const profileForm = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  gender: 2
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
    return
  }
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  callback()
}

const profileRules: FormRules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

const applyUserInfo = (user: UserVO) => {
  profileForm.username = user.username || ''
  profileForm.realName = user.realName || ''
  profileForm.email = user.email || ''
  profileForm.phone = user.phone || ''
  profileForm.gender = user.gender ?? 2
  avatarUrl.value = user.avatarUrl || ''
}

const loadUserInfo = async () => {
  const user = await getCurrentUserInfo()
  applyUserInfo(user)
  userStore.setUserInfo(user)
}

const handleSaveProfile = async () => {
  if (!profileFormRef.value) return
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return

  profileLoading.value = true
  try {
    await updateMyProfile({
      realName: profileForm.realName,
      email: profileForm.email || undefined,
      phone: profileForm.phone || undefined,
      gender: profileForm.gender,
      avatarUrl: avatarUrl.value || undefined
    })
    const latestInfo = await getCurrentUserInfo()
    applyUserInfo(latestInfo)
    userStore.setUserInfo(latestInfo)
    ElMessage.success('个人信息已更新')
  } finally {
    profileLoading.value = false
  }
}

const handleAvatarUploaded = async () => {
  avatarLoading.value = true
  try {
    const latestInfo = await getCurrentUserInfo()
    applyUserInfo(latestInfo)
    userStore.setUserInfo(latestInfo)
  } finally {
    avatarLoading.value = false
  }
}

const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  passwordLoading.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    router.push('/login')
  } finally {
    passwordLoading.value = false
  }
}

onMounted(async () => {
  await loadUserInfo()
})
</script>

<style scoped>
.teacher-profile {
  padding: 0;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.avatar-tip {
  color: #7f8ca8;
  font-size: 12px;
  line-height: 1.7;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
</style>
