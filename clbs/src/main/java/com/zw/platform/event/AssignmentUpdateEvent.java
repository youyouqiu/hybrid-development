package com.zw.platform.event;

import com.zw.platform.domain.infoconfig.form.AssignmentVehicleForm;
import org.springframework.context.ApplicationEvent;

import java.util.List;

public class AssignmentUpdateEvent extends ApplicationEvent {
    private String assignmentId;
    private List<AssignmentVehicleForm> vehiclePerAddList;
    private List<AssignmentVehicleForm> vehiclePerDeleteList;

    public AssignmentUpdateEvent(Object source, String assignmentId, List<AssignmentVehicleForm> vehiclePerAddList,
        List<AssignmentVehicleForm> vehiclePerDeleteList) {
        super(source);
        this.assignmentId = assignmentId;
        this.vehiclePerAddList = vehiclePerAddList;
        this.vehiclePerDeleteList = vehiclePerDeleteList;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public List<AssignmentVehicleForm> getVehiclePerAddList() {
        return vehiclePerAddList;
    }

    public List<AssignmentVehicleForm> getVehiclePerDeleteList() {
        return vehiclePerDeleteList;
    }

}