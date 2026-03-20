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
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 100%);
  border-bottom: 2px solid #F3F4F6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.right-menu {
  cursor: pointer;
  font-size: 14px;
  color: #6B7280;
  transition: all 0.2s ease;
  padding: 8px 16px;
  border-radius: 8px;
}

.right-menu:hover {
  background: #FFF7ED;
}

.right-menu:hover .el-dropdown-link {
  color: #F97316;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #374151;
  font-size: 14px;
  font-weight: 600;
  transition: color 0.2s ease;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-img {
  height: 36px;
  width: auto;
  object-fit: contain;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}
</style>
