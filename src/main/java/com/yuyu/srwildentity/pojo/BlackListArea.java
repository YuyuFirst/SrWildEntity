package com.yuyu.srwildentity.pojo;

/**
 * @author 峰。
 * @version 1.0
 * @project SrWildEntity
 * @date 2024/7/7 14:23:54
 * @description 黑名单区域
 */
public class BlackListArea {
    private String worldName;
    private int x1;
    private int y1;
    private int x2;
    private int y2;

    public BlackListArea(String worldName, int x1, int y1, int x2, int y2) {
        this.worldName = worldName;
        this.setXY(x1,y1,x2,y2);
    }

    /**
     * 此方法可以固定x1>x2   y1>y2
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public void setXY(int x1, int y1,int x2, int y2) {
        if (x1 > x2) {
            this.x1 = x1;
            this.x2 = x2;
        }else {
            this.x1 = x2;
            this.x2 = x1;
        }

        if (y1 > y2) {
            this.y1 = y1;
            this.y2 = y2;
        }else {
            this.y1 = y2;
            this.y2 = y1;
        }
    }

    public String getWorldName() {
        return worldName;
    }

    public boolean isPointInRectangle(int x, int z) {
        // 确保x1 < x2 和 z1 < z2，如果不是，则交换它们（但在这个示例中，我们假设输入总是有效的）

        // 检查x坐标是否在范围内
        if (x >= x1 && x <= x2) {
            // 检查z坐标是否在范围内
            if (z >= y1 && z <= y2) {
                return true; // 如果两个条件都满足，则点在矩形内
            }
        }

        // 如果不满足任一条件，则点在矩形外
        return false;
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }
}
