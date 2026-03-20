package com.vcc.holder.service.impl;

import com.vcc.common.exception.ServiceException;
import com.vcc.common.utils.StringUtils;
import com.vcc.holder.domain.VccCardHolder;
import com.vcc.holder.dto.CardHolderCreateRequest;
import com.vcc.holder.dto.CardHolderStatusRequest;
import com.vcc.holder.dto.CardHolderUpdateRequest;
import com.vcc.holder.mapper.VccCardHolderMapper;
import com.vcc.holder.service.VccCardHolderService;
import com.vcc.upstream.YeeVccClient;
import com.vcc.upstream.dto.YeeVccApiResponse;
import com.vcc.upstream.dto.YeeVccModels;
import com.vcc.upstream.dto.YeeVccRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class VccCardHolderServiceImpl implements VccCardHolderService
{
    private static final Logger log = LoggerFactory.getLogger(VccCardHolderServiceImpl.class);

    private final VccCardHolderMapper holderMapper;
    private final YeeVccClient yeeVccClient;

    public VccCardHolderServiceImpl(VccCardHolderMapper holderMapper, YeeVccClient yeeVccClient)
    {
        this.holderMapper = holderMapper;
        this.yeeVccClient = yeeVccClient;
    }

    @Override
    @Transactional
    public VccCardHolder create(Long merchantId, CardHolderCreateRequest request)
    {
        String[] names = normalizeNames(request.holderName(), request.firstName(), request.lastName());
        YeeVccRequests.AddCardHolderRequest upstreamRequest = new YeeVccRequests.AddCardHolderRequest();
        upstreamRequest.setCustomerId(String.valueOf(merchantId));
        upstreamRequest.setFirstName(names[0]);
        upstreamRequest.setLastName(names[1]);
        upstreamRequest.setEmail(request.email());
        upstreamRequest.setCountry(StringUtils.defaultIfBlank(request.country(), "CN"));
        upstreamRequest.setAddress(request.address());
        upstreamRequest.setPostCode(request.postCode());
        upstreamRequest.setPhone(request.mobile());

        YeeVccApiResponse<YeeVccModels.CardHolderData> response = yeeVccClient.addCardHolder(upstreamRequest);
        if (!response.isSuccess() || response.getData() == null)
        {
            throw new ServiceException("创建上游持卡人失败: " + response.getMessage());
        }

        VccCardHolder holder = new VccCardHolder();
        holder.setMerchantId(merchantId);
        /*
         * P1-3 字段语义说明：userId = merchantId 是业务设计意图。
         * 在当前 B2B VCC 模型中，持卡人由商户创建和管理，操作主体（userId）即为商户自身（merchantId）。
         * 两个字段含义：
         *   - merchantId：持卡人所属商户ID，用于数据隔离和权限校验
         *   - userId：创建/拥有该持卡人记录的操作主体ID，在当前模型下等同于 merchantId
         * 若未来引入商户下的子账户/操作员体系，userId 应改为实际操作员ID。
         */
        holder.setUserId(merchantId);
        holder.setHolderName(request.holderName());
        holder.setFirstName(names[0]);
        holder.setLastName(names[1]);
        holder.setMobile(request.mobile());
        holder.setEmail(request.email());
        holder.setIdCard(request.idCard());
        holder.setAddress(request.address());
        holder.setPostCode(request.postCode());
        holder.setCountry(StringUtils.defaultIfBlank(request.country(), "CN"));
        holder.setStatus(VccCardHolder.STATUS_ENABLED);
        holder.setUpstreamHolderId(response.getData().getId());
        holder.setDeletedFlag(VccCardHolder.DELETED_FLAG_NORMAL);
        holder.setRemark(request.remark());
        holderMapper.insert(holder);
        return holder;
    }

    /**
     * 更新持卡人信息。
     *
     * <p><b>上游同步说明（P1-1 修复）：</b></p>
     * <p>YeeVCC 上游当前不提供持卡人更新接口（仅有 addCardHolder 和 getCardHolder）。</p>
     * <p>因此本方法仅支持更新本地记录。可修改的字段分为两类：</p>
     * <ul>
     *   <li><b>仅本地备注类字段（可自由修改）：</b>holderName, idCard, remark</li>
     *   <li><b>上游关键字段（本地可改，但不会同步到上游，存在不一致风险）：</b>
     *       firstName, lastName, mobile, email, address, postCode, country</li>
     * </ul>
     * <p>修改上游关键字段时会记录 WARN 日志，提示与上游数据可能不一致。</p>
     * <p>TODO: 待 YeeVCC 上游提供持卡人更新接口后，对上游关键字段的修改需同步调用上游。</p>
     */
    @Override
    @Transactional
    public VccCardHolder update(Long merchantId, Long holderId, CardHolderUpdateRequest request)
    {
        VccCardHolder holder = requireOwnerHolder(merchantId, holderId);

        // 记录本地修改的字段，用于审计日志
        List<String> changedLocalFields = new ArrayList<>();
        List<String> changedUpstreamFields = new ArrayList<>();

        if (StringUtils.isNotEmpty(request.holderName()) && !request.holderName().equals(holder.getHolderName()))
        {
            changedLocalFields.add("holderName[" + holder.getHolderName() + " → " + request.holderName() + "]");
            holder.setHolderName(request.holderName());
        }
        if (StringUtils.isNotEmpty(request.firstName()) && !request.firstName().equals(holder.getFirstName()))
        {
            changedUpstreamFields.add("firstName[" + holder.getFirstName() + " → " + request.firstName() + "]");
            holder.setFirstName(request.firstName());
        }
        if (StringUtils.isNotEmpty(request.lastName()) && !request.lastName().equals(holder.getLastName()))
        {
            changedUpstreamFields.add("lastName[" + holder.getLastName() + " → " + request.lastName() + "]");
            holder.setLastName(request.lastName());
        }
        if (StringUtils.isNotEmpty(request.mobile()) && !request.mobile().equals(holder.getMobile()))
        {
            changedUpstreamFields.add("mobile[" + holder.getMobile() + " → " + request.mobile() + "]");
            holder.setMobile(request.mobile());
        }
        if (StringUtils.isNotEmpty(request.email()) && !request.email().equals(holder.getEmail()))
        {
            changedUpstreamFields.add("email[" + holder.getEmail() + " → " + request.email() + "]");
            holder.setEmail(request.email());
        }
        if (StringUtils.isNotEmpty(request.idCard()) && !request.idCard().equals(holder.getIdCard()))
        {
            changedLocalFields.add("idCard[" + holder.getIdCard() + " → " + request.idCard() + "]");
            holder.setIdCard(request.idCard());
        }
        if (StringUtils.isNotEmpty(request.address()) && !request.address().equals(holder.getAddress()))
        {
            changedUpstreamFields.add("address[" + holder.getAddress() + " → " + request.address() + "]");
            holder.setAddress(request.address());
        }
        if (request.postCode() != null && !request.postCode().equals(holder.getPostCode()))
        {
            changedUpstreamFields.add("postCode[" + holder.getPostCode() + " → " + request.postCode() + "]");
            holder.setPostCode(request.postCode());
        }
        if (StringUtils.isNotEmpty(request.country()) && !request.country().equals(holder.getCountry()))
        {
            changedUpstreamFields.add("country[" + holder.getCountry() + " → " + request.country() + "]");
            holder.setCountry(request.country());
        }
        if (request.remark() != null && !request.remark().equals(holder.getRemark()))
        {
            changedLocalFields.add("remark[changed]");
            holder.setRemark(request.remark());
        }

        // 如果没有任何字段变更，直接返回
        if (changedLocalFields.isEmpty() && changedUpstreamFields.isEmpty())
        {
            log.info("持卡人更新无变更, holderId={}, merchantId={}", holderId, merchantId);
            return holder;
        }

        // 记录本地备注类字段变更
        if (!changedLocalFields.isEmpty())
        {
            log.info("持卡人本地字段更新, holderId={}, merchantId={}, fields={}",
                    holderId, merchantId, changedLocalFields);
        }

        // 上游关键字段变更：记录 WARN 日志，因为无法同步到上游
        if (!changedUpstreamFields.isEmpty())
        {
            log.warn("持卡人上游关键字段仅本地更新（YeeVCC不支持更新接口，与上游数据可能不一致）, "
                            + "holderId={}, merchantId={}, upstreamHolderId={}, fields={}",
                    holderId, merchantId, holder.getUpstreamHolderId(), changedUpstreamFields);
        }

        holderMapper.update(holder);
        return holder;
    }

    @Override
    @Transactional
    public int changeStatus(Long merchantId, Long holderId, CardHolderStatusRequest request)
    {
        requireOwnerHolder(merchantId, holderId);
        if (!VccCardHolder.STATUS_ENABLED.equals(request.status())
                && !VccCardHolder.STATUS_DISABLED.equals(request.status()))
        {
            throw new ServiceException("不支持的持卡人状态: " + request.status());
        }
        return holderMapper.updateStatus(holderId, merchantId, request.status());
    }

    @Override
    @Transactional
    public int delete(Long merchantId, Long holderId)
    {
        requireOwnerHolder(merchantId, holderId);
        return holderMapper.logicDelete(holderId, merchantId);
    }

    @Override
    public VccCardHolder getById(Long merchantId, Long holderId)
    {
        return requireOwnerHolder(merchantId, holderId);
    }

    @Override
    public List<VccCardHolder> list(Long merchantId, VccCardHolder query)
    {
        query.setMerchantId(merchantId);
        return holderMapper.selectList(query);
    }

    private VccCardHolder requireOwnerHolder(Long merchantId, Long holderId)
    {
        VccCardHolder holder = holderMapper.selectById(holderId);
        if (holder == null || !merchantId.equals(holder.getMerchantId()))
        {
            throw new ServiceException("持卡人不存在或无权访问");
        }
        return holder;
    }

    private String[] normalizeNames(String holderName, String firstName, String lastName)
    {
        if (StringUtils.isNotEmpty(firstName) && StringUtils.isNotEmpty(lastName))
        {
            return new String[] { firstName, lastName };
        }
        String normalizedName = StringUtils.trim(holderName);
        if (StringUtils.isEmpty(normalizedName))
        {
            throw new ServiceException("持卡人姓名不能为空");
        }
        int splitIndex = normalizedName.indexOf(' ');
        if (splitIndex > 0)
        {
            return new String[] {
                    normalizedName.substring(0, splitIndex).trim(),
                    normalizedName.substring(splitIndex + 1).trim()
            };
        }
        return new String[] { normalizedName, normalizedName };
    }
}
