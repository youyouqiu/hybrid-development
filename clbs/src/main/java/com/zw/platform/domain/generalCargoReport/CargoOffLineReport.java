package com.zw.platform.domain.generalCargoReport;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;


/**
 * 普货监管报表离线时长报表实体类
 *
 * @author XK
 */
@Data
public class CargoOffLineReport {

    /**
     * 车牌号
     */
    @ExcelField(title = "车牌号")
    private String brand = "";

    /**
     * 所属企业
     */
    @ExcelField(title = "所属企业")
    private String groupName = "";

    // /**离线时长*/
    // @ExcelField(title = "离线时长")
    // private String offLineDay;

    @ExcelField(title = "最后在线时间")
    private String lastTime;

    /**
     * 离线前显示位置
     */
    @ExcelField(title = "离线前显示位置")
    private String lastLocation;

    /**
     * 离线原因
     */
    @ExcelField(title = "离线原因")
    private String offLineReason = "正常停班   □\n" + "人为破坏   □\n" + "终端故障   □\n" + "流量卡故障 □";

    private String key;

}
