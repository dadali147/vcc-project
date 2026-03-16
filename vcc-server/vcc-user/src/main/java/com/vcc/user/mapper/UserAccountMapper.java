package com.vcc.user.mapper;

import java.util.List;
import com.vcc.user.domain.UserAccount;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * 用户账户 数据层
 */
public interface UserAccountMapper
{
    public UserAccount selectUserAccountById(Long id);

    public UserAccount selectUserAccountByUserId(@Param("userId") Long userId, @Param("currency") String currency);

    public List<UserAccount> selectUserAccountList(UserAccount userAccount);

    public int insertUserAccount(UserAccount userAccount);

    public int updateUserAccount(UserAccount userAccount);

    public int deductBalance(@Param("userId") Long userId, @Param("currency") String currency, @Param("amount") BigDecimal amount);

    public int addBalance(@Param("userId") Long userId, @Param("currency") String currency, @Param("amount") BigDecimal amount);
}
