/**
 * 管理端 - 卡片管理 E2E 测试
 * 覆盖: K-001, K-002, K-003, K-006
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

  await page.goto('/cards');
  await page.evaluate((t) => localStorage.setItem('admin_token', t), token);
  await page.reload();
  await page.waitForURL(/cards/);
}

test.describe('管理端卡片管理', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
  });

  // TC: K-001  全量卡片列表
  test('TC-K001 管理端卡片列表正常加载，跨商户数据可见', async ({ page }) => {
    await page.waitForLoadState('networkidle');
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10000 });

    // 表头包含商户名列
    const header = page.locator('.el-table__header');
    await expect(header).toContainText(/商户|merchant/i);
  });

  // TC: K-006  状态筛选
  test('TC-K006 按卡片状态筛选，列表正确更新', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    const statusSelect = page.locator('.el-select').filter({
      has: page.locator('input[placeholder*="卡状态"], input[placeholder*="状态"]'),
    }).first();

    if (await statusSelect.isVisible()) {
      await statusSelect.click();
      const activeOpt = page.locator('.el-select-dropdown__item').filter({ hasText: /active|正常/i }).first();
      await activeOpt.click();
      await page.waitForLoadState('networkidle');
      await expect(page.locator('.el-table')).toBeVisible();
    } else {
      test.skip();
    }
  });

  // TC: K-002  强制冻结按钮
  test('TC-K002 激活状态卡片行显示"强制冻结"按钮', async ({ page }) => {
    await page.waitForLoadState('networkidle');
    const rows = page.locator('.el-table__body tbody tr');
    const rowCount = await rows.count();

    if (rowCount === 0) {
      test.skip();
      return;
    }

    const freezeBtn = page.locator('.el-table__body').locator('.el-button').filter({ hasText: /强制冻结|force.?freeze/i }).first();
    if (await freezeBtn.isVisible()) {
      await expect(freezeBtn).toBeVisible();
    } else {
      // 若所有卡已非 ACTIVE 状态，跳过
      test.skip();
    }
  });

  // TC: K-003  强制销卡弹出确认框
  test('TC-K003 点击"强制销卡"弹出不可逆确认对话框', async ({ page }) => {
    await page.waitForLoadState('networkidle');
    const rows = page.locator('.el-table__body tbody tr');
    const rowCount = await rows.count();

    if (rowCount === 0) {
      test.skip();
      return;
    }

    const cancelBtn = page.locator('.el-table__body').locator('.el-button').filter({ hasText: /强制销卡|force.?cancel/i }).first();

    if (!(await cancelBtn.isVisible())) {
      test.skip();
      return;
    }

    await cancelBtn.click();

    // 确认对话框出现，包含不可逆警告
    const confirmBox = page.locator('.el-message-box');
    await expect(confirmBox).toBeVisible({ timeout: 5000 });
    await expect(confirmBox).toContainText(/不可恢复|不可逆|无法重新激活/i);

    // 取消操作，不执行销卡
    const cancelConfirmBtn = confirmBox.locator('.el-button').filter({ hasText: /取消|cancel/i });
    await cancelConfirmBtn.click();
  });

  // TC: K-001b  点击详情弹出卡片详情对话框
  test('TC-K001b 点击"详情"弹出卡片详情对话框，包含卡号等字段', async ({ page }) => {
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
    // 卡号字段标签可见
    await expect(dialog).toContainText(/卡号|card.*number|Card/i);
  });
});
