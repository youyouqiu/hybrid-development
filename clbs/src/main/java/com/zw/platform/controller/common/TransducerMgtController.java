package com.zw.platform.controller.common;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zw.platform.domain.vas.f3.TransduserManage;
import com.zw.platform.domain.vas.f3.TransduserManageQuery;
import com.zw.platform.service.transdu.TransduserService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 正反转传感器管理/温度传感器管理/湿度传感器管理
 * @author Administrator
 */
@Controller
@RequestMapping("/v/TransduserMgt")
public class TransducerMgtController {

    @Autowired
    private TransduserService transduserService;

    @Autowired
    private HttpServletRequest request;

    private static Logger logger = LogManager.getLogger(TransducerMgtController.class);

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${sensor.type.error}")
    private String sensorTypeError;

    @Value("${sensor.type.exist}")
    private String sensorTypeExist;

    /**
     * 根据传感器类型查询传感器信息
     * @param query
     * @return
     */
    @RequestMapping(value = {"/list_{type}"}, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean transduserManage(final TransduserManageQuery query) {
        try {
            int type = query.getType();
            String param = query.getSimpleQueryParam();// 查询参数(用于模糊查询，可为空，为空时查全部)
            param = StringUtil.mysqlLikeWildcardTranslation(param);
            PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue());
            Page<TransduserManage> result = transduserService.getTransduserManage(type, param);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            logger.error("分页查询（getTransduserManage）异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 新增传感器
     * @return transduserManage
     */
    @RequestMapping(value = {"/add"}, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean add(TransduserManage transduserManage) {
        try {
            if (transduserManage != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return transduserService.addTransduserManage(transduserManage, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("新增传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 修改传感器
     * @param transduserManage
     * @return JsonResultBean
     */
    @RequestMapping(value = {"/edit"}, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(TransduserManage transduserManage) {
        try {
            if (transduserManage != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return transduserService.updateTransduserManage(transduserManage, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("修改传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 删除传感器
     * @param id
     * @return JsonResultBean
     */
    @RequestMapping(value = {"/delete_{id}"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean del(@PathVariable("id") String id) {
        try {
            if (id != null && !id.isEmpty()) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return transduserService.deleteTransduserManage(id, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("删除传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 批量删除传感器
     * @return JsonResultBean
     */
    @RequestMapping(value = {"/deleteMore"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delMore() {
        try {
            String items = request.getParameter("deltems");
            if (items != null && !items.isEmpty()) {
                String[] item = items.split(",");
                List<String> ids = Arrays.asList(item);
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return transduserService.updateBatchTransduserManages(ids, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("批量删除传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出传感器管理 @param sensorType @return @throws
     */
    @RequestMapping(value = {"/export"}, method = RequestMethod.GET)
    @ResponseBody
    public void exportMile(HttpServletResponse response, Integer sensorType) {
        try {
            transduserService.export(null, 1, response, sensorType);
        } catch (Exception e) {
            logger.error("导出传感器管理异常", e);
        }
    }

    /**
     * 下载模板
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response, Integer sensorType) {
        try {
            transduserService.generateTemplate(response, sensorType);
        } catch (Exception e) {
            logger.error("下载模板异常", e);
        }
    }

    /**
     * 导入
     * @param file
     * @param sensorType
     * @return JsonResultBean
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importFluxSensor(@RequestParam(value = "file", required = false) MultipartFile file,
                                                 Integer sensorType) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map resultMap = transduserService.importSensor(file, sensorType, ipAddress);
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            JsonResultBean result = new JsonResultBean(true, msg);
            return result;
        } catch (Exception e) {
            logger.error("导入传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
