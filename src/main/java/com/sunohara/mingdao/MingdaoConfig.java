package com.sunohara.mingdao;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 名刀配置管理器
 */
public class MingdaoConfig {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    // 配置值
    private boolean enabled;
    private MingdaoMode mingdaoMode;
    private int onceModeCooldown;
    private int regenerateModeCooldown;
    private int healthRestore;
    private boolean showMessage;
    private boolean showEffect;
    private boolean playSound;

    // Buff配置
    private List<BuffConfig> buffs;

    // 消息
    private String messageMingdaoSaved;
    private String messageCooldownRemaining;
    private String messageAlreadyUsed;
    private String messageNoPermission;
    private String messageDisabled;

    public MingdaoConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Buff配置类
     */
    public static class BuffConfig {
        private final PotionEffectType effectType;
        private final int duration;
        private final int amplifier;

        public BuffConfig(PotionEffectType effectType, int duration, int amplifier) {
            this.effectType = effectType;
            this.duration = duration;
            this.amplifier = amplifier;
        }

        public PotionEffectType getEffectType() {
            return effectType;
        }

        public int getDuration() {
            return duration;
        }

        public int getAmplifier() {
            return amplifier;
        }
    }

    /**
     * 加载配置
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        mergeWithDefaults();

        // 加载基本配置
        enabled = config.getBoolean("enabled", true);
        mingdaoMode = MingdaoMode.fromString(config.getString("mingdao-mode", "once"));

        // 加载一次性模式配置
        onceModeCooldown = config.getInt("once-mode.cooldown-seconds", 0);

        // 加载可再生模式配置
        regenerateModeCooldown = config.getInt("regenerate-mode.cooldown-minutes", 60);
        showMessage = config.getBoolean("regenerate-mode.show-message", true);

        // 加载恢复设置
        healthRestore = config.getInt("restoration.health-restore", 20);
        showEffect = config.getBoolean("restoration.show-effect", true);

        // 加载Buff配置
        loadBuffs();

        // 加载消息
        showMessage = config.getBoolean("once-mode.show-message", true);
        messageMingdaoSaved = parseMessage(config.getString("messages.mingdao-saved",
                "§6[名刀] §a你被名刀救援了！§f获得了 {health} 点生命"));
        messageCooldownRemaining = parseMessage(config.getString("messages.cooldown-remaining",
                "§6[名刀] §c名刀还需冷却 {remaining} 分钟"));
        messageAlreadyUsed = parseMessage(config.getString("messages.already-used",
                "§6[名刀] §c你已经使用过名刀了！"));
        messageNoPermission = parseMessage(config.getString("messages.no-permission",
                "§c你没有使用名刀的权限"));
        messageDisabled = parseMessage(config.getString("messages.disabled",
                "§c名刀插件已禁用"));

        // 加载音效设置
        playSound = config.getBoolean("effects.play-sound", true);
    }

    /**
     * 加载Buff配置
     */
    private void loadBuffs() {
        buffs = new ArrayList<>();
        List<String> buffStrings = config.getStringList("restoration.buffs");
        
        if (buffStrings == null || buffStrings.isEmpty()) {
            return;
        }

        for (String buffStr : buffStrings) {
            try {
                String[] parts = buffStr.split(":");
                if (parts.length < 3) {
                    plugin.getLogger().warning("无效的Buff配置: " + buffStr + " (格式: 效果:持续时间:等级)");
                    continue;
                }

                String effectName = parts[0].toUpperCase();
                int duration = Integer.parseInt(parts[1]);
                int amplifier = Integer.parseInt(parts[2]);

                PotionEffectType effectType = PotionEffectType.getByName(effectName);
                if (effectType == null) {
                    plugin.getLogger().warning("未知的药水效果: " + effectName);
                    continue;
                }

                buffs.add(new BuffConfig(effectType, duration, amplifier));
                plugin.getLogger().info("已加载Buff: " + effectName + " 等级:" + amplifier + " 持续:" + duration + "秒");
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("无效的Buff数值: " + buffStr);
            }
        }
    }

    /**
     * 将默认配置中新增的项合并到现有配置，更新插件后自动补充缺失的配置项
     */
    private void mergeWithDefaults() {
        try (InputStream defaultStream = plugin.getResource("config.yml")) {
            if (defaultStream == null) return;
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            boolean modified = false;
            for (String key : defaultConfig.getKeys(true)) {
                if (defaultConfig.isConfigurationSection(key)) continue;
                if (!config.contains(key)) {
                    config.set(key, defaultConfig.get(key));
                    modified = true;
                }
            }
            if (modified) {
                plugin.saveConfig();
                plugin.getLogger().info("配置文件已更新，已添加新版本配置项");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("配置合并失败: " + e.getMessage());
        }
    }

    /**
     * 解析消息（替换§符号）
     */
    private String parseMessage(String message) {
        return message.replace("&", "§");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public MingdaoMode getMingdaoMode() {
        return mingdaoMode;
    }

    public int getRegenerateModeCooldown() {
        return regenerateModeCooldown;
    }

    public int getHealthRestore() {
        return healthRestore;
    }

    public boolean isShowMessage() {
        return showMessage;
    }

    public boolean isShowEffect() {
        return showEffect;
    }

    public boolean isPlaySound() {
        return playSound;
    }

    /**
     * 获取Buff配置列表
     */
    public List<BuffConfig> getBuffs() {
        return buffs;
    }

    public String getMessageMingdaoSaved() {
        return messageMingdaoSaved;
    }

    public String getMessageCooldownRemaining() {
        return messageCooldownRemaining;
    }

    public String getMessageAlreadyUsed() {
        return messageAlreadyUsed;
    }

    public String getMessageNoPermission() {
        return messageNoPermission;
    }

    public String getMessageDisabled() {
        return messageDisabled;
    }

    public FileConfiguration getConfig() {
        return config;
    }
}

