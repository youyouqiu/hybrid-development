package com.zw.adas.service.riskdisposerecord.impl;

import com.github.pagehelper.Page;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.zw.adas.domain.report.inspectuser.InspectUserDTO;
import com.zw.adas.domain.report.inspectuser.InspectUserQuery;
import com.zw.adas.service.riskdisposerecord.InspectUserService;
import com.zw.adas.utils.FastDFSClient;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.connectionparamsset_809.PlantParam;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.netty.ServerParamList;
import com.zw.platform.domain.reportManagement.Zw809MessageDO;
import com.zw.platform.repository.modules.ConnectionParamsSetDao;
import com.zw.platform.repository.modules.Zw809MessageDao;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t809.T809Message;
import com.zw.protocol.msg.t809.body.ExchangeInfo;
import com.zw.protocol.msg.t809.body.module.InspectUserAck;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author wanxing
 * @Title: 巡检用户
 * @date 2020/12/3017:39
 */
@Service
@Slf4j
public class InspectUserServiceImpl implements InspectUserService, IpAddressService {

    @Autowired
    private Zw809MessageDao zw809MessageDao;

    @Autowired
    private ConnectionParamsSetDao connectionParamsSetDao;

    @Autowired
    private FastDFSClient fastDfsClient;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Autowired
    private ServerParamList serverParamList;


    @Autowired
    private UserService userService;

    @Override
    public PageGridBean getListByKeyWord(InspectUserQuery query) {
        OrganizationLdap currentUserOrg = userService.getCurrentUserOrg();
        query.setOrgIds(Collections.singletonList(currentUserOrg.getUuid()));
        Page<InspectUserDTO> page = PageHelperUtil.doSelect(query, () -> zw809MessageDao.getListByKeyWord(query));
        return new PageGridBean(page, PageGridBean.SUCCESS);
    }

    @Override
    public void export(HttpServletResponse res, InspectUserQuery query) throws IOException {
        query.setOrgIds(userService.getCurrentUserOrgIds());
        List<InspectUserDTO> result = zw809MessageDao.getListByKeyWord(query);
        ExportExcelUtil.export(new ExportExcelParam(null, 1, result,
            InspectUserDTO.class, null, res.getOutputStream()));
    }

    @Override
    public InspectUserDTO getById(String id) {
        Zw809MessageDO data = zw809MessageDao.getMsgById(id);
        if (data == null) {
            return new InspectUserDTO();
        }
        return InspectUserDTO.copy2DTO(data);
    }

    @Override
    public void updateAnswerById(String id) {
        zw809MessageDao.updatePastData(Collections.singletonList(id));
    }

    @Override
    public void update(InspectUserDTO inspectUser) {
        Zw809MessageDO zw809MessageDO = inspectUser.copy2DO();
        zw809MessageDao.update(zw809MessageDO);
    }

    @Override
    public void add(InspectUserDTO inspectUserDTO) {
        Zw809MessageDO zw809MessageDO = inspectUserDTO.copy2DO();
        zw809MessageDao.insert(zw809MessageDO);
    }

    @Override
    public void updateAndAnswer(Integer type, InspectUserDTO inspectUserDTO, MultipartFile image)
        throws IOException {
        //上传图片
        String fullPath = fastDfsClient.uploadFile(image);
        String imageUrl = fullPath.split(fdfsWebServer.getWebServerUrl())[1];
        inspectUserDTO.setMediaUrl(imageUrl);
        inspectUserDTO = generateUserInfo(inspectUserDTO);
        //操作zw_m_808_message表
        if (type == 2 && inspectUserDTO.getAnswerStatus() == 1) {
            // 弹窗未过期可以重复应答，需要进行复制
            inspectUserDTO.setId(UUID.randomUUID().toString());
            add(inspectUserDTO);
        } else {
            //更新数据库
            update(inspectUserDTO);
        }
        //下发给F3
        send2F3(inspectUserDTO, image);
    }

    private void send2F3(InspectUserDTO inspectUserDTO, MultipartFile file) throws IOException {
        InspectUserAck inspectUserAck = new InspectUserAck();
        inspectUserAck.setObjectId(inspectUserDTO.getObjectId());
        inspectUserAck.setObjectType(inspectUserDTO.getObjectType());
        inspectUserAck.setSourceDataType(ConstantUtil.DOWN_PLATFORM_MSG_INSPECTION_USER_REQ);
        inspectUserAck.setSourceMsgSn(inspectUserDTO.getSourceMsgSn());
        inspectUserAck.setResponderIpAddress(getIpAddress());
        inspectUserAck.setResponder(inspectUserDTO.getAnswerUser());
        inspectUserAck.setResponderTel(inspectUserDTO.getAnswerUserTel());
        inspectUserAck.setSocialSecurityNumber(inspectUserDTO.getSocialSecurityNumber());
        inspectUserAck.setIdCardNumber(inspectUserDTO.getAnswerUserIdentityNumber());
        inspectUserAck.setResponderPhoto(file.getBytes());
        String platformId = inspectUserDTO.getPlatformId();
        PlantParam plantParam = connectionParamsSetDao.getConnectionInfoById(platformId);
        if (plantParam == null) {
            log.error("809转发平台已经删除，转发平台id:{}", platformId);
            return;
        }
        ExchangeInfo exchangeInfo = new ExchangeInfo();
        exchangeInfo.setDataType(ConstantUtil.UP_PLATFORM_MSG_INSPECTION_USER_ACK);
        exchangeInfo.setData(MsgUtil.objToJson(inspectUserAck));
        T809Message msg = MsgUtil.getT809Message(ConstantUtil.T809_UP_PLATFORM_MSG, plantParam.getIp(),
            plantParam.getCenterId(), exchangeInfo);
        Message upMsg = MsgUtil.getMsg(ConstantUtil.T809_UP_PLATFORM_MSG, msg).assembleDesc809(platformId);
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(upMsg);
    }

    /**
     * 组装用户信息
     * @param inspectUserDTO
     * @return
     */
    @Override
    public InspectUserDTO generateUserInfo(InspectUserDTO inspectUserDTO) {
        UserDTO currentUserInfo = userService.getCurrentUserInfo();
        inspectUserDTO.setAnswerUser(currentUserInfo.getUsername());
        inspectUserDTO.setAnswerUserTel(currentUserInfo.getMobile());
        inspectUserDTO.setAnswerUserIdentityNumber(currentUserInfo.getIdentityNumber());
        inspectUserDTO.setSocialSecurityNumber(currentUserInfo.getSocialSecurityNumber());
        return inspectUserDTO;
    }

}
