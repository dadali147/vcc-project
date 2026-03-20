<template>
  <div class="navbar">
    <div class="breadcrumb">
      <img src="@/assets/logo/kimoox-logo-text.svg" alt="kimoox" class="logo-img" />
    </div>
    <div class="right-menu">
      <el-dropdown @command="handleCommand">
        <span class="el-dropdown-link">
          {{ authStore.adminInfo.email || 'Admin' }}
          <el-icon class="el-icon--right"><arrow-down /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const handleCommand = (command) => {
  if (command === 'logout') {
    authStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.navbar {
  height: 64px;
  overflow: hidden;
  position: relative;
  background: #fff;
  border-bottom: 1px solid #E5E7EB;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
}

.right-menu {
  cursor: pointer;
  font-size: 14px;
  color: #6B7280;
  transition: color 0.2s;
}

.right-menu:hover .el-dropdown-link {
  color: #111111;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #374151;
  font-size: 14px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-img {
  height: 32px;
  width: auto;
  object-fit: contain;
}
</style>
