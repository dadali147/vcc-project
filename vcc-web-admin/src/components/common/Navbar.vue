<template>
  <div class="navbar">
    <div class="breadcrumb">
      <img src="@/assets/logo/kimoox-logo.png" alt="kimoox" class="logo-img" />
      <span>管理后台</span>
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
  height: 50px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}
.right-menu {
  cursor: pointer;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #111;
}

.logo-img {
  height: 28px;
  width: auto;
  object-fit: contain;
}
</style>
