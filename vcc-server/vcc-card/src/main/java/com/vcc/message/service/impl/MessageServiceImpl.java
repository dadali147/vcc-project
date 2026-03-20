package com.vcc.message.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vcc.message.domain.Message;
import com.vcc.message.mapper.MessageMapper;
import com.vcc.message.service.IMessageService;

/**
 * 站内消息 服务实现
 */
@Service
public class MessageServiceImpl implements IMessageService
{
    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final MessageMapper messageMapper;

    public MessageServiceImpl(MessageMapper messageMapper)
    {
        this.messageMapper = messageMapper;
    }

    @Override
    public Message selectMessageById(Long id)
    {
        return messageMapper.selectMessageById(id);
    }

    @Override
    public List<Message> selectMessageList(Message message)
    {
        return messageMapper.selectMessageList(message);
    }

    @Override
    @Transactional
    public int insertMessage(Message message)
    {
        return messageMapper.insertMessage(message);
    }

    @Override
    @Transactional
    public int updateMessage(Message message)
    {
        return messageMapper.updateMessage(message);
    }

    @Override
    @Transactional
    public int deleteMessageById(Long id)
    {
        return messageMapper.deleteMessageById(id);
    }

    @Override
    @Transactional
    public int deleteMessageByIds(Long[] ids)
    {
        return messageMapper.deleteMessageByIds(ids);
    }

    @Override
    @Transactional
    public int sendMessage(String receiverType, Long receiverId, String title, String content,
                           String messageType, String businessType, String businessId)
    {
        Message message = new Message();
        message.setReceiverType(receiverType);
        message.setReceiverId(receiverId);
        message.setTitle(title);
        message.setContent(content);
        message.setMessageType(messageType);
        message.setBusinessType(businessType);
        message.setBusinessId(businessId);
        message.setIsRead(Message.IS_READ_NO);
        message.setDelFlag(Message.DEL_FLAG_NORMAL);
        log.info("发送站内消息: receiverType={}, receiverId={}, title={}, messageType={}",
                receiverType, receiverId, title, messageType);
        return messageMapper.insertMessage(message);
    }

    @Override
    @Transactional
    public int markAsRead(Long id)
    {
        return messageMapper.markAsRead(id);
    }

    @Override
    @Transactional
    public int markAllAsRead(String receiverType, Long receiverId)
    {
        log.info("标记所有消息已读: receiverType={}, receiverId={}", receiverType, receiverId);
        return messageMapper.markAllAsRead(receiverType, receiverId);
    }

    @Override
    public int getUnreadCount(String receiverType, Long receiverId)
    {
        return messageMapper.selectUnreadCount(receiverType, receiverId);
    }
}
