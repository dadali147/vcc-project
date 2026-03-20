package com.vcc.export.mapper;

import com.vcc.export.domain.ExportTask;

import java.util.List;

/**
 * 导出任务 数据层
 */
public interface ExportTaskMapper
{
    public ExportTask selectExportTaskById(Long id);

    public List<ExportTask> selectExportTaskList(ExportTask exportTask);

    public List<ExportTask> selectPendingTasks();

    public int insertExportTask(ExportTask exportTask);

    public int updateExportTask(ExportTask exportTask);

    public int deleteExportTaskById(Long id);

    public int deleteExportTaskByIds(Long[] ids);
}
