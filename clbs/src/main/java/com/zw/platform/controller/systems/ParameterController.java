package com.zw.platform.controller.systems;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.systems.Parameter;
import com.zw.platform.domain.systems.query.ParameterQuery;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.functionconfig.FenceConfigService;
import com.zw.platform.service.systems.ParameterService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 参数下发Controller
 * Modification by Wjy on 2016/9/2.
 */
@Controller
@RequestMapping("/m/systems/parameter")
public class ParameterController {
    private static Logger log = LogManager.getLogger(ParameterController.class);

    private static final String LIST_PAGE = "modules/systems/parameter/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private UserService userService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    FenceConfigService fenceConfigService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public ModelAndView getListPage() {
        try {
            ModelAndView mav = new ModelAndView(LIST_PAGE);
            // 根据用户名获取用户id
            String userId = SystemHelper.getCurrentUser().getId().toString();
            List<VehicleInfo> vehicleList =
                    vehicleService.findVehicleByUserAndGroup(userId, userService.getOrgByUser(), true);
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            return mav;
        } catch (Exception e) {
            log.error("参数下发界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 分页查询用户
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public PageGridBean list(final ParameterQuery query) {
        try {
            Page<Parameter> result = parameterService.findByPage(query);
            return new PageGridBean(result, true);
        } catch (Exception e) {
            log.error("分页查询用户（findByPage）异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 根据id重新下发
     */
    @RequestMapping(value = "/reload", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean reload(String ids) {
        try {
            String[] item = ids.split(",");
            List<String> idList = Arrays.asList(item);
            List<Directive> directiveList = parameterService.findById(idList);
            if (directiveList != null && directiveList.size() > 0) {
                List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
                for (Directive directive : directiveList) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("paramId", directive.getId());
                    map.put("bindId", directive.getParameterName());
                    map.put("vehicleId", directive.getMonitorObjectId());
                    map.put("sendType", directive.getDirectiveName());
                    mapList.add(map);
                }
                // 电子围栏绑定下发设备
                fenceConfigService.sendFenceByType(mapList);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("重新下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
