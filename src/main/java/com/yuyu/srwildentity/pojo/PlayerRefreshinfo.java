package com.yuyu.srwildentity.pojo;

import java.util.List;
import java.util.UUID;

/**
 * @BelongsProject: SrWildEntity
 * @BelongsPackage: com.yuyu.srwildentity.pojo
 * @FileName: PlayerRefreshinfo
 * @Author: 峰。
 * @Date: 2024/4/4-17:35
 * @Version: 1.0
 * @Description: 储存玩家相关的刷新信息
 */
public class PlayerRefreshinfo {
    private  final String playerName;
    private Integer nums;
    private List<UUID> entityList;

    public void addEntityList(UUID uuid){
        this.entityList.add(uuid);
    }

    public void delEntityList(UUID uuid){

        this.entityList.remove(uuid);
    }

    public String getPlayerName() {
        return playerName;
    }

    public Integer getNums() {
        return nums;
    }

    public void setNums(Integer nums) {
        this.nums = nums;
    }

    public List<UUID> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<UUID> entityList) {
        this.entityList = entityList;
    }

    public PlayerRefreshinfo(String playerName, Integer nums,List<UUID> uuids) {
        this.playerName = playerName;
        this.nums = nums;
        this.entityList = uuids;
    }
}
