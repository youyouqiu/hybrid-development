package com.zw.platform.controller.connectionparamsset_809;

import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.connectionparamsset_809.T809ForwardConfig;
import com.zw.platform.domain.connectionparamsset_809.T809ForwardConfigQuery;
import com.zw.platform.service.connectionparamsset_809.ConnectionParamsConfigService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.ZipUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * 809监控对象转发管理
 * @author hujun
 * @Date 创建时间：2018年3月2日 下午3:53:49
 */
@Controller
@RequestMapping("/m/connectionparamsConfig")
public class ConnectionParamsConfigController {
    private static Logger log = LogManager.getLogger(ConnectionParamsConfigController.class);
    
    private static final String LIST_PAGE = "modules/connectionparamsForward/list";
    
    private static final String ERROR_PAGE = "html/errors/error_exception";
    
    @Autowired
    ConnectionParamsConfigService connectionParamsConfigService;
    
    @Value("${sys.error.msg}")
    private String sysErrorMsg;
    
    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage(final HttpServletRequest request, ModelMap model) {
        return LIST_PAGE;
    }
    
    /**
     * 查询809转发绑定关系数据(分页)
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(T809ForwardConfigQuery query) {
        try {
            Page<T809ForwardConfig> formList = connectionParamsConfigService.findConfig(query);
            return new PageGridBean(query, formList, true);
        } catch (Exception e) {
            return new PageGridBean(PageGridBean.FAULT);
        }
    }
    
    /**
     * 新增809转发绑定关系数据
     * @author hujun
     * @Date 创建时间：2018年3月5日 上午9:54:28
     * @return
     */
    @RequestMapping(value = {"/add"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add809ForwardConfig(HttpServletRequest request, String platFormId, String vehicleIds,
        String platFormName, String protocolType) {
        try {
            // 获得访问ip
            String ipAddress = new GetIpAddr().getIpAddr(request);
            boolean flag =
                connectionParamsConfigService.addConfig(platFormId, vehicleIds, ipAddress, platFormName, protocolType);
            if (flag) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        } catch (Exception e) {
            log.error("新增809转发绑定关系数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
    
    /**
     * 删除809转发绑定关系数据
     * @author hujun
     * @Date 创建时间：2018年3月5日 上午9:55:27
     * @param id
     * @return
     */
    @RequestMapping(value = {"/delete"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete809ForwardConfig(HttpServletRequest request, String id) {
        try {
            if (StringUtils.isNotBlank(id)) {
                // 获得访问ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                List<String> configIds = Arrays.asList(id.split(","));
                boolean flag = connectionParamsConfigService.deleteConfig(configIds, ipAddress);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        } catch (Exception e) {
            log.error("删除809转发绑定关系数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取809监控对象转发树
     * @return
     */
    @RequestMapping(value = "/t809ForwardTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getT809ForwardTree() {
        try {
            String result = connectionParamsConfigService.getT809ForwardTree().toJSONString();
            // 压缩数据
            result = ZipUtil.compress(result);
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("获取809监控对象转发树异常", e);
            return null;
        }
    }

}
