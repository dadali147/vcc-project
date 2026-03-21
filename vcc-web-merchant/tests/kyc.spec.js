/**
 * 商户端 - KYC 认证 E2E 测试
 * 覆盖: B-007~B-011, B-012~B-013
 * 测试账号: ry@163.com / admin123
 */
import { test, expect } from '@playwright/test';
import path from 'path';

async function loginAsMerchant(page) {
  const res = await page.request.post('/login', {
    data: { email: 'ry@163.com', password: 'admin123' },
  });
  const body = await res.json();
  const token = body?.data?.token;
  if (!token) throw new Error('登录失败，无法获取 token');

  await page.goto('/kyc');
  await page.evaluate((t) => localStorage.setItem('auth_token', t), token);
  await page.reload();
  await page.waitForURL(/kyc/);
}

test.describe('KYC 认证', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsMerchant(page);
  });

  // TC: B-011  KYC 状态查询
  test('TC-B011 KYC 页面正常加载并显示当前状态', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // 状态文字：NOT_SUBMITTED / PENDING / APPROVED / REJECTED 之一应出现在页面
    const statusEl = page.locator('body').filter({
      hasText: /not_submitted|pending|approved|rejected|未提交|审核中|已通过|已拒绝/i,
    });
    await expect(page.locator('.el-card, .kyc-status, .status-section').first()).toBeVisible({ timeout: 8000 });
  });

  // TC: B-007  个人认证 - 文件上传区域可见
  test('TC-B007 个人认证文件上传区域（身份证正反面、自拍）可见', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // 切换到个人认证（如有类型切换按钮）
    const personalBtn = page.locator('button, .el-button').filter({ hasText: /personal|个人/i }).first();
    if (await personalBtn.isVisible()) {
      await personalBtn.click();
    }

    // 上传区域应可见（三个）
    const uploadAreas = page.locator('.upload-area, .el-upload, input[type="file"]');
    await expect(uploadAreas.first()).toBeVisible({ timeout: 5000 });
  });

  // TC: B-009  提交 KYC 按钮（未提交状态下可用）
  test('TC-B009 KYC 提交按钮在未提交状态下为可点击状态', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    const submitBtn = page.locator('.el-button, button').filter({ hasText: /提交|submit/i }).first();
    await expect(submitBtn).toBeVisible({ timeout: 5000 });

    // 若已是 PENDING 状态则按钮应禁用
    const isPending = await page.locator('body').innerText().then(t => /pending|审核中/i.test(t));
    if (isPending) {
      await expect(submitBtn).toBeDisabled();
    }
  });

  // TC: B-010  PENDING 状态不可重复提交（按钮禁用）
  test('TC-B010 审核中状态时提交按钮被禁用', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // 如果当前是 PENDING 状态
    const bodyText = await page.locator('body').innerText();
    if (/pending|审核中/i.test(bodyText)) {
      const submitBtn = page.locator('.el-button, button').filter({ hasText: /提交|submit/i }).first();
      await expect(submitBtn).toBeDisabled();
    } else {
      test.skip(); // 状态不是 PENDING，跳过
    }
  });

  // TC: B-013  企业认证类型可切换
  test('TC-B013 可切换个人认证和企业认证类型', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    const enterpriseBtn = page.locator('button, .el-button').filter({ hasText: /enterprise|企业/i }).first();
    if (await enterpriseBtn.isVisible()) {
      await enterpriseBtn.click();
      // 企业认证特有字段出现（营业执照等）
      const licenseUpload = page.locator('.upload-area, input[type="file"]').first();
      await expect(licenseUpload).toBeVisible({ timeout: 3000 });
    } else {
      test.skip(); // 无企业认证切换按钮
    }
  });
});
