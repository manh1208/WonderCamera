package com.superapp.wondercamera.model;

/**
 * Created by ManhNV on 2/12/17.
 */

public class Language {
    private int id;
    private String name;
    private String key;
    private String save;
    private String analyze;
    private String takePhoto;
    private String choosePhoto;



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSave() {
        return save;
    }

    public void setSave(String save) {
        this.save = save;
    }

    public String getAnalyze() {
        return analyze;
    }

    public void setAnalyze(String analyze) {
        this.analyze = analyze;
    }

    public String getTakePhoto() {
        return takePhoto;
    }

    public void setTakePhoto(String takePhoto) {
        this.takePhoto = takePhoto;
    }

    public String getChoosePhoto() {
        return choosePhoto;
    }

    public void setChoosePhoto(String choosePhoto) {
        this.choosePhoto = choosePhoto;
    }
}
