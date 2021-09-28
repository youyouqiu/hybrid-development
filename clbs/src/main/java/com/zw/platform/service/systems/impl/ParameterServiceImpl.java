package com.zw.platform.service.systems.impl;

import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.event.ConfigUnBindEvent;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.systems.Parameter;
import com.zw.platform.domain.systems.form.DirectiveForm;
import com.zw.platform.domain.systems.query.ParameterQuery;
import com.zw.platform.repository.modules.FenceConfigDao;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.systems.ParameterService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.Converter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ParameterServiceImpl implements ParameterService {

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private UserService userService;

    @Autowired
    private FenceConfigDao fenceConfigDao;

    @Override
    public boolean updateStatusByMsgSNVid(Integer msgSN, String vehicleId, Integer status) {
        this.parameterDao.updateStatusByMsgSNAndReplyCode(msgSN, vehicleId, status);
        return true;
    }

    @Override
    public boolean updateStatusByMsgSN(Integer msgSN, String vehicleId, Integer status) {
        this.parameterDao.updateStatusByMsgSN(msgSN, vehicleId, status);
        return true;
    }

    @Override
    public Page<Parameter> findByPage(ParameterQuery query) throws Exception {
        Page<Parameter> result = PageHelperUtil.doSelect(query, () -> parameterDao.find(query));
        // 时间转换
        for (Parameter p : result) {
            String str = Converter.toString(Converter.toDate(p.getDownTime()), "yyyy-MM-dd HH:mm:ss");
            p.setDownTime(str);
        }
        // 处理result，将groupId对应的groupName给result相应的值赋上
        setGroupNameByGroupId(result);
        return result;
    }

    public void setGroupNameByGroupId(Page<Parameter> result) {
        if (CollectionUtils.isEmpty(result)) {
            return;
        }
        List<OrganizationLdap> orgLdapList = userService.getAllOrganization();
        Pattern splitter = Pattern.compile("#");
        for (Parameter parameter : result) {
            String[] groupIds = splitter.split(parameter.getGroupId());
            if (groupIds.length == 0) {
                continue;
            }
            // config与group是一对多的关系
            StringBuilder groupName = new StringBuilder();
            for (String groupId : groupIds) {
                for (OrganizationLdap orgLdap : orgLdapList) {
                    if (Converter.toBlank(orgLdap.getId()).equals(groupId)) {
                        groupName.append(Converter.toBlank(orgLdap.getName())).append(",");
                    }
                }
            }
            if (groupName.length() > 0) { // 去掉最后一个逗号
                groupName = new StringBuilder(groupName.substring(0, groupName.length() - 1));
            }
            parameter.setGroupName(groupName.toString());
        }
    }

    @Override
    public List<Directive> findById(List<String> ids) {
        if (ids != null && ids.size() > 0) {
            return parameterDao.findById(ids);
        }
        return null;
    }

    @Override
    public boolean updateFenceConfig(String vehicleId, int msgSN) {
        if (StringUtils.isNotBlank(vehicleId)) {
            // 获得围栏绑定id
            String configId = parameterDao.selectFenceConfigId(vehicleId, msgSN);
            if (StringUtils.isNotBlank(configId)) {
                // 恢复围栏绑定
                fenceConfigDao.updateFenceConfigById(configId);
                return true;
            }
        }
        return false;
    }

    @Override
    public void deleteByConfigId(String configId) {
        if (StringUtils.isNotBlank(configId)) {
            parameterDao.deleteByConfigId(configId);
        }
    }

    @Override
    public void deleteByVechicleidParameterName(String vehicleId, String parameterName, String type) {
        if (null == vehicleId) {
            log.error("按照vehicle_id、parameter_name、parameter_type来删除指令下发记录时必须指定vehicle_id，不然不走索引！");
        }
        this.parameterDao.deleteByVechicleidParameterName(vehicleId, parameterName, type);
    }

    @Override
    public void updateMsgSNAndNameById(List<String> ids, int msgSN, Integer status, String parameterName,
        Integer replyCode) {
        parameterDao.updateMsgSNAndNameById(ids, msgSN, status, parameterName, replyCode);
    }

    @Override
    public void addDirective(DirectiveForm form) {
        parameterDao.addDirective(form);
    }

    @Override
    public List<Directive> findParameterByType(String vehicleId, String parameterName, String parameterType) {
        return this.parameterDao.findParameterByType(vehicleId, parameterName, parameterType);
    }

    /**
     * 修改参数下发表数据
     * @param form 参数下发id
     */
    @Override
    public String updateParameterStatus(DirectiveForm form) {
        String sendId;
        if (form.getId() != null && !"".equals(form.getId())) {
            List<String> paramIds = new ArrayList<>();
            paramIds.add(form.getId());
            // 重新下发 ，修改流水号
            sendId = form.getId();
            parameterDao
                .updateMsgSNAndNameById(paramIds, form.getSwiftNumber(), 4, form.getParameterName(), 1); // 1 :// 下发未回应
        } else {
            form.setId(UUID.randomUUID().toString());
            // 新增下发参数数据
            form.setDownTime(new Date());
            form.setReplyCode(1);
            form.setStatus(form.getStatus());
            parameterDao.addDirective(form);
            sendId = form.getId();
        }
        return sendId;
    }

    @Override
    public void addDirective(String vehicleId, int status, int msgSN, String parameterName, int replyCode,
        String parameterType, String directiveName) {
        DirectiveForm form = new DirectiveForm();
        form.setDownTime(new Date());
        form.setMonitorObjectId(vehicleId);
        form.setStatus(status);
        form.setParameterType(parameterType);
        form.setDirectiveName(directiveName);
        form.setParameterName(parameterName);
        form.setSwiftNumber(msgSN);
        form.setReplyCode(replyCode);
        parameterDao.addDirective(form);
    }

    @Override
    public Directive selectDirectiveByConditions(Map<String, Object> map) {
        List<Directive> list = parameterDao.selectDirectiveByConditions(map);
        if (list != null && list.size() > 0) {
            // 数据库中应该只有一条数据,如果有多条,那么只取第一条
            return list.get(0);
        }
        return null;
    }

    @Override
    public void updateMsgSNAndNameAndRemarkById(List<String> paramIds, int msgSN, int status, String parameterName,
        int i, String remark) {
        final Date downTime = new Date();
        parameterDao.updateMsgSNAndNameAndRemarkById(paramIds, msgSN, downTime, status, parameterName, i, remark);
    }

    @Override
    public void deleteByMonitorIds(Set<String> monitorIds) {
        if (CollectionUtils.isNotEmpty(monitorIds)) {
            parameterDao.deleteByMonitorIds(monitorIds);
        }
    }

    @EventListener
    public void updateVehicleUnbound(ConfigUnBindEvent unBindEvent) {
        if (Objects.equals("update", unBindEvent.getOperation())) {
            return;
        }
        Set<String> monitorIds = unBindEvent.getUnbindList().stream().map(BindDTO::getId).collect(Collectors.toSet());
        deleteByMonitorIds(monitorIds);
    }

    @Override
    public List<Directive> findParameterByVehicleIds(List<String> vehicleIds, List<String> parameterNames,
        String type) {
        return parameterDao.findParameterByVehicleIds(vehicleIds, parameterNames, type);
    }

}
