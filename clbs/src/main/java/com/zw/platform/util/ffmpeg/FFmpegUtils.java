package com.zw.platform.util.ffmpeg;

import com.sun.media.jfxmedia.track.VideoResolution;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tonydeng on 15/4/15.
 */
public class FFmpegUtils {
    private static final String regexDuration = "Duration: (.*?), start: (.*?), bitrate: (\\d*) kb/s";
    private static final String regexVideo = "Video: (.*?), (.*?), (.*?)[,\\s]";
    private static final String regexRotate = "rotate (.*?): (\\d*)(\\d*)";

    public static VideoInfo regInfo(String stdout, VideoInfo vi) {
        if (StringUtils.isNotEmpty(stdout)) {
            if (vi == null) {
                vi = new VideoInfo();
            }

            Pattern patternDuration = Pattern.compile(regexDuration);
            Matcher matcherDuration = patternDuration.matcher(stdout);
            if (matcherDuration.find()) {
                String duration = matcherDuration.group(1);
                if (StringUtils.isNotBlank(duration) && duration.contains(":")) {
                    String[] time = duration.split(":");
                    int hour = Integer.parseInt(time[0]);
                    int minute = Integer.parseInt(time[1]);
                    int second;
                    if (time[2].contains(".")) {
                        second = Integer.parseInt(time[2].substring(0, time[2].indexOf(".")));
                    } else {
                        second = Integer.parseInt(time[2]);
                    }
                    vi.setDuration(((long) hour * 60 * 60) + (minute * 60L) + second);
                }
            }

            Pattern patternVideo = Pattern.compile(regexVideo);
            Matcher matcherVideo = patternVideo.matcher(stdout);

            if (matcherVideo.find()) {
                String[] wh = matcherVideo.group(3).split("x");
                if (wh.length == 2) {
                    vi.setResolution(new VideoResolution(Integer.parseInt(wh[0]), Integer.parseInt(wh[1])));
                }
                String format = matcherVideo.group(1);
                if (StringUtils.isNotBlank(format)) {
                    vi.setFormat(format.split(" ")[0]);
                }
            }

            Pattern patternRotate = Pattern.compile(regexRotate);
            Matcher matcherRotate = patternRotate.matcher(stdout);
            if (matcherRotate.find()) {
                String rotate = matcherRotate.group(2);
                if (StringUtils.isNumeric(rotate)) {
                    vi.setRotate(Integer.parseInt(rotate));
                }
            }
            return vi;
        }
        return null;
    }

}
