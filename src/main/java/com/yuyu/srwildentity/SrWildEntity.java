package com.yuyu.srwildentity;

import com.yuyu.srwildentity.JDBC.JdbcSqlClass;
import com.yuyu.srwildentity.config.ConfigManager;
import com.yuyu.srwildentity.listener.AreaRefershListener;
import com.yuyu.srwildentity.listener.EntityRefreshListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 1.在Towny内不刷新怪物(需要Towny依赖)
 * 2.可以通过指令控制,例如 randspawn Player_Name, 那么插件会在这个玩家周围刷怪
 * 3.不同的生物群系刷怪不同
 * 4.要求不需要重启服务器就可以刷新配置(可以通过指令实现配置读取)
 * 5.(对于玩家的)刷新速率, 比如每秒一次. 即上秒因为这个玩家而刷新了一个新的实体, 则不会在接下来的一秒内刷新(注册定时任务)
 * 6.玩家周边生成的最大数量, 是否达到上限 (papi, 跟危险度相关，需要导入危险度插件,配置文件中定义最大生成等)
 */

public final class SrWildEntity extends JavaPlugin implements CommandExecutor {

    private EntityRefreshListener entityRefreshListener;
    private AreaRefershListener areaRefershListener;

    public static Plugin getInance(){
        return  Bukkit.getPluginManager().getPlugin("SrWildEntity");

    }


    @Override
    public void onEnable() {
        getLogger().info(ChatColor.AQUA+"SrWildEntity开始运行");
        ConfigManager onload = onload();


        //TODO(注册监听类,测试)
         this.entityRefreshListener = new EntityRefreshListener(this.getLogger(),onload,this);
         this.areaRefershListener = new AreaRefershListener(onload);

        Bukkit.getPluginManager().registerEvents(entityRefreshListener,this);
        Bukkit.getPluginManager().registerEvents(areaRefershListener,this);

        //注册定时任务
        this.getServer().getScheduler().scheduleSyncRepeatingTask
                (this,this.entityRefreshListener::timedRdfreshEneity,
                        0,this.entityRefreshListener.getConfigManager().getRefreshTime()*20);

        getLogger().info(ChatColor.DARK_GREEN+"SrWildEntity定时任务触发");


        this.getCommand("despawn").setExecutor(entityRefreshListener);
        this.getCommand("SrWildEntity").setExecutor(this::onCommand);

    }

    /**
     *
     */
    public ConfigManager onload(){
       return new ConfigManager(this);

    }


    @Override
    public void onDisable() {
        //关闭时，保存数据
        JdbcSqlClass.deleteData();
        JdbcSqlClass.saveData(areaRefershListener.getUuidStringHashMap());
    }

    /**
     * 重载插件
     * @param commandSender
     * @param command
     * @param s
     * @param strings
     * @return
     */
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings){
        if (!commandSender.isOp()){
            getLogger().info(ChatColor.RED+"只有OP能执行此指令!");
        }
        if (!s.equalsIgnoreCase("srwildentity")){
            //前缀不通过
            return false;
        } else {
            if (strings.length == 0){
                commandSender.sendMessage(ChatColor.RED+"请加上reload操作");
            }else {
                if (strings[0].equalsIgnoreCase("reload")){

                    getLogger().info(ChatColor.GOLD+"SrWildEntity定时任务关闭");
                    Bukkit.getScheduler().cancelTasks(this);

                    getLogger().info(ChatColor.AQUA+"SrWildEntity重新读取配置文件");
                    commandSender.sendMessage(ChatColor.YELLOW+"SrWildEntity重新读取配置文件");
                    ConfigManager onload = this.onload();
                    entityRefreshListener.setConfigManager(onload);
                    areaRefershListener.setConfigManager(onload);

                    //注册定时任务
                    this.getServer().getScheduler().scheduleSyncRepeatingTask
                            (this,this.entityRefreshListener::timedRdfreshEneity,
                                    0,this.entityRefreshListener.getConfigManager().getRefreshTime()*20);

                    getLogger().info(ChatColor.DARK_GREEN+"SrWildEntity定时任务触发");

                }
            }
        }
        return false;
    }

}
