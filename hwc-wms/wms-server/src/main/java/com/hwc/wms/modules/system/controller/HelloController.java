package com.hwc.wms.modules.system.controller;

import com.hwc.wms.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试接口 - 验证项目是否正常启动
 */
@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public Result<Map<String, Object>> hello() {
        Map<String, Object> info = new HashMap<>();
        info.put("message", "海外仓管理系统运行正常！");
        info.put("version", "1.0.0-SNAPSHOT");
        info.put("time", LocalDateTime.now().toString());
        return Result.ok(info);
    }
}
