package com.vcc.finance.task;

import com.vcc.finance.domain.CardDebt;
import com.vcc.finance.service.IDebtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 欠费定时检查任务
 *
 * 注册方式：在 sys_job 表中配置 invokeTarget = "debtCheckJob.checkOutstandingDebts()"
 * 建议 cron：0 0 9 * * ?（每天上午9点）
 */
@Component("debtCheckJob")
public class DebtCheckJob
{
    private static final Logger log = LoggerFactory.getLogger(DebtCheckJob.class);

    private final IDebtService debtService;

    public DebtCheckJob(IDebtService debtService)
    {
        this.debtService = debtService;
    }

    /**
     * 检查未结清欠费，可用于触发消息通知
     */
    public void checkOutstandingDebts()
    {
        log.info("[DEBT-JOB] 开始检查未结清欠费");
        CardDebt query = new CardDebt();
        query.setStatus(CardDebt.STATUS_OUTSTANDING);
        List<CardDebt> outstandingDebts = debtService.selectCardDebtList(query);

        if (outstandingDebts.isEmpty())
        {
            log.info("[DEBT-JOB] 无未结清欠费");
            return;
        }

        log.info("[DEBT-JOB] 发现 {} 笔未结清欠费", outstandingDebts.size());

        // TODO: 对每笔未结清欠费发送消息通知
        // 可通过注入 IMessageService 发送站内消息
        for (CardDebt debt : outstandingDebts)
        {
            log.info("[DEBT-JOB] 未结清欠费 debtId={}, merchantId={}, cardId={}, amount={}",
                    debt.getId(), debt.getMerchantId(), debt.getCardId(), debt.getDebtAmount());
        }
    }
}
