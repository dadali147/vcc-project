package com.vcc.message.mapper;

import com.vcc.message.domain.Message;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 站内消息 数据层
 */
public interface MessageMapper
{
    public Message selectMessageById(Long id);

    public List<Message> selectMessageList(Message message);

    public int selectUnreadCount(@Param("receiverType") String receiverType, @Param("receiverId") Long receiverId);

    public int insertMessage(Message message);

    public int updateMessage(Message message);

    public int markAsRead(Long id);

    public int markAllAsRead(@Param("receiverType") String receiverType, @Param("receiverId") Long receiverId);

    public int deleteMessageById(Long id);

    public int deleteMessageByIds(Long[] ids);
}
