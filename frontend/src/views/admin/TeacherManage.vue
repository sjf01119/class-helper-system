<template>
  <div class="teacher-manage">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="关键字">
          <el-input 
            v-model="queryParams.keyword" 
            placeholder="用户名/姓名" 
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
            <el-icon><Plus /></el-icon>新增教师
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        :data="teacherList" 
        v-loading="loading" 
        border 
        stripe
        highlight-current-row
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="realName" label="真实姓名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column label="管理班级" width="220">
          <template #default="{ row }">
            <el-tooltip :content="row.className" placement="top" :disabled="!row.className">
              <div class="class-names-cell">{{ row.className || '未关联班级' }}</div>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="班主任班级" width="220">
          <template #default="{ row }">
            <el-tooltip :content="row.headTeacherClassName" placement="top" :disabled="!row.headTeacherClassName">
              <div class="class-names-cell">{{ row.headTeacherClassName || '未设置' }}</div>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              active-color="#67C23A"
              inactive-color="#F56C6C"
              @change="(val: number) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="warning" @click="handleSetHeadTeacher(row)">
              设为班主任
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
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
      width="520px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" :disabled="isEdit" placeholder="请输入用户名" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            :placeholder="isEdit ? '留空则不修改原密码' : '默认123456'"
            show-password
          />
          <span class="form-tip">{{ isEdit ? '留空不修改原密码' : '不填写则默认为 123456' }}</span>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入真实姓名" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" maxlength="50" />
        </el-form-item>
        <el-form-item label="管理班级">
          <el-select
            v-model="formData.classIds"
            multiple
            filterable
            clearable
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择管理班级"
            style="width: 100%"
          >
            <el-option v-for="cls in classList" :key="cls.id" :label="cls.className" :value="cls.id" />
          </el-select>
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

    <el-dialog
      title="设置班主任"
      v-model="headTeacherDialogVisible"
      width="480px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="headTeacherForm" label-width="96px">
        <el-form-item label="教师姓名">
          <el-input :model-value="headTeacherTarget?.realName || '-'" disabled />
        </el-form-item>
        <el-form-item label="选择班级" required>
          <el-select
            v-model="headTeacherForm.classId"
            placeholder="请选择班级"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="cls in classList"
              :key="cls.id"
              :label="getHeadTeacherOptionLabel(cls)"
              :value="cls.id"
              :disabled="Boolean(cls.teacherId && cls.teacherId !== headTeacherForm.teacherId)"
            />
          </el-select>
          <span class="form-tip">已绑定其他教师的班级不可重复设置为班主任班级</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="headTeacherDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="headTeacherSubmitting" @click="handleBindHeadTeacher">确定绑定</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getUserList, addUser, updateUser, deleteUser, type UserVO, type UserDTO } from '@/api/user'
import { getAllClasses, bindHeadTeacher, type ClassVO } from '@/api/class'

const loading = ref(false)
const submitLoading = ref(false)
const teacherList = ref<UserVO[]>([])
const classList = ref<ClassVO[]>([])
const total = ref(0)

const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  role: 'teacher',
  status: undefined as number | undefined
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑教师' : '新增教师')
const formRef = ref()
const headTeacherDialogVisible = ref(false)
const headTeacherSubmitting = ref(false)
const headTeacherTarget = ref<UserVO>()

const formData = reactive<UserDTO>({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  roles: ['teacher'],
  classIds: [],
  status: 1
})

const headTeacherForm = reactive({
  teacherId: undefined as number | undefined,
  classId: undefined as number | undefined
})

const validateOptionalPhone = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!value || !value.trim()) {
    callback()
    return
  }
  if (!/^1[3-9]\d{9}$/.test(value.trim())) {
    callback(new Error('手机号格式不正确'))
    return
  }
  callback()
}

const validateOptionalPassword = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!value || !value.trim()) {
    callback()
    return
  }
  if (value.trim().length < 6 || value.trim().length > 20) {
    callback(new Error('密码长度必须在6-20位'))
    return
  }
  callback()
}

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{3,20}$/, message: '用户名只能包含字母、数字、下划线，长度3-20', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { validator: validateOptionalPhone, trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { validator: validateOptionalPassword, trigger: 'blur' }
  ]
}

const buildSubmitPayload = (): UserDTO => ({
  ...formData,
  password: formData.password?.trim() || undefined,
  phone: formData.phone?.trim() || undefined,
  email: formData.email?.trim() || undefined,
  classIds: (formData.classIds || []).filter((id): id is number => typeof id === 'number')
})

const formatTime = (time?: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const getHeadTeacherOptionLabel = (cls: ClassVO) => {
  if (cls.teacherId && cls.teacherId !== headTeacherForm.teacherId) {
    return `${cls.className}（已绑定：${cls.headTeacherName || '其他教师'}）`
  }
  return cls.className
}

const loadTeacherList = async () => {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    teacherList.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const loadClassOptions = async () => {
  classList.value = await getAllClasses()
}

const handleSearch = () => {
  queryParams.current = 1
  loadTeacherList()
}

const handleReset = () => {
  queryParams.keyword = ''
  queryParams.status = undefined
  queryParams.current = 1
  loadTeacherList()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, {
    id: undefined,
    username: '',
    password: '',
    realName: '',
    phone: '',
    email: '',
    roles: ['teacher'],
    classIds: [],
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row: UserVO) => {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    username: row.username,
    password: '',
    realName: row.realName,
    phone: row.phone || '',
    email: row.email || '',
    roles: row.roles || ['teacher'],
    classIds: row.classIds || [],
    status: row.status
  })
  dialogVisible.value = true
}

const handleSetHeadTeacher = (row: UserVO) => {
  headTeacherTarget.value = row
  headTeacherForm.teacherId = row.id
  headTeacherForm.classId = row.headTeacherClassId
  headTeacherDialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    const payload = buildSubmitPayload()
    if (isEdit.value) {
      await updateUser({ ...payload, id: formData.id })
      ElMessage.success('更新成功')
    } else {
      await addUser(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadTeacherList()
  } catch (error) {
    console.error('保存失败', error)
  } finally {
    submitLoading.value = false
  }
}

const handleBindHeadTeacher = async () => {
  if (!headTeacherForm.teacherId) {
    ElMessage.warning('未选择教师')
    return
  }
  if (!headTeacherForm.classId) {
    ElMessage.warning('请选择班级')
    return
  }

  headTeacherSubmitting.value = true
  try {
    await bindHeadTeacher({
      teacherId: headTeacherForm.teacherId,
      classId: headTeacherForm.classId
    })
    ElMessage.success('班主任绑定成功')
    headTeacherDialogVisible.value = false
    await Promise.all([loadTeacherList(), loadClassOptions()])
  } catch (error) {
    console.error('班主任绑定失败', error)
  } finally {
    headTeacherSubmitting.value = false
  }
}

const handleStatusChange = async (row: UserVO, val: number) => {
  try {
    await updateUser({
      id: row.id,
      username: row.username,
      realName: row.realName,
      roles: row.roles,
      classIds: row.classIds || [],
      status: val
    } as UserDTO)
    if (val === 1) {
      ElMessage.success('已启用')
    } else {
      ElMessage.error('已禁用')
    }
  } catch (error) {
    row.status = val === 1 ? 0 : 1
    console.error('状态更新失败', error)
  }
}

const handleDelete = (row: UserVO) => {
  ElMessageBox.confirm(`确定删除教师 "${row.realName}" 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteUser(row.id)
    ElMessage.error('删除成功')
    loadTeacherList()
  }).catch(() => {})
}

const handleSizeChange = (val: number) => {
  queryParams.size = val
  loadTeacherList()
}

const handleCurrentChange = (val: number) => {
  queryParams.current = val
  loadTeacherList()
}

onMounted(() => {
  loadClassOptions()
  loadTeacherList()
})
</script>

<style scoped>
.teacher-manage {
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

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.class-names-cell {
  width: 100%;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 18px;
  max-height: 36px;
  word-break: break-all;
}

:deep(.el-dialog__body .el-select) {
  width: 100%;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}
</style>
