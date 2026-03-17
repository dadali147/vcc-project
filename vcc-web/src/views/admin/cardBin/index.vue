<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="BIN名称" prop="binName">
        <el-input v-model="queryParams.binName" placeholder="请输入BIN名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="卡品牌" prop="brand">
        <el-select v-model="queryParams.brand" placeholder="全部" clearable style="width: 140px">
          <el-option label="Visa" value="Visa" />
          <el-option label="Mastercard" value="Mastercard" />
          <el-option label="UnionPay" value="UnionPay" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="启用" value="0" />
          <el-option label="禁用" value="1" />
        </el-select>
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

    <el-table :data="binList" v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="BIN名称" prop="binName" width="160" />
      <el-table-column label="BIN号" prop="binNo" width="120" />
      <el-table-column label="卡品牌" prop="brand" width="100" />
      <el-table-column label="卡类型" prop="cardType" width="80">
        <template #default="scope">
          {{ scope.row.cardType === '1' ? '储值卡' : '预算卡' }}
        </template>
      </el-table-column>
      <el-table-column label="开卡费" prop="openFee" width="100" align="right">
        <template #default="scope">
          {{ scope.row.openFee }} USD
        </template>
      </el-table-column>
      <el-table-column label="月费" prop="monthlyFee" width="100" align="right">
        <template #default="scope">
          {{ scope.row.monthlyFee }} USD
        </template>
      </el-table-column>
      <el-table-column label="交易手续费" prop="transRate" width="110" align="right">
        <template #default="scope">
          {{ scope.row.transRate }}%
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="80" align="center">
        <template #default="scope">
          <el-switch v-model="scope.row.status" active-value="0" inactive-value="1"
            @change="handleStatusChange(scope.row)" />
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
      <el-form :model="form" :rules="rules" ref="binRef" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="BIN名称" prop="binName">
              <el-input v-model="form.binName" placeholder="请输入BIN名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="BIN号" prop="binNo">
              <el-input v-model="form.binNo" placeholder="请输入BIN号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="卡品牌" prop="brand">
              <el-select v-model="form.brand" placeholder="请选择" style="width: 100%">
                <el-option label="Visa" value="Visa" />
                <el-option label="Mastercard" value="Mastercard" />
                <el-option label="UnionPay" value="UnionPay" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="卡类型" prop="cardType">
              <el-select v-model="form.cardType" placeholder="请选择" style="width: 100%">
                <el-option label="储值卡" value="1" />
                <el-option label="预算卡" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开卡费 (USD)" prop="openFee">
              <el-input-number v-model="form.openFee" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="月费 (USD)" prop="monthlyFee">
              <el-input-number v-model="form.monthlyFee" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="交易手续费%" prop="transRate">
              <el-input-number v-model="form.transRate" :min="0" :max="100" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="充值手续费%" prop="rechargeRate">
              <el-input-number v-model="form.rechargeRate" :min="0" :max="100" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="渠道" prop="channel">
          <el-input v-model="form.channel" placeholder="请输入渠道" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AdminCardBin">
import { listCardBin, getCardBin, addCardBin, updateCardBin, delCardBin, changeCardBinStatus } from "@/api/admin/cardBin"

const { proxy } = getCurrentInstance()

const binList = ref([])
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
    binName: undefined,
    brand: undefined,
    status: undefined
  },
  rules: {
    binName: [{ required: true, message: "BIN名称不能为空", trigger: "blur" }],
    binNo: [{ required: true, message: "BIN号不能为空", trigger: "blur" }],
    brand: [{ required: true, message: "请选择卡品牌", trigger: "change" }],
    cardType: [{ required: true, message: "请选择卡类型", trigger: "change" }],
    openFee: [{ required: true, message: "请输入开卡费", trigger: "blur" }],
    transRate: [{ required: true, message: "请输入交易手续费", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listCardBin(queryParams.value).then(response => {
    binList.value = response.rows
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
    binId: undefined,
    binName: undefined,
    binNo: undefined,
    brand: undefined,
    cardType: undefined,
    openFee: 0,
    monthlyFee: 0,
    transRate: 0,
    rechargeRate: 0,
    channel: undefined,
    remark: undefined
  }
  proxy.resetForm("binRef")
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增卡BIN"
}

function handleUpdate(row) {
  reset()
  getCardBin(row.binId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "编辑卡BIN"
  })
}

function submitForm() {
  proxy.$refs["binRef"].validate(valid => {
    if (valid) {
      if (form.value.binId != undefined) {
        updateCardBin(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addCardBin(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const binIds = row.binId || ids.value
  proxy.$modal.confirm('是否确认删除所选卡BIN?').then(() => {
    return delCardBin(binIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleStatusChange(row) {
  const text = row.status === "0" ? "启用" : "禁用"
  proxy.$modal.confirm('确认' + text + ' "' + row.binName + '" ?').then(() => {
    return changeCardBinStatus(row.binId, row.status)
  }).then(() => {
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(() => {
    row.status = row.status === "0" ? "1" : "0"
  })
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.binId)
  multiple.value = !selection.length
}

onMounted(() => {
  getList()
})
</script>
