package com.example.youxian.wifidirecttransfer;

/**
 * Created by Youxian on 11/11/15.
 */
public class Music {
    private long id = -1;
    private String title;
    private String album;
    private String artist;
    private String genre;
    // playOrder is for playlist
    private int playOrder;
    private String path;
    private boolean selected;
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getPlayOrder() {
        return playOrder;
    }

    public void setPlayOrder(int order) {
        this.playOrder = order;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean getSelected(){
        return selected;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }
    @Override
    public String toString() {
        return id+"."+title+", album:"+album+", artist:"+artist+", genre:"+genre;
    }
}
