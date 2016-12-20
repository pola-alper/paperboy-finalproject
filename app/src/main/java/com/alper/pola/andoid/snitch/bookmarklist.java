package com.alper.pola.andoid.snitch;

public class bookmarklist {
    private String id, title, author, description, date, url, img;

    public bookmarklist(){}

    public bookmarklist(String title, String location, String description, String date, String url, String img) {
        this.title = title;
        this.author = location;
        this.description = description;
        this.date = date;
        this.url = url;
        this.img = img;

    }

    public String getId() {
        return id;
    }



    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String location) {
        this.author = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

  public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
