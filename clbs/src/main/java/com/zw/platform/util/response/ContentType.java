package com.zw.platform.util.response;

public enum ContentType {
    ZIP("application/zip"), WORD("application/msword"), EXCEL("application/ms-excel"),
    JPEG("image/jpeg"), JPG("image/jpg"), PNG("image/png"), MP3("audio/mp3"), MP4("video/mpeg4"),
    TIF("image/tiff");

    ContentType(String value) {
        this.value = value;
    }

    private String value;

    @Override
    public String toString() {
        return this.value;
    }
}
