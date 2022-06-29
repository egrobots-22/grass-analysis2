package com.egrobots.grassanalysis2.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.List;

public class Request {

    private String id;
    private String userId;
    private List<Image> images;
    private String status;
    private String audioQuestion;
    private String textQuestion;
    private HashMap<String, Object> answer;

    public Request() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Image> getImages() {
        return images;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAudioQuestion() {
        return audioQuestion;
    }

    public void setAudioQuestion(String audioQuestion) {
        this.audioQuestion = audioQuestion;
    }

    public String getTextQuestion() {
        return textQuestion;
    }

    public void setTextQuestion(String textQuestion) {
        this.textQuestion = textQuestion;
    }

    public HashMap<String, Object> getAnswer() {
        return answer;
    }

    public void setAnswer(HashMap<String, Object> answer) {
        this.answer = answer;
    }

}
