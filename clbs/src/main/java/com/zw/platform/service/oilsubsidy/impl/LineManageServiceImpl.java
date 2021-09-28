package com.zw.platform.service.oilsubsidy.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.netty.ServerParamList;
import com.zw.platform.domain.oilsubsidy.line.DirectionDO;
import com.zw.platform.domain.oilsubsidy.line.DirectionDTO;
import com.zw.platform.domain.oilsubsidy.line.DirectionStationDTO;
import com.zw.platform.domain.oilsubsidy.line.DirectionStationMiddleDO;
import com.zw.platform.domain.oilsubsidy.line.Line1301CommandDTO;
import com.zw.platform.domain.oilsubsidy.line.Line1302CommandDTO;
import com.zw.platform.domain.oilsubsidy.line.LineDO;
import com.zw.platform.domain.oilsubsidy.line.LineDTO;
import com.zw.platform.domain.oilsubsidy.line.LineQuery;
import com.zw.platform.domain.oilsubsidy.station.StationDO;
import com.zw.platform.repository.oilsubsidy.DirectionManageDao;
import com.zw.platform.repository.oilsubsidy.DirectionStationMiddleDao;
import com.zw.platform.repository.oilsubsidy.LineManageDao;
import com.zw.platform.repository.oilsubsidy.StationManageDao;
import com.zw.platform.repository.vas.ForwardVehicleManageDao;
import com.zw.platform.service.oilsubsidy.ForwardVehicleManageService;
import com.zw.platform.service.oilsubsidy.LineManageService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.OilSubsidyCommand;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t809.body.OilSubsidyUpData;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wanxing
 * @Title: 线路serviceimpl
 * @date 2020/10/913:48
 */
@Service
@Slf4j
public class LineManageServiceImpl implements LineManageService {

    @Autowired
    private LineManageDao lineManageDao;

    @Autowired
    private ServerParamList serverParamList;
    @Autowired
    private StationManageDao stationManageDao;

    @Autowired
    private DirectionManageDao directionManageDao;

    @Autowired
    private DirectionStationMiddleDao directionStationMiddleDao;

    @Autowired
    private ForwardVehicleManageDao forwardVehicleManageDao;

    @Autowired
    private UserService userService;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private ForwardVehicleManageService forwardVehicleManageService;

    @Autowired
    private OrganizationService organizationService;

    @Value("${send.line.flag:true}")
    private Boolean sendLineFlag;

    @Override
    public boolean add(LineDTO lineDTO) throws BusinessException {

        checkIdentifyExist(lineDTO);
        List<DirectionDTO> direction = lineDTO.getDirection();
        List<DirectionDO> directionDOList = Lists.newArrayList();
        String lineId = lineDTO.getId();
        List<DirectionStationMiddleDO> list = Lists.newArrayList();
        try {
            DirectionDO directionDO;
            for (DirectionDTO directionDTO : direction) {
                directionDTO.setLineId(lineId);
                directionDO = new DirectionDO();
                BeanUtils.copyProperties(directionDTO, directionDO);
                directionDOList.add(directionDO);
                generateDirectionStationMiddleData(list, directionDTO);
            }
            lineManageDao.add(lineDTO.copyDto2DO());
            directionManageDao.addBatch(directionDOList);
            directionStationMiddleDao.addBatch(list);
            //记录日志
            addLog("线路管理：新增线路（" + lineDTO.getName() + ": " + lineDTO.getIdentify() + "）");
            upLineInfo(true);
        } catch (Exception e) {
            throw new BusinessException(e.getMessage(), "线路插入异常");
        }
        return true;
    }

    /**
     * 日志
     * @param message
     */
    private void addLog(String message) {
        logSearchService.addLog(getIpAddress(), message, "3", "线路管理");
    }

    /**
     * 校验同企业下，名称重复
     * @param lineDTO
     * @throws BusinessException
     */
    private void checkIdentifyExist(LineDTO lineDTO) throws BusinessException {
        String identify = lineDTO.getIdentify();
        String orgId = lineDTO.getDockingCodeOrgId();
        //通过企业Id和名称检查重复性
        int no = lineManageDao.checkIdentifyExist(orgId, lineDTO.getId(), identify);
        if (no > 0) {
            throw new BusinessException("", "同企业下已经存在该标识");
        }
    }

    private void generateDirectionStationMiddleData(List<DirectionStationMiddleDO> list, DirectionDTO directionDTO) {
        List<String> stationIds = directionDTO.getStationIds();
        DirectionStationMiddleDO directionStationMiddleDO;
        String stationId;
        for (int i = 0; i < stationIds.size(); i++) {
            stationId = stationIds.get(i);
            directionStationMiddleDO = new DirectionStationMiddleDO(directionDTO.getId(), stationId, (short) i,
                directionDTO.getDirectionType());
            list.add(directionStationMiddleDO);
        }
    }

    @Override
    public boolean update(LineDTO lineDTO) throws BusinessException {
        checkIdentifyExist(lineDTO);
        List<DirectionDTO> directions = lineDTO.getDirection();
        List<String> directionIds = directions.stream().map(DirectionDO::getId).collect(Collectors.toList());
        if (directionIds.isEmpty()) {
            return false;
        }
        List<DirectionStationMiddleDO> list = Lists.newArrayList();
        try {
            //先删除中间表
            directionStationMiddleDao.deleteBatch(directionIds);
            //获取
            LineDO oldLine = lineManageDao.getAllFieldById(lineDTO.getId());
            if (!oldLine.getIdentify().equals(lineDTO.getIdentify())) {
                //标识更新,需要通知车
                List<String> vehicleIds = lineManageDao.getVehicleIdByLineId(lineDTO.getId());
                if (!vehicleIds.isEmpty()) {
                    forwardVehicleManageService.send809Message(vehicleIds);
                }
            }
            lineManageDao.update(lineDTO.copyDto2DO());
            DirectionDO directionDO;
            for (DirectionDTO directionDTO : directions) {
                directionDO = new DirectionDO();
                BeanUtils.copyProperties(directionDTO, directionDO);
                directionManageDao.update(directionDO);
                generateDirectionStationMiddleData(list, directionDTO);
            }
            //添加中间表关系
            directionStationMiddleDao.addBatch(list);
            //记录日志
            addLog("线路管理：修改线路（" + lineDTO.getName() + ": " + lineDTO.getIdentify() + "）");
            upLineInfo(sendLineFlag);
        } catch (Exception e) {
            throw new BusinessException(e.getMessage(), "线路更新异常");
        }
        return true;
    }

    @Override
    public boolean delete(String id) throws BusinessException {

        if (StringUtils.isEmpty(id)) {
            return false;
        }
        //先检查是否具有绑定车辆关系
        Set<String> bindList = forwardVehicleManageDao.checkBindLineIds(Collections.singleton(id));
        if (!bindList.isEmpty()) {
            for (String s : bindList) {
                LineDO lineDo = lineManageDao.getAllFieldById(s);
                if (lineDo != null) {
                    throw new BusinessException("", lineDo.getName() + "线路,已经使用，不能删除哦！");
                }
            }
        }
        List<String> directionIds = directionManageDao.getIdsByLineId(id);
        if (!directionIds.isEmpty()) {
            directionManageDao.deleteBatch(directionIds);
            directionStationMiddleDao.deleteBatch(directionIds);
        }
        LineDO lineDo = lineManageDao.getAllFieldById(id);
        lineManageDao.delete(id);
        //记录日志
        addLog("线路管理：删除线路（" + lineDo.getName() + ": " + lineDo.getIdentify() + "）");
        return true;
    }

    @Override
    public String delBatchReturnStr(Collection<String> ids) throws BusinessException {

        if (ids.isEmpty()) {
            throw new BusinessException("", "参数为空");
        }
        Set<String> lineIds = Sets.newHashSet(ids);
        int size = lineIds.size();
        Set<String> bindList = forwardVehicleManageDao.checkBindLineIds(lineIds);
        lineIds.removeAll(bindList);
        int bindSize = bindList.size();
        int no = size - bindSize;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("删除成功").append(no).append("条记录。");
        if (no != size) {
            List<String> list = lineManageDao.getNamesByIds(bindList);
            if (list.isEmpty()) {
                throw new BusinessException("", "车辆和线路数据库存在垃圾数据，请联系管理员");
            }
            String name;
            for (int i = 0; i < list.size(); i++) {
                name = list.get(i);
                if (i != list.size() - 1) {
                    stringBuilder.append(name).append("线路").append("、");
                } else {
                    stringBuilder.append(name).append("线路");
                }
            }
            stringBuilder.append("这").append(bindSize).append("条线路已经被使用，不能被删除！");
        }
        if (!lineIds.isEmpty()) {
            lineManageDao.deleteBatch(lineIds);
            List<String> directionIds = directionManageDao.getIdsByLineIds(lineIds);
            if (!directionIds.isEmpty()) {
                directionManageDao.deleteBatch(directionIds);
                directionStationMiddleDao.deleteBatch(directionIds);
            }
            //记录日志
            List<Map<String, String>> list = lineManageDao.getNameAndIdentifyByIds(lineIds);
            for (Map<String, String> map : list) {
                List<String> values = new ArrayList<>(map.values());
                addLog("线路管理：删除线路（" + values.get(0) + ": " + values.get(1) + "）");
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public List<LineDTO> getLineByOrgId(String orgId) {
        return lineManageDao.getLineByOrgId(orgId);
    }

    @Override
    public Page<LineDTO> getListByKeyword(LineQuery query) throws BusinessException {

        if (StringUtils.isEmpty(query.getOrgId())) {
            List<String> orgIds = userService.getCurrentUserOrgIds();
            query.setCurrentUserOrgIds(orgIds);
        } else {
            query.setCurrentUserOrgIds(Lists.newArrayList(query.getOrgId()));
        }
        Page<LineDTO> result = PageHelperUtil.doSelect(query, () -> lineManageDao.getListByKeyword(query));
        OrganizationLdap organizationLdap;
        Map<String, OrganizationLdap> map =
            organizationService.getOrgByUuids(Sets.newHashSet(query.getCurrentUserOrgIds()));
        for (LineDTO lineDTO : result) {
            organizationLdap = map.get(lineDTO.getDockingCodeOrgId());
            if (organizationLdap != null) {
                lineDTO.setOrgName(organizationLdap.getName());
            }
        }
        return result;
    }

    @Override
    public LineDTO getById(String id) throws BusinessException {
        LineDO lindDo = lineManageDao.getAllFieldById(id);
        if (lindDo == null) {
            return new LineDTO();
        }
        LineDTO lineDTO = lindDo.copyDo2DTO();
        try {
            List<DirectionDO> directionDOList = directionManageDao.getListByLineId(lindDo.getId());
            OrganizationLdap org = organizationService.getOrganizationByUuid(lindDo.getDockingCodeOrgId());
            if (org != null) {
                lineDTO.setOrgName(org.getName());
            }
            if (directionDOList.isEmpty()) {
                return lineDTO;
            }
            List<DirectionDTO> directionDTOList = Lists.newArrayList();
            List<String> directionIds = Lists.newArrayList();
            Map<String, DirectionDTO> map = new HashMap<>(2);
            DirectionDTO directionDTO;
            for (DirectionDO directionDO : directionDOList) {
                directionDTO = new DirectionDTO();
                BeanUtils.copyProperties(directionDO, directionDTO);
                directionDTO.setStationDTOList(Lists.newArrayList());
                map.put(directionDTO.getId(), directionDTO);
                directionDTOList.add(directionDTO);
                directionIds.add(directionDO.getId());
            }
            lineDTO.setDirection(directionDTOList);
            List<DirectionStationDTO> list = directionStationMiddleDao.getStationInfoByDirectionId(directionIds);
            for (DirectionStationDTO directionStationDTO : list) {
                directionDTO = map.get(directionStationDTO.getDirectionInfoId());
                if (directionDTO == null) {
                    continue;
                }
                directionDTO.getStationDTOList().add(directionStationDTO);
            }
        } catch (Exception e) {
            throw new BusinessException(e.getMessage(), "车辆和线路数据库存在垃圾数据，请联系管理员");
        }
        return lineDTO;
    }

    @Override
    public void upData1301Command() {
        log.info("河南油补——定时下发1301指令任务开始");
        List<Line1301CommandDTO> list = lineManageDao.getBindLine();
        if (list.isEmpty()) {
            log.info("没有数据进行下发");
            return;
        }
        List<String> stationIds = Lists.newLinkedList();
        for (Line1301CommandDTO dto : list) {
            stationIds.add(dto.getLastStationId());
            stationIds.add(dto.getFirstStationId());
        }
        List<StationDO> stationList = stationManageDao.getByIds(stationIds);
        Map<String, String> map = new HashMap<>(stationList.size());
        for (StationDO stationDO : stationList) {
            map.put(stationDO.getId(), stationDO.getName());
        }
        for (Line1301CommandDTO dto : list) {
            dto.setFirstStationName(map.get(dto.getFirstStationId()));
            dto.setLastStationName(map.get(dto.getLastStationId()));
            ofParameter(dto.toString(), dto.getOrgCode(), OilSubsidyCommand.UP_BASE_MSG_LINE_INFO_REQ, dto.getIp(),
                dto.getCenterId(), dto.getForwardingPlatformId());
        }
        log.info("河南油补——定时下发1301指令任务结束");
    }

    @Override
    public void upData1302Command() {
        log.info("河南油补——定时下发1302指令任务开始");
        List<Line1302CommandDTO> list = lineManageDao.getBindLineStation();
        if (list.isEmpty()) {
            log.info("没有线路数据进行下发");
            return;
        }
        for (Line1302CommandDTO dto : list) {
            ofParameter(dto.toString(), dto.getOrgCode(), OilSubsidyCommand.UP_BASE_MSG_GIS_INFO_REQ, dto.getIp(),
                dto.getCenterId(), dto.getForwardingPlatformId());
        }
        log.info("河南油补——定时下发1302指令任务结束");
    }

    /**
     * 组装参数
     * @param content
     * @param orgCode
     * @param upBaseMsgGisInfoReq
     * @param ip
     * @param centerId
     * @param forwardingPlatformId
     */
    private void ofParameter(String content, String orgCode, int upBaseMsgGisInfoReq, String ip, Integer centerId,
        String forwardingPlatformId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("content", content);
        OilSubsidyUpData oilSubsidyUpData =
            new OilSubsidyUpData(orgCode, upBaseMsgGisInfoReq, jsonObject, content.length());
        /* 组装809消息下发message */
        Message message = MsgUtil.getMsg(OilSubsidyCommand.UP_BASE_MSG,
            MsgUtil.getT809Message(OilSubsidyCommand.UP_BASE_MSG, ip, centerId, oilSubsidyUpData))
            .assembleDesc809(forwardingPlatformId);
        /* 信息下发 */
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
    }

    private void upLineInfo(boolean sendLineFlag) {
        try {
            if (sendLineFlag) {
                upData1301Command();
                upData1302Command();
            }
        } catch (Exception e) {
            log.error("下发线路相关信息异常了", e);
        }

    }
}
