package com.zw.platform.util.ffmpeg;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class FFmpegCommandRunner {
    private static final Logger log = LogManager.getLogger(FFmpegCommandRunner.class);

    /**
     * 转换视频格式为MP4
     */
    public static VideoFile coverToMp4(File input) {
        if (!isFfmpegSupport(input.getPath())) {
            return null;
        }
        final File targetFile = FileUtils.getMp4OutputByInput(input);
        if (!targetFile.exists()) {
            return null;
        }
        VideoFile vf = new VideoFile(input, targetFile);
        vf.setInputInfo(getVideoInfo(input));
        if (vf.getInputInfo().getSize()  <= 0) {
            return null;
        }
        List<String> commands = Lists.newArrayList(BaseCommandOption.getFFmpegBinary());

        commands.addAll(BaseCommandOption
            .toMP4CmdArrays(input.getAbsolutePath(), targetFile.getAbsolutePath(), vf.getInputInfo()));

        if (StringUtils.isNotEmpty(runProcess(commands))) {
            vf.setTargetInfo(getVideoInfo(targetFile));
            vf.setSuccess(true);
            return vf;
        }
        return null;
    }

    public static File coverToMp4of0801(File input) throws IOException {
        File video = getVideoFile("mp4", input);
        File audio = null;
        try {
            audio = getVideoFile("wav", input);
        } catch (Exception e) {
            log.info("0801该视频没有音频");
        }
        if (video == null || audio == null) {
            return video;
        }
        try {
            File res = null;
            if (checkContentType(input.getPath()) == 0) {
                List<String> commands = Lists.newArrayList(BaseCommandOption.getFFmpegBinary());
                res = new File(input.getParent() + File.separator + FileUtils.getFileName(input) + "_all.mp4File");
                List<String> command =
                    BaseCommandOption.toMP4Cmd(video.getAbsolutePath(), audio.getAbsolutePath(), res.getAbsolutePath());
                commands.addAll(command);
                if (StringUtils.isEmpty(runProcess(commands))) {
                    throw new IllegalThreadStateException();
                }
            }
            return res;
        } finally {
            Files.deleteIfExists(audio.toPath());
            Files.deleteIfExists(video.toPath());
        }
    }

    private static File getVideoFile(String filetype, File input) {
        if (checkContentType(input.getPath()) != 0) {
            return null;
        }
        File result;
        List<String> commands = Lists.newArrayList(BaseCommandOption.getFFmpegBinary());
        if (filetype.equals("mp4")) {
            result = FileUtils.getMp4OutputByInput(input);
            commands.addAll(
                BaseCommandOption.toMP4CmdArrays(input.getAbsolutePath(), result.getAbsolutePath()));
        } else {
            result = FileUtils.getWAVOutputByInput(input);
            commands.addAll(
                BaseCommandOption.toMAVCmdArrays(input.getAbsolutePath(), result.getAbsolutePath()));
        }
        if (!result.exists()) {
            if (StringUtils.isNotEmpty(runProcess(commands))) {
                return result;
            } else {
                throw new IllegalThreadStateException();
            }
        }
        return result;
    }

    /**
     * 复制到mp4文件
     */
    public static VideoFile copyToMp4(File input) {
        VideoFile vf = null;
        if (checkContentType(input.getPath()) == 0) {
            vf = new VideoFile(input, FileUtils.getMp4OutputByInput(input));
            if (vf.getTarget() != null && !vf.getTarget().exists()) {
                vf.setInputInfo(getVideoInfo(input));
                if (vf.getInputInfo().getSize() > 0) {
                    List<String> commands = Lists.newArrayList(BaseCommandOption.getFFmpegBinary());

                    commands.addAll(BaseCommandOption
                        .copyMP4CmdArrays(input.getAbsolutePath(), vf.getTarget().getAbsolutePath(),
                            vf.getInputInfo()));

                    if (StringUtils.isNotEmpty(runProcess(commands))) {
                        vf.setTargetInfo(getVideoInfo(vf.getTarget()));
                        vf.setSuccess(true);
                        return vf;
                    }
                }

            }
        }
        return vf;
    }

    /**
     * 转换视频格式为MP4
     */
    public static VideoFile coverToMp3(File input) {
        VideoFile vf = null;
        if (checkWavType(input.getPath()) == 0) {
            vf = new VideoFile(input, FileUtils.getMp3OutputByInput(input));
            if (vf.getTarget() != null && !vf.getTarget().exists()) {
                vf.setInputInfo(getVideoInfo(input));
                if (vf.getInputInfo().getSize() > 0) {
                    List<String> commands = Lists.newArrayList(BaseCommandOption.getFFmpegBinary());

                    commands.addAll(BaseCommandOption
                        .toMP3CmdArrays(input.getAbsolutePath(), vf.getTarget().getAbsolutePath(), vf.getInputInfo()));
                    if (StringUtils.isNotEmpty(runProcess(commands))) {
                        vf.setTargetInfo(getVideoInfo(vf.getTarget()));
                        vf.setSuccess(true);
                        return vf;
                    }
                }

            }
        }
        return vf;
    }

    /**
     * 获取视频信息
     */
    public static VideoInfo getVideoInfo(File input) {
        VideoInfo vi = new VideoInfo();
        if (input != null && input.exists()) {
            List<String> commands = Lists.newArrayList(BaseCommandOption.getFFprobeBinary());
            commands.add(input.getAbsolutePath());
            vi.setSize(FileUtils.getFineSize(input));
            if (vi.getSize() > 0) {
                return FFmpegUtils.regInfo(runProcess(commands), vi);
            }
        } else {
            if (log.isErrorEnabled()) {
                log.error("video '{}' is not fount! ", input.getAbsolutePath());
            }
        }

        return vi;
    }

    private static final List<String> supportedType = Arrays.asList(
        "avi", "mpg", "3gp", "h264", "264", "dat", "flv", "asx", "asf", "mov", "mp4"
    );

    /**
     * 判断是否是ffmpeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv）
     */
    private static boolean isFfmpegSupport(String path) {
        String type = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
        return supportedType.contains(type);
    }

    /**
     * 校验文件格式
     */
    private static int checkContentType(String path) {
        String type = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
        // ffmpeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv, ts等）
        // 对ffmpeg无法解析的文件格式(wmv9，rm，rmvb等),
        // 可以先用别的工具（mencoder）转换为avi(ffmpeg能解析的)格式.
        switch (type) {
            case "avi":
            case "mpg":
            case "wmv":
            case "3gp":
            case "h264":
            case "264":
            case "dat":
            case "flv":
            case "asx":
            case "asf":
            case "mp4":
            case "mov":
            case "ts":
                return 0;
            case "wmv9":
            case "rmvb":
            case "rm":
                return 1;
            default:
                return 9;
        }
    }

    /**
     * 校验wav文件格式
     */
    private static int checkWavType(String path) {
        String type = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
        if (type.equals("wav")) {
            return 0;
        }
        return 9;
    }

    /**
     * 执行命令
     */
    public static String runProcess(List<String> commands) {
        long start = System.currentTimeMillis();
        ProcessBuilder pb = new ProcessBuilder(commands);

        pb.redirectErrorStream(true);

        String result = null;
        Process process = null;
        try {
            process = pb.start();
            result = getExecuteResult(process);
            int flag = process.waitFor();
            if (flag != 0) {
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("errorStream:", e);
        } finally {
            if (null != process) {
                try {
                    process.getInputStream().close();
                    process.getOutputStream().close();
                    process.getErrorStream().close();
                    process.destroy();
                } catch (IOException e) {
                    log.error("Close stream error:{}", result, e);
                }
            }
            if (log.isInfoEnabled()) {
                log.info("ffmpeg run cost {} milliseconds", System.currentTimeMillis() - start);
            }
        }
        return result;
    }

    private static String getExecuteResult(Process process) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = input.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

}
