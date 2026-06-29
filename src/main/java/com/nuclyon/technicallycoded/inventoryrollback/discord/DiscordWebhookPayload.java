package com.nuclyon.technicallycoded.inventoryrollback.discord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiscordWebhookPayload {

    private final String content;
    private final String username;
    private final String avatarUrl;
    private final List<DiscordEmbed> embeds;

    private DiscordWebhookPayload(Builder builder) {
        this.content = builder.content;
        this.username = builder.username;
        this.avatarUrl = builder.avatarUrl;
        this.embeds = builder.embeds != null
                ? Collections.unmodifiableList(new ArrayList<>(builder.embeds))
                : Collections.emptyList();
    }

    public String getContent() {
        return content;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public List<DiscordEmbed> getEmbeds() {
        return embeds;
    }

    public static class Builder {

        private String content;
        private String username;
        private String avatarUrl;
        private List<DiscordEmbed> embeds;

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public Builder embeds(List<DiscordEmbed> embeds) {
            this.embeds = embeds;
            return this;
        }

        public DiscordWebhookPayload build() {
            return new DiscordWebhookPayload(this);
        }

    }

}
