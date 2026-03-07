package com.sunohara.mingdao;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * 名刀管理器
 * 处理名刀功能的核心逻辑
 */
public class MingdaoManager {

    private final JavaPlugin plugin;
    private final MingdaoConfig config;
    private final Map<String, PlayerMingdaoData> playerDataMap;

    public MingdaoManager(JavaPlugin plugin, MingdaoConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.playerDataMap = new HashMap<>();
    }

    /**
     * 初始化玩家
     */
    public void initializePlayer(Player player) {
        if (!playerDataMap.containsKey(player.getName())) {
            playerDataMap.put(player.getName(), new PlayerMingdaoData(player.getName()));
        }
    }

    /**
     * 移除玩家数据
     */
    public void removePlayerData(String playerName) {
        playerDataMap.remove(playerName);
    }

    /**
     * 尝试使用名刀
     */
    public boolean tryUseMingdao(Player player) {
        if (!config.isEnabled()) {
            player.sendMessage(config.getMessageDisabled());
            return false;
        }

        PlayerMingdaoData data = playerDataMap.get(player.getName());
        if (data == null) {
            initializePlayer(player);
            data = playerDataMap.get(player.getName());
        }

        MingdaoMode mode = config.getMingdaoMode();
        boolean shouldShowMessage = config.isShowMessage();

        // 检查是否可以使用
        if (!data.canUseMingdao(mode, config.getRegenerateModeCooldown())) {
            if (shouldShowMessage) {
                if (mode == MingdaoMode.ONCE) {
                    player.sendMessage(config.getMessageAlreadyUsed());
                } else {
                    int remaining = data.getRemainingCooldown(config.getRegenerateModeCooldown());
                    String message = config.getMessageCooldownRemaining()
                            .replace("{remaining}", String.valueOf(remaining));
                    player.sendMessage(message);
                }
            }
            return false;
        }

        // 使用名刀
        useMingdao(player, data);
        return true;
    }

    /**
     * 执行名刀救援
     */
    private void useMingdao(Player player, PlayerMingdaoData data) {
        // 恢复血量
        double newHealth = Math.min(player.getMaxHealth(), player.getHealth() + config.getHealthRestore());
        player.setHealth(newHealth);

        // 应用Buff效果
        applyBuffs(player);

        // 标记为已使用
        data.markUsed();

        // 播放效果
        playEffects(player);

        // 发送消息
        if (config.isShowMessage()) {
            String message = config.getMessageMingdaoSaved()
                    .replace("{health}", String.valueOf(config.getHealthRestore()));
            player.sendMessage(message);
        }

        // 记录日志
        plugin.getLogger().info(player.getName() + " 使用了名刀救援！");
    }

    /**
     * 应用Buff效果
     */
    private void applyBuffs(Player player) {
        if (config.getBuffs() == null || config.getBuffs().isEmpty()) {
            return;
        }

        for (MingdaoConfig.BuffConfig buff : config.getBuffs()) {
            try {
                org.bukkit.potion.PotionEffect effect = new org.bukkit.potion.PotionEffect(
                        buff.getEffectType(),
                        buff.getDuration() * 20, // 转换为游戏刻 (1秒 = 20刻)
                        buff.getAmplifier(),
                        true,  // 粒子效果
                        true   // 环境效果
                );
                player.addPotionEffect(effect);
                plugin.getLogger().info("已为玩家 " + player.getName() + " 应用Buff: " + 
                        buff.getEffectType().getName() + " 等级:" + buff.getAmplifier());
            } catch (Exception e) {
                plugin.getLogger().warning("应用Buff失败: " + buff.getEffectType().getName() + " - " + e.getMessage());
            }
        }
    }

    /**
     * 播放视觉和音效效果
     */
    private void playEffects(Player player) {
        Location location = player.getLocation().add(0, 1, 0);

        // 播放粒子效果
        if (config.isShowEffect()) {
            // 图腾粒子（濒死救援效果，无需额外数据，兼容 Paper 1.21.x）
            player.getWorld().spawnParticle(
                    Particle.TOTEM_OF_UNDYING,
                    location,
                    50,
                    0.5,
                    1.0,
                    0.5
            );

            // 发光效果
            player.getWorld().spawnParticle(
                    Particle.GLOW,
                    location,
                    20,
                    0.3,
                    0.5,
                    0.3
            );
        }

        // 播放声音
        if (config.isPlaySound()) {
            try {
                Sound totemSound = Sound.valueOf("UI_TOTEM_ACTIVATED");
                player.getWorld().playSound(location, totemSound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                // 如果声音不存在，使用通用声音
                player.getWorld().playSound(
                        location,
                        Sound.ENTITY_GENERIC_EXTINGUISH_FIRE,
                        1.0f,
                        1.0f
                );
            }
        }
    }

    /**
     * 获取玩家名刀数据
     */
    public PlayerMingdaoData getPlayerData(String playerName) {
        return playerDataMap.get(playerName);
    }

    /**
     * 获取玩家是否已使用过名刀
     */
    public boolean hasPlayerUsedMingdao(String playerName) {
        PlayerMingdaoData data = playerDataMap.get(playerName);
        return data != null && data.isUsed();
    }

    /**
     * 重置玩家名刀状态（管理员命令）
     */
    public void resetPlayerMingdao(String playerName) {
        PlayerMingdaoData data = playerDataMap.get(playerName);
        if (data != null) {
            data.setUsed(false);
            data.setLastUsedTime(0);
        }
    }
}
