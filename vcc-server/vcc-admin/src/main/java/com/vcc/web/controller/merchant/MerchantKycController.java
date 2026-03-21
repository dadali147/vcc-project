package com.vcc.web.controller.merchant;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;

@RestController
@RequestMapping("/kyc")
public class MerchantKycController extends BaseController
{
    @GetMapping("/status")
    public AjaxResult getStatus()
    {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "pending");
        return success(status);
    }

    @PostMapping("/documents")
    public AjaxResult uploadDocument(@RequestParam("file") MultipartFile file)
    {
        return success();
    }

    @PostMapping("/submit")
    public AjaxResult submit()
    {
        return success();
    }

    @GetMapping("/documents")
    public AjaxResult getDocuments()
    {
        return success();
    }
}
