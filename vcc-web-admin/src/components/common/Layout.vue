<template>
  <el-container class="app-wrapper">
    <el-aside width="240px" class="sidebar-container">
      <Sidebar />
    </el-aside>
    <el-container class="main-container">
      <el-header height="64px" style="padding: 0">
        <Navbar />
      </el-header>
      <el-main>
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'
</script>

<style scoped>
.app-wrapper {
  height: 100vh;
  width: 100%;
}
.sidebar-container {
  height: 100%;
  background-color: #fff;
  border-right: 1px solid #F3F4F6;
}
.main-container {
  height: 100%;
}
</style>
