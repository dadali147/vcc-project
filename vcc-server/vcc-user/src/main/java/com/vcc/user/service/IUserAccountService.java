package com.vcc.user.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.vcc.upstream.dto.YeeVccApiResponse;
import com.vcc.upstream.dto.YeeVccModels;
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

    // -----------------------------------------------------------------------
    // 上游账户接口（通过 ChannelAwareYeeVccAdapter 调用 YeeVCC）
    // -----------------------------------------------------------------------

    /**
     * 在上游创建账户（预算卡需要）
     *
     * @param merchantId 商户ID（B2B 设计中 userId = merchantId）
     * @param currency   币种
     * @return 上游返回的账户数据
     */
    public YeeVccApiResponse<YeeVccModels.AccountData> createUpstreamAccount(Long merchantId, String currency);

    /**
     * 账户充值（转入），支持 USDT/USD
     *
     * @param accountNo      上游账户编号
     * @param amount         金额
     * @param currency       币种
     * @param deductCurrency 扣费币种
     * @param orderId        业务订单号
     * @return 上游操作结果
     */
    public YeeVccApiResponse<YeeVccModels.OperationData> upstreamTransferIn(
            String accountNo, BigDecimal amount, String currency, String deductCurrency, String orderId);

    /**
     * 账户提现（转出）
     *
     * @param accountNo      上游账户编号
     * @param amount         金额
     * @param currency       币种
     * @param deductCurrency 扣费币种
     * @param orderId        业务订单号
     * @return 上游操作结果
     */
    public YeeVccApiResponse<YeeVccModels.OperationData> upstreamTransferOut(
            String accountNo, BigDecimal amount, String currency, String deductCurrency, String orderId);

    /**
     * 查询上游账户信息（余额等）
     *
     * @param merchantId 商户ID（B2B 设计中 userId = merchantId）
     * @param accountNo  上游账户编号（可选）
     * @return 上游账户信息（分页）
     */
    public YeeVccApiResponse<YeeVccModels.PageData<YeeVccModels.AccountData>> getUpstreamAccountInfo(
            Long merchantId, String accountNo);
}
