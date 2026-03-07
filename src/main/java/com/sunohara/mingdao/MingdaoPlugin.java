package com.sunohara.mingdao;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 名刀插件主类 - 濒死救援插件
 */
public class MingdaoPlugin extends JavaPlugin implements Listener {

    private MingdaoConfig mingdaoConfig;
    private MingdaoManager mingdaoManager;

    @Override
    public void onEnable() {
        // 初始化配置管理器
        mingdaoConfig = new MingdaoConfig(this);
        mingdaoConfig.loadConfig();

        // 初始化名刀管理器
        mingdaoManager = new MingdaoManager(this, mingdaoConfig);

        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(this, this);

        // 注册命令
        MingdaoCommand commandExecutor = new MingdaoCommand(this);
        getCommand("mingdao").setExecutor(commandExecutor);
        getCommand("mingdao").setTabCompleter(commandExecutor);
        getCommand("mhelp").setExecutor(commandExecutor);

        getLogger().info("================================");
        getLogger().info("  Sunohara Mingdao（名刀）");
        getLogger().info("  版本: " + getDescription().getVersion());
        getLogger().info("  模式: " + mingdaoConfig.getMingdaoMode().getDisplayName());
        getLogger().info("  已启用");
        getLogger().info("================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("Sunohara Mingdao 已禁用");
    }

    /**
     * 玩家加入事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // 初始化玩家数据
        mingdaoManager.initializePlayer(player);
    }

    /**
     * 玩家退出事件
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        mingdaoManager.removePlayerData(player.getName());
    }

    /**
     * 玩家受伤事件 - 名刀检查点
     */
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!mingdaoConfig.isEnabled()) {
            return;
        }

        if (!player.hasPermission("sunohara.mingdao.use")) {
            return;
        }

        // 计算受伤后的血量
        double futureHealth = player.getHealth() - event.getFinalDamage();

        // 检查是否会导致死亡 - 防止任何致命伤害
        if (futureHealth <= 0) {
            // 尝试使用名刀
            if (mingdaoManager.tryUseMingdao(player)) {
                // 成功使用名刀，取消伤害事件
                event.setCancelled(true);
            }
        }
    }

    /**
     * 获取名刀配置
     */
    public MingdaoConfig getMingdaoConfig() {
        return mingdaoConfig;
    }

    /**
     * 获取名刀管理器
     */
    public MingdaoManager getMingdaoManager() {
        return mingdaoManager;
    }

    /**
     * 获取日志前缀
     */
    public String getLogPrefix() {
        return "§6[" + this.getName() + "§6]§r ";
    }
}
