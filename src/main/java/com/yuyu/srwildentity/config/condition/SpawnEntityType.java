package com.yuyu.srwildentity.config.condition;

public enum SpawnEntityType {
    PROTOGENESIS(0),//mc原生实体
    MMENTITY(1),//MM怪物
    GERMENTITY(2),
    NULL(199999);//萌芽怪物

    private final int id;
    public int getId() {
        return id;
    }
    SpawnEntityType(int id) {
        this.id = id;
    }
    public static SpawnEntityType fromId(int id) {
        for (SpawnEntityType type : SpawnEntityType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return NULL;
    }
}
