package com.zw.api2.swaggerEntity;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class SwaggerDeviceForm implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiParam(value = "终端ID", required = true)
    private String id;

    @NotEmpty(message = "【终端号】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Pattern(message = "【终端号】格式错误，只能输入字母数字下划线！", regexp = "^[A-Za-z0-9_-]+$", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @Size(max = 20, message = "【终编号】长度不超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "终端ID,字母数字下划线", required = true)
    private String deviceNumber; // 终端编号

    @Size(max = 50, message = "【终端名称】长度不超过50！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "终端名称")
    private String deviceName; // 终端名称

    /**
     * 注册信息-制造商ID
     */
    @ApiParam(value = "制造商ID")
    private String manufacturerId;

    /**
     * 注册信息-终端型号
     */
    @ApiParam(value = "终端型号")
    private String deviceModelNumber;

    /**
     * 组织
     */
    @ApiParam(value = "所属企业名称")
    private String groupName;

    @NotEmpty(message = "【通讯类型】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Pattern(message = "【通讯类型】输入错误，只能输入1,2,3,5,6,7,8,9,10 其中0:：交通部2011版,1:交通部2013-F3版,2:GV320,3:TH,"
        + "5:北斗天地协议,6:康凯斯有线,7:康凯斯无线,8:博实结,9:艾赛欧超长待机,10:F3超长待机设备,11:交通部2013版", regexp = "^[0-9]\\d?|2$", groups = {
        ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "通讯类型,只能输入1,2,3,5,6,7,8,9,10 其中0:：交通部2011版,1:交通部2013-F3版,2:GV320,3:TH,\"\n"
        + "                    + \"5:北斗天地协议,6:康凯斯有线,7:康凯斯无线,8:博实结,9:艾赛欧超长待机,10:F3超长待机设备,11:交通部2013版", required = true)
    private String deviceType; // 通讯类型

    @ApiParam(value = "功能类型,1:简易车,2:行车记录仪,3:对讲设备,4:手咪设备,5:超长待机设备,6:定位终端", required = true)
    private String functionalType; // 功能类型

    @Min(value = 1, message = "【通道数】输入错误，只能输入1,2,3,4;其中1:4,2:5,3:8,4:16！", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @Max(value = 4, message = "【通道数】输入错误，只能输入1,2,3,4;其中1:4,2:5,3:8,4:16！", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @ApiParam(value = "通道数,只能输入1,2,3,4", required = true)
    private Integer channelNumber; // 通道数

    @Size(max = 64, message = "【条码】长度不超过64！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "条码,长度不超过64")
    private String barCode; // 条码

    @Size(max = 100, message = "【制造商】长度不超过100！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "制造商,长度不超过100")
    private String manuFacturer; // 制造商

    @ApiParam(value = "备注")
    private String remark;//备注

    @ApiParam(value = "采购时间")
    private String procurementTimeStr;//采购时间

    @ApiParam(value = "安装时间")
    private String installTimeStr; // 安装时间Str

    @Min(value = 0, message = "【启停状态】输入错误，只能输入0,1,其中0:停用,1:启用！", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @Max(value = 1, message = "【启停状态】输入错误，只能输入0,1,其中0:停用,1:启用！", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @ApiParam(value = "启停状态,0:停用,1:启用", required = true)
    private Integer isStart; // 启停状态

    @Min(value = 0, message = "【是否视频】输入错误，只能输入0,1,其中0:否,1:是！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Max(value = 1, message = "【是否视频】输入错误，只能输入0,1,其中0:否,1:是！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "是否视频 1:是,2:否", required = true)
    private Integer isVideo; // 是否视频

    /**
     * 组织id
     */
    @NotEmpty(message = "【所属企业】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "企业ID", required = true)
    private String groupId;
}
