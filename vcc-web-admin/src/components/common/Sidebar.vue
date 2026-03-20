<template>
  <el-menu
    :default-active="activeMenu"
    router
    class="sidebar-menu"
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409EFF"
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
}
</style>
