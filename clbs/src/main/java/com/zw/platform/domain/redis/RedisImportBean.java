package com.zw.platform.domain.redis;

import com.zw.platform.domain.basicinfo.form.*;
import com.zw.platform.domain.infoconfig.ConfigList;
import com.zw.platform.domain.infoconfig.form.AssignmentVehicleForm;
import com.zw.platform.domain.infoconfig.form.ConfigImportForm;
import com.zw.platform.domain.infoconfig.form.ConfigTransportImportForm;
import com.zw.platform.domain.infoconfig.form.ProfessionalForConfigFrom;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 封装导入redis数据的bean
 */
@Data
public class RedisImportBean {

    private Set<AssignmentVehicleForm> assignVehicleForms;

    private Set<ConfigImportForm> configFormList;

    private Set<ConfigList> configList;

    private Set<DeviceForm> deviceForms;

    private Set<DeviceForm> deviceUpdateForms;

    private Set<DeviceGroupForm> deviceGroupForms;

    private Set<LifecycleInfoForm> lifecycleInfoForms;

    private Set<PersonnelForm> personnelForms;

    private Set<VehicleGroupForm> personGroupForms;

    private Set<ThingInfoForm> thingForms;

    private Set<ProfessionalForConfigFrom> professionalForms;

    private Set<SimcardForm> simcardForms;

    private Set<SimcardForm> simcardUpdateForms;

    private Set<String> simcardExistedList;

    private Set<SimGroupForm> simGroupForms;

    private Set<VehicleForm> vehicleForms;

    private Set<VehicleForm> vehicleUpdateForms;

    private Set<PersonnelForm> peopleUpdateForms;

    private Set<ThingInfoForm> thingUpdateForms;

    private Set<String> vehicleExistedList;

    private Set<String> peopleExistedList;

    private Set<String> thingExistedList;

    private Set<VehicleGroupForm> vehicleGroupForms;

    private Set<AssignmentForm> assignmentForms;

    private Set<AssignmentGroupForm> assignmentGroupForms;

    private Map<String, Set<AssignmentForm>> groupWithAssignments;

    /**
     * 新增分组时存储groupId 和 assignmentIds, 用于分组授权
     */
    private Map<String, Set<AssignmentForm>> groupIdAssignmentMap;

    private Map<String, String> vehicleBindChannelNum;

    /**
     * 已存在的groupId和分组
     */
    private Map<String, Set<AssignmentForm>> cacheGroupIdAssignmentMap;

    private Set<ConfigTransportImportForm> configTransportImportFormList;

    private Boolean vehicleFlag = false;

    private Boolean peopleFlag = false;

    private Boolean thingFlag = false;

    private StringBuilder assignmentBuilder;
    private Set<String> assignmentNames;
    private Integer totalSize = 0;

    private Map<String, Integer> assignmentWithVehicles;

    public RedisImportBean() {
        assignVehicleForms = new HashSet<>();
        configList = new HashSet<>();
        configFormList = new HashSet<>();
        deviceForms = new HashSet<>();
        deviceUpdateForms = new HashSet<>();
        deviceGroupForms = new HashSet<>();
        lifecycleInfoForms = new HashSet<>();
        personnelForms = new HashSet<>();
        personGroupForms = new HashSet<>();
        professionalForms = new HashSet<>();
        simcardForms = new HashSet<>();
        simcardUpdateForms = new HashSet<>();
        simcardExistedList = new HashSet<>();
        simGroupForms = new HashSet<>();
        vehicleForms = new HashSet<>();
        vehicleUpdateForms = new HashSet<>();
        vehicleExistedList = new HashSet<>();
        vehicleGroupForms = new HashSet<>();
        thingForms = new HashSet<>();
        peopleExistedList = new HashSet<>();
        thingExistedList = new HashSet<>();
        peopleUpdateForms = new HashSet<>();
        thingUpdateForms = new HashSet<>();
        assignmentForms = new HashSet<>();
        assignmentGroupForms = new HashSet<>();
        groupIdAssignmentMap = new HashMap<>();
        configTransportImportFormList = new HashSet<>();
        assignmentBuilder = new StringBuilder();
        assignmentNames = new HashSet<>();
        groupWithAssignments = new HashMap<>();
        assignmentWithVehicles = new HashMap<>();
        cacheGroupIdAssignmentMap = new HashMap<>();
        vehicleBindChannelNum = new HashMap<>();
    }

    public void addAssignVehicleForm(AssignmentVehicleForm form) {
        assignVehicleForms.add(form);
    }

    public void addConfig(ConfigList config) {
        configList.add(config);
    }

    public void addConfigForm(ConfigImportForm form) {
        configFormList.add(form);
    }

    public void addDeviceForm(DeviceForm form) {
        deviceForms.add(form);
    }

    public void addDeviceForms(List<DeviceForm> forms) {
        deviceForms.addAll(forms);
    }

    public void addDeviceUpdateForm(DeviceForm form) {
        deviceUpdateForms.add(form);
    }

    public void addDeviceGroupForm(DeviceGroupForm form) {
        deviceGroupForms.add(form);
    }

    public void addDeviceGroupForms(List<DeviceGroupForm> forms) {
        deviceGroupForms.addAll(forms);
    }

    public void addLifecycleInfoForm(LifecycleInfoForm form) {
        lifecycleInfoForms.add(form);
    }

    public void addPersonnelForm(PersonnelForm form) {
        personnelForms.add(form);
    }

    public void addPersonGroupForm(VehicleGroupForm form) {
        personGroupForms.add(form);
    }

    public void addThingForm(ThingInfoForm form) {
        thingForms.add(form);
    }

    public void addProfessionalForm(ProfessionalForConfigFrom form) {
        professionalForms.add(form);
    }

    public void addSimCardForm(SimcardForm form) {
        simcardForms.add(form);
    }

    public void addSimCardUpdateForm(SimcardForm form) {
        simcardUpdateForms.add(form);
    }

    public void addExistedSimCard(String number) {
        simcardExistedList.add(number);
    }

    public void addSimGroupForm(SimGroupForm form) {
        simGroupForms.add(form);
    }

    public void addVehicleForm(VehicleForm form) {
        vehicleForms.add(form);
    }

    public void addVehicleUpdateForm(VehicleForm form) {
        vehicleUpdateForms.add(form);
    }

    public void addPeopleUpdateForm(PersonnelForm form) {
        peopleUpdateForms.add(form);
    }

    public void addThingUpdateForm(ThingInfoForm form) {
        thingUpdateForms.add(form);
    }

    public void addExistedVehicle(String brand) {
        vehicleExistedList.add(brand);
    }

    public void addExistedPeople(String peopleNumber) {
        peopleExistedList.add(peopleNumber);
    }

    public void addExistedThing(String thingNumber) {
        thingExistedList.add(thingNumber);
    }

    public void addVehicleGroupForm(VehicleGroupForm form) {
        vehicleGroupForms.add(form);
    }

    public void changeVehicleFlag(boolean flag) {
        vehicleFlag = flag;
    }

    public void changePeopleFlag(boolean flag) {
        peopleFlag = flag;
    }

    public void changeThingFlag(boolean flag) {
        thingFlag = flag;
    }

    public void addAssignment(AssignmentForm assignmentForm) {
        assignmentForms.add(assignmentForm);
    }

    public void addAssignGroupForm(AssignmentGroupForm form) {
        assignmentGroupForms.add(form);
    }

    public void put(String groupId, AssignmentForm assignment) {
        groupIdAssignmentMap.computeIfAbsent(groupId, x -> new HashSet<>()).add(assignment);
    }

    public Boolean checkGroupIsExist(String groupId) {
        return cacheGroupIdAssignmentMap.containsKey(groupId);
    }

    public void addTransportConfigForm(ConfigTransportImportForm form) {
        configTransportImportFormList.add(form);
    }

    public void addAssignmentName(String groupName) {
        if (!assignmentNames.contains(groupName)) {
            assignmentNames.add(groupName);
        }
    }

    public Set<String> getAssignmentNames() {
        return assignmentNames;
    }

    public Integer getTotalSize() {
        return totalSize;
    }

    public void addTotalSize(Integer totalSize) {
        this.totalSize = totalSize;
    }

    /**
     * 获取组织下未存满100个监控对象的分组
     * @param groupId
     * @return
     */
    public AssignmentForm getCanAddVehicleOfAssignmentForm(String groupId) {
        Set<AssignmentForm> assignmentFormSets = cacheGroupIdAssignmentMap.get(groupId);
        if (CollectionUtils.isNotEmpty(assignmentFormSets)) {
            assignmentFormSets.forEach(assign -> {
                if (assign.getOrderNum() == null) {
                    assign.setOrderNum(0);
                }
            });
            List<AssignmentForm> assignmentFormList =
                assignmentFormSets.stream().sorted(Comparator.comparing(AssignmentForm::getOrderNum))
                    .collect(Collectors.toList());
            // 当前企业下的分组, 找到第一个未存满100个监控对象的分组, 往该分组下添加数据
            for (AssignmentForm assignmentForm : assignmentFormList) {
                Integer assignmentNumber = getAssignmentNumber(assignmentForm.getId());
                if (assignmentNumber < 100) {
                    return assignmentForm;
                }
            }
        }
        return null;
    }

    public Integer getAssignmentNumber(String assignmentId) {
        return assignmentWithVehicles.get(assignmentId);
    }

    /**
     * 分组下的车辆
     * @param assignmentId 分组ID
     * @param monitorNumber 分组下的监控对象数量
     */
    public void putAssignmentNumber(String assignmentId, Integer monitorNumber) {
        assignmentWithVehicles.put(assignmentId, monitorNumber);
    }

    public void putCacheGroup(String groupId, AssignmentForm assignment) {
        cacheGroupIdAssignmentMap.computeIfAbsent(groupId, x -> new HashSet<>()).add(assignment);
    }

    public void putVehicleBindChanelNum(String vehicleId, String terminalTypeId) {
        vehicleBindChannelNum.put(vehicleId, terminalTypeId);
    }

    public Map<String, String> getVehicleBindChannelNum() {
        return vehicleBindChannelNum;
    }
}
