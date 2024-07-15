package com.yuyu.srwildentity.config.condition;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @BelongsProject: SrWildEntity
 * @BelongsPackage: com.yuyu.srwildentity.config.condition
 * @FileName: BiomeEntity
 * @Author: 峰。
 * @Date: 2024/4/3-15:06
 * @Version: 1.0
 * @Description: 读取实体刷新的配置相关文件
 */
public class BiomeEntityRefreshSettings {
//    private final HashMap<String,List<String>> biomeEntityMap;//此集合储存了群系内需要刷新的怪物
//
//    private final HashMap<String,HashMap<String,EntityCondition>> biomeEntityConditionMap;//此集合储存需要刷新的怪物的相关配置
    private final List<LevelRefresh> levelRefreshesList;


    public BiomeEntityRefreshSettings(Plugin plugin,int levelMax) {
//        this.biomeEntityMap = new HashMap<>();
//        this.biomeEntityConditionMap = new HashMap<>();
        this.levelRefreshesList = new ArrayList<>();

//        this.loadBiomeEntitiesConfig(plugin,levelMax);
//
//        this.loadEntityConditionConfig(plugin,levelMax);

        this.loadRefreshEntity(plugin,levelMax);

    }

    private void loadRefreshEntity(Plugin plugin,int level){
        //保存文件
        plugin.saveResource("biomeEntity.yml",false);

        FileConfiguration config;
        File file = new File(plugin.getDataFolder(), "biomeEntity.yml");
        config = YamlConfiguration.loadConfiguration(file);

        //获取所有生物群系
        plugin.saveResource("biome.yml",false);
        FileConfiguration biomeconfig = null;
        File biomeName = new File(plugin.getDataFolder(),"biome.yml");
        biomeconfig = YamlConfiguration.loadConfiguration(biomeName);
        List<String> biomeList = biomeconfig.getStringList("biome");



        if (config == null || biomeconfig == null){
            plugin.getLogger().info("biomeEntity.yml或者 biome.yml文件读取失败!");
            return;
        }
        //      等级             群系         实体列表
        HashMap<String,HashMap<String,List<String>>> levelBiomeMap = new HashMap<>();

        //按level循环
        for (int i = 1;i <= level;i++){
            HashMap<String,List<String>> biomeMap = new HashMap<>();
            //按群系循环
            for (String biome : biomeList){
                //判断该群系是否存在该等级的配置
                if (config.contains(biome)){
                    //获取到后保存
                    List<String> stringList = config.getStringList(biome + ".ENTITY_LEVEL_" + i);
                    if (stringList.size() > 0) {
                        biomeMap.put(biome, stringList);
                    }
                }
            }
            levelBiomeMap.put("level_"+i,biomeMap);
        }

        for (int i = 1;i <= level;i++){

            //读取对应的等级文件
//            plugin.saveResource("LEVEL_"+i+".yml",false);

            FileConfiguration levelConfig = null;
            File levelFile = new File(plugin.getDataFolder(), "LEVEL_"+i+".yml");
            levelConfig =  YamlConfiguration.loadConfiguration(levelFile);

            HashMap<String, List<String>> stringListHashMap = levelBiomeMap.get("level_" + i);

            //获取最大,最小危险度
            int riskMax = levelConfig.getInt("RISK_MAX");
            int riskMin = levelConfig.getInt("RISK_MIN");

            HashMap<String,List<EntityCondition>>  listHashMap = new HashMap<>();

            for (String biome : stringListHashMap.keySet()){


                List<EntityCondition> entityConditions = new ArrayList<>();

                List<String> strings = stringListHashMap.get(biome);
                for (String entityName : strings){
                    SpawnEntityType spawnEntityType = SpawnEntityType.fromId(levelConfig.getInt(biome + "." + entityName + ".type"));
                    EntitySite entitySite = EntitySite.fromId(levelConfig.getInt(biome + "." + entityName + ".site"));
                    int light = levelConfig.getInt(biome + "." + entityName + ".light");
                    long stime = levelConfig.getLong(biome + "." + entityName + ".startTiming");
                    long etime = levelConfig.getLong(biome + "." + entityName + ".endTiming");
                    int nums = levelConfig.getInt(biome + "." + entityName + ".nums");
                    int yMax = levelConfig.getInt(biome + "." + entityName + ".yMax");
                    int yMin = levelConfig.getInt(biome + "." + entityName + ".yMin");
//                    int riskMax = levelConfig.getInt(biome + "." + entityName + ".riskMax");
//                    int riskMin = levelConfig.getInt(biome + "." + entityName + ".riskMin");
                    double weight = levelConfig.getDouble(biome + "." + entityName + ".weight");
                    EntityCondition entityCondition = new EntityCondition(entityName, biome,
                            spawnEntityType, entitySite, light, stime, etime, nums, yMax, yMin, 50, 0, weight);

                    entityConditions.add(entityCondition);
                    plugin.getLogger().info(ChatColor.AQUA +"Level:"+i +"\t"+ entityCondition.toString());
                }

                //此处按群系储存list
                listHashMap.put(biome,entityConditions);
            }

            //此处按等级储存对应等级的LevelRefresh对象
            this.levelRefreshesList.add(new LevelRefresh(riskMax,riskMin,listHashMap));
        }



    }

//    private void loadEntityConditionConfig(Plugin plugin,int levelMax) {
//
//        //保存文件
//        plugin.saveResource("entityCondition.yml",false);
//
//        FileConfiguration config;
//        File file = new File(plugin.getDataFolder(), "entityCondition.yml");
//        config = YamlConfiguration.loadConfiguration(file);
//
//
//        for (int i = 1;i<levelMax;i++) {
//            for (String biomeName : this.biomeEntityMap.keySet()) {
//                plugin.getLogger().info(ChatColor.GOLD + biomeName);
//                HashMap<String, EntityCondition> entityConditionHashMap = new HashMap<>();
//
//                List<String> entities = this.biomeEntityMap.get(biomeName);
//                for (String entityName : entities) {
//                    SpawnEntityType spawnEntityType = SpawnEntityType.fromId(config.getInt(biomeName + "." + entityName + ".type"));
//                    EntitySite entitySite = EntitySite.fromId(config.getInt(biomeName + "." + entityName + ".site"));
//                    int light = config.getInt(biomeName + "." + entityName + ".light");
//                    long stime = config.getLong(biomeName + "." + entityName + ".startTiming");
//                    long etime = config.getLong(biomeName + "." + entityName + ".endTiming");
//                    int nums = config.getInt(biomeName + "." + entityName + ".nums");
//                    int yMax = config.getInt(biomeName + "." + entityName + ".yMax");
//                    int yMin = config.getInt(biomeName + "." + entityName + ".yMin");
//                    int riskMax = config.getInt(biomeName + "." + entityName + ".riskMax");
//                    int riskMin = config.getInt(biomeName + "." + entityName + ".riskMin");
//                    double weight = config.getDouble(biomeName + "." + entityName + ".weight");
//                    EntityCondition entityCondition = new EntityCondition(entityName, biomeName,
//                            spawnEntityType, entitySite, light, stime, etime, nums, yMax, yMin, riskMax, riskMin, weight);
//
//                    plugin.getLogger().info(ChatColor.AQUA + entityCondition.toString());
//
//                    //存入单个群系刷新map
//                    entityConditionHashMap.put(entityName, entityCondition);
//                }
//                //存入记录总群系的map
//                biomeEntityConditionMap.put(biomeName, entityConditionHashMap);
//            }
//        }
//
//        plugin.getLogger().info("群系刷新相关文件读取完毕");
//    }
//
//    public void loadBiomeEntitiesConfig(Plugin plugin,int levelMax){
//        //保存文件
//        plugin.saveResource("biomeEntity.yml",false);
//
//        FileConfiguration config;
//        File file = new File(plugin.getDataFolder(), "biomeEntity.yml");
//        config = YamlConfiguration.loadConfiguration(file);
//
//        //获取所有生物群系
//        plugin.saveResource("biome.yml",false);
//        FileConfiguration biomeconfig = null;
//        File biomeName = new File(plugin.getDataFolder(),"biome.yml");
//        biomeconfig = YamlConfiguration.loadConfiguration(biomeName);
//        List<String> biomeList = biomeconfig.getStringList("biome");
//
//
//
//        if (config == null || biomeconfig == null){
//            plugin.getLogger().info("biomeEntity.yml或者 biome.yml文件读取失败!");
//            return;
//        }
//
//        //遍历所有可能的生物群系
//        for (String section : biomeList){
//            //判断是否存在
//            if (config.contains(section)){
//                List<String> entityList = config.getStringList(section+".ENTITY");
//                this.biomeEntityMap.put(section,entityList);
//                plugin.getLogger().info(ChatColor.MAGIC+section+ entityList.toString());
//            }
//        }
//
//        plugin.getLogger().info("biomeEntity.读取完成");
//    }

//    public HashMap<String, List<String>> getBiomeEntityMap() {
//        return biomeEntityMap;
//    }
//
//    public HashMap<String, HashMap<String, EntityCondition>> getBiomeEntityConditionMap() {
//        return biomeEntityConditionMap;
//    }

    public List<LevelRefresh> getLevelRefreshesList() {
        return levelRefreshesList;
    }
}
