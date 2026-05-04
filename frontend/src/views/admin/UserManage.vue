<template>
  <div class="user-manage">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="关键字">
          <el-input v-model="queryParams.keyword" placeholder="用户名/真实姓名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="queryParams.role" placeholder="全部角色" clearable>
            <el-option label="管理员" value="admin" />
            <el-option label="教师" value="teacher" />
            <el-option label="学生" value="student" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="queryParams.classId" placeholder="全部班级" clearable>
            <el-option v-for="item in classList" :key="item.id" :label="item.className" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增用户</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table :data="userList" v-loading="loading" border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="真实姓名" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="角色" min-width="120">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role" size="small" class="role-tag">
              {{ getRoleLabel(role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="className" label="班级" min-width="120" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" size="small" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          :current-page="queryParams.current"
          :page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="formData.password" type="password" placeholder="默认123456" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="roles">
          <el-select v-model="formData.roles" multiple placeholder="请选择角色">
            <el-option label="管理员" value="admin" />
            <el-option label="教师" value="teacher" />
            <el-option label="学生" value="student" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" prop="classId" v-if="formData.roles.includes('student')">
          <el-select v-model="formData.classId" placeholder="请选择班级">
            <el-option v-for="item in classList" :key="item.id" :label="item.className" :value="item.id" />
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
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, addUser, updateUser, deleteUser, resetPassword, type UserVO, type UserDTO } from '@/api/user'
import { getAllClasses, type ClassVO } from '@/api/class'

const loading = ref(false)
const userList = ref<UserVO[]>([])
const classList = ref<ClassVO[]>([])
const total = ref(0)

const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  role: '',
  classId: undefined as number | undefined
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')
const formRef = ref()

const formData = reactive<UserDTO>({
  username: '',
  password: '',
  realName: '',
  phone: '',
  roles: [],
  classId: undefined,
  status: 1
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

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { validator: validateOptionalPhone, trigger: 'blur' }
  ],
  roles: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const buildSubmitPayload = (): UserDTO => ({
  ...formData,
  password: formData.password?.trim() || undefined,
  phone: formData.phone?.trim() || undefined
})

const getRoleLabel = (role: string) => {
  const map: Record<string, string> = {
    admin: '管理员',
    teacher: '教师',
    student: '学生'
  }
  return map[role] || role
}

const loadUserList = async () => {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    userList.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const loadClassList = async () => {
  try {
    const res = await getAllClasses()
    classList.value = res
  } catch (error) {
    console.error('加载班级列表失败', error)
  }
}

const handleSearch = () => {
  queryParams.current = 1
  loadUserList()
}

const handleReset = () => {
  queryParams.keyword = ''
  queryParams.role = ''
  queryParams.classId = undefined
  queryParams.current = 1
  loadUserList()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, {
    id: undefined,
    username: '',
    password: '',
    realName: '',
    phone: '',
    roles: [],
    classId: undefined,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row: UserVO) => {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    username: row.username,
    realName: row.realName,
    phone: row.phone || '',
    roles: row.roles,
    classId: row.classId,
    status: row.status
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    const payload = buildSubmitPayload()
    if (isEdit.value) {
      await updateUser(payload)
      ElMessage.success('更新成功')
    } else {
      await addUser(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadUserList()
  } catch (error) {
    console.error('保存失败', error)
  }
}

const handleDelete = (row: UserVO) => {
  ElMessageBox.confirm(`确定删除用户 "${row.realName}" 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    loadUserList()
  }).catch(() => {})
}

const handleResetPwd = (row: UserVO) => {
  ElMessageBox.confirm(`确定重置 "${row.realName}" 的密码吗？默认密码：123456`, '提示', {
    type: 'warning'
  }).then(async () => {
    await resetPassword(row.id)
    ElMessage.success('密码重置成功')
  }).catch(() => {})
}

const handleSizeChange = (val: number) => {
  queryParams.size = val
  loadUserList()
}

const handleCurrentChange = (val: number) => {
  queryParams.current = val
  loadUserList()
}

onMounted(() => {
  loadUserList()
  loadClassList()
})
</script>

<style scoped>
.user-manage {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.role-tag {
  margin-right: 5px;
}
</style>
