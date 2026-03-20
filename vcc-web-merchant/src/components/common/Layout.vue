<template>
  <div class="layout">
    <Navbar />
    <div class="layout-body">
      <Sidebar />
      <main class="layout-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import Navbar from './Navbar.vue'
import Sidebar from './Sidebar.vue'
</script>

<style scoped>
.layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f9fafb;
}

.layout-body {
  display: flex;
  flex: 1;
}

.layout-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

@media (max-width: 768px) {
  .layout-content {
    padding: 16px;
  }
}
</style>
