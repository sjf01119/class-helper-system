<template>
  <div class="student-manage">
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
        <el-form-item label="班级">
          <el-select v-model="queryParams.classId" placeholder="全部班级" clearable style="width: 150px">
            <el-option v-for="cls in classList" :key="cls.id" :label="cls.className" :value="cls.id" />
          </el-select>
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
            <el-icon><Plus /></el-icon>新增学生
          </el-button>
          <el-button type="warning" @click="handleBatchAssign">
            <el-icon><User /></el-icon>批量分班
          </el-button>
          <el-button type="danger" @click="handleBatchDelete">
            <el-icon><Delete /></el-icon>批量删除
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        :data="studentList" 
        v-loading="loading" 
        border 
        stripe
        highlight-current-row
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="学号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="realName" label="姓名" min-width="100" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="班级" min-width="150">
          <template #default="{ row }">
            <el-tag v-if="row.className" type="primary" size="small">{{ row.className }}</el-tag>
            <el-tag v-else type="info" size="small">未分配</el-tag>
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
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button link type="warning" @click="handleAssignClass(row)">
              <el-icon><OfficeBuilding /></el-icon>分配班级
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
        <el-form-item label="学号" prop="username">
          <el-input v-model="formData.username" :disabled="isEdit" placeholder="请输入学号" maxlength="20" show-word-limit />
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
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入姓名" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="分配班级" prop="classId">
          <el-select v-model="formData.classId" clearable placeholder="请选择班级" style="width: 100%">
            <el-option label="暂不分配" :value="null" />
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

    <!-- 分配班级弹窗 -->
    <el-dialog title="分配班级" v-model="assignDialogVisible" width="400px">
      <el-form :model="assignForm" label-width="80px">
        <el-form-item label="学生">
          <span v-if="isBatchAssign">已选择 {{ selectedStudents.length }} 名学生</span>
          <span v-else>{{ currentStudent?.realName }} ({{ currentStudent?.username }})</span>
        </el-form-item>
        <el-form-item label="分配班级">
          <el-select v-model="assignForm.classId" clearable placeholder="请选择班级" style="width: 100%">
            <el-option label="取消分配" :value="null" />
            <el-option v-for="cls in classList" :key="cls.id" :label="cls.className" :value="cls.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignSubmit" :loading="assignLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight, Plus, Edit, Delete, OfficeBuilding, User } from '@element-plus/icons-vue'
import { getUserList, addUser, updateUser, deleteUser, deleteUserBatch, type UserVO, type UserDTO } from '@/api/user'
import { getAllClasses, type ClassVO } from '@/api/class'

const loading = ref(false)
const submitLoading = ref(false)
const assignLoading = ref(false)
const studentList = ref<UserVO[]>([])
const classList = ref<ClassVO[]>([])
const total = ref(0)
const selectedStudents = ref<UserVO[]>([])
const isBatchAssign = ref(false)

const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  role: 'student',
  status: undefined as number | undefined,
  classId: undefined as number | undefined
})

const dialogVisible = ref(false)
const assignDialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑学生' : '新增学生')
const formRef = ref()
const currentStudent = ref<UserVO>()

const formData = reactive<UserDTO>({
  username: '',
  password: '',
  realName: '',
  phone: '',
  roles: ['student'],
  classId: null,
  status: 1
})

const assignForm = reactive({
  ids: [] as number[],
  classId: null as number | null
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
    { required: true, message: '请输入学号', trigger: 'blur' },
    { pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_]{2,20}$/, message: '用户名只能包含中文、字母、数字、下划线，长度2-20', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { pattern: /^[\u4e00-\u9fa5a-zA-Z]{2,20}$/, message: '真实姓名只能包含中文、字母，长度2-20', trigger: 'blur' }
  ],
  phone: [
    { validator: validateOptionalPhone, trigger: 'blur' }
  ],
  password: [
    { validator: validateOptionalPassword, trigger: 'blur' }
  ]
}

const buildSubmitPayload = (): UserDTO => ({
  ...formData,
  password: formData.password?.trim() || undefined,
  phone: formData.phone?.trim() || undefined,
  classId: formData.classId ?? null
})

const formatTime = (time?: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const loadStudentList = async () => {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    studentList.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const loadClassList = async () => {
  try {
    const res = await getAllClasses()
    classList.value = res || []
  } catch (error) {
    console.error('加载班级列表失败', error)
  }
}

const handleSearch = () => {
  queryParams.current = 1
  loadStudentList()
}

const handleReset = () => {
  queryParams.keyword = ''
  queryParams.status = undefined
  queryParams.classId = undefined
  queryParams.current = 1
  loadStudentList()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, {
    id: undefined,
    username: '',
    password: '',
    realName: '',
    phone: '',
    roles: ['student'],
    classId: null,
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
    roles: row.roles || ['student'],
    classId: row.classId ?? null,
    status: row.status
  })
  dialogVisible.value = true
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
    loadStudentList()
  } catch (error) {
    console.error('保存失败', error)
  } finally {
    submitLoading.value = false
  }
}

const handleAssignClass = (row: UserVO) => {
  isBatchAssign.value = false
  currentStudent.value = row
  assignForm.ids = [row.id!]
  assignForm.classId = row.classId ?? null
  assignDialogVisible.value = true
}

const handleBatchAssign = () => {
  if (selectedStudents.value.length === 0) {
    ElMessage.warning('请先选择学生')
    return
  }
  isBatchAssign.value = true
  assignForm.ids = selectedStudents.value.map(s => s.id!)
  assignForm.classId = null
  assignDialogVisible.value = true
}

const handleAssignSubmit = async () => {
  assignLoading.value = true
  try {
    const assignTargets = isBatchAssign.value
      ? selectedStudents.value
      : (currentStudent.value ? [currentStudent.value] : [])

    if (assignTargets.length === 0) {
      ElMessage.warning('未找到可分配的学生')
      return
    }

    for (const id of assignForm.ids) {
      const target = assignTargets.find(item => item.id === id) || studentList.value.find(item => item.id === id)
      if (!target) {
        continue
      }
      await updateUser({
        id,
        username: target.username,
        realName: target.realName,
        phone: target.phone,
        roles: ['student'],
        classId: assignForm.classId,
        status: target.status
      } as UserDTO)
    }
    ElMessage.success('分配成功')
    assignDialogVisible.value = false
    selectedStudents.value = []
    loadStudentList()
    loadClassList()
  } catch (error) {
    console.error('分配失败', error)
  } finally {
    assignLoading.value = false
  }
}

const handleStatusChange = async (row: UserVO, val: number) => {
  try {
    await updateUser({
      id: row.id,
      username: row.username,
      realName: row.realName,
      roles: row.roles,
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
  ElMessageBox.confirm(`确定删除学生 "${row.realName}" 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteUser(row.id)
    ElMessage.error('删除成功')
    loadStudentList()
  }).catch(() => {})
}

const handleBatchDelete = () => {
  if (selectedStudents.value.length === 0) {
    ElMessage.warning('请先选择要删除的学生')
    return
  }

  ElMessageBox.confirm(`确定批量删除已选中的 ${selectedStudents.value.length} 名学生吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteUserBatch(selectedStudents.value.map(student => student.id))
    ElMessage.success('批量删除成功')
    selectedStudents.value = []
    loadStudentList()
    loadClassList()
  }).catch(() => {})
}

const handleSelectionChange = (selection: UserVO[]) => {
  selectedStudents.value = selection
}

const handleSizeChange = (val: number) => {
  queryParams.size = val
  loadStudentList()
}

const handleCurrentChange = (val: number) => {
  queryParams.current = val
  loadStudentList()
}

onMounted(() => {
  loadStudentList()
  loadClassList()
})
</script>

<style scoped>
.student-manage {
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

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}
</style>
