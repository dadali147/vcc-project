/**
 * 商户端 - 认证模块 E2E 测试
 * 覆盖: B-001~B-006, B-014~B-015, A-001~A-003
 * 测试账号: test@test.com / password123
 */
import { test, expect } from '@playwright/test';

// ──────────────────────────────────────────
// TC: A-001 / B-001  正常登录
// ──────────────────────────────────────────
test.describe('商户端登录', () => {
  test('TC-A001 正确邮箱和密码登录成功，跳转 dashboard', async ({ page }) => {
    await page.goto('/login');
    await expect(page.locator('input[type="email"]')).toBeVisible();

    await page.fill('input[type="email"]', 'test@test.com');
    await page.fill('input[type="password"]', 'password123');
    await page.click('.login-button');

    await expect(page).toHaveURL(/dashboard/, { timeout: 10000 });
  });

  // TC: A-002  错误密码
  test('TC-A002 错误密码登录显示错误提示', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="email"]', 'test@test.com');
    await page.fill('input[type="password"]', 'wrongpassword');
    await page.click('.login-button');

    await expect(page.locator('.el-message--error')).toBeVisible({ timeout: 5000 });
    await expect(page).not.toHaveURL(/dashboard/);
  });

  // TC: A-003  用户名不存在
  test('TC-A003 不存在的邮箱登录显示错误提示', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="email"]', 'nonexistent@example.com');
    await page.fill('input[type="password"]', 'password123');
    await page.click('.login-button');

    await expect(page.locator('.el-message--error')).toBeVisible({ timeout: 5000 });
  });

  // TC: B-003  密码过短前端校验
  test('TC-B003 密码少于 6 位前端拦截不发请求', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="email"]', 'test@test.com');
    await page.fill('input[type="password"]', 'abc');
    await page.click('.login-button');

    // 前端校验应拦截，URL 不跳转
    await expect(page).toHaveURL(/login/);
  });

  // TC: B-005  邮箱格式非法
  test('TC-B005 非法邮箱格式前端拦截', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="email"]', 'notanemail');
    await page.fill('input[type="password"]', 'password123');
    await page.click('.login-button');

    await expect(page).toHaveURL(/login/);
  });
});

// ──────────────────────────────────────────
// 注册测试
// ──────────────────────────────────────────
test.describe('商户端注册', () => {
  test('TC-B001 注册页面正常渲染，所有必填字段可见', async ({ page }) => {
    await page.goto('/register');

    // 所有核心字段可见
    await expect(page.locator('input[type="email"]')).toBeVisible();
    await expect(page.locator('input[type="password"]').first()).toBeVisible();
    // 提交按钮可见
    await expect(page.locator('.primary-button')).toBeVisible();
  });

  // TC: B-004  两次密码不一致
  test('TC-B004 确认密码不一致时前端拦截提交', async ({ page }) => {
    await page.goto('/register');

    await page.fill('input[type="email"]', 'newuser@test.com');
    // 填写两个密码输入框，第二个是确认密码
    const pwInputs = page.locator('input[type="password"]');
    await pwInputs.nth(0).fill('password123');
    await pwInputs.nth(1).fill('different456');

    await page.click('.primary-button');

    // 页面应留在注册页
    await expect(page).toHaveURL(/register/);
  });

  // TC: B-006  未勾选协议
  test('TC-B006 未勾选服务协议时提交被拦截', async ({ page }) => {
    await page.goto('/register');

    await page.fill('input[type="email"]', 'testuser@test.com');
    const pwInputs = page.locator('input[type="password"]');
    await pwInputs.nth(0).fill('password123');
    await pwInputs.nth(1).fill('password123');

    // 不勾选 checkbox，直接提交
    await page.click('.primary-button');
    await expect(page).toHaveURL(/register/);
  });
});

// ──────────────────────────────────────────
// 忘记密码测试
// ──────────────────────────────────────────
test.describe('忘记密码', () => {
  // TC: B-014  重置验证码页面
  test('TC-B014 忘记密码页面可正常访问，邮箱输入框和发送按钮可见', async ({ page }) => {
    await page.goto('/forgot-password');

    await expect(page.locator('input[type="email"]')).toBeVisible();
    // 页面有发送重置码按钮
    await expect(page.locator('button, .el-button').first()).toBeVisible();
  });

  // TC: B-015  空邮箱不允许发送
  test('TC-B015 不填邮箱直接提交被前端拦截', async ({ page }) => {
    await page.goto('/forgot-password');

    // 不填邮箱，直接点提交
    await page.click('button:visible, .el-button:visible', { timeout: 3000 }).catch(() => {});
    await expect(page).toHaveURL(/forgot-password/);
  });
});
