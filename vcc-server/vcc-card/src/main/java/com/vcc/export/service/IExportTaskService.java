package com.vcc.export.service;

import com.vcc.export.domain.ExportTask;

import java.util.List;

/**
 * 导出任务 服务层
 */
public interface IExportTaskService
{
    /**
     * 提交导出任务（创建 PENDING 状态任务）
     *
     * @param merchantId   商户ID
     * @param exportType   导出类型
     * @param language     语言
     * @param fileFormat   文件格式
     * @param filterParams 筛选参数（JSON字符串）
     * @param requesterId  请求人ID
     * @return 导出任务
     */
    ExportTask submitTask(Long merchantId, String exportType, String language,
                          String fileFormat, String filterParams, Long requesterId);

    /**
     * 更新任务状态
     *
     * @param id           任务ID
     * @param status       状态
     * @param filePath     文件路径
     * @param fileName     文件名
     * @param errorMessage 错误信息
     * @return 影响行数
     */
    int updateTaskStatus(Long id, String status, String filePath, String fileName, String errorMessage);

    /**
     * 查询导出任务列表
     *
     * @param exportTask 查询条件
     * @return 导出任务集合
     */
    List<ExportTask> selectExportTaskList(ExportTask exportTask);

    /**
     * 查询导出任务详情
     *
     * @param id 任务ID
     * @return 导出任务
     */
    ExportTask selectExportTaskById(Long id);

    /**
     * 查询待处理的导出任务
     *
     * @return 待处理任务集合
     */
    List<ExportTask> selectPendingTasks();

    /**
     * 批量删除导出任务
     *
     * @param ids 需要删除的任务ID数组
     * @return 影响行数
     */
    int deleteExportTaskByIds(Long[] ids);
}
