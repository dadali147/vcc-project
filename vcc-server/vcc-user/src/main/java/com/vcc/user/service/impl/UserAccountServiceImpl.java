package com.vcc.user.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vcc.upstream.adapter.ChannelAwareYeeVccAdapter;
import com.vcc.upstream.dto.YeeVccApiResponse;
import com.vcc.upstream.dto.YeeVccModels;
import com.vcc.upstream.dto.YeeVccRequests;
import com.vcc.user.domain.UserAccount;
import com.vcc.user.mapper.UserAccountMapper;
import com.vcc.user.service.IUserAccountService;

/**
 * 用户账户 服务实现
 */
@Service
public class UserAccountServiceImpl implements IUserAccountService
{
    private static final Logger log = LoggerFactory.getLogger(UserAccountServiceImpl.class);

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private ChannelAwareYeeVccAdapter channelAdapter;

    @Override
    public UserAccount selectUserAccountById(Long id)
    {
        return userAccountMapper.selectUserAccountById(id);
    }

    @Override
    public UserAccount selectUserAccountByUserId(Long userId, String currency)
    {
        return userAccountMapper.selectUserAccountByUserId(userId, currency);
    }

    @Override
    public List<UserAccount> selectUserAccountList(UserAccount userAccount)
    {
        return userAccountMapper.selectUserAccountList(userAccount);
    }

    @Override
    public int insertUserAccount(UserAccount userAccount)
    {
        return userAccountMapper.insertUserAccount(userAccount);
    }

    @Override
    public int updateUserAccount(UserAccount userAccount)
    {
        return userAccountMapper.updateUserAccount(userAccount);
    }

    @Override
    public UserAccount lockUserAccount(Long userId, String currency)
    {
        return userAccountMapper.selectUserAccountForUpdate(userId, currency);
    }

    @Override
    @Transactional
    public boolean deductBalance(Long userId, String currency, BigDecimal amount)
    {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new RuntimeException("扣减金额必须大于0");
        }
        int rows = userAccountMapper.deductBalance(userId, currency, amount);
        if (rows == 0)
        {
            log.warn("余额不足, userId={}, currency={}, amount={}", userId, currency, amount);
            return false;
        }
        log.info("余额扣减成功, userId={}, currency={}, amount={}", userId, currency, amount);
        return true;
    }

    @Override
    @Transactional
    public int addBalance(Long userId, String currency, BigDecimal amount)
    {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new RuntimeException("增加金额必须大于0");
        }
        int rows = userAccountMapper.addBalance(userId, currency, amount);
        if (rows == 0)
        {
            // 账户不存在，自动创建
            UserAccount account = new UserAccount();
            account.setUserId(userId);
            account.setCurrency(currency);
            account.setBalance(amount);
            account.setTotalRecharge(amount);
            account.setFrozenAmount(BigDecimal.ZERO);
            account.setTotalWithdraw(BigDecimal.ZERO);
            account.setStatus(1);
            userAccountMapper.insertUserAccount(account);
            rows = 1;
        }
        log.info("余额增加成功, userId={}, currency={}, amount={}", userId, currency, amount);
        return rows;
    }

    @Override
    public List<Map<String, Object>> selectAccountTransactions(Long userId, int pageNum, int pageSize)
    {
        // TODO: 待 vcc_account_transaction 表创建后实现查询
        log.info("查询用户动账明细, userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public boolean adjustBalance(Long userId, String currency, BigDecimal amount, String reason, Long operatorId)
    {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0)
        {
            throw new RuntimeException("调整金额不能为零");
        }
        if (amount.compareTo(BigDecimal.ZERO) > 0)
        {
            addBalance(userId, currency, amount);
        }
        else
        {
            boolean success = deductBalance(userId, currency, amount.abs());
            if (!success)
            {
                throw new RuntimeException("余额不足，无法扣减");
            }
        }
        // TODO: 记录操作日志到 sys_oper_log（operatorId, reason）
        log.info("管理员调整余额, userId={}, currency={}, amount={}, reason={}, operatorId={}",
                userId, currency, amount, reason, operatorId);
        return true;
    }

    // -----------------------------------------------------------------------
    // 上游账户接口（通过 ChannelAwareYeeVccAdapter 调用 YeeVCC）
    // -----------------------------------------------------------------------

    @Override
    public YeeVccApiResponse<YeeVccModels.AccountData> createUpstreamAccount(Long merchantId, String currency)
    {
        // B2B 设计：userId = merchantId，此处 customerId 取 merchantId 的字符串形式
        YeeVccRequests.CreateAccountRequest request = new YeeVccRequests.CreateAccountRequest();
        request.setCustomerId(String.valueOf(merchantId));
        request.setCurrency(currency);
        log.info("创建上游账户, merchantId={}, currency={}", merchantId, currency);
        return channelAdapter.createAccount(request);
    }

    @Override
    public YeeVccApiResponse<YeeVccModels.OperationData> upstreamTransferIn(
            String accountNo, BigDecimal amount, String currency, String deductCurrency, String orderId)
    {
        YeeVccRequests.AccountTransferInRequest request = new YeeVccRequests.AccountTransferInRequest();
        request.setAccountNo(accountNo);
        request.setTransferInAmount(amount);
        request.setDeductCurrency(deductCurrency);
        request.setOrderId(orderId);
        log.info("账户充值(转入), accountNo={}, amount={}, currency={}, deductCurrency={}, orderId={}",
                accountNo, amount, currency, deductCurrency, orderId);
        return channelAdapter.accountTransferIn(request);
    }

    @Override
    public YeeVccApiResponse<YeeVccModels.OperationData> upstreamTransferOut(
            String accountNo, BigDecimal amount, String currency, String deductCurrency, String orderId)
    {
        YeeVccRequests.AccountTransferOutRequest request = new YeeVccRequests.AccountTransferOutRequest();
        request.setAccountNo(accountNo);
        request.setTransferOutAmount(amount);
        request.setDepositCurrency(currency);
        request.setDeductCurrency(deductCurrency);
        request.setOrderId(orderId);
        log.info("账户提现(转出), accountNo={}, amount={}, currency={}, deductCurrency={}, orderId={}",
                accountNo, amount, currency, deductCurrency, orderId);
        return channelAdapter.accountTransferOut(request);
    }

    @Override
    public YeeVccApiResponse<YeeVccModels.PageData<YeeVccModels.AccountData>> getUpstreamAccountInfo(
            Long merchantId, String accountNo)
    {
        // B2B 设计：userId = merchantId
        YeeVccRequests.GetAccountInfoRequest request = new YeeVccRequests.GetAccountInfoRequest();
        request.setCustomerId(String.valueOf(merchantId));
        if (accountNo != null && !accountNo.isEmpty())
        {
            request.setAccountNo(accountNo);
        }
        log.info("查询上游账户信息, merchantId={}, accountNo={}", merchantId, accountNo);
        return channelAdapter.getAccountInfo(request);
    }
}
