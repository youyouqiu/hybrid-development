package com.zw.platform.controller.basicinfo;

import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.query.DriverDiscernManageQuery;
import com.zw.platform.dto.driverMiscern.DeviceDriverDto;
import com.zw.platform.dto.driverMiscern.DriverDiscernManageDto;
import com.zw.platform.service.driverDiscernManage.DriverDiscernManageService;
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

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 终端驾驶员识别管理
 * @author penghj
 * @version 1.0
 * @date 2020/9/24 14:10
 */
@Controller
@RequestMapping("/m/driver/discern/manage")
public class DriverDiscernManageController {
    private static final Logger logger = LogManager.getLogger(DriverDiscernManageController.class);

    private static final String LIST_PAGE = "modules/basicinfo/monitoring/driverDiscernManage/list";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private DriverDiscernManageService manageService;

    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 分页
     */
    @RequestMapping(value = "/pageQuery", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean pageQuery(final DriverDiscernManageQuery query) {
        try {
            Page<DriverDiscernManageDto> result = manageService.list(query);
            return new PageGridBean(result, true);
        } catch (Exception e) {
            logger.error("分页查询终端驾驶员识别管理异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 详情
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean detail(@NotNull String id) {
        try {
            List<DeviceDriverDto> detail = manageService.listDriverDetail(id);
            return new JsonResultBean(detail);
        } catch (Exception e) {
            logger.error("终端驾驶员列表详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
