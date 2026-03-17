<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="姓名" prop="holderName">
        <el-input v-model="queryParams.holderName" placeholder="请输入持卡人姓名" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="证件号" prop="idNumber">
        <el-input v-model="queryParams.idNumber" placeholder="请输入证件号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table :data="holderList" v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="姓名" prop="holderName" width="120" />
      <el-table-column label="证件类型" prop="idType" width="100">
        <template #default="scope">
          {{ scope.row.idType === '1' ? '身份证' : scope.row.idType === '2' ? '护照' : '其他' }}
        </template>
      </el-table-column>
      <el-table-column label="证件号" prop="idNumber" width="180" show-overflow-tooltip />
      <el-table-column label="手机号" prop="phone" width="140" />
      <el-table-column label="邮箱" prop="email" width="180" show-overflow-tooltip />
      <el-table-column label="地址" prop="address" show-overflow-tooltip />
      <el-table-column label="创建时间" prop="createTime" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form :model="form" :rules="rules" ref="holderRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="holderName">
              <el-input v-model="form.holderName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="英文姓名" prop="holderNameEn">
              <el-input v-model="form.holderNameEn" placeholder="请输入英文姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="证件类型" prop="idType">
              <el-select v-model="form.idType" placeholder="请选择" style="width: 100%">
                <el-option label="身份证" value="1" />
                <el-option label="护照" value="2" />
                <el-option label="其他" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="证件号" prop="idNumber">
              <el-input v-model="form.idNumber" placeholder="请输入证件号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MerchantCardHolder">
import { listCardHolder, getCardHolder, addCardHolder, updateCardHolder, delCardHolder } from "@/api/cardHolder"

const { proxy } = getCurrentInstance()

const holderList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    holderName: undefined,
    idNumber: undefined
  },
  rules: {
    holderName: [{ required: true, message: "姓名不能为空", trigger: "blur" }],
    idType: [{ required: true, message: "请选择证件类型", trigger: "change" }],
    idNumber: [{ required: true, message: "证件号不能为空", trigger: "blur" }],
    phone: [{ required: true, message: "手机号不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listCardHolder(queryParams.value).then(response => {
    holderList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function reset() {
  form.value = {
    holderId: undefined,
    holderName: undefined,
    holderNameEn: undefined,
    idType: '1',
    idNumber: undefined,
    phone: undefined,
    email: undefined,
    address: undefined
  }
  proxy.resetForm("holderRef")
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增持卡人"
}

function handleUpdate(row) {
  reset()
  getCardHolder(row.holderId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "编辑持卡人"
  })
}

function submitForm() {
  proxy.$refs["holderRef"].validate(valid => {
    if (valid) {
      if (form.value.holderId != undefined) {
        updateCardHolder(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addCardHolder(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const holderIds = row.holderId || ids.value
  proxy.$modal.confirm('是否确认删除所选持卡人?').then(() => {
    return delCardHolder(holderIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.holderId)
  multiple.value = !selection.length
}

onMounted(() => {
  getList()
})
</script>
