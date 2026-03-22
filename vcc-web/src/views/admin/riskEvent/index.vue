<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="事件状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable>
          <el-option label="待处理" value="PENDING" />
          <el-option label="已处理" value="RESOLVED" />
          <el-option label="已忽略" value="IGNORED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table :data="eventList" v-loading="loading">
      <el-table-column label="事件ID" prop="id" width="80" align="center" />
      <el-table-column label="风险类型" prop="riskType" width="120" align="center">
        <template #default="scope">
          <el-tag type="danger">{{ scope.row.riskType || '未知' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="关联卡号" prop="cardNoMask" min-width="180" />
      <el-table-column label="触发详情" prop="triggerDetail" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" prop="status" width="100" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 'PENDING'" type="warning">待处理</el-tag>
          <el-tag v-else-if="scope.row.status === 'RESOLVED'" type="success">已处理</el-tag>
          <el-tag v-else-if="scope.row.status === 'IGNORED'" type="info">已忽略</el-tag>
          <el-tag v-else>{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理结果" prop="handleResult" min-width="150" show-overflow-tooltip />
      <el-table-column label="触发时间" prop="createTime" min-width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button v-if="scope.row.status === 'PENDING'" link type="success" icon="Check" @click="handleResolve(scope.row)">处理</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 详情对话框 -->
    <el-dialog title="风控事件详情" v-model="detailOpen" width="600px" append-to-body>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="事件ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="风险类型">{{ detailData.riskType }}</el-descriptions-item>
        <el-descriptions-item label="关联卡号">{{ detailData.cardNoMask }}</el-descriptions-item>
        <el-descriptions-item label="触发详情">{{ detailData.triggerDetail }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailData.status }}</el-descriptions-item>
        <el-descriptions-item label="处理结果">{{ detailData.handleResult || '-' }}</el-descriptions-item>
        <el-descriptions-item label="触发时间">{{ parseTime(detailData.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ parseTime(detailData.updateTime) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 处理对话框 -->
    <el-dialog title="处理风控事件" v-model="resolveOpen" width="500px" append-to-body>
      <el-form ref="resolveFormRef" :model="resolveForm" :rules="resolveRules" label-width="100px">
        <el-form-item label="处理方式" prop="status">
          <el-select v-model="resolveForm.status" placeholder="请选择">
            <el-option label="已处理" value="RESOLVED" />
            <el-option label="已忽略" value="IGNORED" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理结果" prop="handleResult">
          <el-input v-model="resolveForm.handleResult" type="textarea" :rows="3" placeholder="请输入处理结果说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resolveOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitResolve">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AdminRiskEvent">
import { listRiskEvent, getRiskEvent, handleRiskEvent } from "@/api/admin/risk"

const { proxy } = getCurrentInstance()

const eventList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const detailOpen = ref(false)
const resolveOpen = ref(false)
const detailData = ref({})
const resolveForm = reactive({
  id: undefined,
  status: "RESOLVED",
  handleResult: ""
})
const resolveRules = {
  status: [{ required: true, message: "请选择处理方式", trigger: "change" }],
  handleResult: [{ required: true, message: "请输入处理结果", trigger: "blur" }]
}

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    status: undefined
  }
})
const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listRiskEvent(queryParams.value).then(response => {
    eventList.value = response.rows
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

function handleDetail(row) {
  getRiskEvent(row.id).then(response => {
    detailData.value = response.data
    detailOpen.value = true
  })
}

function handleResolve(row) {
  resolveForm.id = row.id
  resolveForm.status = "RESOLVED"
  resolveForm.handleResult = ""
  resolveOpen.value = true
}

function submitResolve() {
  proxy.$refs["resolveFormRef"].validate(valid => {
    if (valid) {
      handleRiskEvent(resolveForm.id, resolveForm).then(() => {
        proxy.$modal.msgSuccess("处理成功")
        resolveOpen.value = false
        getList()
      })
    }
  })
}

onMounted(() => {
  getList()
})
</script>
