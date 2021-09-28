package com.zw.platform.util.ffmpeg;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Created by tonydeng on 15/4/15.
 */
public class BaseCommandOption {
    private static final Logger log = LoggerFactory.getLogger(BaseCommandOption.class);
    private static boolean isWin = false;
    private static boolean isLinux = false;
    private static final Integer DEFAULT_IO_THREADS;

    private static List<String> FFMPEG_BINARY;
    private static List<String> FFPROBE_BINARY;

    public static final String WINCMD = "cmd";
    public static final String WINCMDOP = "/c";
    public static final String LINUXCMD = "/usr/bin/env";
    public static final String FFMPEG = "ffmpeg";
    public static final String FFPROBE = "ffprobe";

    public static final String Y = "-y";
    public static final String INPUT = "-i";
    public static final String T = "-t";
    public static final String F = "-f";
    public static final String S = "-s";
    public static final String SS = "-ss";
    public static final String CV = "-c:v";
    public static final String CA = "-c:a";
    public static final String STRICT = "-strict";
    public static final String VF = "-vf";
    public static final String THREADS = "-threads";
    public static final String C = "-c";

    public static final String AN = "-an";
    public static final String VN = "-vn";

    public static final String COPY = "copy";

    public static final String FORMAT_HLS = "hls";
    public static final String FORMAT_IMAGE = "image2";
    public static final String FORMAT_LIB264 = "libx264";
    public static final String FORMAT_AAC = "aac";

    public static final String HLS_TIME = "-hls_time";
    public static final String HLS_LIST_SIZE = "-hls_list_size";
    public static final String HLS_WRAP = "-hls_wrap";
    public static final String HLS_BASE_URL = "-hls_base_url";

    public static final String ACODEC = "-acodec";
    public static final String LIBMP3LAME = "libmp3lame";

    static {
        String env = System.getProperty("os.name");
        if (log.isDebugEnabled()) {
            log.debug("current operate system :{}", env);
        }

        if (null != env) {
            String os = env.toLowerCase();
            if (os.contains("win")) {
                isWin = true;
            } else if (os.contains("linux") || os.contains("mac")) {
                isLinux = true;
            }
        }
        //获得当前机器的CPU核数
        DEFAULT_IO_THREADS = Runtime.getRuntime().availableProcessors();
        if (log.isDebugEnabled()) {
            log.debug("isWindows : '{}'  or isLinux:'{}' DEFAULT_IO_THREADS:'{}'", isWin, isLinux, DEFAULT_IO_THREADS);
        }
    }

    /**
     * 得到ffmpeg命令参数
     */
    public static List<String> getFFmpegBinary() {
        if (FFMPEG_BINARY == null) {
            if (isWin) {
                FFMPEG_BINARY = Lists.newArrayList(WINCMD, WINCMDOP, FFMPEG);
            } else if (isLinux) {
                FFMPEG_BINARY = Lists.newArrayList(LINUXCMD, FFMPEG);
            }
        }
        return FFMPEG_BINARY;
    }

    /**
     * 得到ffprobe命令
     */
    public static List<String> getFFprobeBinary() {
        if (null == FFPROBE_BINARY) {
            if (isWin) {
                FFPROBE_BINARY = Lists.newArrayList(WINCMD, WINCMDOP, FFPROBE);
            } else if (isLinux) {
                FFPROBE_BINARY = Lists.newArrayList(LINUXCMD, FFPROBE);
            }
        }
        return FFPROBE_BINARY;
    }

    /**
     * 截图的命令参数
     */
    public static List<String> toScreenshotCmdArrays(String input, String output, int shotSecond, VideoInfo vi) {
        if (vi == null || vi.getSize() <= 0) {
            return Collections.emptyList();
        }
        if (vi.getRotate() > 0) {
            return Lists.newArrayList(
                Y, //如果目标文件存在，则直接覆盖
                INPUT, input, //视频输入文件
                VF, "transpose=1", //视频转向
                SS, String.valueOf(shotSecond),
                T, "0.001",
                F, FORMAT_IMAGE,
                output
            );
        }
        return Lists.newArrayList(
            Y, //如果目标文件存在，则直接覆盖
            INPUT, input, //视频输入文件
            SS, String.valueOf(shotSecond),
            T, "0.001",
            F, FORMAT_IMAGE,
            output
        );
    }

    /**
     * 转成HLS的命令参数
     */
    public static List<String> toHLSCmdArrays(String input, String m3u8Output, int cutSecond, String tsBaseUrl,
        VideoInfo vi) {
        if (vi == null || vi.getSize() <= 0) {
            return Collections.emptyList();
        }
        if (vi.getRotate() > 0) {
            return Lists.newArrayList(
                Y, //如果目标文件存在，则直接覆盖
                INPUT, input, //视频输入文件
                VF, "transpose=1", //视频转向
                CV, FORMAT_LIB264, //视频编码格式:H264
                CA, FORMAT_AAC, //音频编码格式:AAC
                STRICT, "-2",
                F, FORMAT_HLS,
                THREADS, DEFAULT_IO_THREADS.toString(), //执行线程数
                HLS_TIME, String.valueOf(cutSecond),
                HLS_LIST_SIZE, "0",
                HLS_WRAP, "0",
                HLS_BASE_URL, tsBaseUrl,
                m3u8Output //输出文件
            );
        }
        return Lists.newArrayList(
            Y, //如果目标文件存在，则直接覆盖
            INPUT, input, //视频输入文件
            CV, FORMAT_LIB264, //视频编码格式:H264
            CA, FORMAT_AAC, //音频编码格式:AAC
            STRICT, "-2",
            F, FORMAT_HLS,
            THREADS, DEFAULT_IO_THREADS.toString(), //执行线程数
            HLS_TIME, String.valueOf(cutSecond),
            HLS_LIST_SIZE, "0",
            HLS_WRAP, "0",
            HLS_BASE_URL, tsBaseUrl,
            m3u8Output //输出文件
        );
    }

    /**
     * 转码到mp4的命令参数(将音频和视频合成)
     */
    public static List<String> toMP4Cmd(String mp4Input, String mp3Input, String output) {
        return Lists.newArrayList(
            Y, //如果目标文件存在，则直接覆盖
            INPUT, mp4Input, //视频输入文件
            INPUT, mp3Input, //音频输入文件
            THREADS, DEFAULT_IO_THREADS.toString(), //执行线程数
            output //输出文件
        );
    }

    /**
     * 转码到mp4的命令参数(不包含音频)
     */
    public static List<String> toMP4CmdArrays(String input, String output) {
        return Lists.newArrayList(
            Y, //如果目标文件存在，则直接覆盖
            INPUT, input, //视频输入文件
            AN, //不录制音频数据
            THREADS, DEFAULT_IO_THREADS.toString(), //执行线程数
            output //输出文件
        );
    }

    /**
     * 转码到mp4的命令参数
     */
    public static List<String> toMP4CmdArrays(String input, String output, VideoInfo vi) {
        if (vi == null || vi.getSize() <= 0) {
            return Collections.emptyList();
        }
        if (vi.getRotate() > 0) {
            return Lists.newArrayList(
                Y, //如果目标文件存在，则直接覆盖
                INPUT, input,  //视频输入文件
                VF, "transpose=1", //视频转向
                CV, COPY, //视频编码格式:copy
                CA, FORMAT_AAC, //音频编码格式:AAC
                STRICT, "-2",
                THREADS, DEFAULT_IO_THREADS.toString(), //执行线程数
                output //输出文件
            );
        }
        return Lists.newArrayList(
            Y, //如果目标文件存在，则直接覆盖
            INPUT, input,  //视频输入文件
            CV, COPY, //视频编码格式:copy
            CA, FORMAT_AAC, //音频编码格式:AAC
            STRICT, "-2",
            THREADS, DEFAULT_IO_THREADS.toString(), //执行线程数
            output //输出文件
        );
    }

    public static List<String> copyMP4CmdArrays(String input, String output, VideoInfo vi) {
        return toMP4CmdArrays(input, output, vi);
    }

    /**
     * 转码到mp4的命令参数
     */
    public static List<String> toMP3CmdArrays(String input, String output, VideoInfo vi) {
        if (vi == null || vi.getSize() <= 0) {
            return Collections.emptyList();
        }
        if (vi.getRotate() > 0) {
            return Lists.newArrayList(
                Y, //如果目标文件存在，则直接覆盖
                INPUT, input,  //视频输入文件
                VF, "transpose=1", //视频转向
                ACODEC, LIBMP3LAME, //音频编码格式：MP3
                output //输出文件
            );
        }
        return Lists.newArrayList(
            Y, //如果目标文件存在，则直接覆盖
            INPUT, input,  //视频输入文件
            ACODEC, LIBMP3LAME, //音频编码格式：MP3
            output //输出文件
        );
    }

    /**
     * 转码到mav的命令参数
     */
    public static List<String> toMAVCmdArrays(String input, String output) {
        return Lists.newArrayList(
            Y, //如果目标文件存在，则直接覆盖
            INPUT, input,  //视频输入文件
            VN,
            output //输出文件
        );
    }
}
