package com.onlystudents.admin.dto.request;

import lombok.Data;

@Data
public class SystemConfigRequest {

    private String configKey;

    private String configValue;

    private String description;

    private String category;
}
