package com.yuyu.srwildentity.config;

import com.yuyu.srwildentity.config.condition.BiomeEntityRefreshSettings;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * @BelongsProject: SrWildEntity
 * @BelongsPackage: com.yuyu.srwildentity.config
 * @FileName: ConfigManager
 * @Author: 峰。
 * @Date: 2024/4/3-15:05
 * @Version: 1.0
 * @Description: 读取配置文件
 */
public class ConfigManager {
    private final BiomeEntityRefreshSettings biomeEntityRefreshSettings;//读取了群系相关刷新条件和需要刷新方块
    private final int refreshTime;
    private final int total;

    public ConfigManager(Plugin plugin) {
        //加载配置文件
        this.biomeEntityRefreshSettings = new BiomeEntityRefreshSettings(plugin);
        //保存配置文件
        plugin.saveResource("config.yml",false);
        FileConfiguration config;
        File file = new File(plugin.getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(file);

        this.refreshTime = config.getInt("RefreshTime");
        this.total = config.getInt("total");
    }

    public BiomeEntityRefreshSettings getBiomeEntityRefreshSettings() {
        return biomeEntityRefreshSettings;
    }

    public int getRefreshTime() {
        return refreshTime;
    }

    public int getTotal() {
        return total;
    }
}
