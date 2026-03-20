<template>
  <el-menu
    :default-active="activeMenu"
    router
    class="sidebar-menu"
  >
    <el-menu-item index="/dashboard">仪表盘</el-menu-item>
    <el-menu-item index="/users">用户管理</el-menu-item>
    <el-menu-item index="/kyc">KYC 审核</el-menu-item>
    <el-menu-item index="/cards">卡管理</el-menu-item>
    <el-menu-item index="/card-bins">BIN 管理</el-menu-item>
    <el-menu-item index="/transactions">交易记录</el-menu-item>
    <el-menu-item index="/fee-config">费率配置</el-menu-item>
    <el-menu-item index="/risk-config">风控配置</el-menu-item>
    <el-menu-item index="/logs">系统日志</el-menu-item>
    <el-menu-item index="/settings">系统设置</el-menu-item>
  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.sidebar-menu {
  height: 100%;
  border-right: none;
  background-color: #fff;
  padding: 8px 12px;
}

.sidebar-menu :deep(.el-menu-item) {
  border-radius: 8px;
  margin-bottom: 2px;
  color: #6B7280;
  font-size: 14px;
  height: 44px;
  line-height: 44px;
  transition: background-color 0.15s, color 0.15s;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: #FFF7ED;
  color: #F97316;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: #FFF7ED;
  color: #F97316;
  font-weight: 500;
}
</style>
