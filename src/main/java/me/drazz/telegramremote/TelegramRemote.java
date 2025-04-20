package me.drazz.telegramremote;

import me.drazz.telegramremote.bot.Main_BOT;
import me.drazz.telegramremote.commands.TR_CMD;
import me.drazz.telegramremote.commands.TR_TabCompleter;
import me.drazz.telegramremote.events.Notifications_Event;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;

public final class TelegramRemote extends JavaPlugin {

    private Main_BOT telegramBot = new Main_BOT();
    private static TelegramRemote instance;
    private static YamlConfiguration messagesConfig;
    private TelegramLogHandler logHandler;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        loadMessagesConfig();

        Objects.requireNonNull(getCommand("telegramremote")).setExecutor(new TR_CMD());
        Objects.requireNonNull(getCommand("telegramremote")).setTabCompleter(new TR_TabCompleter());
        Bukkit.getPluginManager().registerEvents(new Notifications_Event(), this);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBot);
            getLogger().info("Bot launched successfully!");
            
            setupLogHandler();
            
            telegramBot.bot_started_notif();
        } catch (Exception e) {
            getLogger().severe("Bot error!");
            e.printStackTrace();
        }
        
        if (getConfig().getBoolean("update.enable")) {
            String currentVersion = getDescription().getVersion();
            CheckUpdate checkUpdate = new CheckUpdate(this, currentVersion);
            checkUpdate.checkForUpdateAsync();
        }

        getLogger().info("Plugin launched successfully!");
    }

    @Override
    public void onDisable() {
        if (logHandler != null) {
            Logger rootLogger = getLogger().getParent();
            rootLogger.removeHandler(logHandler);
        }
        
        getLogger().info("Plugin disabled successfully!");
    }

    private void setupLogHandler() {
        try {
            if (logHandler != null) {
                Logger rootLogger = getLogger().getParent();
                rootLogger.removeHandler(logHandler);
            }
            
            logHandler = new TelegramLogHandler(this, telegramBot);
            
            Logger rootLogger = getLogger().getParent();
            rootLogger.addHandler(logHandler);
        } catch (Exception e) {
            getLogger().severe("Failed to setup log handler: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void reloadLogHandler() {
        if (logHandler != null) {
            logHandler.loadConfig();
        } else {
            setupLogHandler();
        }
    }
    
    public void setLogMode(TelegramLogHandler.LogMode mode) {
        if (logHandler != null) {
            logHandler.setLogMode(mode);
        } else {
            setupLogHandler();
            if (logHandler != null) {
                logHandler.setLogMode(mode);
            }
        }
    }

    public void loadMessagesConfig() {
        File messagesFile = new File(getDataFolder(), "messages_" + getConfig().getString("language") + ".yml");

        if (!messagesFile.exists()) {
            saveResource("messages_ru.yml", false);
            saveResource("messages_en.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public static String getMessage(String key) {
        if (messagesConfig == null) {
            throw new IllegalStateException("Messages configuration not loaded.");
        }

        return messagesConfig.getString(key);
    }

    public static TelegramRemote getInstance() {
        return instance;
    }
    
    public TelegramLogHandler getLogHandler() {
        return logHandler;
    }
}