package com.vcc.user.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.vcc.user.domain.UserAccount;

/**
 * 用户账户 服务层
 */
public interface IUserAccountService
{
    public UserAccount selectUserAccountById(Long id);

    public UserAccount selectUserAccountByUserId(Long userId, String currency);

    public List<UserAccount> selectUserAccountList(UserAccount userAccount);

    /**
     * 管理端：查询用户动账明细（分页）
     */
    public List<Map<String, Object>> selectAccountTransactions(Long userId, int pageNum, int pageSize);

    /**
     * 管理端：调整余额（正数增加/负数减少）
     */
    public boolean adjustBalance(Long userId, String currency, BigDecimal amount, String reason, Long operatorId);

    public int insertUserAccount(UserAccount userAccount);

    public int updateUserAccount(UserAccount userAccount);

    /**
     * SELECT FOR UPDATE 锁定用户账户行（用于充值风控并发控制）
     */
    public UserAccount lockUserAccount(Long userId, String currency);

    /**
     * 扣减余额（余额不足返回 false）
     */
    public boolean deductBalance(Long userId, String currency, BigDecimal amount);

    /**
     * 增加余额
     */
    public int addBalance(Long userId, String currency, BigDecimal amount);
}
