<template>
  <router-view></router-view>
</template>

<style>
:root {
  --color-primary: #F97316;
  --color-bg: #F9FAFB;
  --color-text: #111111;
  --color-text-secondary: #6B7280;
  --color-border: #E5E7EB;
}

body {
  margin: 0;
  font-family: Inter, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  background-color: var(--color-bg);
  color: var(--color-text);
}

/* Element Plus overrides */
.el-button--primary {
  --el-button-bg-color: var(--color-primary);
  --el-button-border-color: var(--color-primary);
  --el-button-hover-bg-color: #ea6c0a;
  --el-button-hover-border-color: #ea6c0a;
  --el-button-active-bg-color: #c2560a;
}

.el-pagination.is-background .el-pager li.is-active {
  background-color: var(--color-primary);
}

.el-table th.el-table__cell {
  background-color: #F9FAFB;
  color: var(--color-text-secondary);
  font-weight: 500;
  font-size: 13px;
  border-bottom: 1px solid var(--color-border);
}

.el-table td.el-table__cell {
  border-bottom: 1px solid #F3F4F6;
  font-size: 14px;
}

.el-table--border .el-table__cell {
  border-right: none;
}

.el-table--border {
  border: 1px solid var(--color-border);
  border-radius: 12px;
  overflow: hidden;
}

.el-card {
  border-radius: 12px;
  border: 1px solid var(--color-border);
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

.app-container {
  padding: 24px;
}

.filter-container {
  display: flex;
  gap: 8px;
  align-items: center;
}

.el-main {
  background-color: var(--color-bg);
}
</style>
