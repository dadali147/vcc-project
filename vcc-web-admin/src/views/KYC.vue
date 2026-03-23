<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="listQuery.keyword" placeholder="用户邮箱/姓名" style="width: 200px;" @keyup.enter="handleFilter" />
      <el-select v-model="listQuery.status" placeholder="审核状态" clearable style="width: 150px; margin-left: 10px;">
        <el-option label="待审核" value="UNDER_REVIEW" />
        <el-option label="已通过" value="APPROVED" />
        <el-option label="已拒绝" value="REJECTED" />
      </el-select>
      <el-select v-model="listQuery.type" placeholder="认证类型" clearable style="width: 150px; margin-left: 10px;">
        <el-option label="个人认证" value="PERSONAL" />
        <el-option label="企业认证" value="BUSINESS" />
      </el-select>
      <el-button type="primary" @click="handleFilter" style="margin-left: 10px;">筛选</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border style="width: 100%; margin-top: 16px;">
      <el-table-column prop="id" label="申请 ID" width="100" />
      <el-table-column prop="userId" label="用户 ID" width="100" />
      <el-table-column prop="email" label="用户邮箱" min-width="160" show-overflow-tooltip />
      <el-table-column prop="realName" label="真实姓名" width="110" />
      <el-table-column prop="type" label="认证类型" width="100">
        <template #default="scope">
          {{ scope.row.type === 'PERSONAL' ? '个人' : '企业' }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="提交时间" width="160" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 'APPROVED'" type="success" size="small">已通过</el-tag>
          <el-tag v-else-if="scope.row.status === 'REJECTED'" type="danger" size="small">已拒绝</el-tag>
          <el-tag v-else-if="scope.row.status === 'UNDER_REVIEW'" type="warning" size="small">待审核</el-tag>
          <el-tag v-else type="info" size="small">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注/拒绝原因" min-width="140" show-overflow-tooltip />
      <el-table-column label="操作" width="210" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleView(scope.row)">查看</el-button>
          <el-button v-if="scope.row.status === 'UNDER_REVIEW'" size="small" type="success" :loading="scope.row._approving" @click="handleApprove(scope.row)">通过</el-button>
          <el-button v-if="scope.row.status === 'UNDER_REVIEW'" size="small" type="danger" @click="handleReject(scope.row)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container" style="margin-top: 20px" v-if="total > 0">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        v-model:current-page="listQuery.page"
        v-model:page-size="listQuery.limit"
        :page-sizes="[10, 20, 50]"
        @current-change="getList"
        @size-change="handleFilter"
      />
    </div>

    <el-dialog v-model="dialogVisible" title="KYC 详情" width="700px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="申请ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="用户邮箱">{{ currentRow.email }}</el-descriptions-item>
        <el-descriptions-item label="认证类型">{{ currentRow.type === 'PERSONAL' ? '个人认证' : '企业认证' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ currentRow.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ currentRow.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="证件号码">{{ currentRow.idNumber || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="currentRow?.idCardImage" style="margin-top: 20px;">
        <div style="font-weight: bold; margin-bottom: 10px;">证件图片:</div>
        <img :src="currentRow.idCardImage" style="max-width: 100%; border: 1px solid #ddd; border-radius: 4px;" />
      </div>
    </el-dialog>

    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="500px">
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef" label-width="80px">
        <el-form-item label="拒绝原因" prop="reason">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import client from '@/api/client'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const listQuery = ref({ page: 1, limit: 20, keyword: '', status: '', type: '' })
const dialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const currentRow = ref(null)
const rejectFormRef = ref(null)
const rejectForm = ref({ reason: '' })
const rejectRules = {
  reason: [
    { required: true, message: '拒绝原因不能为空', trigger: 'blur' },
    { min: 5, message: '拒绝原因不少于 5 个字符', trigger: 'blur' }
  ]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await client.get('/admin/merchant/list', { params: listQuery.value })
    list.value = res.rows || res.data?.items || res.data?.rows || []
    total.value = res.total || res.data?.total || 0
  } catch (e) {
    ElMessage.error('获取KYC列表失败')
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  listQuery.value.page = 1
  getList()
}

const handleView = (row) => {
  currentRow.value = row
  dialogVisible.value = true
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确认通过该KYC申请？通过后商户将获得完整功能权限。', '审核确认', { type: 'warning' })
    row._approving = true
    await client.put(`/admin/merchant/${row.userId || row.id}/approve`, { remark: '' })
    ElMessage.success('审核通过')
    getList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  } finally {
    row._approving = false
  }
}

const handleReject = (row) => {
  currentRow.value = row
  rejectForm.value.reason = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  try {
    await rejectFormRef.value.validate()
    await client.put(`/admin/merchant/${currentRow.value.userId || currentRow.value.id}/reject`, { reason: rejectForm.value.reason })
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    getList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(getList)
</script>
