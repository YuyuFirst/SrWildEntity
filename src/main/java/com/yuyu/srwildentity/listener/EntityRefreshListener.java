package com.yuyu.srwildentity.listener;


import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.PlayerEnterTownEvent;
import com.palmergames.bukkit.towny.event.PlayerLeaveTownEvent;
import com.palmergames.bukkit.towny.object.Town;
import com.yuyu.srwildentity.conditionCheck.ConditionCheck;
import com.yuyu.srwildentity.config.ConfigManager;
import com.yuyu.srwildentity.config.condition.EntityCondition;
import com.yuyu.srwildentity.config.condition.EntitySite;
import com.yuyu.srwildentity.pojo.PlayerRefreshinfo;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.block.Biome;
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



    public EntityRefreshListener(Logger logger, ConfigManager configManager, Plugin plugin){

        this.entityUUIDToPlayer = new HashMap<>();
        this.refreshPlayer = new HashMap<>();
        this.configManager = configManager;
        this.random = new Random();
        this.logger = logger;
        this.flag = true;
        this.noRefreshPlayer = new ArrayList<>();
        this.plugin = plugin;


        logger.info(ChatColor.DARK_GREEN+"SrWildEntity定时任务触发");
        //注册定时任务
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask
                (this.plugin,this::timedRdfreshEneity,0,configManager.getRefreshTime()*20);

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
                return true;
            }else {
                String choice = strings[0];//只能是on，或者off
                if (choice.equalsIgnoreCase("on") || choice.equalsIgnoreCase("off")){
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
                    }else {
                        Player player = plugin.getServer().getPlayer(strings[1]);
                        if (player == null){
                            commandSender.sendMessage("请输入正确的玩家姓名!");
                            return true;
                        }else {
                            //玩家不为空
                            refreshPlayer.remove(strings[1]);
                            noRefreshPlayer.add(strings[1]);
                            return true;
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

    /**
     * 玩家离开城镇
     * @param event
     */
    @EventHandler
    public void onPlayerLeaveTown(PlayerLeaveTownEvent event) {
        if (flag) {
            String name = event.getPlayer().getName();
            if (!noRefreshPlayer.contains(name)) {
                PlayerRefreshinfo playerRefreshinfo = new PlayerRefreshinfo(name, 0, new ArrayList<>());
                refreshPlayer.put(name, playerRefreshinfo);
                logger.info(name + "开始刷新实体");
            }
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
            Town town = TownyAPI.getInstance().getTown(player.getLocation());
            if (town == null && !noRefreshPlayer.contains(name)) {
                PlayerRefreshinfo playerRefreshinfo = new PlayerRefreshinfo(name, 0, new ArrayList<>());
                refreshPlayer.put(name, playerRefreshinfo);
                logger.info(ChatColor.MAGIC + player.getName() + "在野外登录，开始刷新实体");
            }
        }
    }

    /**
     * 用于玩家下线后停止刷新移出list集合
     * @param event
     */
    @EventHandler
    public void onPlayerOffOnline(PlayerQuitEvent event){
      if (flag) {
            Player player = event.getPlayer();
            String name = player.getName();
            //下线后移出刷新列表
            if (refreshPlayer.get(name) != null) {
                refreshPlayer.remove(name);
                logger.info(ChatColor.GOLD + "玩家被移出刷新集合");
            }
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
            if (entityUUIDToPlayer.containsKey(uniqueId)) {
                //通过死亡实体的uuid获取玩家姓名然后删除uuid
                String playerName = entityUUIDToPlayer.get(entityUUIDToPlayer);
                refreshPlayer.get(playerName).delEntityList(uniqueId);
            }
        }
    }


    /**
     * 定时任务，每隔一段时间在玩家附近刷新实体
     */
    public void timedRdfreshEneity(){
        if (flag) {
            int num = configManager.getNum();//每次执行时刷新的总数
            for (String name : refreshPlayer.keySet()) {
                int sum = 0;//用于记录此次刷新生成的总数
                //获取玩家信息
                Player player = plugin.getServer().getPlayer(name);
                PlayerRefreshinfo playerRefreshinfo = refreshPlayer.get(name);

                //玩家危险度,用于刷新实体的等级
                int level = this.prRisk(player);
                //TODO(危险度提示,后续需要使用这个变量去控制怪物的参数等级`)
                logger.info(ChatColor.GREEN + "危险度为:" + level);


                Location playerlocation = player.getLocation();

                //获取玩家位置信息
                World world = playerlocation.getWorld();
                int blockZ = playerlocation.getBlockZ();
                int blockX = playerlocation.getBlockX();
                Biome biome = world.getBiome(blockX, blockZ);
                String biomeName = biome.name();

                //需要刷新的entity列表
                List<String> entityList = configManager.getBiomeEntityRefreshSettings().getBiomeEntityMap().get(biomeName);
                //entity条件配置
                HashMap<String, EntityCondition> entityConditionHashMap =
                        configManager.getBiomeEntityRefreshSettings().getBiomeEntityConditionMap().get(biomeName);

                if (entityList == null || entityConditionHashMap == null){
                    throw new RuntimeException("entityList和entityConditionHashMap读取出错");
                }

                for (String entityName : entityList){
                    //获取entity配置
                    EntityCondition entityCondition = entityConditionHashMap.get(entityName);

                    int nums = entityCondition.getNums();//当前实体需要刷新的数量

                    for (int i = 0;i<nums &&
                            playerRefreshinfo.getEntityList().size() < configManager.getTotal()
                            && sum <= num; i++) {

                        // 生成x和y坐标，范围在-30到30之间（包括-30和30）
                        int x = random.nextInt(61) - 30; // 生成0到60之间的随机数，然后减去30，得到-30到30的范围
                        int z = random.nextInt(61) - 30; // 同上,在玩家附近随机位置生成
                        int y;
                        //刷新在地上，直接获取最高的x z 最高处的坐标
                        if (entityCondition.getEntitySite() == EntitySite.ON_GROUND) {
                            y = world.getHighestBlockYAt(x, z);
                        } else {
                            int range = entityCondition.getyMax() - entityCondition.getyMin() + 1;
                            y = random.nextInt(range) - 1;//随机高度
                        }

                        Location location = new Location(world, x, y + 1, z);

                        //判断位置是否符合
                        if (ConditionCheck.checkEntityRefresh(world, location, entityCondition)) {
                            //通过则生成
                            Entity entity = world.spawnEntity(location, EntityType.valueOf(entityCondition.getEntityName()));
                            logger.info(ChatColor.GOLD+"在"+x+" "+y+" "+z+"位置刷新了"+entityName);
                            UUID uniqueId = entity.getUniqueId();

                            //存入玩家对应的刷新的实体集合，便于寻找
                            entityUUIDToPlayer.put(uniqueId,name);

                            //存入
                            playerRefreshinfo.addEntityList(uniqueId);

                            sum++;

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



}