package com.yuyu.srwildentity.config.condition;

import java.util.HashMap;
import java.util.List;

/**
 * @BelongsProject: SrWildEntity
 * @BelongsPackage: com.yuyu.srwildentity.config.condition
 * @FileName: LevelRefresh
 * @Author: 峰。
 * @Date: 2024/4/25-22:06
 * @Version: 1.0
 * @Description: 按照等级记录怪物
 */
public class LevelRefresh {
    private final int riskMax;
    private final int riskMin;
    //                      群系名
    private final HashMap<String, List<EntityCondition>> entityConditionHashMap;

    public int getRiskMax() {
        return riskMax;
    }

    public int getRiskMin() {
        return riskMin;
    }

    public HashMap<String, List<EntityCondition>> getEntityConditionHashMap() {
        return entityConditionHashMap;
    }

    public LevelRefresh(int riskMax, int riskMin, HashMap<String, List<EntityCondition>> entityConditionHashMap) {
        this.riskMax = riskMax;
        this.riskMin = riskMin;
        this.entityConditionHashMap = entityConditionHashMap;
    }
}
