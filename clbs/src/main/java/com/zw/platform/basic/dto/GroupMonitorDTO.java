package com.zw.platform.basic.dto;

import com.zw.platform.domain.infoconfig.form.AssignmentVehicleForm;
import lombok.Data;

/**
 * @author wanxing
 * @Title: 分组-监控对象实体
 * @date 2020/11/314:43
 */
@Data
public class GroupMonitorDTO {

    private String id;
    private String brand;
    /**
     * 监控对象类型 0：车；1：人；2：物品
     */
    private String monitorType;

    /**
     * 分组Id
     */
    private String groupId;

    /**
     * 监控对象Id
     */
    private String monitorId;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 组旋钮位置编号
     */
    private Integer knobNo;

    /**
     * 对讲群组id
     */
    private Long intercomGroupId;

    /**
     * 原分组名称
     */
    private String sourceGroupName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GroupMonitorDTO that = (GroupMonitorDTO) o;

        if (getGroupId() != null ? !getGroupId().equals(that.getGroupId()) : that.getGroupId() != null) {
            return false;
        }
        return getMonitorId() != null ? getMonitorId().equals(that.getMonitorId()) : that.getMonitorId() == null;
    }

    @Override
    public int hashCode() {
        int result = getGroupId() != null ? getGroupId().hashCode() : 0;
        result = 31 * result + (getMonitorId() != null ? getMonitorId().hashCode() : 0);
        return result;
    }

    /**
     * 转换
     * @param ass
     */
    public static GroupMonitorDTO translate(AssignmentVehicleForm ass) {
        GroupMonitorDTO groupMonitorDTO = new GroupMonitorDTO();
        groupMonitorDTO.setMonitorId(ass.getVehicleId());
        groupMonitorDTO.setGroupId(ass.getAssignmentId());
        groupMonitorDTO.setGroupName(ass.getAssignmentName());
        groupMonitorDTO.setMonitorType(ass.getMonitorType());
        groupMonitorDTO.setSourceGroupName(ass.getSourceAssignName());
        return groupMonitorDTO;
    }
}
