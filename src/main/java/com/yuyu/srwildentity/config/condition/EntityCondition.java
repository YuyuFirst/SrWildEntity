package com.yuyu.srwildentity.config.condition;

/**
 * @BelongsProject: SrWildEntity
 * @BelongsPackage: com.yuyu.srwildentity.config.condition
 * @FileName: EntityCondition
 * @Author: 峰。
 * @Date: 2024/4/3-15:20
 * @Version: 1.0
 * @Description: 每个entity刷新需要的条件
 */
public class EntityCondition {
    private final String entityName;//实体的名字
    private final String biome;//实体刷新的群系
    private final EntitySite entitySite;//刷新位置
    private final int light;//刷新亮度
    private final long timing;//刷新时间
    private final int nums;//刷新的数量
    private final int yMax;
    private final int yMin;

    public EntityCondition(String entityName, String biome, EntitySite entitySite, int light, long timing, int nums, int yMax, int yMin) {
        this.entityName = entityName;
        this.biome = biome;
        this.entitySite = entitySite;
        this.light = light;
        this.timing = timing;
        this.nums = nums;
        this.yMax = yMax;
        this.yMin = yMin;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getBiome() {
        return biome;
    }

    public EntitySite getEntitySite() {
        return entitySite;
    }

    public int getLight() {
        return light;
    }

    public long getTiming() {
        return timing;
    }

    public int getNums() {
        return nums;
    }

    public int getyMax() {
        return yMax;
    }

    public int getyMin() {
        return yMin;
    }
}
