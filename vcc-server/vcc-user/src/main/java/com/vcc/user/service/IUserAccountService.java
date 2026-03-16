package com.vcc.user.service;

import java.math.BigDecimal;
import java.util.List;
import com.vcc.user.domain.UserAccount;

/**
 * 用户账户 服务层
 */
public interface IUserAccountService
{
    public UserAccount selectUserAccountById(Long id);

    public UserAccount selectUserAccountByUserId(Long userId, String currency);

    public List<UserAccount> selectUserAccountList(UserAccount userAccount);

    public int insertUserAccount(UserAccount userAccount);

    public int updateUserAccount(UserAccount userAccount);

    /**
     * 扣减余额（余额不足返回 false）
     */
    public boolean deductBalance(Long userId, String currency, BigDecimal amount);

    /**
     * 增加余额
     */
    public int addBalance(Long userId, String currency, BigDecimal amount);
}
