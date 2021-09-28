package com.zw.platform.controller.systems;

import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.service.systems.DataCleanService;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author denghuabing
 * @version V1.0
 * @description: 数据清理
 * @date 2020/10/27
 **/
@Controller
@RequestMapping("/m/dataClean")
public class DataCleanController {

    private Logger log = LogManager.getLogger(DataCleanController.class);

    @Autowired
    private DataCleanService dataCleanService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    private static final String LIST_PAGE = "modules/systems/dataClean/list";

    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getListData() {
        try {
            return dataCleanService.getListData();
        } catch (Exception e) {
            log.error("数据清理查询异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/setting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveSetting(Integer type, Integer value) {
        try {
            if (!SystemHelper.isAdmin()) {
                return new JsonResultBean(JsonResultBean.FAULT, "只有admin才有权限");
            }
            return new JsonResultBean(dataCleanService.saveSetting(type, value));
        } catch (Exception e) {
            log.error("设置清除规则异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/default", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveDefault(Integer type) {
        try {
            if (!SystemHelper.isAdmin()) {
                return new JsonResultBean(JsonResultBean.FAULT, "只有admin才有权限");
            }
            return new JsonResultBean(dataCleanService.saveDefault(type));
        } catch (Exception e) {
            log.error("数据清理恢复默认异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/saveTime", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveTime(String time, String cleanType) {
        try {
            return new JsonResultBean(dataCleanService.saveTime(time, cleanType));
        } catch (Exception e) {
            log.error("提交数据清理时间异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
