package com.vcc.export.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vcc.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 导出任务 vcc_export_task
 */
public class ExportTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 导出类型：交易 */
    public static final String EXPORT_TYPE_TRANSACTION = "TRANSACTION";
    /** 导出类型：充值 */
    public static final String EXPORT_TYPE_RECHARGE = "RECHARGE";
    /** 导出类型：账单 */
    public static final String EXPORT_TYPE_STATEMENT = "STATEMENT";

    /** 文件格式：CSV */
    public static final String FILE_FORMAT_CSV = "CSV";
    /** 文件格式：EXCEL */
    public static final String FILE_FORMAT_EXCEL = "EXCEL";

    /** 状态：待处理 */
    public static final String STATUS_PENDING = "PENDING";
    /** 状态：处理中 */
    public static final String STATUS_PROCESSING = "PROCESSING";
    /** 状态：成功 */
    public static final String STATUS_SUCCESS = "SUCCESS";
    /** 状态：失败 */
    public static final String STATUS_FAILED = "FAILED";
    /** 状态：已过期 */
    public static final String STATUS_EXPIRED = "EXPIRED";

    private Long id;

    /** 商户ID */
    private Long merchantId;

    /** 导出类型：TRANSACTION/RECHARGE/STATEMENT */
    private String exportType;

    /** 语言 */
    private String language;

    /** 文件名 */
    private String fileName;

    /** 文件路径 */
    private String filePath;

    /** 文件格式 */
    private String fileFormat;

    /** 状态：PENDING/PROCESSING/SUCCESS/FAILED/EXPIRED */
    private String status;

    /** 筛选参数（JSON） */
    private String filterParams;

    /** 请求人ID */
    private Long requesterId;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;

    /** 错误信息 */
    private String errorMessage;

    /** 删除标志（0代表存在 1代表删除） */
    private String delFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    public String getExportType()
    {
        return exportType;
    }

    public void setExportType(String exportType)
    {
        this.exportType = exportType;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    public String getFileFormat()
    {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat)
    {
        this.fileFormat = fileFormat;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getFilterParams()
    {
        return filterParams;
    }

    public void setFilterParams(String filterParams)
    {
        this.filterParams = filterParams;
    }

    public Long getRequesterId()
    {
        return requesterId;
    }

    public void setRequesterId(Long requesterId)
    {
        this.requesterId = requesterId;
    }

    public Date getSubmitTime()
    {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime)
    {
        this.submitTime = submitTime;
    }

    public Date getFinishTime()
    {
        return finishTime;
    }

    public void setFinishTime(Date finishTime)
    {
        this.finishTime = finishTime;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public Date getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt)
    {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt()
    {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt)
    {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("merchantId", getMerchantId())
                .append("exportType", getExportType())
                .append("language", getLanguage())
                .append("fileName", getFileName())
                .append("fileFormat", getFileFormat())
                .append("status", getStatus())
                .append("requesterId", getRequesterId())
                .append("submitTime", getSubmitTime())
                .append("finishTime", getFinishTime())
                .toString();
    }
}
