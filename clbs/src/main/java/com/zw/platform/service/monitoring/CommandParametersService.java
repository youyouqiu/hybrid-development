package com.zw.platform.service.monitoring;

import com.github.pagehelper.Page;
import com.zw.platform.domain.param.form.CommandParametersForm;
import com.zw.platform.domain.vas.monitoring.MonitorCommandBindForm;
import com.zw.platform.domain.vas.monitoring.query.CommandParametersQuery;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

public interface CommandParametersService {

    Page<CommandParametersForm> getList(CommandParametersQuery query) throws Exception;

    void findInfo(ModelAndView mav, String vid, String commandType) throws Exception;

    List<MonitorCommandBindForm> findReferVehicle(String commandType, String vid, Integer deviceType);

    JsonResultBean delete(String id, String vid, String commandType) throws Exception;

    JsonResultBean sendParam(String vid, String commandType, String videoTactics);

    JsonResultBean getReferenceInfo(String vid, String commandType) throws Exception;

    String getCommandName(String commandType);

    /**
     * 指令参数-下发参数
     * @param monitorIds
     * @param commandType
     * @return
     * @throws Exception
     */
    JsonResultBean sendParamByCommandType(String monitorIds, Integer commandType, String upgradeType);

    /**
     * 指令参数-保存参数
     * @param file
     * @param monitorIds
     * @param paramJsonStr
     * @param commandType
     * @return
     * @throws Exception
     */
    JsonResultBean saveParamByCommandType(MultipartFile file, String monitorIds, String paramJsonStr,
        Integer commandType) throws Exception;

    Integer sendOneMediaSearchUpMsg(String monitorId, Integer mediaId, Integer deleteSign);

    /**
     * 删除终端围栏
     * @param monitorIds monitorIds
     * @param commandType commandType
     * @return JsonResultBean
     */
    JsonResultBean sendDeleteDeviceFence(String monitorIds, Integer commandType, Integer deviceFence);
}