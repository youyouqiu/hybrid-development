package com.zw.platform.domain.vas.alram;

import lombok.Data;

import java.io.Serializable;

/**
 * 拍照和录像总用这一个类.
 */
@Data
public class PhotoDTO implements Serializable {
    private String id;
    /**
     * 通道ID集合, 多个逗号分隔.
     */
    private String wayId;

    /**
     * 拍摄命令，0-停止拍摄；0xFFFF-录像；其它表示拍照张数
     * 貌似是用这个字段来区分是录像还是拍照
     */
    private Integer command;

    /**
     * 拍照间隔/录像时间; 单位：秒，0 表示按最小间隔拍照或一直录像
     */
    private Integer time;

    /**
     * 保存标志: 1：保存；0：实时上传
     */
    private Integer saveSign;

    /**
     * 分辨率
     */
    private Integer resolution;

    /**
     * 图像/视频质量；范围1-10，1代表质量损失最小，10表示压缩比最大
     */
    private Integer quality;

    /**
     * 亮度 0-255
     */
    private Integer luminance;

    /**
     * 对比度 0-127
     */
    private Integer contrast;

    /**
     * 饱和度0-127
     */
    private Integer saturability;

    /**
     * 色度 0-255
     */
    private Integer chroma;
}
