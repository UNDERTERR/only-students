package com.onlystudents.file.config;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class JodConverterConfig {
    @Value("${jodconverter.office.home:}")
    private String officeHome;
    
    @Bean
    public LocalOfficeManager localOfficeManager() {
        LocalOfficeManager.Builder builder = LocalOfficeManager.builder();
        
        // 如果配置了LibreOffice路径
        if (officeHome != null && !officeHome.isEmpty()) {
            builder.officeHome(officeHome);
        } else {
            // 尝试自动检测常见路径
            String[] possiblePaths = {
                "C:/Program Files/LibreOffice",
                "C:/Program Files (x86)/LibreOffice",
                "/usr/lib/libreoffice",
                "/Applications/LibreOffice.app/Contents"
            };
            
            for (String path : possiblePaths) {
                if (new File(path).exists()) {
                    builder.officeHome(path);
                    break;
                }
            }
        }
        
        return builder.build();
    }
    
    @Bean
    public DocumentConverter documentConverter(LocalOfficeManager localOfficeManager) {
        return LocalConverter.builder()
                .officeManager(localOfficeManager)
                .build();
    }
}
