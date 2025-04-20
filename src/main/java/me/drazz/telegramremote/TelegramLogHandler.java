/**
 * -----------------------------------------------------------------------------
 * TelegramLogHandler.java
 * by Brokoli5191
 * -----------------------------------------------------------------------------
 * Description: This class implements a custom log handler that forwards server
 * logs to Telegram chats when enabled.
 * -----------------------------------------------------------------------------
 * Version: 1.0.0
 * Created: April 20, 2025
 * -----------------------------------------------------------------------------
 */
package me.drazz.telegramremote;

import me.drazz.telegramremote.bot.Main_BOT;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class TelegramLogHandler extends Handler {

    private final Main_BOT telegramBot;
    private final TelegramRemote plugin;
    private boolean logsEnabled = false;
    private List<Long> adminChatIds;

    public TelegramLogHandler(TelegramRemote plugin, Main_BOT telegramBot) {
        this.plugin = plugin;
        this.telegramBot = telegramBot;
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        logsEnabled = config.getBoolean("telegram.notifications.enable_server_logs", false);
        adminChatIds = config.getLongList("telegram.admin_ids");
    }

    @Override
    public void publish(LogRecord record) {
        if (!logsEnabled || record == null) {
            return;
        }

        // Format the log message
        String message = String.format("[%s] %s", record.getLevel().getName(), record.getMessage());
        
        // Send to all admin chat IDs
        for (Long chatId : adminChatIds) {
            telegramBot.sendMsg(chatId, message);
        }
    }

    @Override
    public void flush() {
        // No buffering, so nothing to flush
    }

    @Override
    public void close() throws SecurityException {
        // No resources to close
    }
    
    public void setLogsEnabled(boolean enabled) {
        this.logsEnabled = enabled;
    }
    
    public boolean isLogsEnabled() {
        return logsEnabled;
    }
}