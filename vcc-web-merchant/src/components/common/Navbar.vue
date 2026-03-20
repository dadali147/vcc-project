<template>
  <nav class="navbar">
    <div class="navbar-left">
      <img src="@/assets/logo/kimoox-logo-text.png" alt="kimoox" class="logo-img" />
    </div>
    <div class="navbar-right">
      <div class="navbar-menu">
        <!-- Language Selector -->
        <el-dropdown>
          <span class="el-dropdown-link">
            {{ currentLanguage.toUpperCase() }}
            <el-icon class="el-icon--right">
              <ArrowDown />
            </el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="setLanguage('en')">English</el-dropdown-item>
              <el-dropdown-item @click="setLanguage('zh')">中文</el-dropdown-item>
              <el-dropdown-item @click="setLanguage('zh_TW')">繁体中文</el-dropdown-item>
              <el-dropdown-item @click="setLanguage('vi')">Tiếng Việt</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- User Menu -->
        <el-dropdown>
          <span class="el-dropdown-link user-menu">
            <el-icon><User /></el-icon>
            {{ authStore.user?.name || 'User' }}
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>{{ $t('common.profile') }}</el-dropdown-item>
              <el-dropdown-item>{{ $t('common.settings') }}</el-dropdown-item>
              <el-divider margin="5px 0" />
              <el-dropdown-item @click="logout">{{ $t('common.logout') }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAuthStore } from '@/stores/auth'

import { ArrowDown, User } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const userStore = useUserStore()
const authStore = useAuthStore()
const i18n = useI18n()

const currentLanguage = computed(() => userStore.language)

function setLanguage(lang) {
  userStore.setLanguage(lang)
  i18n.locale.value = lang
}

function logout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  padding: 0 24px;
  background: white;
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 10;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo-img {
  height: 32px;
  width: auto;
  object-fit: contain;
}


.navbar-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.navbar-menu {
  display: flex;
  align-items: center;
  gap: 16px;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.el-dropdown-link:hover {
  background-color: #f3f4f6;
}

.user-menu {
  gap: 8px;
}

@media (max-width: 768px) {
  .navbar {
    padding: 0 16px;
  }

  .app-title {
    display: none;
  }

  .navbar-menu {
    gap: 8px;
  }
}
</style>
