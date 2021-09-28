package com.zw.platform.util;

import java.util.regex.Pattern;

/**
 * 参考<a href="https://en.wikipedia.org/wiki/List_of_file_signatures">List of file signatures</a>
 */
public enum FileType {
    AVI("52494646\\w{8}41564920.*"),
    FLV("464C56.*"),
    GIF("47494638.*"),
    H264("00000001.*"),
    JPG("FFD8FF.*"),
    MP3("(FFFB|FFF3|FFF2|494433).*"),
    MOV("\\w{8}6674797071742020.*"), //type: ftyp(66 74 79 70), subtype: qt  (71 74 20 20)
    MP4("\\w{8}6674797069736F6D.*"), //type: ftyp(66 74 79 70), subtype: isom(69 73 6F 6D)
    MPG("(000001BA|47|000001B3).*"),
    PNG("89504E470D0A1A0A.*"),
    TIF("(49492A00|4D4D002A).*"),
    WAV("52494646\\w{8}57415645.*"),
    WMV("(3026B2758E66CF11|A6D900AA0062CE6C).*");

    Pattern head;

    FileType(String head) {
        this.head = Pattern.compile(head);
    }

    public Pattern getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = Pattern.compile(head);
    }

    public static String match(String head) {
        for (FileType fileType : values()) {
            if (fileType.getHead().matcher(head).matches()) {
                return fileType.name().toLowerCase();
            }
        }
        return "";
    }
}
