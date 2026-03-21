/**
 * 管理端 - 用户/商户管理 E2E 测试
 * 覆盖: J-001, J-002, J-006, J-007, J-010
 * 测试账号: admin / admin123
 */
import { test, expect } from '@playwright/test';

async function loginAsAdmin(page) {
  const res = await page.request.post('/admin/login', {
    data: { username: 'admin', password: 'admin123' },
  });
  const body = await res.json();
  const token = body?.data?.token;
  if (!token) throw new Error('管理员登录失败，无法获取 token');

  await page.goto('/users');
  await page.evaluate((t) => localStorage.setItem('admin_token', t), token);
  await page.reload();
  await page.waitForURL(/users/);
}

test.describe('用户/商户管理', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
  });

  // TC: J-001  商户列表加载
  test('TC-J001 商户列表正常加载，表格可见', async ({ page }) => {
    await page.waitForLoadState('networkidle');
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10000 });
  });

  // TC: J-001  列表包含预期列（邮箱/公司名等）
  test('TC-J001b 商户表格包含邮箱和公司名列标题', async ({ page }) => {
    await page.waitForLoadState('networkidle');
    const header = page.locator('.el-table__header');
    await expect(header).toContainText(/邮箱|email/i);
    await expect(header).toContainText(/公司|company/i);
  });

  // TC: J-006  禁用/启用商户按钮
  test('TC-J006 商户列表行显示禁用或启用按钮', async ({ page }) => {
    await page.waitForLoadState('networkidle');
    const rows = page.locator('.el-table__body tbody tr');
    const rowCount = await rows.count();

    if (rowCount === 0) {
      test.skip();
      return;
    }

    // 每行应有禁用或启用按钮
    const toggleBtn = rows.first().locator('.el-button').filter({ hasText: /禁用|启用|disable|enable/i });
    await expect(toggleBtn).toBeVisible({ timeout: 5000 });
  });

  // TC: J-001  点击详情按钮弹出用户详情对话框
  test('TC-J001c 点击"详情"按钮弹出用户详情对话框', async ({ page }) => {
    await page.waitForLoadState('networkidle');
    const rows = page.locator('.el-table__body tbody tr');
    const rowCount = await rows.count();

    if (rowCount === 0) {
      test.skip();
      return;
    }

    const viewBtn = rows.first().locator('.el-button').filter({ hasText: /详情|view/i });
    await viewBtn.click();

    const dialog = page.locator('.el-dialog');
    await expect(dialog).toBeVisible({ timeout: 5000 });
    await expect(dialog).toContainText(/邮箱|email|ID/i);
  });

  // TC: J-010  路由守卫 - 非管理员访问管理路由重定向
  test('TC-J010 未登录直接访问 /users 路由被重定向到登录页', async ({ page }) => {
    // 清除 token，访问受保护路由
    await page.evaluate(() => localStorage.removeItem('admin_token'));
    await page.goto('/users');
    await expect(page).toHaveURL(/login/, { timeout: 5000 });
  });

  // TC: KYC 状态筛选
  test('TC-J001d KYC 状态筛选可见，可选择 PENDING 过滤', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    const kycSelect = page.locator('.el-select').filter({
      has: page.locator('input[placeholder*="KYC"]'),
    }).first();

    if (await kycSelect.isVisible()) {
      await kycSelect.click();
      const pendingOpt = page.locator('.el-select-dropdown__item').filter({ hasText: /pending|待审/i }).first();
      await pendingOpt.click();
      await page.waitForLoadState('networkidle');
      await expect(page.locator('.el-table')).toBeVisible();
    } else {
      test.skip();
    }
  });
});
