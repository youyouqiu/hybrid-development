package com.zw.platform.basic.dto.result;

import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.dto.ThingDTO;
import joptsimple.internal.Strings;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: zjc
 * @Description:删除物品时候，遇到绑定的时候，后端需要重新组装一些额外参数给前端
 * @Date: create in 2020/12/23 11:51
 */
@Data
public class DeleteThingDTO {
    /**
     * 绑定的物品编号
     */
    private String boundThingNumbers = "";
    /**
     * 绑定的物品id,按照逗号隔开
     */
    private String boundThingIds = "";
    /**
     * 返回的消息信息
     */
    private String infoMsg = "";

    private transient List<String> notBindMonitorIds = new ArrayList<>();

    public static DeleteThingDTO getResult(List<ThingDTO> details, String vehicleBrandBound) {
        DeleteThingDTO deleteThingDTO = new DeleteThingDTO();
        List<String> bindMonitorIds = new ArrayList<>();
        List<String> bindMonitorNames = new ArrayList<>();

        for (ThingDTO detail : details) {
            if (Vehicle.BindType.HAS_BIND.equals(detail.getBindType())) {
                bindMonitorIds.add(detail.getId());
                bindMonitorNames.add(detail.getName());
            } else {
                deleteThingDTO.notBindMonitorIds.add(detail.getId());
            }
        }
        if (bindMonitorNames.size() <= 0) {
            return deleteThingDTO;
        }
        deleteThingDTO.boundThingIds = Strings.join(bindMonitorIds, ",");
        deleteThingDTO.boundThingNumbers = Strings.join(bindMonitorNames, ",");
        deleteThingDTO.infoMsg = vehicleBrandBound;
        return deleteThingDTO;

    }
}
