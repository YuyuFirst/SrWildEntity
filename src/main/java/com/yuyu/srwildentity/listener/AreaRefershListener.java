package com.yuyu.srwildentity.listener;

import com.germ.germplugin.GermPlugin;
import com.yuyu.srwildentity.JDBC.JdbcSqlClass;
import com.yuyu.srwildentity.SrWildEntity;
import com.yuyu.srwildentity.conditionCheck.ConditionCheck;
import com.yuyu.srwildentity.config.ConfigManager;
import com.yuyu.srwildentity.config.condition.EntityCondition;
import com.yuyu.srwildentity.config.condition.SpawnEntityType;
import com.yuyu.srwildentity.pojo.AreaRefresh;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.exceptions.InvalidMobTypeException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author 峰。
 * @version 1.0
 * @project SrWildEntity
 * @date 2024/7/8 13:48:50
 * @description 区域刷新实体监听类
 */
public class AreaRefershListener implements Listener {
    //实体死亡监听，每当有记录的实体死亡后，根据刷新时间新建一个定时任务，刷新实体
    private HashMap<UUID, String> uuidStringHashMap;  //根据uuid定位到刷新区域，之后再根据区域定位到需要刷新的目标
    private HashMap<String, AreaRefresh> areaStringHashMap;//根据区域和UUID定位到需要定时刷新的实体
    private ConfigManager configManager;   //根据ConfigManager开始开始刷新怪物
    private Random random;


    public AreaRefershListener(ConfigManager configManager) {
        this.uuidStringHashMap = new HashMap<>();
        this.random = new Random();
        this.configManager = configManager;
        this.areaStringHashMap = configManager.getAreaRefreshHashMap();
        //监听器初始化时查询数据库
        this.loadData();
    }

    public  synchronized void loadData() {
        for (String area : areaStringHashMap.keySet()) {
            AreaRefresh areaRefresh = areaStringHashMap.get(area);
            HashMap<String, EntityCondition> entityConditionList = areaRefresh.getEntityConditionList();
            for (String entity : entityConditionList.keySet()) {

                //拼接字符
                String area_entity = area + "/" + entity;

                EntityCondition entityCondition = entityConditionList.get(entity);
                //获取uuid
                List<UUID> uuidByAreaAndEntity = JdbcSqlClass.getUUIDByAreaAndEntity(area, entity);

                HashSet<UUID> hashSetUUID = new HashSet<>();


                for (UUID uuid : uuidByAreaAndEntity) {
                    hashSetUUID.add(uuid);
                    this.uuidStringHashMap.put(uuid, area_entity);
                }

                areaRefresh.setEntityNums(entity, uuidByAreaAndEntity.size());

                if (uuidByAreaAndEntity.size() < entityCondition.getNums()) {
                    int n = entityCondition.getNums() - uuidByAreaAndEntity.size();
                    for (int j = 0; j < n; j++) {
                        if (!this.refreshEntity(entityCondition, areaRefresh)) {

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    //默认最多尝试二十次
                                    int rangeX = areaRefresh.getX2() - areaRefresh.getX1();
                                    int rangeY = areaRefresh.getY2() - areaRefresh.getY1();
                                    int rangeZ = areaRefresh.getZ2() - areaRefresh.getZ1();
                                    World world = Bukkit.getServer().getWorld(areaRefresh.getWorldName());

                                    int x = random.nextInt(rangeX) + areaRefresh.getX1();
                                    int z = random.nextInt(rangeZ) + areaRefresh.getZ1();

                                    if (!world.getChunkAt(x, z).isLoaded()) {
                                        world.getChunkAt(x, z).load();
                                    }
                                    for (int i = 0; i < 20; i++) {
                                        //范围
                                        int y = random.nextInt(rangeY) + areaRefresh.getY1();

                                        Location location = new Location(world, x, y, z);


                                        //检查位置是否通过
                                        if (ConditionCheck.checkEntityRefresh(world, location, entityCondition)) {
                                            //刷新通过
                                            if (entityCondition.getSpawnEntityType() == SpawnEntityType.MMENTITY) {
                                                Entity entityNew;
                                                try {
                                                    entityNew = MythicMobs.inst().getAPIHelper().spawnMythicMob(entityCondition.getEntityName(), location);
                                                } catch (InvalidMobTypeException e) {
                                                    throw new RuntimeException(e);
                                                }

                                                UUID uniqueId1 = entityNew.getUniqueId();
                                                uuidStringHashMap.put(uniqueId1, area_entity);
                                                Integer nums = areaRefresh.getEntityNums().get(entity);
                                                areaStringHashMap.get(areaRefresh.getArea()).setEntityNums(entity, nums + 1);
                                                this.cancel();
                                                return;
                                            } else {
                                                Entity entityNew = world.spawnEntity(location, EntityType.valueOf(entityCondition.getEntityName()));
                                                UUID uniqueId1 = entityNew.getUniqueId();
                                                uuidStringHashMap.put(uniqueId1, area_entity);
                                                Integer nums = areaRefresh.getEntityNums().get(entity);
                                                areaStringHashMap.get(areaRefresh.getArea()).setEntityNums(entity, nums + 1);
                                                this.cancel();
                                                return;
                                            }
                                        }
                                    }
                                }
                            }.runTaskTimer(SrWildEntity.getInance(),1200,entityCondition.getRefreshTime() * 20);
                        }

                    }
                }
            }
        }
    }

    //监控实体
    @EventHandler
    public  synchronized void MonitorEntity(EntityDeathEvent event) {
        if (!uuidStringHashMap.containsKey(event.getEntity().getUniqueId())) {
            //死亡的实体不在Map集合内则返回
            return;
        }

        UUID uniqueId = event.getEntity().getUniqueId();
        //记录在map表中
        String areaX_entity = uuidStringHashMap.get(uniqueId);

        this.uuidStringHashMap.remove(uniqueId);//死亡后删除
        //分割字符串
        String[] split = areaX_entity.split("/");
        String area = split[0];
        String entity = split[1];
        AreaRefresh areaRefresh = configManager.getAreaRefreshHashMap().get(area);
        //获取实体的数量
        Integer num = areaRefresh.getEntityNums().get(entity);

        //更新实体数量
        areaRefresh.setEntityNums(entity, num - 1);
        //根据entityCondition设置只执行一次的定时任务并返回。
        EntityCondition entityCondition = areaRefresh.getEntityConditionList().get(entity);


        //此处可以用定时器来执行任务刷新怪物。
        long refreshTime = entityCondition.getRefreshTime();//定时器注册的时间


        new BukkitRunnable() {
            @Override
            public void run() {
                //默认最多尝试二十次
                int rangeX = areaRefresh.getX2() - areaRefresh.getX1();
                int rangeY = areaRefresh.getY2() - areaRefresh.getY1();
                int rangeZ = areaRefresh.getZ2() - areaRefresh.getZ1();
                World world = Bukkit.getServer().getWorld(areaRefresh.getWorldName());

                int x = random.nextInt(rangeX) + areaRefresh.getX1();
                int z = random.nextInt(rangeZ) + areaRefresh.getZ1();

                if (!world.getChunkAt(x, z).isLoaded()) {
                    world.getChunkAt(x, z).load();
                }

                for (int i = 0; i < 20; i++) {
                    //范围
                    int y = random.nextInt(rangeY) + areaRefresh.getY1();

                    Location location = new Location(world, x, y, z);


                    //检查位置是否通过
                    if (ConditionCheck.checkEntityRefresh(world, location, entityCondition)) {
                        //刷新通过
                        if (entityCondition.getSpawnEntityType() == SpawnEntityType.MMENTITY) {
                            Entity entityNew;
                            try {
                                entityNew = MythicMobs.inst().getAPIHelper().spawnMythicMob(entityCondition.getEntityName(), location);
                            } catch (InvalidMobTypeException e) {
                                throw new RuntimeException(e);
                            }

                            UUID uniqueId1 = entityNew.getUniqueId();
                            uuidStringHashMap.put(uniqueId1, areaX_entity);
                            Integer nums = areaRefresh.getEntityNums().get(entity);
                            areaStringHashMap.get(areaRefresh.getArea()).setEntityNums(entity, nums + 1);
                            this.cancel();
                            return;
                        } else {
                            Entity entityNew = world.spawnEntity(location, EntityType.valueOf(entityCondition.getEntityName()));
                            UUID uniqueId1 = entityNew.getUniqueId();
                            uuidStringHashMap.put(uniqueId1, areaX_entity);
                            Integer nums = areaRefresh.getEntityNums().get(entity);
                            areaStringHashMap.get(areaRefresh.getArea()).setEntityNums(entity, nums + 1);
                            this.cancel();
                            return;
                        }
                    }
                }
            }
        }.runTaskTimer(SrWildEntity.getInance(),refreshTime * 20,refreshTime * 20);

        return;
    }

    /**
     * 此方法用于刷新实体
     */
    public  boolean  refreshEntity(EntityCondition entityCondition, AreaRefresh areaRefresh) {
        //默认最多尝试二十次
        int rangeX = areaRefresh.getX2() - areaRefresh.getX1();
        int rangeY = areaRefresh.getY2() - areaRefresh.getY1();
        int rangeZ = areaRefresh.getZ2() - areaRefresh.getZ1();
        String entity = entityCondition.getEntityName();
        World world = Bukkit.getServer().getWorld(areaRefresh.getWorldName());

        int x = random.nextInt(rangeX) + areaRefresh.getX1();
        int z = random.nextInt(rangeZ) + areaRefresh.getZ1();

        if (!world.getChunkAt(x, z).isLoaded()) {
            world.getChunkAt(x, z).load();
        }

        for (int i = 0; i < 20; i++) {
            //范围
            int y = random.nextInt(rangeY) + areaRefresh.getY1();

            Location location = new Location(world, x, y, z);

            //检查位置是否通过
            if (ConditionCheck.checkEntityRefresh(world, location, entityCondition)) {
                //刷新通过
                if (entityCondition.getSpawnEntityType() == SpawnEntityType.MMENTITY) {
                    Entity entityNew;
                    try {
                        entityNew = MythicMobs.inst().getAPIHelper().spawnMythicMob(entityCondition.getEntityName(), location);
                    } catch (InvalidMobTypeException e) {
                        throw new RuntimeException(e);
                    }
                    //拼接字符
                    String area_entity = areaRefresh.getArea() + "/" + entity;
                    UUID uniqueId1 = entityNew.getUniqueId();
                    uuidStringHashMap.put(uniqueId1, area_entity);
                    Integer nums = areaRefresh.getEntityNums().get(entity);
                    this.areaStringHashMap.get(areaRefresh.getArea()).setEntityNums(entity, nums + 1);
                    return true;
                }else {
                    Entity entityNew = world.spawnEntity(location, EntityType.valueOf(entityCondition.getEntityName()));

                    String area_entity = areaRefresh.getArea() + "/" + entity;
                    UUID uniqueId1 = entityNew.getUniqueId();
                    uuidStringHashMap.put(uniqueId1, area_entity);
                    Integer nums = areaRefresh.getEntityNums().get(entity);
                    this.areaStringHashMap.get(areaRefresh.getArea()).setEntityNums(entity, nums + 1);
                    return true;
                }

            }
        }
        return false;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
        this.uuidStringHashMap = new HashMap<>();
        this.random = new Random();
        this.areaStringHashMap = configManager.getAreaRefreshHashMap();
        //监听器初始化时查询数据库
        this.loadData();
    }

    public HashMap<UUID, String> getUuidStringHashMap() {
        return uuidStringHashMap;
    }

    public HashMap<String, AreaRefresh> getAreaStringHashMap() {
        return areaStringHashMap;
    }
}
