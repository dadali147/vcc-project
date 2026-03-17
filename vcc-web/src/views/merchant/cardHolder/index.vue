<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="姓名" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入持卡人姓名"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">添加持卡人</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="holderList">
      <el-table-column label="姓名" align="center" prop="name" width="120" />
      <el-table-column label="证件类型" align="center" width="100">
        <template #default="scope">
          <span>{{ scope.row.idType === 1 ? '身份证' : '护照' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="证件号码" align="center" prop="idCardMask" width="180" />
      <el-table-column label="手机号" align="center" prop="phoneMask" width="140" />
      <el-table-column label="邮箱" align="center" prop="email" width="180" />
      <el-table-column label="国家/地区" align="center" prop="country" width="120" />
      <el-table-column label="状态" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 1" type="success">正常</el-tag>
          <el-tag v-else-if="scope.row.status === 0" type="warning">待审核</el-tag>
          <el-tag v-else type="danger">已拒绝</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createdAt" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加/修改持卡人对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="holderRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入持卡人姓名" />
        </el-form-item>
        <el-form-item label="证件类型" prop="idType">
          <el-radio-group v-model="form.idType">
            <el-radio :label="1">身份证</el-radio>
            <el-radio :label="2">护照</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="证件号码" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入证件号码" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="国家/地区" prop="country">
          <el-select v-model="form.country" placeholder="请选择国家/地区" style="width: 100%">
            <el-option label="中国" value="CN" />
            <el-option label="美国" value="US" />
            <el-option label="英国" value="GB" />
            <el-option label="新加坡" value="SG" />
            <el-option label="香港" value="HK" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" type="textarea" placeholder="请输入详细地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CardHolder">
import { listCardHolder, addCardHolder, updateCardHolder, delCardHolder } from '@/api/cardHolder'

const { proxy } = getCurrentInstance()

const holderList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref('')
const open = ref(false)
const isEdit = ref(false)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null
  },
  rules: {
    name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
    idType: [{ required: true, message: '请选择证件类型', trigger: 'change' }],
    idCard: [{ required: true, message: '请输入证件号码', trigger: 'blur' }],
    phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
    email: [
      { required: true, message: '请输入邮箱', trigger: 'blur' },
      { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
    ],
    country: [{ required: true, message: '请选择国家/地区', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询持卡人列表 */
function getList() {
  loading.value = true
  listCardHolder(queryParams.value).then(response => {
    holderList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

/** 添加按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = '添加持卡人'
  isEdit.value = false
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  form.value = { ...row }
  open.value = true
  title.value = '修改持卡人'
  isEdit.value = true
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs['holderRef'].validate(valid => {
    if (valid) {
      if (isEdit.value) {
        updateCardHolder(form.value).then(response => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addCardHolder(form.value).then(response => {
          proxy.$modal.msgSuccess('添加成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    id: null,
    name: null,
    idType: 1,
    idCard: null,
    phone: null,
    email: null,
    country: null,
    address: null
  }
  proxy.resetForm('holderRef')
}

/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal.confirm('确认删除该持卡人吗？').then(() => {
    delCardHolder(row.id).then(() => {
      proxy.$modal.msgSuccess('删除成功')
      getList()
    })
  })
}

getList()
</script>
