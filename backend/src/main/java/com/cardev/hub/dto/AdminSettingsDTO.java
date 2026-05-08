package com.cardev.hub.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AdminSettingsDTO {
    private List<Map<String, Object>> categories;
    private List<Map<String, Object>> domains;
    private List<Map<String, Object>> tagGroups;
    private Map<String, Object> systemSettings;
}
