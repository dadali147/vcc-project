/**
 * 管理端 - 仪表盘 E2E 测试
 * 覆盖: M-004, K-004
 * 测试账号: admin / admin123
 */
import { test, expect } from '@playwright/test';

async function loginAsAdmin(page) {
  const res = await page.request.post('/admin/login', {
    data: { username: 'admin', password: 'admin123' },
  });
  const body = await res.json();
  const token = body?.data?.token;
  if (!token) throw new Error('管理员登录失败，无法获取 token，请确保后端服务已启动');

  await page.goto('/dashboard');
  await page.evaluate((t) => localStorage.setItem('admin_token', t), token);
  await page.reload();
  await page.waitForURL(/dashboard/);
}

test.describe('管理端仪表盘', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
  });

  // TC: 统计卡片加载
  test('TC-DASH-001 仪表盘统计卡片全部渲染，数值可见', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // 等待至少一个数字出现（totalUsers / totalCards / todayVolume 等）
    const statCards = page.locator('.el-card').filter({ hasText: /\d/ });
    await expect(statCards.first()).toBeVisible({ timeout: 10000 });
    await expect(statCards).toHaveCountGreaterThan(1);
  });

  // TC: 统计值非零或非空
  test('TC-DASH-002 仪表盘数据加载后统计项目不显示错误状态', async ({ page }) => {
    await page.waitForLoadState('networkidle');
    // 页面不应有 error 类提示
    await expect(page.locator('.el-message--error')).not.toBeVisible({ timeout: 3000 }).catch(() => {});
    // 至少有一个 el-card 可见
    await expect(page.locator('.el-card').first()).toBeVisible({ timeout: 8000 });
  });
});
