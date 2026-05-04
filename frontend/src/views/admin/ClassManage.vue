<template>
  <div class="class-manage">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="班级名称">
          <el-input 
            v-model="queryParams.keyword" 
            placeholder="请输入班级名称" 
            clearable 
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>重置
          </el-button>
          <el-button type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon>创建班级
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        :data="classList" 
        v-loading="loading" 
        border 
        stripe
        highlight-current-row
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="className" label="班级名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="邀请码" width="160" align="center">
          <template #default="{ row }">
            <div class="invite-code">
              <span class="code-text">{{ row.inviteCode || '待生成' }}</span>
              <el-button 
                link 
                type="primary" 
                size="small"
                :disabled="!row.inviteCode"
                @click="copyInviteCode(row.inviteCode)"
              >
                <el-icon><CopyDocument /></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="currentCount" label="学生数" width="90" align="center" />
        <el-table-column label="班主任" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.headTeacherName || '未设置' }}
          </template>
        </el-table-column>
        <el-table-column label="班级描述" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.description || '暂无描述' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val: number) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button link type="warning" @click="handleRegenerateCode(row)">
              <el-icon><Refresh /></el-icon>重置邀请码
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          :current-page="queryParams.current"
          :page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog 
      :title="dialogTitle" 
      v-model="dialogVisible" 
      width="550px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="90px">
        <el-form-item label="班级名称" prop="className">
          <el-input v-model="formData.className" placeholder="请输入班级名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="邀请码" prop="inviteCode">
          <div class="invite-code-input">
            <el-input
              :model-value="isEdit ? (formData.inviteCode || '系统自动生成') : '创建后自动生成'"
              disabled
              placeholder="系统自动生成"
            />
          </div>
        </el-form-item>
        <el-form-item label="班主任" prop="teacherId">
          <el-select
            v-model="formData.teacherId"
            filterable
            clearable
            placeholder="请选择班主任教师"
            style="width: 100%"
          >
            <el-option
              v-for="teacher in teacherOptions"
              :key="teacher.id"
              :label="`${teacher.realName}（${teacher.username}）`"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班级描述" prop="description">
          <el-input 
            v-model="formData.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入班级描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight, Plus, Edit, Delete, Refresh, CopyDocument } from '@element-plus/icons-vue'
import { getClassList, addClass, updateClass, deleteClass, resetInviteCode, type ClassVO, type ClassDTO } from '@/api/class'
import { getUserList, type UserVO } from '@/api/user'

const loading = ref(false)
const submitLoading = ref(false)
const classList = ref<ClassVO[]>([])
const teacherOptions = ref<UserVO[]>([])
const total = ref(0)

const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  status: undefined as number | undefined
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑班级' : '创建班级')
const formRef = ref()

const formData = reactive<ClassDTO>({
  id: undefined,
  className: '',
  description: '',
  inviteCode: '',
  teacherId: undefined,
  status: 1
})

const formRules = {
  className: [
    { required: true, message: '请输入班级名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  teacherId: [
    { required: true, message: '请选择班主任', trigger: 'change' }
  ],
  description: [
    { max: 500, message: '班级描述长度不能超过 500 个字符', trigger: 'blur' }
  ]
}

const pad = (value: number) => value.toString().padStart(2, '0')

const formatDate = (date: Date) => {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const formatTime = (time?: string) => {
  const date = time ? new Date(time) : new Date()
  if (Number.isNaN(date.getTime())) {
    return formatDate(new Date())
  }
  return formatDate(date)
}

const loadClassList = async () => {
  loading.value = true
  try {
    const res = await getClassList(queryParams)
    classList.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const loadTeacherOptions = async () => {
  const res = await getUserList({
    current: 1,
    size: 200,
    role: 'teacher',
    status: 1
  })
  teacherOptions.value = res.records || []
}

const handleSearch = () => {
  queryParams.current = 1
  loadClassList()
}

const handleReset = () => {
  queryParams.keyword = ''
  queryParams.status = undefined
  queryParams.current = 1
  loadClassList()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, {
    id: undefined,
    className: '',
    description: '',
    inviteCode: '',
    teacherId: undefined,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row: ClassVO) => {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    className: row.className,
    description: row.description || '',
    inviteCode: row.inviteCode,
    teacherId: row.teacherId,
    status: row.status
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateClass(formData)
      ElMessage.success('更新成功')
    } else {
      await addClass(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadClassList()
  } catch (error) {
    console.error('保存失败', error)
  } finally {
    submitLoading.value = false
  }
}

const handleStatusChange = async (row: ClassVO, val: number) => {
  try {
    await updateClass({
      id: row.id,
      className: row.className,
      description: row.description,
      teacherId: row.teacherId,
      status: val
    } as ClassDTO)
    ElMessage.success(val === 1 ? '已启用' : '已禁用')
  } catch (error) {
    row.status = val === 1 ? 0 : 1
    console.error('状态更新失败', error)
  }
}

const handleRegenerateCode = async (row: ClassVO) => {
  ElMessageBox.confirm(`确定为班级 "${row.className}" 重新生成邀请码吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    const res = await resetInviteCode(row.id)
    row.inviteCode = res.inviteCode
    loadClassList()
    ElMessage.success('邀请码已重置')
  }).catch(() => {})
}

const copyByExecCommand = (code: string) => {
  const textarea = document.createElement('textarea')
  textarea.value = code
  textarea.setAttribute('readonly', 'readonly')
  textarea.style.position = 'fixed'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)
  textarea.select()
  const copied = document.execCommand('copy')
  document.body.removeChild(textarea)
  return copied
}

const copyInviteCode = async (code?: string) => {
  if (!code) return
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(code)
    } else if (!copyByExecCommand(code)) {
      throw new Error('execCommand copy failed')
    }
    ElMessage.success('邀请码已复制到剪贴板')
  } catch (error) {
    console.error('复制邀请码失败', error)
    ElMessage.error('复制失败')
  }
}

const handleDelete = (row: ClassVO) => {
  ElMessageBox.confirm(`确定删除班级 "${row.className}" 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteClass(row.id)
    ElMessage.success('删除成功')
    loadClassList()
  }).catch(() => {})
}

const handleSizeChange = (val: number) => {
  queryParams.size = val
  loadClassList()
}

const handleCurrentChange = (val: number) => {
  queryParams.current = val
  loadClassList()
}

onMounted(() => {
  loadTeacherOptions()
  loadClassList()
})
</script>

<style scoped>
.class-manage {
  padding: 0;
}

.search-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.table-card {
  border-radius: 12px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.invite-code {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.code-text {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #667eea;
  letter-spacing: 2px;
}

.invite-code-input {
  width: 100%;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}
</style>
