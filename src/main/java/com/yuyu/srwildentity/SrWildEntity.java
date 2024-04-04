package com.yuyu.srwildentity;

import com.yuyu.srwildentity.config.ConfigManager;
import com.yuyu.srwildentity.listener.EntityRefreshListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 1.在Towny内不刷新怪物(需要Towny依赖)
 * 2.可以通过指令控制,例如 randspawn Player_Name, 那么插件会在这个玩家周围刷怪
 * 3.不同的生物群系刷怪不同
 * 4.要求不需要重启服务器就可以刷新配置(可以通过指令实现配置读取)
 * 5.(对于玩家的)刷新速率, 比如每秒一次. 即上秒因为这个玩家而刷新了一个新的实体, 则不会在接下来的一秒内刷新(注册定时任务)
 * 6.玩家周边生成的最大数量, 是否达到上限 (papi, 跟危险度相关，需要导入危险度插件,配置文件中定义最大生成等)
 */

public final class SrWildEntity extends JavaPlugin {

    private ConfigManager configManager;


    @Override
    public void onEnable() {
        getLogger().info(ChatColor.AQUA+"SrWildEntity开始运行");
        onload();


        //TODO(注册监听类,测试)
        EntityRefreshListener entityRefreshListener = new EntityRefreshListener(this.getLogger(),configManager,this);

        Bukkit.getPluginManager().registerEvents(entityRefreshListener,this);

        this.getCommand("despawn").setExecutor(entityRefreshListener);

    }

    /**
     *
     */
    public void onload(){
        this.configManager = new ConfigManager(this);

    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
