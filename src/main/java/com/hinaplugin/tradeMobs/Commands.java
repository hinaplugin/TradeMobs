package com.hinaplugin.tradeMobs;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Commands implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1){
            if (strings[0].equalsIgnoreCase("reload")){
                if (commandSender.hasPermission("trademobs.commands")){
                    TradeMobs.plugin.reloadConfiguration();
                    commandSender.sendMessage(MiniMessage.miniMessage().deserialize("<green>config.ymlを再読み込みしました．</green>"));
                }else {
                    commandSender.sendMessage(MiniMessage.miniMessage().deserialize("<red>あなたはこのコマンドを実行するための権限がありません．</red>"));
                }
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        final List<String> complete = Lists.newArrayList();
        if (strings.length == 1){
            if (strings[0].isEmpty()){
                if (commandSender.hasPermission("trademobs.commands")){
                    complete.add("reload");
                }
            }else if ("reload".startsWith(strings[0])){
                if (commandSender.hasPermission("trademobs.commands")){
                    complete.add("reload");
                }
            }
        }
        return complete;
    }
}
