package com.yuyu.srwildentity.config;

import com.yuyu.srwildentity.JDBC.JdbcSqlClass;
import com.yuyu.srwildentity.conditionCheck.ConditionCheck;
import com.yuyu.srwildentity.config.condition.BiomeEntityRefreshSettings;
import com.yuyu.srwildentity.config.condition.EntityCondition;
import com.yuyu.srwildentity.config.condition.EntitySite;
import com.yuyu.srwildentity.config.condition.SpawnEntityType;
import com.yuyu.srwildentity.pojo.AreaRefresh;
import com.yuyu.srwildentity.pojo.BlackListArea;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
    private final int num;
    private final int attempts;
    private final int LEVEL;
    private final HashMap<String, List<BlackListArea>> blackListAreaMap;
    private final HashSet<String> blacklistWorldSet;
    private final HashMap<String, AreaRefresh> areaRefreshHashMap;

    public ConfigManager(Plugin plugin) {
        //保存配置文件
        plugin.saveResource("config.yml",false);
        FileConfiguration config;
        File file = new File(plugin.getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(file);


        this.refreshTime = config.getInt("RefreshTime");
        this.total = config.getInt("total");
        this.num = config.getInt("num");
        this.LEVEL = config.getInt("LEVEL_MAX");
        this.attempts = config.getInt("attempts");
        this.blackListAreaMap = new HashMap<>();
        this.blacklistWorldSet = new HashSet<>();
        this.areaRefreshHashMap = new HashMap<>();

        //加载配置文件
        this.biomeEntityRefreshSettings = new BiomeEntityRefreshSettings(plugin,LEVEL);

        this.loadEntityCheck();
        this.loadBlacklistWorld(plugin);
        this.loadBlacklistArea(plugin);
        this.loadAreaRefresh(plugin);
        this.loadDataBase(plugin);
        JdbcSqlClass.initTable();
        JdbcSqlClass.setChartseUtf8();

    }

    private void loadEntityCheck() {
        ConditionCheck.noEntityCollision.add(Material.AIR);
        ConditionCheck.noEntityCollision.add(Material.GRASS);
        ConditionCheck.noEntityCollision.add(Material.SAPLING);
        ConditionCheck.noEntityCollision.add(Material.WATER);
        ConditionCheck.noEntityCollision.add(Material.STATIONARY_WATER);
        ConditionCheck.noEntityCollision.add(Material.LAVA);
        ConditionCheck.noEntityCollision.add(Material.STATIONARY_LAVA);
        ConditionCheck.noEntityCollision.add(Material.POWERED_RAIL);
        ConditionCheck.noEntityCollision.add(Material.DETECTOR_RAIL);
        ConditionCheck.noEntityCollision.add(Material.WEB);
        ConditionCheck.noEntityCollision.add(Material.LONG_GRASS);
        ConditionCheck.noEntityCollision.add(Material.DEAD_BUSH);
        ConditionCheck.noEntityCollision.add(Material.RED_ROSE);
        ConditionCheck.noEntityCollision.add(Material.BROWN_MUSHROOM);
        ConditionCheck.noEntityCollision.add(Material.RED_MUSHROOM);
        ConditionCheck.noEntityCollision.add(Material.TORCH);
        ConditionCheck.noEntityCollision.add(Material.FIRE);
        ConditionCheck.noEntityCollision.add(Material.REDSTONE_WIRE);
        ConditionCheck.noEntityCollision.add(Material.CROPS);
        ConditionCheck.noEntityCollision.add(Material.SIGN_POST);
        ConditionCheck.noEntityCollision.add(Material.LADDER);
        ConditionCheck.noEntityCollision.add(Material.RAILS);
        ConditionCheck.noEntityCollision.add(Material.WALL_SIGN);
        ConditionCheck.noEntityCollision.add(Material.REDSTONE_TORCH_OFF);
        ConditionCheck.noEntityCollision.add(Material.REDSTONE_TORCH_ON);
        ConditionCheck.noEntityCollision.add(Material.STONE_BUTTON);
        ConditionCheck.noEntityCollision.add(Material.SNOW);
        ConditionCheck.noEntityCollision.add(Material.SUGAR_CANE_BLOCK);
        ConditionCheck.noEntityCollision.add(Material.PUMPKIN_STEM);
        ConditionCheck.noEntityCollision.add(Material.MELON_STEM);
        ConditionCheck.noEntityCollision.add(Material.VINE);
        ConditionCheck.noEntityCollision.add(Material.WATER_LILY);
        ConditionCheck.noEntityCollision.add(Material.NETHER_WARTS);
        ConditionCheck.noEntityCollision.add(Material.CARROT);
        ConditionCheck.noEntityCollision.add(Material.POTATO);
        ConditionCheck.noEntityCollision.add(Material.BEETROOT);
    }


    public void loadBlacklistArea(Plugin plugin){

        plugin.saveResource("blacklistArea.yml",false);

        FileConfiguration config;
        File file = new File(plugin.getDataFolder(), "blacklistArea.yml");

        config = YamlConfiguration.loadConfiguration(file);

        int blacklistNums = config.getInt("blacklistNums");
        for (int i = 1; i <= blacklistNums; i++) {
            String locationX = "blacklist_"+i;
            BlackListArea blackListArea = new BlackListArea(config.getString(
                    locationX + "." + "world_name"),
                    config.getInt(locationX +"." +"x1"),
                    config.getInt(locationX +"." +"z1"),
                    config.getInt(locationX +"." +"x2"),
                    config.getInt(locationX +"." +"z2"));
            if (blackListAreaMap.containsKey(blackListArea.getWorldName())) {
                blackListAreaMap.get(blackListArea.getWorldName()).add(blackListArea);
            }else {
                List<BlackListArea> blackListAreas = new ArrayList<>();
                blackListAreas.add(blackListArea);
                blackListAreaMap.put(blackListArea.getWorldName(), blackListAreas);
            }
        }

    }

    public void loadAreaRefresh(Plugin plugin) {
        FileConfiguration config;
        File file = new File(plugin.getDataFolder(), "areaRefresh.yml");
        config = YamlConfiguration.loadConfiguration(file);

        FileConfiguration configuration;
        File file2 = new File(plugin.getDataFolder(), "area_Entity.yml");
        configuration = YamlConfiguration.loadConfiguration(file2);


        int areaNumber = config.getInt("Area_Number");
        for (int i = 1; i <= areaNumber; i++) {
            String areaName = "area_"+i;
            List<String> entityList = config.getStringList(areaName + ".refreshList");
            String worldName = config.getString(areaName + ".worldName");
            int x1 = config.getInt(areaName + ".x1");
            int z1 = config.getInt(areaName + ".z1");
            int x2 = config.getInt(areaName + ".x2");
            int z2 = config.getInt(areaName + ".z2");
            int y1 = config.getInt(areaName + ".yMax");
            int y2 = config.getInt(areaName + ".yMin");
            HashMap<String,EntityCondition> entityConditions = new HashMap<>();
            //获取刷新列表
            for (String entity : entityList) {
                SpawnEntityType spawnEntityType = SpawnEntityType.fromId(configuration.getInt(areaName + "." + entity + ".type"));
                EntitySite entitySite = EntitySite.fromId(configuration.getInt(areaName + "." + entity + ".site"));
                int light = configuration.getInt(areaName + "." + entity + ".light");
                long refreshTime = configuration.getLong(areaName + "." + entity + ".refreshTime");
                int nums = configuration.getInt(areaName + "." + entity + ".nums");
                int yMax = configuration.getInt(areaName + "." + entity + ".yMax");
                int yMin = configuration.getInt(areaName + "." + entity + ".yMin");
                double weight = configuration.getDouble(areaName + "." + entity + ".weight");
                EntityCondition entityCondition = new EntityCondition(entity, null,
                        spawnEntityType, entitySite, light, 0, 24000, nums, yMax, yMin, 50, 0, weight,refreshTime);
                entityConditions.put(entity, entityCondition);
            }

            this.areaRefreshHashMap.put(areaName,new AreaRefresh(x1, z1, x2, z2, worldName, entityConditions,y1 , y2,areaName));
        }
    }

    public void loadBlacklistWorld(Plugin plugin){
        plugin.saveResource("blacklistWorld.yml",false);
        FileConfiguration config;
        File file = new File(plugin.getDataFolder(), "blacklistWorld.yml");
        config = YamlConfiguration.loadConfiguration(file);

        List<String> stringList = config.getStringList("blacklistWorld");
        for (String string : stringList) {
            this.blacklistWorldSet.add(string);
        }

    }

    public HashSet<String> getBlacklistWorldSet() {
        return blacklistWorldSet;
    }

    public int getAttempts() {
        return attempts;
    }

    public int getLEVEL() {
        return LEVEL;
    }

    public BiomeEntityRefreshSettings getBiomeEntityRefreshSettings() {
        return biomeEntityRefreshSettings;
    }

    public int getRefreshTime() {
        return refreshTime;
    }

    public int getNum() {
        return num;
    }

    public int getTotal() {
        return total;
    }


    public HashMap<String, AreaRefresh> getAreaRefreshHashMap() {
        return areaRefreshHashMap;
    }


    public void loadDataBase(Plugin plugin){
        plugin.saveResource("datasource.yml",false);

        File datasource = new File(plugin.getDataFolder(), "datasource.yml");

        FileConfiguration datasourceConfig = YamlConfiguration.loadConfiguration(datasource);

        JdbcSqlClass.setUser(datasourceConfig.getString("database.sql.username"));
        JdbcSqlClass.setPassword(datasourceConfig.getString("database.sql.password"));
        JdbcSqlClass.setDriver(datasourceConfig.getString("database.sql.driver"));
        JdbcSqlClass.setUrl(datasourceConfig.getString("database.sql.url"));

        plugin.getLogger().info(ChatColor.GREEN+"username:"+JdbcSqlClass.getUser()+"\npassword:"+JdbcSqlClass.getPassword()
                +"\ndriver:"+JdbcSqlClass.getDriver()+"\nurl:"+JdbcSqlClass.getUrl());

        JdbcSqlClass.initTable();
        JdbcSqlClass.setChartseUtf8();
    }
    public HashMap<String, List<BlackListArea>> getBlackListAreaMap() {
        return blackListAreaMap;
    }
}
