package com.yuyu.srwildentity.conditionCheck;

import com.yuyu.srwildentity.config.condition.EntityCondition;
import com.yuyu.srwildentity.config.condition.EntitySite;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashSet;

/**
 * @BelongsProject: SrWildEntity
 * @BelongsPackage: com.yuyu.srwildentity.conditionCheck
 * @FileName: ConditionCheck
 * @Author: 峰。
 * @Date: 2024/4/4-14:50
 * @Version: 1.0
 * @Description: 用于检查是否通过
 */
public class ConditionCheck {


    //实体碰撞
    public static final HashSet<Material> noEntityCollision = new HashSet<>();

    /**
     * 检查entity生成位置是否符合
     * 1.位置
     * 2.亮度
     * 3.刷新的世界的时间
     * 4.高度
     * @param world
     * @param location
     * @param entityCondition
     * @return
     */
    public static boolean checkEntityRefresh(World world, Location location, EntityCondition entityCondition){

        return (checkLocation(world,location,entityCondition.getEntitySite())
                && checkLight(world,location,entityCondition.getLight())
                && checkTimed(world,entityCondition.getStartTiming(), entityCondition.getEndTimeing())
                && checkY(location,entityCondition.getyMax(), entityCondition.getyMin()));
    }

//    private static boolean checkHeight(Location location, World world) {
//        int blockY = location.getBlockY();
//        for (int i = 1;i<2;i++){
//            blockY+=2;
//            location.setY(blockY);
//            Block block = location.getBlock();
//            if(block.getType() != Material.AIR
//            && block.getType() != Material.WATER
//            && block.getType() != Material.STATIONARY_WATER
//            && block.getType() != Material.LONG_GRASS
//            && block.getType() != Material.YELLOW_FLOWER
//            && block.getType() != Material.FLOWER_POT
//            && block.getType() != Material.CHORUS_FLOWER){
//                return false;
//            }
//        }
//        return true;
//    }

    /**
     * 检查刷新位置
     * @param world
     * @param location
     * @param entitySite
     * @return
     */
    public static boolean checkLocation(World world, Location location, EntitySite entitySite){

        if (entitySite == null || world == null || location == null){
            throw new RuntimeException("checkLocation接收的参数错误");
        }

        if (entitySite == EntitySite.ON_GROUND){
            //实体的刷新位置
            Block block = world.getBlockAt(location);
            //实体的落脚点
            Block blockAt = world.getBlockAt(new Location(world,location.getBlockX(),location.getBlockY() + 1,location.getBlockZ()));
            Block blockAt0 = world.getBlockAt(new Location(world,location.getBlockX(),location.getBlockY() + 2,location.getBlockZ()));
            Block blockAt1 = world.getBlockAt(new Location(world,location.getBlockX(),location.getBlockY() - 1,location.getBlockZ()));

            return  (noEntityCollision.contains(block.getType()) &&
                    noEntityCollision.contains(blockAt.getType()) &&
                    !noEntityCollision.contains(blockAt1.getType()) &&
                    noEntityCollision.contains(blockAt0.getType()));

        }
        if (entitySite == EntitySite.NULL){
            return true;
        }
        if (entitySite == EntitySite.ON_WATER){
            Block blockAt = world.getBlockAt(location);
            return blockAt.getType().name().equals(Material.WATER.name());
        }
        if (entitySite == EntitySite.ON_MAGMA){
            Block blockAt = world.getBlockAt(location);
            return blockAt.getType().name().equals(Material.MAGMA.name());
        }
        if (entitySite == EntitySite.UNDER_GROUND){
            Block blockAt = world.getBlockAt(location);
            Block block = world.getBlockAt(new Location(world,location.getX(),location.getY()+1,location.getZ()));
            return  blockAt.getType().name().equals(Material.AIR.name()) && block.getType().name().equals(Material.AIR.name());
        }
        return false;
    }

    /**
     * 检查亮度
     * @param world
     * @param location
     * @param light
     * @return
     */
    public static boolean checkLight(World world, Location location, int  light){

        if (world == null || location == null || location == null){
            throw new RuntimeException("checkLight接收的参数错误");
        }

        Block blockAt = world.getBlockAt(location);
        int lightLevel = blockAt.getLightLevel();
        return lightLevel <= light;//若方块位置的亮度小于或者等于设定值，则通过
    }

    public static boolean checkTimed(World world ,long stimed,long etimed){
        if (world == null ){
            throw new RuntimeException("checkTime参数错误");
        }
        long time = world.getTime();

        return time > stimed && time < etimed;
    }

    public static boolean checkY(Location location,int yMax,int yMin){
        if (location == null){
            throw new RuntimeException("checkY接收参数错误");
        }
        int blockY = location.getBlockY();
        return blockY >= yMin && blockY <= yMax;
    }

}
