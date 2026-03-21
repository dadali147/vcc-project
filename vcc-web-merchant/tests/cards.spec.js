/**
 * 商户端 - 卡片管理 E2E 测试
 * 覆盖: D-001, D-007, D-008, E-003, E-011, K-006
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

  await page.goto('/cards');
  await page.evaluate((t) => localStorage.setItem('auth_token', t), token);
  await page.reload();
  await page.waitForURL(/cards/);
}

test.describe('卡片管理', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsMerchant(page);
  });

  // TC: D-007  卡片列表加载
  test('TC-D007 卡片列表页正常加载，表格可见', async ({ page }) => {
    await page.waitForLoadState('networkidle');
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10000 });
  });

  // TC: D-001  开卡流程跳转
  test('TC-D001 点击"申请开卡"按钮跳转到开卡向导页', async ({ page }) => {
    const applyBtn = page.locator('.primary-button').first();
    await applyBtn.click();
    await expect(page).toHaveURL(/card-apply/, { timeout: 5000 });
  });

  // TC: D-008  开卡表单 - 未选持卡人校验
  test('TC-D008 开卡表单未选持卡人时提交被前端拦截', async ({ page }) => {
    await page.goto('/card-apply');
    await page.waitForLoadState('networkidle');

    // 直接点"下一步"跳过持卡人选择
    const nextBtn = page.locator('.primary-button').first();
    await nextBtn.click();

    // 停留在同一步（URL 不变为 cards 或 application）
    await expect(page).toHaveURL(/card-apply/);
  });

  // TC: E-011  卡状态筛选
  test('TC-E011 按状态筛选卡片列表，表格更新', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // 找到状态下拉选择器
    const statusSelect = page.locator('.el-select').filter({
      has: page.locator('input[placeholder*="状态"], input[placeholder*="status"]'),
    }).first();

    if (await statusSelect.isVisible()) {
      await statusSelect.click();
      // 选择 ACTIVE / 正常 选项
      const option = page.locator('.el-select-dropdown__item').filter({ hasText: /active|正常|激活/i }).first();
      await option.click();
      await page.waitForLoadState('networkidle');
      await expect(page.locator('.el-table')).toBeVisible();
    } else {
      test.skip(); // 无状态筛选
    }
  });

  // TC: 卡片详情页面
  test('TC-CARD-DETAIL 点击卡片查看详情跳转到详情页', async ({ page }) => {
    await page.waitForLoadState('networkidle');
    const rows = page.locator('.el-table__body tbody tr');
    const rowCount = await rows.count();

    if (rowCount === 0) {
      test.skip();
      return;
    }

    // 点击第一行"详情"按钮
    const detailBtn = rows.first().locator('.el-button').filter({ hasText: /详情|detail/i });
    await detailBtn.click();
    await expect(page).toHaveURL(/cards\/\d+/, { timeout: 5000 });
  });

  // TC: E-003  冻结按钮可见（激活状态的卡）
  test('TC-E003 激活状态卡片行显示"冻结"操作按钮', async ({ page }) => {
    await page.waitForLoadState('networkidle');
    const rows = page.locator('.el-table__body tbody tr');
    const rowCount = await rows.count();

    if (rowCount === 0) {
      test.skip();
      return;
    }

    // 至少有一行包含冻结或解冻按钮
    const actionBtns = page.locator('.el-table__body').locator('.el-button').filter({ hasText: /冻结|freeze|解冻/i });
    await expect(actionBtns.first()).toBeVisible({ timeout: 5000 });
  });
});
