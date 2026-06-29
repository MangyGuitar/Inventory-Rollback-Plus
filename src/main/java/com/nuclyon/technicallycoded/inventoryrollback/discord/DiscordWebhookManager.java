package com.nuclyon.technicallycoded.inventoryrollback.discord;

import com.nuclyon.technicallycoded.inventoryrollback.InventoryRollbackPlus;
import me.danjono.inventoryrollback.config.ConfigData;
import org.bukkit.Bukkit;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscordWebhookManager {

    private static DiscordWebhookManager instance;

    private boolean enabled;
    private String webhookUrl;
    private String username;
    private String avatarUrl;
    private boolean embedEnabled;
    private int embedColor;
    private String embedTitle;
    private String embedFooter;
    private boolean embedTimestamp;
    private String embedThumbnail;
    private String serverName;

    private DiscordWebhookManager() {}

    public static DiscordWebhookManager getInstance() {
        if (instance == null) {
            instance = new DiscordWebhookManager();
        }
        return instance;
    }

    public void reload() {
        loadConfig();
    }

    private void loadConfig() {
        this.enabled = ConfigData.isDiscordEnabled();
        this.webhookUrl = ConfigData.getDiscordWebhookUrl();
        this.embedEnabled = ConfigData.isDiscordEmbedEnabled();
        this.embedColor = parseHexColor(ConfigData.getDiscordEmbedColor());
        this.embedTitle = ConfigData.getDiscordEmbedTitle();
        this.embedFooter = ConfigData.getDiscordEmbedFooter();
        this.embedTimestamp = ConfigData.isDiscordEmbedTimestamp();
        this.embedThumbnail = ConfigData.getDiscordEmbedThumbnail();
        this.serverName = ConfigData.getDiscordServerName();
    }

    public void sendRollbackWebhook(RollbackData data) {
        if (!enabled || webhookUrl == null || webhookUrl.isEmpty()) return;

        DiscordWebhookPayload payload;
        if (embedEnabled) {
            payload = buildRollbackEmbed(data);
        } else {
            payload = buildRollbackPlain(data);
        }

        sendAsync(payload);
    }

    public void sendPlainMessage(String message) {
        if (!enabled || webhookUrl == null || webhookUrl.isEmpty()) return;

        DiscordWebhookPayload payload = new DiscordWebhookPayload.Builder()
                .content(message)
                .username(username)
                .avatarUrl(avatarUrl)
                .build();

        sendAsync(payload);
    }

    private DiscordWebhookPayload buildRollbackEmbed(RollbackData data) {
        DiscordEmbed.Builder embedBuilder = new DiscordEmbed.Builder()
                .title(embedTitle)
                .color(embedColor)
                .addField(new DiscordEmbedField("Player", data.playerName, true))
                .addField(new DiscordEmbedField("Staff", data.staffName, true))
                .addField(new DiscordEmbedField("Items Restored", String.valueOf(data.itemCount), true))
                .addField(new DiscordEmbedField("Rollback ID", data.rollbackId, true))
                .addField(new DiscordEmbedField("World", data.world, true))
                .addField(new DiscordEmbedField("Coordinates",
                        "X: " + data.x + " Y: " + data.y + " Z: " + data.z, true))
                .addField(new DiscordEmbedField("Date & Time", data.timestamp, false));

        if (serverName != null && !serverName.isEmpty()) {
            embedBuilder.addField(new DiscordEmbedField("Server", serverName, true));
        }

        embedBuilder.addField(new DiscordEmbedField("Status",
                data.success ? ":white_check_mark: Success" : ":x: Failed", true));

        if (embedFooter != null && !embedFooter.isEmpty()) {
            embedBuilder.footer(embedFooter);
        }

        if (embedTimestamp) {
            embedBuilder.timestamp(formatIsoTimestamp(System.currentTimeMillis()));
        }

        if (embedThumbnail != null && !embedThumbnail.isEmpty()) {
            embedBuilder.thumbnail(embedThumbnail);
        }

        return new DiscordWebhookPayload.Builder()
                .username(username)
                .avatarUrl(avatarUrl)
                .embeds(Collections.singletonList(embedBuilder.build()))
                .build();
    }

    private DiscordWebhookPayload buildRollbackPlain(RollbackData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("**Rollback Completed**\n");
        sb.append("**Player:** ").append(data.playerName).append("\n");
        sb.append("**Staff:** ").append(data.staffName).append("\n");
        sb.append("**Items Restored:** ").append(data.itemCount).append("\n");
        sb.append("**Rollback ID:** ").append(data.rollbackId).append("\n");
        sb.append("**World:** ").append(data.world).append("\n");
        sb.append("**Coordinates:** X=").append(data.x).append(" Y=").append(data.y).append(" Z=").append(data.z).append("\n");
        sb.append("**Date & Time:** ").append(data.timestamp).append("\n");
        if (serverName != null && !serverName.isEmpty()) {
            sb.append("**Server:** ").append(serverName).append("\n");
        }
        sb.append("**Status:** ").append(data.success ? "Success" : "Failed");

        return new DiscordWebhookPayload.Builder()
                .content(sb.toString())
                .username(username)
                .avatarUrl(avatarUrl)
                .build();
    }

    private void sendAsync(final DiscordWebhookPayload payload) {
        Bukkit.getScheduler().runTaskAsynchronously(InventoryRollbackPlus.getInstance(), () -> {
            send(payload);
        });
    }

    private void send(DiscordWebhookPayload payload) {
        Logger logger = InventoryRollbackPlus.getInstance().getLogger();

        if (ConfigData.isDebugEnabled()) {
            logger.info("[DiscordWebhook] Sending webhook to: " + webhookUrl);
        }

        HttpURLConnection connection = null;
        try {
            String json = toJson(payload);

            if (ConfigData.isDebugEnabled()) {
                logger.info("[DiscordWebhook] Payload: " + json);
            }

            URL url = new URL(this.webhookUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "InventoryRollbackPlus");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();

            if (ConfigData.isDebugEnabled()) {
                logger.info("[DiscordWebhook] Response code: " + responseCode);
            }

            if (responseCode < 200 || responseCode >= 300) {
                logger.warning("[DiscordWebhook] Webhook returned non-success status: " + responseCode);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "[DiscordWebhook] Failed to send webhook", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String toJson(DiscordWebhookPayload payload) {
        StringBuilder json = new StringBuilder();
        json.append("{");

        if (payload.getUsername() != null && !payload.getUsername().isEmpty()) {
            json.append("\"username\":").append(escapeJson(payload.getUsername())).append(",");
        }

        if (payload.getAvatarUrl() != null && !payload.getAvatarUrl().isEmpty()) {
            json.append("\"avatar_url\":").append(escapeJson(payload.getAvatarUrl())).append(",");
        }

        if (payload.getContent() != null && !payload.getContent().isEmpty()) {
            json.append("\"content\":").append(escapeJson(payload.getContent())).append(",");
        }

        if (!payload.getEmbeds().isEmpty()) {
            json.append("\"embeds\":[");
            for (int i = 0; i < payload.getEmbeds().size(); i++) {
                if (i > 0) json.append(",");
                json.append(embedToJson(payload.getEmbeds().get(i)));
            }
            json.append("],");
        }

        if (json.charAt(json.length() - 1) == ',') {
            json.deleteCharAt(json.length() - 1);
        }

        json.append("}");
        return json.toString();
    }

    private static String embedToJson(DiscordEmbed embed) {
        StringBuilder json = new StringBuilder();
        json.append("{");

        if (embed.getTitle() != null && !embed.getTitle().isEmpty()) {
            json.append("\"title\":").append(escapeJson(embed.getTitle())).append(",");
        }

        if (embed.getDescription() != null && !embed.getDescription().isEmpty()) {
            json.append("\"description\":").append(escapeJson(embed.getDescription())).append(",");
        }

        json.append("\"color\":").append(embed.getColor()).append(",");

        if (embed.getFooter() != null && !embed.getFooter().isEmpty()) {
            json.append("\"footer\":{\"text\":").append(escapeJson(embed.getFooter())).append("},");
        }

        if (embed.getTimestamp() != null && !embed.getTimestamp().isEmpty()) {
            json.append("\"timestamp\":").append(escapeJson(embed.getTimestamp())).append(",");
        }

        if (embed.getThumbnail() != null && !embed.getThumbnail().isEmpty()) {
            json.append("\"thumbnail\":{\"url\":").append(escapeJson(embed.getThumbnail())).append("},");
        }

        if (!embed.getFields().isEmpty()) {
            json.append("\"fields\":[");
            for (int i = 0; i < embed.getFields().size(); i++) {
                if (i > 0) json.append(",");
                json.append(fieldToJson(embed.getFields().get(i)));
            }
            json.append("],");
        }

        if (json.charAt(json.length() - 1) == ',') {
            json.deleteCharAt(json.length() - 1);
        }

        json.append("}");
        return json.toString();
    }

    private static String fieldToJson(DiscordEmbedField field) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"name\":").append(escapeJson(field.getName())).append(",");
        json.append("\"value\":").append(escapeJson(field.getValue())).append(",");
        json.append("\"inline\":").append(field.isInline());
        json.append("}");
        return json.toString();
    }

    static String escapeJson(String s) {
        if (s == null) return "\"\"";
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        sb.append('"');
        return sb.toString();
    }

    static int parseHexColor(String hex) {
        if (hex == null || hex.isEmpty()) return 0x57F287;
        String sanitized = hex.startsWith("#") ? hex.substring(1) : hex;
        try {
            return (int) Long.parseLong(sanitized, 16);
        } catch (NumberFormatException e) {
            return 0x57F287;
        }
    }

    static String formatIsoTimestamp(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(millis));
    }

    public void onEnable() {
        loadConfig();
    }

    public void onDisable() {
        instance = null;
    }

    public static class RollbackData {

        public final String playerName;
        public final String staffName;
        public final int itemCount;
        public final String rollbackId;
        public final String world;
        public final double x;
        public final double y;
        public final double z;
        public final String timestamp;
        public final boolean success;

        public RollbackData(String playerName, String staffName, int itemCount,
                            String rollbackId, String world, double x, double y, double z,
                            String timestamp, boolean success) {
            this.playerName = playerName;
            this.staffName = staffName;
            this.itemCount = itemCount;
            this.rollbackId = rollbackId;
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.timestamp = timestamp;
            this.success = success;
        }

    }

}
