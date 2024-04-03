package com.yuyu.srwildentity.listener;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

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
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        return false;
    }
}
