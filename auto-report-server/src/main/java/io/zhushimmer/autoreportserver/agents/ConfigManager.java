package io.zhushimmer.autoreportserver.agents;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private final String configFilePath;
    private final Properties properties;

    /**
     * 构造函数：指定配置文件路径
     */
    public ConfigManager(String configFilePath) {
        this.configFilePath = configFilePath;
        this.properties = new Properties();
        loadConfig();
    }

    /**
     * 从文件加载配置（如果文件存在）
     */
    public void loadConfig() {
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            System.out.println("配置文件不存在，将创建新文件: " + configFilePath);
            // 可选：初始化默认值
            setDefaultValues();
            saveConfig(); // 创建文件
            return;
        }

        try (InputStream input = new FileInputStream(configFile)) {
            properties.load(input);
        } catch (IOException e) {
            System.err.println("加载配置文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 保存当前配置到文件
     */
    public void saveConfig() {
        try (OutputStream output = new FileOutputStream(configFilePath)) {
            properties.store(output, "Auto-generated config file");
        } catch (IOException e) {
            System.err.println("保存配置文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取配置项（支持默认值）
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * 设置配置项
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * 可选：设置默认配置值
     */
    private void setDefaultValues() {
        // OpenAI 模型和兼容 API 配置
        properties.setProperty("api.url", "");
        properties.setProperty("api.key", "");
        properties.setProperty("api.model_name", "");

        // 数据库配置
        properties.setProperty("database.type", "mysql");
        properties.setProperty("database.host", "localhost");
        properties.setProperty("database.port", "3306");
        properties.setProperty("database.database", "");
        properties.setProperty("database.username", "");
        properties.setProperty("database.password", "");
    }

    // 便捷方法：立即保存（设置后自动保存）
    public void setPropertyAndSave(String key, String value) {
        setProperty(key, value);
        saveConfig();
    }
}