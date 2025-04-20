package me.drazz.telegramremote;

import me.drazz.telegramremote.bot.Main_BOT;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class TelegramLogHandler extends Handler {

    private final Main_BOT telegramBot;
    private final TelegramRemote plugin;
    private boolean logsEnabled = false;
    private List<Long> adminChatIds;
    private boolean isProcessingLog = false;
    private LogMode logMode = LogMode.IMPORTANT_ONLY;
    
    private final Queue<String> logQueue = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final int MAX_LOGS_PER_BATCH = 5;
    private static final int BATCH_INTERVAL_SECONDS = 2;
    
    private static final Set<String> IMPORTANT_EVENTS = new HashSet<>();
    
    public enum LogMode {
        DISABLED,
        IMPORTANT_ONLY,
        ALL_LOGS
    }
    
    static {
        IMPORTANT_EVENTS.add("logged in");
        IMPORTANT_EVENTS.add("joined");
        IMPORTANT_EVENTS.add("left the game");
        IMPORTANT_EVENTS.add("said");
        IMPORTANT_EVENTS.add("chat");
        IMPORTANT_EVENTS.add("CHAT");
        IMPORTANT_EVENTS.add("message");
    }
    
    public TelegramLogHandler(TelegramRemote plugin, Main_BOT telegramBot) {
        this.plugin = plugin;
        this.telegramBot = telegramBot;
        loadConfig();
        
        scheduler.scheduleAtFixedRate(this::processLogQueue, 
                                     BATCH_INTERVAL_SECONDS, 
                                     BATCH_INTERVAL_SECONDS, 
                                     TimeUnit.SECONDS);
    }

    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        logsEnabled = config.getBoolean("telegram.notifications.enable_server_logs", false);
        adminChatIds = config.getLongList("telegram.admin_ids");
        
        String modeStr = config.getString("telegram.notifications.log_mode", "IMPORTANT_ONLY");
        try {
            logMode = LogMode.valueOf(modeStr);
        } catch (IllegalArgumentException e) {
            logMode = LogMode.IMPORTANT_ONLY;
        }
    }
    
    public void setLogMode(LogMode mode) {
        this.logMode = mode;
        logsEnabled = (mode != LogMode.DISABLED);
        
        FileConfiguration config = plugin.getConfig();
        config.set("telegram.notifications.enable_server_logs", logsEnabled);
        config.set("telegram.notifications.log_mode", mode.name());
        plugin.saveConfig();
    }

    public LogMode getLogMode() {
        return logMode;
    }

    @Override
    public void publish(LogRecord record) {
        if (logMode == LogMode.DISABLED || record == null || isProcessingLog) {
            return;
        }
        
        if (isInternalBotLog(record.getLoggerName(), record.getMessage())) {
            return;
        }

        if (logMode == LogMode.IMPORTANT_ONLY && !isImportantEvent(record.getMessage())) {
            return;
        }

        try {
            isProcessingLog = true;
            
            String formattedMessage = String.format("[%s] %s", record.getLevel().getName(), record.getMessage());
            logQueue.add(formattedMessage);
            
        } finally {
            isProcessingLog = false;
        }
    }
    
    private void processLogQueue() {
        if (logMode == LogMode.DISABLED || logQueue.isEmpty() || adminChatIds.isEmpty()) {
            return;
        }
        
        StringBuilder batchMessage = new StringBuilder();
        int count = 0;
        
        while (!logQueue.isEmpty() && count < MAX_LOGS_PER_BATCH) {
            String log = logQueue.poll();
            if (log != null) {
                batchMessage.append(log).append("\n");
                count++;
            }
        }
        
        if (count > 0) {
            String message = batchMessage.toString();
            for (Long chatId : adminChatIds) {
                telegramBot.sendSilentMsg(chatId, message);
            }
        }
    }
    
    private boolean isInternalBotLog(String loggerName, String message) {
        if (message != null && (
                message.contains("Message not delivered") ||
                message.contains("TG-Console") ||
                message.contains("Unable to execute") ||
                message.contains("TelegramLogHandler"))) {
            return true;
        }
        
        if (loggerName != null && (
                loggerName.equals("org.telegram.telegrambots") ||
                loggerName.contains("telegrambots") ||
                loggerName.contains("TelegramLongPollingBot"))) {
            return true;
        }
        
        return false;
    }
    
    private boolean isImportantEvent(String message) {
        if (message == null) {
            return false;
        }
        
        for (String event : IMPORTANT_EVENTS) {
            if (message.toLowerCase().contains(event.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    public boolean isLogsEnabled() {
        return logMode != LogMode.DISABLED;
    }
}