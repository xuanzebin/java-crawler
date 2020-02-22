package com.github.hcsp;

import java.time.Instant;

public class News {
    private int id;
    private String link;
    private String title;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;

    public News() {

    }

    public News(String link, String title, String content) {
        this.link = link;
        this.title = title;
        this.content = content;
    }

    public News(News old) {
        this.id = old.id;
        this.link = old.link;
        this.title = old.title;
        this.content = old.content;
        this.createdAt = old.createdAt;
        this.updatedAt = old.updatedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
