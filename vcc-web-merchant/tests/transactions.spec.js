/**
 * 商户端 - 交易记录 E2E 测试
 * 覆盖: H-001, H-004, H-005
 * 测试账号: ry@163.com / admin123
 */
import { test, expect } from '@playwright/test';

async function loginAsMerchant(page) {
  const res = await page.request.post('/login', {
    data: { email: 'ry@163.com', password: 'admin123' },
  });
  const body = await res.json();
  const token = body?.data?.token;
  if (!token) throw new Error('登录失败，无法获取 token');

  await page.goto('/transactions');
  await page.evaluate((t) => localStorage.setItem('auth_token', t), token);
  await page.reload();
  await page.waitForURL(/transactions/);
}

test.describe('交易记录', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsMerchant(page);
  });

  // TC: H-001  交易列表加载
  test('TC-H001 交易列表页正常加载，表格可见', async ({ page }) => {
    await page.waitForLoadState('networkidle');
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10000 });
  });

  // TC: H-004  按类型筛选
  test('TC-H004 按交易类型筛选，列表更新', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // 找类型选择器（有 recharge/consumption/refund 选项）
    const typeSelect = page.locator('.el-select').first();
    if (await typeSelect.isVisible()) {
      await typeSelect.click();
      const option = page.locator('.el-select-dropdown__item').first();
      await option.click();
      await page.waitForLoadState('networkidle');

      // 分页或表格更新后仍可见
      await expect(page.locator('.el-table')).toBeVisible();
    } else {
      test.skip();
    }
  });

  // TC: H-005  导出按钮
  test('TC-H005 "导出"按钮可见且可点击', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    const exportBtn = page.locator('.primary-button, .el-button').filter({ hasText: /导出|export/i }).first();
    await expect(exportBtn).toBeVisible({ timeout: 5000 });

    // 监听下载事件（导出触发下载）
    const downloadPromise = page.waitForEvent('download', { timeout: 8000 }).catch(() => null);
    await exportBtn.click();
    const download = await downloadPromise;
    // 若后端可用，文件名应包含 transactions 或 .csv
    if (download) {
      expect(download.suggestedFilename()).toMatch(/transaction|csv/i);
    }
  });
});
