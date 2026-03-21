/**
 * 管理端 - 登录 E2E 测试
 * 覆盖: A-001, A-002, J-011
 * 测试账号: admin / admin123
 */
import { test, expect } from '@playwright/test';

test.describe('管理端登录', () => {
  // TC: J-011 / A-001  正常登录
  test('TC-J011 管理员正确账号密码登录，跳转 dashboard', async ({ page }) => {
    await page.goto('/login');
    await expect(page.locator('input[type="email"], input[name="username"]').first()).toBeVisible();

    await page.locator('input[type="email"], input[name="username"]').first().fill('admin');
    await page.locator('input[type="password"]').fill('admin123');
    await page.click('.login-btn, button[type="submit"], .el-button--primary');

    await expect(page).toHaveURL(/dashboard/, { timeout: 10000 });
  });

  // TC: A-002  错误密码
  test('TC-A002 错误密码登录显示错误提示，不进入 dashboard', async ({ page }) => {
    await page.goto('/login');
    await page.locator('input[type="email"], input[name="username"]').first().fill('admin');
    await page.locator('input[type="password"]').fill('wrongpassword');
    await page.click('.login-btn, button[type="submit"], .el-button--primary');

    await expect(page.locator('.el-message--error')).toBeVisible({ timeout: 5000 });
    await expect(page).not.toHaveURL(/dashboard/);
  });
});
