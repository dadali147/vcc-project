package com.vcc.message.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.message.domain.Message;
import com.vcc.message.service.IMessageService;

/**
 * 站内消息 Controller
 */
@RestController
@RequestMapping("/merchant/message")
public class MessageController extends BaseController
{
    private final IMessageService messageService;

    public MessageController(IMessageService messageService)
    {
        this.messageService = messageService;
    }

    /**
     * 查询消息列表（分页）
     */
    @PreAuthorize("@ss.hasPermi('message:message:list')")
    @GetMapping("/list")
    public TableDataInfo list(Message message)
    {
        message.setReceiverType(Message.RECEIVER_TYPE_MERCHANT);
        message.setReceiverId(getUserId());
        startPage();
        List<Message> list = messageService.selectMessageList(message);
        return getDataTable(list);
    }

    /**
     * 获取消息详情（自动标记已读）
     */
    @PreAuthorize("@ss.hasPermi('message:message:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        Message message = messageService.selectMessageById(id);
        if (message == null || !message.getReceiverId().equals(getUserId()))
        {
            return error("消息不存在或无权访问");
        }
        // 自动标记已读
        if (Message.IS_READ_NO.equals(message.getIsRead()))
        {
            messageService.markAsRead(id);
            message.setIsRead(Message.IS_READ_YES);
        }
        return success(message);
    }

    /**
     * 获取未读消息数量
     */
    @PreAuthorize("@ss.hasPermi('message:message:query')")
    @GetMapping("/unread-count")
    public AjaxResult unreadCount()
    {
        int count = messageService.getUnreadCount(Message.RECEIVER_TYPE_MERCHANT, getUserId());
        return success(count);
    }

    /**
     * 标记单条消息已读
     */
    @PreAuthorize("@ss.hasPermi('message:message:edit')")
    @PostMapping("/read/{id}")
    public AjaxResult markAsRead(@PathVariable Long id)
    {
        Message message = messageService.selectMessageById(id);
        if (message == null || !message.getReceiverId().equals(getUserId()))
        {
            return error("消息不存在或无权访问");
        }
        return toAjax(messageService.markAsRead(id));
    }

    /**
     * 标记所有消息已读
     */
    @PreAuthorize("@ss.hasPermi('message:message:edit')")
    @PostMapping("/read-all")
    public AjaxResult markAllAsRead()
    {
        return toAjax(messageService.markAllAsRead(Message.RECEIVER_TYPE_MERCHANT, getUserId()));
    }

    /**
     * 批量删除消息
     */
    @PreAuthorize("@ss.hasPermi('message:message:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(messageService.deleteMessageByIds(ids));
    }
}
