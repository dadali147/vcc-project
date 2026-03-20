package com.vcc.export.task;

import com.vcc.export.domain.ExportTask;
import com.vcc.export.service.IExportTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 导出任务定时处理
 *
 * 注册方式：在 sys_job 表中配置 invokeTarget = "exportTaskJob.processPendingTasks()"
 * 建议 cron：0 * / 1 * * * ?（每分钟执行）
 */
@Component("exportTaskJob")
public class ExportTaskJob
{
    private static final Logger log = LoggerFactory.getLogger(ExportTaskJob.class);

    private final IExportTaskService exportTaskService;

    public ExportTaskJob(IExportTaskService exportTaskService)
    {
        this.exportTaskService = exportTaskService;
    }

    /**
     * 处理待执行的导出任务
     */
    public void processPendingTasks()
    {
        List<ExportTask> pendingTasks = exportTaskService.selectPendingTasks();
        if (pendingTasks.isEmpty())
        {
            return;
        }
        log.info("[EXPORT-JOB] 发现 {} 个待处理导出任务", pendingTasks.size());
        for (ExportTask task : pendingTasks)
        {
            try
            {
                // 标记为处理中
                exportTaskService.updateTaskStatus(task.getId(),
                        ExportTask.STATUS_PROCESSING, null, null, null);

                // TODO: 根据 exportType 执行实际导出逻辑（生成CSV/Excel）
                // 实际实现时需注入对应的数据查询Service

                log.info("[EXPORT-JOB] 导出任务处理中 taskId={}, type={}", task.getId(), task.getExportType());
            }
            catch (Exception e)
            {
                log.error("[EXPORT-JOB] 导出任务处理失败 taskId={}", task.getId(), e);
                exportTaskService.updateTaskStatus(task.getId(),
                        ExportTask.STATUS_FAILED, null, null, e.getMessage());
            }
        }
    }

    /**
     * 清理过期导出文件（超过7天的SUCCESS任务标记为EXPIRED）
     *
     * 注册方式：invokeTarget = "exportTaskJob.cleanupExpiredTasks()"
     * 建议 cron：0 0 3 * * ?（每天凌晨3点）
     */
    public void cleanupExpiredTasks()
    {
        log.info("[EXPORT-JOB] 开始清理过期导出任务");
        // 查询所有 SUCCESS 状态的任务，检查 finish_time 是否超过7天
        ExportTask query = new ExportTask();
        query.setStatus(ExportTask.STATUS_SUCCESS);
        List<ExportTask> successTasks = exportTaskService.selectExportTaskList(query);

        int count = 0;
        long expireThreshold = 7L * 24 * 60 * 60 * 1000; // 7天
        long now = System.currentTimeMillis();

        for (ExportTask task : successTasks)
        {
            if (task.getFinishTime() != null
                    && (now - task.getFinishTime().getTime()) > expireThreshold)
            {
                exportTaskService.updateTaskStatus(task.getId(),
                        ExportTask.STATUS_EXPIRED, null, null, "导出文件已过期");
                count++;
            }
        }
        log.info("[EXPORT-JOB] 清理过期导出任务完成，共标记 {} 个任务为过期", count);
    }
}
