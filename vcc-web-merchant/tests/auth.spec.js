/**
 * 商户端 - 认证模块 E2E 测试
 * 测试账号: merchant / admin123
 */
import { test, expect } from '@playwright/test';

const TEST_USERNAME = 'merchant';  // 用用户名登录，不是邮箱
const TEST_PASSWORD = 'admin123';

test.describe('商户端登录', () => {
  test('TC-A001 正确用户名和密码登录成功，跳转 dashboard', async ({ page }) => {
    await page.goto('/login');
    await expect(page.locator('input[type="email"], input[type="text"]')).toBeVisible();

    // 商户端登录页可能是邮箱输入框，但后端接受用户名
    await page.fill('input[type="email"], input[type="text"]', TEST_USERNAME);
    await page.fill('input[type="password"]', TEST_PASSWORD);
    await page.click('button[type="submit"], .login-button');

    await expect(page).toHaveURL(/dashboard/, { timeout: 10000 });
  });

  test('TC-A002 错误密码登录显示错误提示', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="email"], input[type="text"]', TEST_USERNAME);
    await page.fill('input[type="password"]', 'wrongpassword');
    await page.click('button[type="submit"], .login-button');

    await expect(page.locator('.el-message--error, .error-message')).toBeVisible({ timeout: 5000 });
  });

  test('TC-A003 不存在的用户名登录显示错误提示', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="email"], input[type="text"]', 'nonexistent');
    await page.fill('input[type="password"]', TEST_PASSWORD);
    await page.click('button[type="submit"], .login-button');

    await expect(page.locator('.el-message--error, .error-message')).toBeVisible({ timeout: 5000 });
  });
});
