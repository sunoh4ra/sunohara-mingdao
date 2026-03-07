package com.sunohara.mingdao;

/**
 * 玩家名刀数据
 * 记录每个玩家是否已使用过名刀，以及冷却时间
 */
public class PlayerMingdaoData {

    private final String playerName;
    private boolean used;
    private long lastUsedTime;
    private int usageCount;

    public PlayerMingdaoData(String playerName) {
        this.playerName = playerName;
        this.used = false;
        this.lastUsedTime = 0;
        this.usageCount = 0;
    }

    public String getPlayerName() {
        return playerName;
    }

    /**
     * 检查是否可以使用名刀
     */
    public boolean canUseMingdao(MingdaoMode mode, int cooldownMinutes) {
        if (mode == MingdaoMode.ONCE) {
            // 一次性模式：检查是否已使用
            return !used;
        } else if (mode == MingdaoMode.REGENERATE) {
            // 可再生模式：检查冷却时间
            if (lastUsedTime == 0) {
                return true; // 从未使用过
            }
            long elapsedMinutes = (System.currentTimeMillis() - lastUsedTime) / (60 * 1000);
            return elapsedMinutes >= cooldownMinutes;
        }
        return false;
    }

    /**
     * 获取剩余冷却时间（分钟）
     */
    public int getRemainingCooldown(int cooldownMinutes) {
        if (lastUsedTime == 0) {
            return 0;
        }
        long elapsedMinutes = (System.currentTimeMillis() - lastUsedTime) / (60 * 1000);
        int remaining = (int) (cooldownMinutes - elapsedMinutes);
        return Math.max(0, remaining);
    }

    /**
     * 标记为已使用
     */
    public void markUsed() {
        this.used = true;
        this.lastUsedTime = System.currentTimeMillis();
        this.usageCount++;
    }

    public boolean isUsed() {
        return used;
    }

    public long getLastUsedTime() {
        return lastUsedTime;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public void setLastUsedTime(long time) {
        this.lastUsedTime = time;
    }
}
