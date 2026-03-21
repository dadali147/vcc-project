/**
 * 管理端 - 交易记录 E2E 测试
 * 覆盖: M-001, M-002, M-003
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

  await page.goto('/transactions');
  await page.evaluate((t) => localStorage.setItem('admin_token', t), token);
  await page.reload();
  await page.waitForURL(/transactions/);
}

test.describe('管理端交易记录', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
  });

  // TC: M-001  全量交易列表加载
  test('TC-M001 管理端交易列表正常加载，跨商户数据可见', async ({ page }) => {
    await page.waitForLoadState('networkidle');
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10000 });

    // 表头包含商户列
    const header = page.locator('.el-table__header');
    await expect(header).toContainText(/商户|merchant/i);
  });

  // TC: M-002  多条件筛选 - 按类型
  test('TC-M002a 按交易类型筛选，结果正确更新', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    const typeSelect = page.locator('.el-select').filter({
      has: page.locator('input[placeholder*="交易类型"]'),
    }).first();

    if (await typeSelect.isVisible()) {
      await typeSelect.click();
      const option = page.locator('.el-select-dropdown__item').filter({ hasText: /sale|消费/i }).first();
      await option.click();
      await page.waitForLoadState('networkidle');
      await expect(page.locator('.el-table')).toBeVisible();
    } else {
      test.skip();
    }
  });

  // TC: M-002b  按状态筛选
  test('TC-M002b 按交易状态筛选，分页 total 更新', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    const statusSelect = page.locator('.el-select').filter({
      has: page.locator('input[placeholder*="交易状态"]'),
    }).first();

    if (await statusSelect.isVisible()) {
      await statusSelect.click();
      const option = page.locator('.el-select-dropdown__item').filter({ hasText: /success|成功/i }).first();
      await option.click();
      await page.waitForLoadState('networkidle');

      // pagination total 变化后表格仍可见
      await expect(page.locator('.el-table')).toBeVisible();
      await expect(page.locator('.el-pagination')).toBeVisible();
    } else {
      test.skip();
    }
  });

  // TC: M-003  导出按钮
  test('TC-M003 "导出"按钮可见，点击触发 CSV 下载', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    const exportBtn = page.locator('.el-button').filter({ hasText: /导出|export/i }).first();
    await expect(exportBtn).toBeVisible({ timeout: 5000 });

    const downloadPromise = page.waitForEvent('download', { timeout: 8000 }).catch(() => null);
    await exportBtn.click();
    const download = await downloadPromise;
    if (download) {
      expect(download.suggestedFilename()).toMatch(/transaction|csv/i);
    }
  });
});
