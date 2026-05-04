<template>
  <el-upload
    class="avatar-upload-card"
    :show-file-list="false"
    :auto-upload="true"
    :accept="accept"
    :http-request="handleUploadRequest"
    :before-upload="beforeUpload"
    :disabled="uploading || disabled"
  >
    <div
      class="avatar-upload-card__surface"
      :class="{ 'is-uploading': uploading, 'is-disabled': disabled }"
    >
      <el-avatar :size="size" :src="previewUrl || defaultAvatar" class="avatar-upload-card__avatar">
        {{ fallbackText }}
      </el-avatar>
      <div class="avatar-upload-card__overlay">
        <span class="avatar-upload-card__overlay-title">{{ uploading ? '上传中...' : '更换头像' }}</span>
        <span class="avatar-upload-card__overlay-tip">支持 JPG / PNG / JPEG，大小不超过 50MB</span>
      </div>
      <div v-if="uploading" class="avatar-upload-card__loading">
        <el-icon class="is-loading"><Loading /></el-icon>
      </div>
    </div>
  </el-upload>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadRequestOptions } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
  modelValue?: string
  defaultAvatar: string
  fallbackText: string
  size?: number
  disabled?: boolean
  uploadHandler: (data: FormData) => Promise<string>
}>(), {
  modelValue: '',
  size: 96,
  disabled: false
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'success', value: string): void
}>()

const accept = 'image/png,image/jpeg,image/jpg'
const previewUrl = ref(props.modelValue || '')
const uploading = ref(false)

watch(() => props.modelValue, (value) => {
  if (!uploading.value) {
    previewUrl.value = value || ''
  }
}, { immediate: true })

const normalizedExtensions = ['jpg', 'jpeg', 'png']

const revokePreviewObjectUrl = (value: string) => {
  if (value.startsWith('blob:')) {
    URL.revokeObjectURL(value)
  }
}

const beforeUpload = (rawFile: File) => {
  const extension = rawFile.name.split('.').pop()?.toLowerCase() || ''
  const isAllowedType = normalizedExtensions.includes(extension)
  const isAllowedSize = rawFile.size <= 50 * 1024 * 1024

  if (!isAllowedType) {
    ElMessage.error('头像仅支持 JPG、PNG、JPEG 格式')
    return false
  }

  if (!isAllowedSize) {
    ElMessage.error('头像大小不能超过 50MB')
    return false
  }

  return true
}

const handleUploadRequest = async (options: UploadRequestOptions) => {
  const previousUrl = previewUrl.value
  const localPreviewUrl = URL.createObjectURL(options.file)
  previewUrl.value = localPreviewUrl
  uploading.value = true

  try {
    const formData = new FormData()
    formData.append('file', options.file)
    const uploadedUrl = await props.uploadHandler(formData)
    emit('update:modelValue', uploadedUrl)
    emit('success', uploadedUrl)
    previewUrl.value = uploadedUrl
    ElMessage.success('头像上传成功')
  } catch (error: any) {
    revokePreviewObjectUrl(localPreviewUrl)
    previewUrl.value = previousUrl
    ElMessage.error(error?.message || '头像上传失败')
  } finally {
    uploading.value = false
    if (previewUrl.value !== localPreviewUrl) {
      revokePreviewObjectUrl(localPreviewUrl)
    }
  }
}

const size = computed(() => props.size)
</script>

<style scoped>
.avatar-upload-card {
  display: inline-flex;
  cursor: pointer;
}

.avatar-upload-card :deep(.el-upload) {
  display: inline-flex;
  cursor: pointer;
}

.avatar-upload-card__surface {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease, filter 0.2s ease;
}

.avatar-upload-card__surface:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(31, 52, 87, 0.16);
}

.avatar-upload-card__surface.is-uploading,
.avatar-upload-card__surface.is-disabled {
  cursor: not-allowed;
  filter: grayscale(0.1);
}

.avatar-upload-card__avatar {
  /* 保持外层容器不变，内层头像放大并留出均匀留白（6px） */
  width: calc(100% - 12px) !important;
  height: calc(100% - 12px) !important;
  border-radius: 50%;
  display: block;
  overflow: hidden;
}

.avatar-upload-card__avatar :deep(.el-avatar__img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
}

.avatar-upload-card__overlay,
.avatar-upload-card__loading {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  border-radius: 50%;
}

.avatar-upload-card__overlay {
  gap: 4px;
  padding: 16px;
  background: rgba(18, 34, 60, 0.62);
  color: #fff;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.avatar-upload-card__surface:hover .avatar-upload-card__overlay,
.avatar-upload-card__surface.is-uploading .avatar-upload-card__overlay {
  opacity: 1;
}

.avatar-upload-card__overlay-title {
  font-size: 14px;
  font-weight: 700;
}

.avatar-upload-card__overlay-tip {
  font-size: 11px;
  line-height: 1.5;
  opacity: 0.92;
}

.avatar-upload-card__loading {
  background: rgba(18, 34, 60, 0.48);
  color: #fff;
  font-size: 24px;
}
</style>
