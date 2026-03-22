<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="规则名称" prop="ruleName">
        <el-input v-model="queryParams.ruleName" placeholder="请输入规则名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="规则类型" prop="ruleType">
        <el-select v-model="queryParams.ruleType" placeholder="请选择" clearable>
          <el-option label="MCC风控" value="MCC" />
          <el-option label="地区风控" value="REGION" />
          <el-option label="频次风控" value="FREQUENCY" />
          <el-option label="金额风控" value="AMOUNT" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable>
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增规则</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table :data="ruleList" v-loading="loading">
      <el-table-column label="规则ID" prop="id" width="80" align="center" />
      <el-table-column label="规则名称" prop="ruleName" min-width="150" />
      <el-table-column label="规则类型" prop="ruleType" width="120" align="center">
        <template #default="scope">
          <el-tag>{{ scope.row.ruleType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="阈值" prop="threshold" width="120" align="center" />
      <el-table-column label="状态" prop="status" width="80" align="center">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleToggle(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="生效时间" prop="effectiveTime" min-width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.effectiveTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" min-width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogOpen" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="form.ruleType" placeholder="请选择">
            <el-option label="MCC风控" value="MCC" />
            <el-option label="地区风控" value="REGION" />
            <el-option label="频次风控" value="FREQUENCY" />
            <el-option label="金额风控" value="AMOUNT" />
          </el-select>
        </el-form-item>
        <el-form-item label="阈值" prop="threshold">
          <el-input v-model="form.threshold" placeholder="请输入阈值" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AdminRiskRule">
import { listRiskRule, getRiskRule, addRiskRule, updateRiskRule, deleteRiskRule, toggleRiskRule } from "@/api/admin/risk"

const { proxy } = getCurrentInstance()

const ruleList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const dialogOpen = ref(false)
const dialogTitle = ref("")

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    ruleName: undefined,
    ruleType: undefined,
    status: undefined
  },
  form: {}
})
const { queryParams, form } = toRefs(data)

const rules = {
  ruleName: [{ required: true, message: "规则名称不能为空", trigger: "blur" }],
  ruleType: [{ required: true, message: "请选择规则类型", trigger: "change" }],
  threshold: [{ required: true, message: "阈值不能为空", trigger: "blur" }]
}

function getList() {
  loading.value = true
  listRiskRule(queryParams.value).then(response => {
    ruleList.value = response.rows
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

function handleAdd() {
  dialogTitle.value = "新增风控规则"
  form.value = {}
  dialogOpen.value = true
}

function handleEdit(row) {
  dialogTitle.value = "编辑风控规则"
  getRiskRule(row.id).then(response => {
    form.value = response.data
    dialogOpen.value = true
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.id) {
        updateRiskRule(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          dialogOpen.value = false
          getList()
        })
      } else {
        addRiskRule(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          dialogOpen.value = false
          getList()
        })
      }
    }
  })
}

function handleToggle(row) {
  toggleRiskRule(row.id).then(() => {
    proxy.$modal.msgSuccess("状态切换成功")
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除规则 "' + row.ruleName + '"？').then(() => {
    return deleteRiskRule(row.id)
  }).then(() => {
    proxy.$modal.msgSuccess("删除成功")
    getList()
  }).catch(() => {})
}

onMounted(() => {
  getList()
})
</script>
