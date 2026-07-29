package com.hwc.wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 海外仓管理系统 - 启动类
 */
@SpringBootApplication
public class WmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmsApplication.class, args);
        System.out.println("========================================");
        System.out.println("  海外仓管理系统 (HWC-WMS) 启动成功！");
        System.out.println("  接口文档: http://localhost:8080/doc.html");
        System.out.println("========================================");
    }
}
