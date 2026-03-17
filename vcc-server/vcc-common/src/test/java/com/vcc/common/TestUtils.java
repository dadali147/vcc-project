package com.vcc.common;

import com.vcc.common.core.domain.entity.SysRole;
import com.vcc.common.core.domain.entity.SysUser;
import com.vcc.common.core.domain.model.LoginUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class TestUtils
{
    private TestUtils()
    {
    }

    public static SysUser buildSysUser(Long userId, String username, String... roleKeys)
    {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setDeptId(100L);
        user.setUserName(username);
        user.setNickName(username);
        user.setPassword("password");
        user.setStatus("0");
        user.setRoles(buildRoles(roleKeys));
        return user;
    }

    public static LoginUser buildLoginUser(Long userId, String username, String... roleKeys)
    {
        SysUser user = buildSysUser(userId, username, roleKeys);
        LoginUser loginUser = new LoginUser(userId, user.getDeptId(), user, Set.of());
        loginUser.setUser(user);
        loginUser.setPermissions(Set.of());
        return loginUser;
    }

    public static Authentication buildAuthentication(Long userId, String username, String... roleKeys)
    {
        LoginUser loginUser = buildLoginUser(userId, username, roleKeys);
        return new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
    }

    public static <T> T buildCard(Long cardId, Long userId, Integer status)
    {
        return buildCard(cardId, userId, status, 1);
    }

    public static <T> T buildCard(Long cardId, Long userId, Integer status, Integer cardType)
    {
        Object card = newInstance("com.vcc.card.domain.Card");
        set(card, "setId", Long.class, cardId);
        set(card, "setUserId", Long.class, userId);
        set(card, "setHolderId", Long.class, 501L);
        set(card, "setStatus", Integer.class, status);
        set(card, "setCardType", Integer.class, cardType);
        set(card, "setCurrency", String.class, "USD");
        set(card, "setCardNoMask", String.class, "4111********1111");
        set(card, "setCardBinId", String.class, "BIN-001");
        set(card, "setBalance", BigDecimal.class, new BigDecimal("100.00"));
        set(card, "setUpstreamCardId", String.class, "up-card-" + cardId);
        return cast(card);
    }

    public static <T> T buildCardHolder(Long holderId, Long userId)
    {
        Object holder = newInstance("com.vcc.card.domain.CardHolder");
        set(holder, "setId", Long.class, holderId);
        set(holder, "setUserId", Long.class, userId);
        set(holder, "setHolderName", String.class, "Test Holder");
        set(holder, "setFirstName", String.class, "Test");
        set(holder, "setLastName", String.class, "Holder");
        set(holder, "setEmail", String.class, "holder@example.com");
        set(holder, "setMobile", String.class, "13800138000");
        set(holder, "setCountry", String.class, "US");
        set(holder, "setStatus", Integer.class, 1);
        set(holder, "setUpstreamHolderId", String.class, "up-holder-" + holderId);
        return cast(holder);
    }

    public static <T> T buildRecharge(Long rechargeId, Long userId, Long cardId, String orderNo,
                                      BigDecimal amount, Integer status)
    {
        Object recharge = newInstance("com.vcc.finance.domain.Recharge");
        BigDecimal fee = new BigDecimal("1.00");
        set(recharge, "setId", Long.class, rechargeId);
        set(recharge, "setUserId", Long.class, userId);
        set(recharge, "setCardId", Long.class, cardId);
        set(recharge, "setOrderNo", String.class, orderNo);
        set(recharge, "setAmount", BigDecimal.class, amount);
        set(recharge, "setCurrency", String.class, "USD");
        set(recharge, "setFee", BigDecimal.class, fee);
        set(recharge, "setActualAmount", BigDecimal.class, amount.subtract(fee));
        set(recharge, "setStatus", Integer.class, status);
        set(recharge, "setRechargeType", Integer.class, 1);
        set(recharge, "setUpstreamOrderNo", String.class, "up-" + orderNo);
        return cast(recharge);
    }

    public static <T> T buildWebhookLog(Long id, String webhookType)
    {
        Object webhookLog = newInstance("com.vcc.card.domain.WebhookLog");
        set(webhookLog, "setId", Long.class, id);
        set(webhookLog, "setWebhookType", String.class, webhookType);
        set(webhookLog, "setProcessed", Integer.class, 0);
        set(webhookLog, "setRetryCount", Integer.class, 0);
        return cast(webhookLog);
    }

    public static BigDecimal amount(String value)
    {
        return new BigDecimal(value);
    }

    private static List<SysRole> buildRoles(String... roleKeys)
    {
        if (roleKeys == null || roleKeys.length == 0)
        {
            return Collections.emptyList();
        }

        Set<String> uniqueRoleKeys = new LinkedHashSet<>(Arrays.asList(roleKeys));
        return uniqueRoleKeys.stream().map(TestUtils::buildRole).toList();
    }

    private static SysRole buildRole(String roleKey)
    {
        SysRole role = new SysRole();
        role.setRoleId("admin".equals(roleKey) ? 1L : Math.abs((long) roleKey.hashCode()));
        role.setRoleKey(roleKey);
        role.setRoleName(roleKey);
        role.setRoleSort(1);
        role.setStatus("0");
        return role;
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object target)
    {
        return (T) target;
    }

    private static Object newInstance(String className)
    {
        try
        {
            return Class.forName(className).getDeclaredConstructor().newInstance();
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("无法创建测试对象: " + className, ex);
        }
    }

    private static void set(Object target, String methodName, Class<?> parameterType, Object value)
    {
        try
        {
            Method method = target.getClass().getMethod(methodName, parameterType);
            method.invoke(target, value);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("设置测试对象属性失败: " + methodName, ex);
        }
    }
}
