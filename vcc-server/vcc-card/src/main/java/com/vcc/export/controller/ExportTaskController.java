package com.vcc.export.controller;

import com.vcc.common.annotation.Log;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.common.enums.BusinessType;
import com.vcc.export.domain.ExportTask;
import com.vcc.export.service.IExportTaskService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导出任务 Controller
 */
@RestController
@RequestMapping("/merchant/export")
public class ExportTaskController extends BaseController
{
    private final IExportTaskService exportTaskService;

    public ExportTaskController(IExportTaskService exportTaskService)
    {
        this.exportTaskService = exportTaskService;
    }

    /**
     * 查询导出任务列表（分页，按当前用户商户过滤）
     */
    @PreAuthorize("@ss.hasPermi('export:task:list')")
    @GetMapping("/list")
    public TableDataInfo list(ExportTask exportTask)
    {
        startPage();
        exportTask.setMerchantId(getUserId());
        List<ExportTask> list = exportTaskService.selectExportTaskList(exportTask);
        return getDataTable(list);
    }

    /**
     * 查询导出任务详情
     */
    @PreAuthorize("@ss.hasPermi('export:task:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        ExportTask task = exportTaskService.selectExportTaskById(id);
        if (task == null || !getUserId().equals(task.getMerchantId()))
        {
            return error("任务不存在或无权访问");
        }
        return success(task);
    }

    /**
     * 提交导出任务
     */
    @PreAuthorize("@ss.hasPermi('export:task:add')")
    @Log(title = "提交导出任务", businessType = BusinessType.INSERT)
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody Map<String, Object> params)
    {
        String exportType = (String) params.get("exportType");
        String language = (String) params.get("language");
        String fileFormat = (String) params.get("fileFormat");
        String filterParams = params.get("filterParams") != null ? params.get("filterParams").toString() : null;

        if (exportType == null || exportType.isEmpty())
        {
            return error("导出类型不能为空");
        }
        if (!ExportTask.EXPORT_TYPE_TRANSACTION.equals(exportType)
                && !ExportTask.EXPORT_TYPE_RECHARGE.equals(exportType)
                && !ExportTask.EXPORT_TYPE_STATEMENT.equals(exportType))
        {
            return error("不支持的导出类型: " + exportType);
        }

        ExportTask task = exportTaskService.submitTask(
                getUserId(), exportType, language, fileFormat, filterParams, getUserId());
        return success(task);
    }

    /**
     * 下载导出文件（仅 SUCCESS 状态可下载）
     */
    @PreAuthorize("@ss.hasPermi('export:task:download')")
    @GetMapping("/download/{id}")
    public AjaxResult download(@PathVariable Long id)
    {
        ExportTask task = exportTaskService.selectExportTaskById(id);
        if (task == null || !getUserId().equals(task.getMerchantId()))
        {
            return error("任务不存在或无权访问");
        }
        if (!ExportTask.STATUS_SUCCESS.equals(task.getStatus()))
        {
            return error("任务尚未完成，当前状态: " + task.getStatus());
        }
        if (task.getFilePath() == null || task.getFilePath().isEmpty())
        {
            return error("文件路径为空");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", task.getId());
        data.put("fileName", task.getFileName());
        data.put("filePath", task.getFilePath());
        data.put("fileFormat", task.getFileFormat());
        return success(data);
    }

    /**
     * 批量删除导出任务
     */
    @PreAuthorize("@ss.hasPermi('export:task:remove')")
    @Log(title = "删除导出任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(exportTaskService.deleteExportTaskByIds(ids));
    }
}
