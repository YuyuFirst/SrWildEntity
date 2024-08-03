package com.yuyu.srwildentity.pojo;

import com.yuyu.srwildentity.JDBC.JdbcSqlClass;
import com.yuyu.srwildentity.config.condition.EntityCondition;

import java.util.HashMap;
import java.util.List;

/**
 * @author 峰。
 * @version 1.0
 * @project SrWildEntity
 * @date 2024/7/8 23:13:33
 * @description 区域刷新的类
 */
public class AreaRefresh {
    private final int x1;
    private final int z1;
    private final int x2;
    private final int z2;
    private final int y1;
    private final int y2;
    private final String worldName;
    private final String area;
    private final HashMap<String,EntityCondition> entityConditionList;
    private HashMap<String,Integer> entityNums;//用于记录怪物区域的怪物数量

    public AreaRefresh(int x1, int z1, int x2, int z2, String worldName, HashMap<String,EntityCondition> entityConditionList, int y1, int y2, String area) {
        if (x2 >= x1){
            this.x2 = x2;
            this.x1 = x1;
        }else {
            this.x2 = x1;
            this.x1 = x2;
        }
        if (z2 >= z1){
            this.z2 = z2;
            this.z1 = z1;
        }else {
            this.z2 = z1;
            this.z1 = z2;
        }
        if (y2 >= y1){
            this.y2 = y2;
            this.y1 = y1;
        }else {
            this.y2 = y1;
            this.y1 = y2;
        }
        this.area = area;
        this.worldName = worldName;
        this.entityConditionList = entityConditionList;
        this.entityNums = new HashMap<>();


        //属性初始化完成后,查询数据库

    }



    public String getArea() {
        return area;
    }

    public void setEntityNums(String entityName, int num) {
        this.entityNums.put(entityName, num);
    }

    public int getY1() {
        return y1;
    }

    public int getY2() {
        return y2;
    }

    public int getX1() {
        return x1;
    }

    public int getZ1() {
        return z1;
    }

    public int getX2() {
        return x2;
    }

    public int getZ2() {
        return z2;
    }

    public String getWorldName() {
        return worldName;
    }

    public HashMap<String, Integer> getEntityNums() {
        return entityNums;
    }

    public HashMap<String, EntityCondition> getEntityConditionList() {
        return entityConditionList;
    }
}
