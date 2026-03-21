/**
 * 商户端 - 认证模块 E2E 测试
 * 测试账号: merchant@test.com / admin123
 */
import { test, expect } from '@playwright/test';

const TEST_EMAIL = 'merchant@test.com';
const TEST_PASSWORD = 'admin123';

test.describe('商户端登录', () => {
  test('TC-A001 正确邮箱和密码登录成功，跳转 dashboard', async ({ page }) => {
    await page.goto('/login');
    await expect(page.locator('input[type="email"]')).toBeVisible();

    await page.fill('input[type="email"]', TEST_EMAIL);
    await page.fill('input[type="password"]', TEST_PASSWORD);
    await page.click('button[type="submit"], .login-button');

    await expect(page).toHaveURL(/dashboard/, { timeout: 10000 });
  });

  test('TC-A002 错误密码登录显示错误提示', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="email"]', TEST_EMAIL);
    await page.fill('input[type="password"]', 'wrongpassword');
    await page.click('button[type="submit"], .login-button');

    await expect(page.locator('.el-message--error, .error-message')).toBeVisible({ timeout: 5000 });
  });

  test('TC-A003 不存在的邮箱登录显示错误提示', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="email"]', 'nonexistent@example.com');
    await page.fill('input[type="password"]', TEST_PASSWORD);
    await page.click('button[type="submit"], .login-button');

    await expect(page.locator('.el-message--error, .error-message')).toBeVisible({ timeout: 5000 });
  });
});
