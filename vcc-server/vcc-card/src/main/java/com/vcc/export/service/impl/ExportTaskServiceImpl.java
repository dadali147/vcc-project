package com.vcc.export.service.impl;

import com.vcc.export.domain.ExportTask;
import com.vcc.export.mapper.ExportTaskMapper;
import com.vcc.export.service.IExportTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 导出任务 服务实现
 */
@Service
public class ExportTaskServiceImpl implements IExportTaskService
{
    private static final Logger log = LoggerFactory.getLogger(ExportTaskServiceImpl.class);

    private final ExportTaskMapper exportTaskMapper;

    public ExportTaskServiceImpl(ExportTaskMapper exportTaskMapper)
    {
        this.exportTaskMapper = exportTaskMapper;
    }

    @Override
    @Transactional
    public ExportTask submitTask(Long merchantId, String exportType, String language,
                                 String fileFormat, String filterParams, Long requesterId)
    {
        ExportTask task = new ExportTask();
        task.setMerchantId(merchantId);
        task.setExportType(exportType);
        task.setLanguage(language != null ? language : "zh_CN");
        task.setFileFormat(fileFormat != null ? fileFormat : ExportTask.FILE_FORMAT_CSV);
        task.setFilterParams(filterParams);
        task.setRequesterId(requesterId);
        task.setStatus(ExportTask.STATUS_PENDING);
        task.setSubmitTime(new Date());

        exportTaskMapper.insertExportTask(task);
        log.info("导出任务已提交, taskId={}, exportType={}, merchantId={}", task.getId(), exportType, merchantId);
        return task;
    }

    @Override
    @Transactional
    public int updateTaskStatus(Long id, String status, String filePath, String fileName, String errorMessage)
    {
        ExportTask task = new ExportTask();
        task.setId(id);
        task.setStatus(status);
        if (filePath != null)
        {
            task.setFilePath(filePath);
        }
        if (fileName != null)
        {
            task.setFileName(fileName);
        }
        if (errorMessage != null)
        {
            task.setErrorMessage(errorMessage);
        }
        if (ExportTask.STATUS_SUCCESS.equals(status) || ExportTask.STATUS_FAILED.equals(status))
        {
            task.setFinishTime(new Date());
        }
        log.info("导出任务状态更新, taskId={}, status={}", id, status);
        return exportTaskMapper.updateExportTask(task);
    }

    @Override
    public List<ExportTask> selectExportTaskList(ExportTask exportTask)
    {
        return exportTaskMapper.selectExportTaskList(exportTask);
    }

    @Override
    public ExportTask selectExportTaskById(Long id)
    {
        return exportTaskMapper.selectExportTaskById(id);
    }

    @Override
    public List<ExportTask> selectPendingTasks()
    {
        return exportTaskMapper.selectPendingTasks();
    }

    @Override
    @Transactional
    public int deleteExportTaskByIds(Long[] ids)
    {
        return exportTaskMapper.deleteExportTaskByIds(ids);
    }
}
