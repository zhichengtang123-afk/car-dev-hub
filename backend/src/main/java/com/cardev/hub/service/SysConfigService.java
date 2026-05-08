package com.cardev.hub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cardev.hub.entity.SysConfig;
import com.cardev.hub.mapper.SysConfigMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统配置服务
 */
@Service
@RequiredArgsConstructor
public class SysConfigService {

    private final SysConfigMapper configMapper;
    private final ObjectMapper objectMapper;

    /**
     * 获取配置值（结果缓存至Redis，减少数据库查询）
     */
    @Cacheable(value = "sysConfig", key = "#key", unless = "#result == null")
    public String getConfigValue(String key) {
        SysConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 获取配置值列表
     */
    public List<String> getConfigList(String key) {
        String value = getConfigValue(key);
        if (value == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    /**
     * 设置配置值（更新时清除对应缓存）
     */
    @Transactional
    @CacheEvict(value = "sysConfig", key = "#key")
    public void setConfigValue(String key, String value) {
        SysConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));

        if (config != null) {
            config.setConfigValue(value);
            configMapper.updateById(config);
        } else {
            config = new SysConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            configMapper.insert(config);
        }
    }

    /**
     * 获取知识类型列表（缓存至Redis）
     */
    @Cacheable(value = "sysConfig", key = "'knowledge_types'", unless = "#result == null")
    public List<String> getKnowledgeTypes() {
        return getConfigList("knowledge_types");
    }

    /**
     * 获取知识领域列表（缓存至Redis）
     */
    @Cacheable(value = "sysConfig", key = "'knowledge_domains'", unless = "#result == null")
    public List<String> getKnowledgeDomains() {
        return getConfigList("knowledge_domains");
    }

    /**
     * 获取所有配置
     */
    public List<SysConfig> getAllConfigs() {
        return configMapper.selectList(null);
    }

    /**
     * 更新配置（更新时清除所有配置缓存）
     */
    @Transactional
    @CacheEvict(value = "sysConfig", allEntries = true)
    public void updateConfig(Long id, String value) {
        SysConfig config = configMapper.selectById(id);
        if (config != null) {
            config.setConfigValue(value);
            configMapper.updateById(config);
        }
    }
}
