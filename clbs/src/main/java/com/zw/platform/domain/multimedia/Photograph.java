package com.zw.platform.domain.multimedia;

import com.zw.protocol.msg.t808.T808MsgBody;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Created by LiaoYuecai on 2017/3/31.
 */
@Data
public class Photograph implements T808MsgBody {

    @NotNull(message = "通道号不能为空")
    @Min(value = 1, message = "通道号必须要大于0")
    private Integer wayID;//通道ID

    @NotNull(message = "拍照张数不能为空")
    @Min(value = 1, message = "拍照张数必须要大于0")
    @Max(value = 10, message = "拍照张数必须要小于等于10")
    private Integer command;//拍摄命令

    @NotNull(message = "拍照间隔不能为空")
    @Min(value = 0, message = "拍照间隔不能小于0")
    @Max(value = 65535, message = "拍照间隔最大65535")
    private Integer time;//拍照间隔/录像时间

    @NotNull(message = "保存标志不能为空")
    private Integer saveSign;//保存标志

    @NotNull(message = "分辨率不能为空")
    @Min(value = 1, message = "分辨率选择错误")
    private Integer distinguishability;//分辨率

    @NotNull(message = "图像/视频质量不能为空")
    @Min(value = 1, message = "图像质量必须要大于0")
    @Max(value = 10, message = "图像质量必须要小于等于10")
    private Integer quality;//图像/视频质量

    @NotNull(message = "亮度不能为空")
    @Min(value = 0, message = "亮度值不能小于0")
    @Max(value = 255,  message = "亮度值不能大于255")
    private Integer luminance;//亮度

    @NotNull(message = "对比度不能为空")
    @Min(value = 0, message = "对比度必须要大于等于0")
    @Max(value = 127, message = "对比度必须要小于等于127")
    private Integer contrast;//对比度

    @NotNull(message = "饱和度不能为空")
    @Min(value = 0, message = "饱和度必须要大于等于0")
    @Max(value = 127, message = "饱和度必须要小于等于127")
    private Integer saturability;//饱和度

    @NotNull(message = "色度不能为空")
    @Min(value = 0, message = "色度必须要大于等于0")
    @Max(value = 255,  message = "色度必须要小于等于255")
    private Integer chroma;//

    @NotNull(message = "拍照方式不能为空")
    private Integer photographType;//0 代表平台，1代表终端

}
