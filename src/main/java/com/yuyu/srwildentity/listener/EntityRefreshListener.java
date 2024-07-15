package com.yuyu.srwildentity.listener;


import com.germ.germplugin.GermPlugin;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.PlayerEnterTownEvent;
import com.palmergames.bukkit.towny.event.PlayerLeaveTownEvent;
import com.palmergames.bukkit.towny.object.Town;
import com.yuyu.srwildentity.conditionCheck.ConditionCheck;
import com.yuyu.srwildentity.config.ConfigManager;
import com.yuyu.srwildentity.config.condition.EntityCondition;
import com.yuyu.srwildentity.config.condition.EntitySite;
import com.yuyu.srwildentity.config.condition.LevelRefresh;
import com.yuyu.srwildentity.config.condition.SpawnEntityType;
import com.yuyu.srwildentity.pojo.BlackListArea;
import com.yuyu.srwildentity.pojo.PlayerRefreshinfo;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.exceptions.InvalidMobTypeException;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Logger;

/**
 * @BelongsProject: SrWildEntity
 * @BelongsPackage: com.yuyu.srwildentity.listener
 * @FileName: EntityRefreshListener
 * @Author: 峰。
 * @Date: 2024/4/3-18:12
 * @Version: 1.0
 * @Description:监听类，控制怪物的刷新,同时注册指令，操控刷新
 */
public class EntityRefreshListener implements Listener, CommandExecutor {

    private ConfigManager configManager;//配置相关

    private boolean flag; //用于验证entity是否刷新
    private Logger logger;//日志打印
    private Random random;//生成随机数,随机实体的定位
    private HashMap<UUID,String> entityUUIDToPlayer;
    private HashMap<String, PlayerRefreshinfo> refreshPlayer;
    private List<String> noRefreshPlayer;//记录不需要刷新的玩家

    private Plugin plugin;
    private MythicMobs mythicMobs;
    private GermPlugin germPlugin;



    public EntityRefreshListener(Logger logger, ConfigManager configManager, Plugin plugin){

        this.entityUUIDToPlayer = new HashMap<>();
        this.refreshPlayer = new HashMap<>();
        this.configManager = configManager;
        this.random = new Random();
        this.logger = logger;
        this.flag = true;
        this.noRefreshPlayer = new ArrayList<>();
        this.plugin = plugin;
        //用于获取MM插件和萌芽的实例对象
        this.mythicMobs = MythicMobs.inst();
        this.germPlugin = GermPlugin.getPlugin();




    }
    /**
     * 此类用于获取玩家的危险度
     * @param player
     */
    public int prRisk(Player player){
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            String placeholder = PlaceholderAPI.setPlaceholders(player, "%prisk_level%");
            int level = Integer.parseInt(placeholder);
            return level;
        }else {
            return 0;
        }
    }

    /**
     *注册的指令，可以通过指令去调解flag
     */
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (!s.equalsIgnoreCase("despawn") && !s.equalsIgnoreCase("dsp")){
            return false;
        }

        if (commandSender.isOp()){
            if (strings.length == 0){
                //只输入despawn,直接停止刷新
                flag = !flag;
                if (flag){
                    //flag为true，开始重新刷新实体，此时遍历所有玩家，获取此时在野外的玩家
                    Collection<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers();
                    for (Player player : onlinePlayers){
                        String name = player.getName();
                        if (!this.playerStayTown(player.getLocation())){
                            PlayerRefreshinfo playerRefreshinfo = new PlayerRefreshinfo(name, 0, new ArrayList<>());
                            refreshPlayer.put(name, playerRefreshinfo);
                        }
                        logger.info(ChatColor.GREEN+"所有在野外的玩家开始刷新实体");
                        commandSender.sendMessage(ChatColor.GREEN+"所有在野外的玩家开始刷新实体");
                        return true;
                    }
                }else {
                    //flag=false，清空所有集合，停止刷新实体
                    entityUUIDToPlayer.clear();
                    refreshPlayer.clear();
                    logger.info(ChatColor.AQUA+"所有玩家停止刷新实体");
                    commandSender.sendMessage(ChatColor.AQUA+"所有玩家停止刷新实体");
                    return true;
                }
            }else {
                String choice = strings[0];//只能是on，off或者clear
                if (choice.equalsIgnoreCase("on") || choice.equalsIgnoreCase("off")
                        || choice.equalsIgnoreCase("clear") || choice.equalsIgnoreCase("location")){
                    if (choice.equalsIgnoreCase("on")){
                        Player player = plugin.getServer().getPlayer(strings[1]);
                        if (player == null){
                            commandSender.sendMessage("请输入正确的玩家姓名!");
                            return true;
                        }else {
                            noRefreshPlayer.remove(strings[1]);
                            PlayerRefreshinfo playerRefreshinfo = new PlayerRefreshinfo(strings[1], 0, new ArrayList<>());
                            refreshPlayer.put(strings[1], playerRefreshinfo);
                            commandSender.sendMessage(ChatColor.YELLOW+strings[1]+"开始刷怪");
                            return true;
                        }
                    }else if (choice.equalsIgnoreCase("off")){
                        Player player = plugin.getServer().getPlayer(strings[1]);
                        if (player == null){
                            commandSender.sendMessage("请输入正确的玩家姓名!");
                            return true;
                        }else {
                            //玩家不为空
                            refreshPlayer.remove(strings[1]);
                            noRefreshPlayer.add(strings[1]);
                            commandSender.sendMessage(ChatColor.YELLOW+strings[1]+"停止刷怪");
                            return true;
                        }
                    }else if (choice.equalsIgnoreCase("clear")){
                        //清空刷新的怪物
                       this.clearRefreshEntity();
                        logger.info(ChatColor.GOLD+"SrWildEntity刷新的所有实体被清除!");
                        commandSender.sendMessage(ChatColor.GOLD+"SrWildEntity刷新的所有实体被清除!");
                        return true;
                    }else if (choice.equalsIgnoreCase("location")){
                        if (commandSender instanceof Player){
                            Player player = (Player) commandSender;
                            Location location = player.getLocation();
                            player.sendMessage(ChatColor.GREEN+"x:"+location.getBlockX()+"\ty:"
                                    +location.getBlockY()+"\tz:"+location.getBlockZ()+"\tworld:"+location.getWorld().getName());
                            logger.info(ChatColor.GREEN+"x:"+location.getBlockX()+"\ty:"
                                    +location.getBlockY()+"\tz:"+location.getBlockZ()+"\tworld:"+location.getWorld().getName());
                            return true;
                        }else {
                            return false;
                        }
                    }
                }
            }
        }else {
            commandSender.sendMessage(ChatColor.RED+"只有op能执行！");
            return false;
        }
        return false;
    }


    public void reloadSearch(){
        Collection<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers();
        for (Player player : onlinePlayers){
            String name = player.getName();
            Town town = TownyAPI.getInstance().getTown(player.getLocation());
            if (town == null){
                PlayerRefreshinfo playerRefreshinfo = new PlayerRefreshinfo(name, 0, new ArrayList<>());
                refreshPlayer.put(name, playerRefreshinfo);
            }
            logger.info(ChatColor.GREEN+"所有在野外的玩家开始刷新实体");
        }
    }

    /**
     * 玩家进入城镇,
     * @param event
     */
    @EventHandler
    public void onPlayerInTown(PlayerEnterTownEvent event) {
        if (flag){
            String name = event.getPlayer().getName();
            refreshPlayer.remove(name);
            logger.info(name + "开始停止怪物");
        }
    }

    @EventHandler
    public void playerRevive(PlayerRespawnEvent event){
        Location respawnLocation = event.getRespawnLocation();
        if (!this.playerStayTown(respawnLocation)){
            // 返回true则表示在城镇，不用管
            //反之增加
            String name = event.getPlayer().getName();
            this.playerRefreshEntiyt(name);
        }
    }

    /**
     * 玩家离开城镇
     * @param event
     */
    @EventHandler
    public void onPlayerLeaveTown(PlayerLeaveTownEvent event) {
        if (flag) {
            String name = event.getPlayer().getName();
            this.playerRefreshEntiyt(name);
        }
    }

    /**
     * 玩家进入游戏后，要判断玩家登录的位置是否需要刷新怪物
     * @param event
     */
    @EventHandler
    public void onPlayerJoinGame(PlayerJoinEvent event) {
        if (flag){
            Player player = event.getPlayer();
            String name = player.getName();
            if (!this.playerStayTown(player.getLocation()) && !noRefreshPlayer.contains(name)) {
                this.playerRefreshEntiyt(name);
            }
        }
    }

    /**
     * 用于玩家下线后停止刷新移出list集合
     * @param event
     */
    @EventHandler
    public void onPlayerOffOnline(PlayerQuitEvent event) {
        if (flag) {
            Player player = event.getPlayer();
            String name = player.getName();
            //下线后移出刷新列表
//            if (refreshPlayer.get(name) != null) {
//                List<UUID> entityList = refreshPlayer.get(name).getEntityList();
//                for (UUID uuid : entityList){
//                    //删除储存的实体信息
//                   entityUUIDToPlayer.remove(uuid);
//                }
            refreshPlayer.remove(name);
            logger.info(ChatColor.GOLD + "玩家被移出刷新集合");
        }
    }

    /**
     * 相关怪物死亡后，验证是否本插件刷新，再考虑需不需要操作
     * @param event
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
       if (flag){
            LivingEntity entity = event.getEntity();
            UUID uniqueId = entity.getUniqueId();
            if (entityUUIDToPlayer.containsKey(uniqueId) && refreshPlayer.containsKey(entityUUIDToPlayer.get(uniqueId))) {
                //通过死亡实体的uuid获取玩家姓名然后删除uuid
                String playerName = entityUUIDToPlayer.get(uniqueId);
                refreshPlayer.get(playerName).delEntityList(uniqueId);
                entityUUIDToPlayer.remove(uniqueId);
            }
        }
    }

    /**
     * 区块被卸载时，删除区块上的刷新的实体
     * @param event
     */
    @EventHandler
    public void despawnEntity(ChunkUnloadEvent event){
        Chunk chunk = event.getChunk();
        Entity[] entities = chunk.getEntities();
        for (Entity entity : entities){
            UUID uniqueId = entity.getUniqueId();
            if (entityUUIDToPlayer.containsKey(uniqueId)){
                //通过，则表示该实体记录在map集合中,需要删除
                entity.remove();

                //通过uuid获取对应的玩家姓名,然后通过玩家姓名获取对应的list集合
                String name = entityUUIDToPlayer.get(uniqueId);

                if (refreshPlayer.containsKey(name)) {
                    refreshPlayer.get(name).delEntityList(uniqueId);
                }
            }
        }
    }


    /**
     * 定时任务，每隔一段时间在玩家附近刷新实体
     */
    public void timedRdfreshEneity(){
        if (flag) {
            int num = configManager.getNum();//每次执行时刷新的总数
            //遍历需要刷新的玩家
            tag:
            for (String name : refreshPlayer.keySet()) {
                if (noRefreshPlayer.contains(name)){
                    continue;
                }

                int attempts = 0;
                int sum = 0;//用于记录此次刷新生成的总数
                //获取玩家信息
                Player player = plugin.getServer().getPlayer(name);
                PlayerRefreshinfo playerRefreshinfo = refreshPlayer.get(name);
                Location playerlocation = player.getLocation();
                World world = playerlocation.getWorld();

                String worldName = world.getName();
                 boolean pass = false;

                 //如果黑名单的set集合里面包含有该世界的名字，则跳过该玩家
                 if (configManager.getBlacklistWorldSet().contains(worldName)){
                     continue;
                 }

                if (configManager.getBlackListAreaMap().containsKey(worldName)){
                    List<BlackListArea> blackListAreas = configManager.getBlackListAreaMap().get(worldName);
                    for (BlackListArea blackListArea : blackListAreas){
                        int blockX = playerlocation.getBlockX();
                        int blockZ = playerlocation.getBlockZ();
                        if (blackListArea.getX1() >= blockX && blockX >= blackListArea.getX2()
                                && blackListArea.getY1() >= blockZ && blockZ >= blackListArea.getY2()){
                            //tag为循环标志，此处会直接结束tag标志处的循环
                            continue tag;
                        }
                    }

                }

                //玩家危险度,用于刷新实体的等级
                int riskLevel = this.prRisk(player);
                if (riskLevel == 0){
                    //等于0不刷新怪物,直接跳过
                    continue;
                }

                LevelRefresh refreshList = null;
                List<LevelRefresh> levelRefreshesList = configManager.getBiomeEntityRefreshSettings().getLevelRefreshesList();
                for (LevelRefresh levelRefresh : levelRefreshesList){
                    if (riskLevel > levelRefresh.getRiskMin() && riskLevel <= levelRefresh.getRiskMax()){
                        refreshList = levelRefresh;
                    }
                }

                if (refreshList == null){
                    logger.info(ChatColor.MAGIC+name+"所在群系"+"没有危险度为:"+riskLevel+"时适配的刷新列表");
                    continue;
                }



                //获取玩家位置信息
                int blockZ = playerlocation.getBlockZ();
                int blockX = playerlocation.getBlockX();
                int blockY = playerlocation.getBlockY();
                Biome biome = world.getBiome(blockX, blockZ);
                String biomeName = biome.name();

                List<EntityCondition> entityConditionList = null;

                HashMap<String, List<EntityCondition>> entityConditionHashMap = refreshList.getEntityConditionHashMap();
                for (String refreshBiome : entityConditionHashMap.keySet()){
                    if (biomeName.contains(refreshBiome)){
                        entityConditionList = entityConditionHashMap.get(refreshBiome);
                    }
                }


                if (entityConditionList == null){
                    logger.info(ChatColor.RED+"没有配置"+biomeName+"群系的实体刷新案例");
                    continue;
                }

                for (EntityCondition entityCondition : entityConditionList){


                    int yMin = entityCondition.getyMin();
                    int yMax = entityCondition.getyMax();

                    //如果玩家所在位置不再设置的y轴高度内，则不刷新此entity
                    if (yMin > blockY || yMax < blockY){
//                        logger.info(ChatColor.RED+"刷新高度不通过"+entityCondition.getEntityName());
                        continue;
                    }
                    float v = random.nextFloat();
                    if ( entityCondition.getWeight() < v){
                        //刷新不通过,进入下一个验证的实体
//                        logger.info(ChatColor.RED+"刷新权重不通过"+entityCondition.getEntityName()+"权重:"+entityCondition.getWeight()+"\t"+v);
                        continue;
                    }

                    int nums = entityCondition.getNums();//当前实体需要刷新的数量

                    for (int i = 0;i<nums &&
                            playerRefreshinfo.getEntityList().size() < configManager.getTotal()
                             && sum <= num
                            && attempts <= configManager.getAttempts(); i++) {

                        // 生成x和y坐标,会随机在玩家方圆15个方块的距离内随机生成
                        int x =blockX + random.nextInt(30) - 15; // 生成0到60之间的随机数，然后减去30，得到-30到30的范围
                        int z =blockZ + random.nextInt(30) - 15; // 同上,在玩家附近随机位置生成
                        int y =blockY + random.nextInt(3) - 2; //在玩家Y轴上下3格尝试刷新
                        //刷新在地上，直接获取最高的x z 最高处的坐标
                        if (entityCondition.getEntitySite() == EntitySite.ON_GROUND) {

                            Location location = new Location(world, x, y , z);

                            //判断位置是否符合
                            if (ConditionCheck.checkEntityRefresh(world, location, entityCondition)) {
                                //通过则生成
//                                Entity entity = world.spawnEntity(location, EntityType.valueOf(entityCondition.getEntityName()));
                                Entity  entity;
                                //MC原生实体
                                if (entityCondition.getEntityType() == SpawnEntityType.PROTOGENESIS){
                                    entity = world.spawnEntity(location, EntityType.valueOf(entityCondition.getEntityName()));
                                }else {
                                    //刷新MM怪物
                                    try {
                                        entity = mythicMobs.getAPIHelper().spawnMythicMob(entityCondition.getEntityName(),location,riskLevel);
                                    } catch (InvalidMobTypeException e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                                logger.info(ChatColor.GOLD + "在" + x + " " + y + " " + z + "位置刷新了" + entityCondition.getEntityName());
                                UUID uniqueId = entity.getUniqueId();

                                //存入玩家对应的刷新的实体集合，便于寻找
                                entityUUIDToPlayer.put(uniqueId, name);

                                //存入
                                playerRefreshinfo.addEntityList(uniqueId);
                                attempts++;
                                sum++;

                            }
                        } else {
                            //不刷新在地上，在定义的范围内随机高度，概率生成实体，
                            for (int c = 0; i<nums && c < 10 && sum <= num &&
                                    playerRefreshinfo.getEntityList().size() < configManager.getTotal();
                                 c++) {
                                //循环十次验证刷新位置
                                y = blockY + random.nextInt(3) - 3;//在玩家所在高度的上下三格内生成，便于当玩家在洞穴时，定位洞穴
                                x = blockX + random.nextInt(30) - 15; // 生成0到60之间的随机数，然后减去30，得到-30到30的范围
                                z = blockZ + random.nextInt(30) - 15; // 同上,在玩家附近随机位置生成
                                if (entityCondition.getEntitySite() == EntitySite.ON_GROUND) {
                                    y = world.getHighestBlockYAt(x,z);
                                }

                                Location location = new Location(world, x, y + 1, z);

                                //判断位置是否符合
                                if (ConditionCheck.checkEntityRefresh(world, location, entityCondition)) {
                                    //通过则生成
                                    Entity  entity;
                                    //MC原生实体
                                    if (entityCondition.getEntityType() == SpawnEntityType.PROTOGENESIS){
                                        entity = world.spawnEntity(location, EntityType.valueOf(entityCondition.getEntityName()));
                                    }else {
                                        //刷新MM怪物
                                        try {
                                            entity = mythicMobs.getAPIHelper().spawnMythicMob(entityCondition.getEntityName(), location);
                                        } catch (InvalidMobTypeException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }

                                    logger.info(ChatColor.GOLD + "在" + x + " " + y + " " + z + "位置刷新了" + entityCondition.getEntityName());
                                    UUID uniqueId = entity.getUniqueId();

                                    //存入玩家对应的刷新的实体集合，便于寻找
                                    entityUUIDToPlayer.put(uniqueId, name);

                                    //存入
                                    playerRefreshinfo.addEntityList(uniqueId);
                                    i++;
                                    sum++;
                                    attempts++;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }


    public List<String> getNoRefreshPlayer() {
        return noRefreshPlayer;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }


    /**
     * 清空所有记录在的实体
     */
    public void clearRefreshEntity(){
        Server server = plugin.getServer();
        Collection<? extends Player> onlinePlayers = server.getOnlinePlayers();
        for (Player player : onlinePlayers){
            String name = player.getName();
            List<UUID> entityList = refreshPlayer.get(name).getEntityList();
            for (UUID uuid : entityList){
                Entity entity = server.getEntity(uuid);
                if (entity != null){
                    entity.remove();
                    if (entityUUIDToPlayer.containsKey(uuid)){
                        entityUUIDToPlayer.remove(uuid);
                    }
                }else {
                    if (entityUUIDToPlayer.containsKey(uuid)){
                        entityUUIDToPlayer.remove(uuid);
                    }
                }
            }
            //entity清除完后
            refreshPlayer.get(name).setEntityList(new ArrayList<>());
        }

        for (UUID uuid : entityUUIDToPlayer.keySet()){
            Entity entity = plugin.getServer().getEntity(uuid);
            if (entity != null){
                entity.remove();
                entityUUIDToPlayer.remove(uuid);
            }else {
                entityUUIDToPlayer.remove(uuid);
            }
        }
    }

    public void playerRefreshEntiyt(String name){
        if (!noRefreshPlayer.contains(name) && !entityUUIDToPlayer.containsValue(name)) {

            PlayerRefreshinfo playerRefreshinfo = new PlayerRefreshinfo(name, 0, new ArrayList<>());
            refreshPlayer.put(name, playerRefreshinfo);
            logger.info(name + "开始刷新实体");
        }else {
            List<UUID> uuidList = new ArrayList<>();
            for (UUID uuid : entityUUIDToPlayer.keySet()){
                String s = entityUUIDToPlayer.get(uuid);
                if (s.equals(name)){
                    uuidList.add(uuid);
                }
            }
            PlayerRefreshinfo playerRefreshinfo = new PlayerRefreshinfo(name,0, uuidList);
            refreshPlayer.put(name, playerRefreshinfo);
            logger.info(name + "开始刷新实体");
        }
    }

    /**
     * 用于判断玩家是否在城镇
     * @param location
     * @return
     */
    public boolean playerStayTown(Location location) {
        Town town = TownyAPI.getInstance().getTown(location);
        if (town == null){
            //为空则不再城镇
            return false;
        }else {
            return true;
        }
    }
}
