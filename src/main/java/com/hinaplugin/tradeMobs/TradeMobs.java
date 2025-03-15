package com.hinaplugin.tradeMobs;

import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

public final class TradeMobs extends JavaPlugin {
    public static TradeMobs plugin;
    public static FileConfiguration config;

    @Override
    public void onEnable() {
        // Plugin startup logic
        try {
            plugin = this;
            this.loadConfiguration();

            // プレイヤーがエンティティを右クリックした時のイベントを登録
            this.getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);

            // プラグインのコマンドを取得
            final PluginCommand command = this.getCommand("trademobs");
            if (command != null){
                // コマンドが存在していればコマンドクラスを登録
                command.setExecutor(new Commands());
            }
        }catch (Exception exception){
            exception.printStackTrace(new PrintWriter(new StringWriter()));
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        // 登録されたイベントを削除
        HandlerList.unregisterAll(this);
    }

    private void loadConfiguration(){
        // プラグインフォルダにあるconfig.ymlを取得
        final File configFile = new File(this.getDataFolder(), "config.yml");

        // プラグインフォルダがないまたはプラグインフォルダにconfig.ymlがないときは作成
        if (!configFile.exists()){
            this.saveDefaultConfig();
        }

        // config.ymlを取得
        config = this.getConfig();
    }

    public void reloadConfiguration(){
        // config.ymlを再読み込み
        this.reloadConfig();
        config = this.getConfig();
    }
}
