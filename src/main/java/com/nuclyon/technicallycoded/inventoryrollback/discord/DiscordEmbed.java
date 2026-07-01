package com.nuclyon.technicallycoded.inventoryrollback.discord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiscordEmbed {

    private final String title;
    private final String description;
    private final int color;
    private final String footer;
    private final String timestamp;
    private final String thumbnail;
    private final String footerurl;
    private final List<DiscordEmbedField> fields;

    private DiscordEmbed(Builder builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.color = builder.color;
        this.footer = builder.footer;
        this.timestamp = builder.timestamp;
        this.thumbnail = builder.thumbnail;
        this.footerurl = builder.footerurl;
        this.fields = Collections.unmodifiableList(new ArrayList<>(builder.fields));
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getColor() {
        return color;
    }

    public String getFooter() {
        return footer;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getFooterurl() { return footerurl;}

    public List<DiscordEmbedField> getFields() {
        return fields;
    }

    public static class Builder {

        private String title;
        private String description;
        private int color;
        private String footer;
        private String timestamp;
        private String thumbnail;
        private String footerurl;
        private final List<DiscordEmbedField> fields = new ArrayList<>();

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public Builder footer(String footer) {
            this.footer = footer;
            return this;
        }

        public Builder timestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder thumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
            return this;
        }

        public Builder footerurl(String footerurl) {
            this.footerurl = footerurl;
            return this;
        }

        public Builder addField(DiscordEmbedField field) {
            this.fields.add(field);
            return this;
        }

        public DiscordEmbed build() {
            return new DiscordEmbed(this);
        }

    }

}
