<template>
  <div class="sidebar-wrapper">
    <div class="sidebar-logo">
      <img src="@/assets/logo/kimoox-icon.svg" alt="kimoox" class="logo-icon" />
      <span class="logo-text">kimoox</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      router
      class="sidebar-menu"
    >
      <el-menu-item index="/dashboard">
        <span class="menu-icon">📊</span>
        <span>仪表盘</span>
      </el-menu-item>
      <el-menu-item index="/users">
        <span class="menu-icon">👥</span>
        <span>用户管理</span>
      </el-menu-item>
      <el-menu-item index="/kyc">
        <span class="menu-icon">✓</span>
        <span>KYC 审核</span>
      </el-menu-item>
      <el-menu-item index="/cards">
        <span class="menu-icon">💳</span>
        <span>卡片管理</span>
      </el-menu-item>
      <el-menu-item index="/card-bins">
        <span class="menu-icon">🔢</span>
        <span>BIN 配置</span>
      </el-menu-item>
      <el-menu-item index="/transactions">
        <span class="menu-icon">💰</span>
        <span>交易记录</span>
      </el-menu-item>
      <el-menu-item index="/fee-config">
        <span class="menu-icon">⚙️</span>
        <span>费率配置</span>
      </el-menu-item>
      <el-menu-item index="/risk-config">
        <span class="menu-icon">🛡️</span>
        <span>风控配置</span>
      </el-menu-item>
      <el-menu-item index="/logs">
        <span class="menu-icon">📝</span>
        <span>系统日志</span>
      </el-menu-item>
      <el-menu-item index="/settings">
        <span class="menu-icon">⚙️</span>
        <span>系统设置</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.sidebar-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #ffffff 0%, #fafafa 100%);
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  border-bottom: 1px solid #F3F4F6;
  background: #fff;
}

.logo-icon {
  height: 36px;
  width: 36px;
  object-fit: contain;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #111827;
  letter-spacing: -0.5px;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  background: transparent;
  padding: 12px;
}

.sidebar-menu :deep(.el-menu-item) {
  border-radius: 10px;
  margin-bottom: 4px;
  color: #6B7280;
  font-size: 14px;
  font-weight: 500;
  height: 48px;
  line-height: 48px;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 12px;
}

.menu-icon {
  font-size: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: linear-gradient(135deg, #FFF7ED 0%, #FFEDD5 100%);
  color: #F97316;
  transform: translateX(4px);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #F97316 0%, #EA580C 100%);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);
}

.sidebar-menu :deep(.el-menu-item.is-active .menu-icon) {
  filter: brightness(0) invert(1);
}
</style>
