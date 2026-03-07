package com.sunohara.mingdao;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 名刀命令执行器
 */
public class MingdaoCommand implements CommandExecutor, TabCompleter {

    private final MingdaoPlugin plugin;

    public MingdaoCommand(MingdaoPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("mhelp")) {
            return showHelp(sender);
        }

        if (command.getName().equalsIgnoreCase("mingdao")) {
            if (args.length == 0) {
                return showHelp(sender);
            }

            String subcommand = args[0].toLowerCase();

            return switch (subcommand) {
                case "help" -> showHelp(sender);
                case "status" -> showStatus(sender);
                case "reload" -> reloadConfig(sender);
                case "reset" -> resetPlayer(sender, args);
                default -> showHelp(sender);
            };
        }

        return false;
    }

    /**
     * 显示帮助信息
     */
    private boolean showHelp(CommandSender sender) {
        sender.sendMessage("§6========== §e名刀帮助 §6==========");
        sender.sendMessage("§e/mingdao status §f- 查看个人名刀状态");
        sender.sendMessage("§e/mingdao reload §f- 重新加载配置 (管理员)");
        sender.sendMessage("§e/mingdao reset <玩家名> §f- 重置玩家名刀 (管理员)");
        sender.sendMessage("§6=====================================");
        sender.sendMessage("§7名刀: 在你濒临死亡时自动救援你");
        return true;
    }

    /**
     * 显示玩家状态
     */
    private boolean showStatus(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c此命令只能由玩家执行");
            return true;
        }

        if (!plugin.getMingdaoConfig().isEnabled()) {
            player.sendMessage("§6[名刀] §c插件已禁用");
            return true;
        }

        MingdaoConfig config = plugin.getMingdaoConfig();
        MingdaoManager manager = plugin.getMingdaoManager();
        PlayerMingdaoData data = manager.getPlayerData(player.getName());

        if (data == null) {
            player.sendMessage("§6[名刀] §c无法读取你的数据");
            return true;
        }

        player.sendMessage("§6========== §e名刀状态 §6==========");
        player.sendMessage("§e当前模式: §f" + config.getMingdaoMode().getDisplayName());

        if (config.getMingdaoMode() == MingdaoMode.ONCE) {
            if (data.isUsed()) {
                player.sendMessage("§e状态: §c已使用");
                player.sendMessage("§e使用次数: §f" + data.getUsageCount());
            } else {
                player.sendMessage("§e状态: §a可用");
            }
        } else {
            // 可再生模式
            int remaining = data.getRemainingCooldown(config.getRegenerateModeCooldown());
            if (remaining > 0) {
                // 冷却中
                player.sendMessage("§e状态: §c不可用");
                player.sendMessage("§e剩余冷却: §f" + remaining + " 分钟");
                player.sendMessage("§e使用次数: §f" + data.getUsageCount());
            } else {
                // 冷却完毕
                player.sendMessage("§e状态: §a可用");
            }
        }

        player.sendMessage("§e血量恢复: §f" + config.getHealthRestore() + " 点");

        player.sendMessage("§6=====================================");
        return true;
    }

    /**
     * 重新加载配置
     */
    private boolean reloadConfig(CommandSender sender) {
        if (!sender.hasPermission("sunohara.mingdao.admin")) {
            sender.sendMessage("§c你没有权限执行此命令");
            return true;
        }

        try {
            plugin.getMingdaoConfig().loadConfig();
            sender.sendMessage("§a配置已重新加载！");
            plugin.getLogger().info("配置已重新加载");
            return true;
        } catch (Exception e) {
            sender.sendMessage("§c配置加载失败: " + e.getMessage());
            plugin.getLogger().warning("配置加载失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 重置玩家名刀状态
     */
    private boolean resetPlayer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("sunohara.mingdao.admin")) {
            sender.sendMessage("§c你没有权限执行此命令");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§e用法: /mingdao reset <玩家名>");
            return true;
        }

        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);

        plugin.getMingdaoManager().resetPlayerMingdao(playerName);
        sender.sendMessage("§a已重置玩家 " + playerName + " 的名刀状态");

        if (target != null && target.isOnline()) {
            target.sendMessage("§6[名刀] §a你的名刀已被重置！");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("mingdao")) {
            return List.of();
        }
        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>(Arrays.asList("help", "status"));
            if (sender.hasPermission("sunohara.mingdao.admin")) {
                subcommands.add("reload");
                subcommands.add("reset");
            }
            return filterCompletions(subcommands, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("reset") && sender.hasPermission("sunohara.mingdao.admin")) {
            return filterCompletions(
                    Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(),
                    args[1]
            );
        }
        return List.of();
    }

    private List<String> filterCompletions(List<String> options, String prefix) {
        if (prefix.isEmpty()) return options;
        String lower = prefix.toLowerCase();
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }
}
