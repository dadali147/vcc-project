package com.vcc.message.service;

import com.vcc.message.domain.Message;

import java.util.List;

/**
 * 站内消息 服务层
 */
public interface IMessageService
{
    public Message selectMessageById(Long id);

    public List<Message> selectMessageList(Message message);

    public int insertMessage(Message message);

    public int updateMessage(Message message);

    public int deleteMessageById(Long id);

    public int deleteMessageByIds(Long[] ids);

    /**
     * 发送消息
     *
     * @param receiverType 接收者类型
     * @param receiverId   接收者ID
     * @param title        消息标题
     * @param content      消息内容
     * @param messageType  消息类型
     * @param businessType 关联业务类型
     * @param businessId   关联业务ID
     * @return 结果
     */
    public int sendMessage(String receiverType, Long receiverId, String title, String content,
                           String messageType, String businessType, String businessId);

    /**
     * 标记消息为已读
     *
     * @param id 消息ID
     * @return 结果
     */
    public int markAsRead(Long id);

    /**
     * 标记所有消息为已读
     *
     * @param receiverType 接收者类型
     * @param receiverId   接收者ID
     * @return 结果
     */
    public int markAllAsRead(String receiverType, Long receiverId);

    /**
     * 获取未读消息数量
     *
     * @param receiverType 接收者类型
     * @param receiverId   接收者ID
     * @return 未读数量
     */
    public int getUnreadCount(String receiverType, Long receiverId);
}
