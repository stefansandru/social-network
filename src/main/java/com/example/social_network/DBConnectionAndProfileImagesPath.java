package com.example.social_network;

public enum DBConnectionAndProfileImagesPath {
    INSTANCE;

    private String url;
    private String user;
    private String password;
    private String photosFolder;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhotosFolder() {
        return photosFolder;
    }

    public void setPhotosFolder(String photosFolder) {
        this.photosFolder = photosFolder;
    }
}