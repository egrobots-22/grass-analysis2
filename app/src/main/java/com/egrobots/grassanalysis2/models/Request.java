package com.egrobots.grassanalysis2.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Request implements Parcelable {

    private String id;
    private String userId;
    private List<Image> images;
    private String status;
    private String audioQuestion;
    private String textQuestion;

    public Request() {
    }

    protected Request(Parcel in) {
        id = in.readString();
        userId = in.readString();
        images = in.createTypedArrayList(Image.CREATOR);
        status = in.readString();
        audioQuestion = in.readString();
        textQuestion = in.readString();
        in.readList(images, Image.class.getClassLoader());
    }

    public static final Creator<Request> CREATOR = new Creator<Request>() {
        @Override
        public Request createFromParcel(Parcel in) {
            return new Request(in);
        }

        @Override
        public Request[] newArray(int size) {
            return new Request[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(userId);
        dest.writeTypedList(images);
        dest.writeString(status);
        dest.writeString(audioQuestion);
        dest.writeString(textQuestion);
        dest.writeTypedList(images);
    }
}
