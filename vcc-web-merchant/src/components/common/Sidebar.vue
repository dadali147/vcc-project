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
  background: #111827;
  color: white;
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  position: relative;
  transition: width 0.3s ease;
  overflow-y: auto;
}

.sidebar.collapsed {
  width: 70px;
}

.sidebar-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 0 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  color: #9ca3af;
  text-decoration: none;
  border-radius: 6px;
  transition: all 0.2s;
  white-space: nowrap;
}

.nav-item:hover {
  background: #1f2937;
  color: white;
}

.nav-item.active {
  background: #1f2937;
  color: white;
  border-left: 3px solid #3B82F6;
}

.nav-icon {
  font-size: 18px;
  min-width: 24px;
}

.nav-label {
  flex: 1;
}

.sidebar.collapsed .nav-label {
  display: none;
}

.sidebar-toggle {
  position: absolute;
  right: 0;
  bottom: 16px;
  width: 32px;
  height: 32px;
  border: 1px solid #374151;
  background: #1f2937;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.sidebar-toggle:hover {
  background: #374151;
}

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: -240px;
    top: 64px;
    height: calc(100vh - 64px);
    z-index: 20;
    transition: left 0.3s ease;
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
  width: 6px;
}

.sidebar::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar::-webkit-scrollbar-thumb {
  background: #374151;
  border-radius: 3px;
}

.sidebar::-webkit-scrollbar-thumb:hover {
  background: #4b5563;
}
</style>
