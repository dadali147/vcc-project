package com.vcc.card.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vcc.card.domain.CardHolder;
import com.vcc.card.mapper.CardHolderMapper;
import com.vcc.card.service.ICardHolderService;
import com.vcc.common.utils.FieldEncryptUtil;
import com.vcc.common.utils.StringUtils;
import com.vcc.upstream.adapter.ChannelAwareYeeVccAdapter;
import com.vcc.upstream.dto.YeeVccApiResponse;
import com.vcc.upstream.dto.YeeVccModels;
import com.vcc.upstream.dto.YeeVccRequests;

/**
 * 持卡人 服务实现
 */
@Service
public class CardHolderServiceImpl implements ICardHolderService
{
    private static final Logger log = LoggerFactory.getLogger(CardHolderServiceImpl.class);

    /** BUG-009 fix: 字段级加密密钥，用于身份证号 AES 加密存储 */
    @Value("${vcc.config.encrypt-key:vcc2024SecretKey!}")
    private String encryptKey;

    @Autowired
    private CardHolderMapper cardHolderMapper;

    @Autowired
    private ChannelAwareYeeVccAdapter yeeVccAdapter;

    @Override
    public CardHolder selectCardHolderById(Long id)
    {
        CardHolder holder = cardHolderMapper.selectCardHolderById(id);
        if (holder != null)
        {
            decryptAndMaskIdCard(holder);
        }
        return holder;
    }

    @Override
    public List<CardHolder> selectCardHolderList(CardHolder cardHolder)
    {
        List<CardHolder> list = cardHolderMapper.selectCardHolderList(cardHolder);
        for (CardHolder holder : list)
        {
            decryptAndMaskIdCard(holder);
        }
        return list;
    }

    @Override
    @Transactional
    public CardHolder addCardHolder(CardHolder cardHolder)
    {
        // 调用上游创建持卡人
        YeeVccRequests.AddCardHolderRequest request = new YeeVccRequests.AddCardHolderRequest();
        request.setFirstName(cardHolder.getFirstName());
        request.setLastName(cardHolder.getLastName());
        request.setEmail(cardHolder.getEmail());
        request.setCountry(cardHolder.getCountry());
        request.setAddress(cardHolder.getAddress());
        request.setPostCode(cardHolder.getPostCode());
        request.setPhone(cardHolder.getMobile());

        YeeVccApiResponse<YeeVccModels.CardHolderData> response = yeeVccAdapter.addCardHolder(request);
        if (!response.isSuccess())
        {
            throw new RuntimeException("上游创建持卡人失败: " + response.getMessage());
        }

        YeeVccModels.CardHolderData data = response.getData();
        cardHolder.setUpstreamHolderId(data.getId());
        cardHolder.setStatus(1);

        // 拼接 holderName
        if (cardHolder.getHolderName() == null || cardHolder.getHolderName().isEmpty())
        {
            cardHolder.setHolderName(cardHolder.getFirstName() + " " + cardHolder.getLastName());
        }

        // BUG-009 fix: 身份证号 AES 加密后再入库
        if (StringUtils.isNotEmpty(cardHolder.getIdCard()))
        {
            cardHolder.setIdCard(FieldEncryptUtil.encrypt(cardHolder.getIdCard(), encryptKey));
        }

        cardHolderMapper.insertCardHolder(cardHolder);
        log.info("持卡人创建成功, id={}, upstreamHolderId={}", cardHolder.getId(), cardHolder.getUpstreamHolderId());

        // 返回前脱敏显示身份证号（不返回加密密文或明文）
        decryptAndMaskIdCard(cardHolder);
        return cardHolder;
    }

    @Override
    public int updateCardHolder(CardHolder cardHolder)
    {
        return cardHolderMapper.updateCardHolder(cardHolder);
    }

    @Override
    public int deleteCardHolderById(Long id)
    {
        return cardHolderMapper.deleteCardHolderById(id);
    }

    @Override
    public int deleteCardHolderByIds(Long[] ids)
    {
        return cardHolderMapper.deleteCardHolderByIds(ids);
    }

    /**
     * BUG-009 fix: 解密 idCard 并脱敏展示
     * 存储格式：AES/ECB Base64 密文；展示格式：前3位 + **** + 后4位
     */
    private void decryptAndMaskIdCard(CardHolder holder)
    {
        if (StringUtils.isEmpty(holder.getIdCard()))
        {
            return;
        }
        try
        {
            String plain = FieldEncryptUtil.decrypt(holder.getIdCard(), encryptKey);
            holder.setIdCard(maskIdCard(plain));
        }
        catch (Exception e)
        {
            // 可能是历史明文数据，直接脱敏
            log.warn("持卡人身份证号解密失败（可能为明文历史数据），直接脱敏展示, holderId={}", holder.getId());
            holder.setIdCard(maskIdCard(holder.getIdCard()));
        }
    }

    /**
     * 身份证号脱敏：保留前3位和后4位，中间用 * 替换
     * 示例：310123199001011234 → 310***********1234
     */
    private String maskIdCard(String idCard)
    {
        if (idCard == null || idCard.length() <= 7)
        {
            return idCard;
        }
        int len = idCard.length();
        String prefix = idCard.substring(0, 3);
        String suffix = idCard.substring(len - 4);
        StringBuilder masked = new StringBuilder(prefix);
        for (int i = 3; i < len - 4; i++)
        {
            masked.append('*');
        }
        masked.append(suffix);
        return masked.toString();
    }
}
