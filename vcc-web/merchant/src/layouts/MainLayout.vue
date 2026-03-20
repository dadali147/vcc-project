<template>
  <div class="flex h-screen bg-slate-50 font-sans text-slate-600">
    <!-- Sidebar -->
    <aside :class="['bg-slate-900 text-slate-300 transition-all duration-300 flex flex-col', isCollapse ? 'w-16' : 'w-64']">
      <div class="h-16 flex items-center justify-center p-4 border-b border-slate-800 shrink-0">
        <h1 v-if="!isCollapse" class="text-xl font-bold text-white tracking-wider truncate transition-opacity flex-1">Kimoox VCC</h1>
        <el-icon v-else class="text-white text-xl cursor-default"><Connection /></el-icon>
        <button @click="toggleSidebar" class="p-1 rounded-md text-slate-400 hover:text-white hover:bg-slate-800 transition-colors ml-auto" :title="isCollapse ? 'Expand sidebar' : 'Collapse sidebar'">
          <el-icon class="text-lg"><Fold v-if="!isCollapse"/><Expand v-else/></el-icon>
        </button>
      </div>
      
      <nav class="flex-1 overflow-y-auto py-4 scrollbar-thin scrollbar-thumb-slate-700 hover:scrollbar-thumb-slate-600 space-y-1 px-2">
        <router-link to="/dashboard" custom v-slot="{ isActive, navigate }">
          <a @click="navigate" :class="[isActive ? 'bg-indigo-600 text-white' : 'text-slate-300 hover:bg-slate-800 hover:text-white', 'group flex items-center px-2 py-2 text-sm font-medium rounded-md transition-colors']" :title="isCollapse ? 'Dashboard' : ''">
            <el-icon class="mr-3 flex-shrink-0 h-6 w-6 text-xl transition-colors"><DataBoard /></el-icon>
            <span v-if="!isCollapse" class="truncate">Dashboard</span>
          </a>
        </router-link>
        
        <router-link to="/cards" custom v-slot="{ isActive, navigate }">
          <a @click="navigate" :class="[isActive ? 'bg-indigo-600 text-white' : 'text-slate-300 hover:bg-slate-800 hover:text-white', 'group flex items-center px-2 py-2 text-sm font-medium rounded-md transition-colors']" :title="isCollapse ? 'Cards' : ''">
            <el-icon class="mr-3 flex-shrink-0 h-6 w-6 text-xl transition-colors"><CreditCard /></el-icon>
            <span v-if="!isCollapse" class="truncate">Cards Console</span>
          </a>
        </router-link>

        <router-link to="/users" custom v-slot="{ isActive, navigate }">
          <a @click="navigate" :class="[isActive ? 'bg-indigo-600 text-white' : 'text-slate-300 hover:bg-slate-800 hover:text-white', 'group flex items-center px-2 py-2 text-sm font-medium rounded-md transition-colors']" :title="isCollapse ? 'Cardholders' : ''">
            <el-icon class="mr-3 flex-shrink-0 h-6 w-6 text-xl transition-colors"><User /></el-icon>
            <span v-if="!isCollapse" class="truncate">Cardholders</span>
          </a>
        </router-link>
      </nav>

      <div class="p-4 border-t border-slate-800 shrink-0">
        <div class="flex items-center space-x-3 text-sm">
          <div class="w-8 h-8 rounded-full bg-slate-700 flex items-center justify-center text-slate-300 uppercase shrink-0">M</div>
          <div v-if="!isCollapse" class="truncate">
            <p class="text-slate-200 font-medium truncate">Merchant Admin</p>
            <p class="text-slate-500 text-xs truncate">admin@kimoox.com</p>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="flex-1 flex flex-col overflow-hidden">
      <header class="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-6 shrink-0 z-10">
        <div class="text-xl font-semibold text-slate-800">{{ routeName }}</div>
        <div class="flex items-center space-x-4">
          <button class="p-2 text-slate-400 hover:text-slate-600 rounded-full hover:bg-slate-100 transition-colors">
            <el-icon class="text-xl"><Bell /></el-icon>
          </button>
          
          <el-dropdown trigger="click">
            <span class="el-dropdown-link cursor-pointer flex items-center space-x-2 text-slate-600 font-medium">
              <span>Admin</span>
              <el-icon class="text-xs text-slate-400"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>Profile Settings</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">Sign out</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <div class="flex-1 overflow-auto p-6 bg-slate-50 relative">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const isCollapse = ref(false)

const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

const routeNameMap = {
  '/dashboard': 'Dashboard',
  '/cards': 'Virtual Cards',
  '/users': 'Cardholders Management'
}

const routeName = computed(() => routeNameMap[route.path] || 'Kimoox VCC')

const handleLogout = () => {
  router.push('/login')
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
