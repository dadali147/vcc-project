<template>
  <aside class="sidebar" :class="{ collapsed: userStore.sidebarCollapsed }">
    <nav class="sidebar-nav">
      <!-- Dashboard -->
      <router-link to="/dashboard" class="nav-item" :class="{ active: isActive('/dashboard') }">
        <span class="nav-icon">📊</span>
        <span class="nav-label">{{ $t('common.dashboard') }}</span>
      </router-link>

      <!-- Cardholders -->
      <router-link to="/cardholders" class="nav-item" :class="{ active: isActive('/cardholders') }">
        <span class="nav-icon">👥</span>
        <span class="nav-label">{{ $t('cardholders.title') }}</span>
      </router-link>

      <!-- Cards -->
      <router-link to="/cards" class="nav-item" :class="{ active: isActive('/cards') }">
        <span class="nav-icon">💳</span>
        <span class="nav-label">{{ $t('cards.title') }}</span>
      </router-link>

      <!-- Card Apply -->
      <router-link to="/card-apply" class="nav-item" :class="{ active: isActive('/card-apply') }">
        <span class="nav-icon">➕</span>
        <span class="nav-label">{{ $t('cardApplication.title') }}</span>
      </router-link>

      <!-- Card Applications -->
      <router-link to="/card-applications" class="nav-item" :class="{ active: isActive('/card-applications') }">
        <span class="nav-icon">📋</span>
        <span class="nav-label">{{ $t('cardApplications.title') }}</span>
      </router-link>

      <!-- Transactions -->
      <router-link to="/transactions" class="nav-item" :class="{ active: isActive('/transactions') }">
        <span class="nav-icon">💰</span>
        <span class="nav-label">{{ $t('transactions.title') }}</span>
      </router-link>

      <!-- Shared Card Limits -->
      <router-link to="/shared-card-limits" class="nav-item" :class="{ active: isActive('/shared-card-limits') }">
        <span class="nav-icon">📊</span>
        <span class="nav-label">{{ $t('sharedCardLimits.title') }}</span>
      </router-link>

      <!-- Downloads -->
      <router-link to="/downloads" class="nav-item" :class="{ active: isActive('/downloads') }">
        <span class="nav-icon">⬇️</span>
        <span class="nav-label">{{ $t('downloads.title') }}</span>
      </router-link>

      <!-- KYC -->
      <router-link to="/kyc" class="nav-item" :class="{ active: isActive('/kyc') }">
        <span class="nav-icon">✔️</span>
        <span class="nav-label">{{ $t('kyc.title') }}</span>
      </router-link>

      <!-- Profile -->
      <router-link to="/profile" class="nav-item" :class="{ active: isActive('/profile') }">
        <span class="nav-icon">⚙️</span>
        <span class="nav-label">{{ $t('common.profile') }}</span>
      </router-link>
    </nav>

    <!-- Toggle Button -->
    <button class="sidebar-toggle" @click="userStore.toggleSidebar()">
      <span>{{ userStore.sidebarCollapsed ? '▶' : '◀' }}</span>
    </button>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const isActive = (path) => {
  return route.path.startsWith(path)
}
</script>

<style scoped>
.sidebar {
  width: 240px;
  background: #ffffff;
  border-right: 1px solid #F3F4F6;
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  position: relative;
  transition: width 0.3s ease;
  overflow-y: auto;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 0 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  color: #6B7280;
  text-decoration: none;
  border-radius: 8px;
  transition: background-color 0.2s, color 0.2s;
  white-space: nowrap;
  font-size: 14px;
  font-weight: 500;
}

.nav-item:hover {
  background: #FFF7ED;
  color: #F97316;
}

.nav-item.active {
  background: #FFF7ED;
  color: #F97316;
}

.nav-icon {
  font-size: 16px;
  min-width: 20px;
  text-align: center;
}

.nav-label {
  flex: 1;
}

.sidebar.collapsed .nav-label {
  display: none;
}

.sidebar-toggle {
  position: absolute;
  right: 8px;
  bottom: 16px;
  width: 32px;
  height: 32px;
  border: 1px solid #E5E7EB;
  background: #ffffff;
  color: #6B7280;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  font-size: 12px;
}

.sidebar-toggle:hover {
  background: #FFF7ED;
  color: #F97316;
  border-color: #FDBA74;
}

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: -240px;
    top: 64px;
    height: calc(100vh - 64px);
    z-index: 20;
    transition: left 0.3s ease;
    box-shadow: 4px 0 12px rgba(0, 0, 0, 0.08);
  }

  .sidebar.show {
    left: 0;
  }

  .sidebar-toggle {
    display: none;
  }
}

/* Scrollbar */
.sidebar::-webkit-scrollbar {
  width: 4px;
}

.sidebar::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar::-webkit-scrollbar-thumb {
  background: #E5E7EB;
  border-radius: 2px;
}

.sidebar::-webkit-scrollbar-thumb:hover {
  background: #D1D5DB;
}
</style>
