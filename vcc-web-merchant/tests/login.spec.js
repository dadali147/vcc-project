import { test, expect } from '@playwright/test';

test.describe('商户端登录', () => {
  test('页面能正常打开', async ({ page }) => {
    await page.goto('/login');
    await expect(page.locator('h1')).toContainText('Login');
    await expect(page.locator('button[type="submit"]')).toBeVisible();
  });

  test('密码长度校验', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="email"]', 'test@example.com');
    await page.fill('input[type="password"]', 'short'); // 5位，触发校验
    await page.click('button[type="submit"]');
    // 前端校验拦截，显示提示
    await expect(page.locator('text=密码长度至少为 6 位')).toBeVisible();
  });
});
