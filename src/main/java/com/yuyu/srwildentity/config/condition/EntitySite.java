package com.yuyu.srwildentity.config.condition;

/**
 * 实体刷新的位置
 */
public enum EntitySite {
    ON_GROUND(1), //在地上

    ON_WATER(2),  //在水上

    ON_MAGMA(3),   //在岩浆上

    UNDER_GROUND(4);//在地下

    private final int id;

    public int getId() {
        return id;
    }

    EntitySite( int id) {

        this.id = id;
    }
    public static EntitySite fromId(int id) {
        for (EntitySite type : EntitySite.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant with id " + id);
    }
}
