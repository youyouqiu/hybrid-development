package com.zw.platform.controller.basicinfo;

import com.zw.platform.service.basicinfo.DictionaryService;
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
 * @description: 字典表相关数据查询接口
 * @date 2020/9/28
 **/
@Controller
@RequestMapping("/m/dictionary")
public class DictionaryController {

    private Logger logger = LogManager.getLogger(DictionaryController.class);

    @Autowired
    private DictionaryService dictionaryService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    /**
     * 经营范围
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "businessScope", method = RequestMethod.POST)
    public JsonResultBean getBusinessScope() {
        try {
            return new JsonResultBean(dictionaryService.getBusinessScope());
        } catch (Exception e) {
            logger.error("获取字典数据经营范围异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 经营许可证字别
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "businessLicenseType", method = RequestMethod.POST)
    public JsonResultBean getBusinessLicenseType() {
        try {
            return new JsonResultBean(dictionaryService.getBusinessLicenseType());
        } catch (Exception e) {
            logger.error("获取字典数据经营许可证字别异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
