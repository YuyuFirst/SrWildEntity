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
    private final SpawnEntityType spawnEntityType;//实体的来源
    private final EntitySite entitySite;//刷新位置
    private final int light;//刷新亮度
    private final long startTiming;//刷新时间
    private final long endTimeing;//结束刷新的时间
    private final int nums;//刷新的数量
    private final int yMax;
    private final int yMin;
    private final int riskMax;
    private final int riskMin;

    public EntityCondition(String entityName, String biome, SpawnEntityType spawnEntityType, EntitySite entitySite, int light, long startTiming, long endTiming, int nums, int yMax, int yMin,int riskMax,int riskMin) {
        this.entityName = entityName;
        this.biome = biome;
        this.spawnEntityType = spawnEntityType;
        this.entitySite = entitySite;
        this.light = light;
        this.startTiming = startTiming;
        this.endTimeing = endTiming;
        this.nums = nums;
        this.yMax = yMax;
        this.yMin = yMin;
        this.riskMax = riskMax;
        this.riskMin = riskMin;
    }

    public SpawnEntityType getSpawnEntityType() {
        return spawnEntityType;
    }

    public int getRiskMax() {
        return riskMax;
    }

    public int getRiskMin() {
        return riskMin;
    }

    public SpawnEntityType getEntityType() {
        return spawnEntityType;
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

    public long getStartTiming() {
        return startTiming;
    }

    public long getEndTimeing() {
        return endTimeing;
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

    @Override
    public String toString() {
        return "EntityCondition{" +
                "entityName='" + entityName + '\'' +
                ", biome='" + biome + '\'' +
                ", spawnEntityType=" + spawnEntityType +
                ", entitySite=" + entitySite +
                ", light=" + light +
                ", startTiming=" + startTiming +
                ", endTimeing=" + endTimeing +
                ", nums=" + nums +
                ", yMax=" + yMax +
                ", yMin=" + yMin +
                ", riskMax=" + riskMax +
                ", riskMin=" + riskMin +
                '}';
    }
}
