package com.zw.platform.controller.carbonmgt;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.vas.carbonmgt.FuelType;
import com.zw.platform.domain.vas.carbonmgt.TimingStored;
import com.zw.platform.domain.vas.carbonmgt.form.FuelTypeForm;
import com.zw.platform.domain.vas.carbonmgt.query.FuelTypeQuery;
import com.zw.platform.service.carbonmgt.BasicManagementService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.ValidGroupAdd;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * Created by 王健宇 on 2017/2/14. 碳排放基础管理
 */
@Controller
@RequestMapping("/v/carbonmgt/basicManagement")
public class BasicManagementController {
    private static final String LIST_PAGE = "vas/carbonmgt/basicManagement/fuelPrice";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private static Logger log = LogManager.getLogger(BasicManagementController.class);

    @Autowired
    private BasicManagementService basicManagementService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${fuel.type.exist}")
    private String fuelTypeExist;

    @Auth
    @RequestMapping(value = {"/fuelPrice"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = {"/oilPricesQuery"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTodayFirstData(String timeStart, String timeEnd, String district, String oiltype) {
        try {
            if (timeStart != null && timeEnd != null && district != null && oiltype != null) {
                List<TimingStored> oilPricesQuery =
                        basicManagementService.oilPricesQuery(timeStart, timeEnd, district, oiltype);
                JSONObject msg = new JSONObject();
                msg.put("oilPricesQuery", oilPricesQuery);
                return new JsonResultBean(msg);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("查询燃料价格异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询燃料类型分页
     *
     * @param query
     * @return
     */
    @RequestMapping(value = "/roadFuelType", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(final FuelTypeQuery query) {
        try {
            Page<FuelTypeQuery> result = basicManagementService.findFuelTypeByPage(query);
            return new PageGridBean(result, true);
        } catch (Exception e) {
            log.error("分页查询燃料类型(findFuelTypeByPage)异常");
            return new PageGridBean(false);
        }
    }

    /**
     * 增加燃料类型
     *
     * @return
     */
    @RequestMapping(value = "/addFuelType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addFuelType(@Validated({ValidGroupAdd.class})
                                      @ModelAttribute("fuelType") final FuelTypeForm fuelType) {
        try {
            if (fuelType != null) {
                // 根据燃料类型查询查询数据库是否有相同的燃料类型
                List<FuelType> list = basicManagementService.findFuelType(fuelType.getFuelType());
                if (list == null || list.size() != 0) { // 存在相同的燃料类型
                    return new JsonResultBean(JsonResultBean.FAULT, fuelTypeExist);
                } else {
                    String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
                    boolean flag = basicManagementService.addFuelType(fuelType, ipAddress);
                    if (flag) {
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    }
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增燃料类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据燃料类型查询燃料类型实体
     *
     * @param fuelType
     * @return
     */
    @RequestMapping(value = "/findFuelType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findFuelType(String fuelType) {
        try {
            if (fuelType != null) {
                JSONObject msg = new JSONObject();
                List<FuelType> fuelTypes = basicManagementService.findFuelType(fuelType);
                msg.put("fuelTypeList", fuelTypes);
                return new JsonResultBean(msg);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("查询燃料类型实体异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据id查询燃料类型
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/findFuelTypeById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findFuelTypeById(String id) {
        try {
            if (id != null) {
                JSONObject msg = new JSONObject();
                FuelType fuelType = basicManagementService.get(id);
                msg.put("fuelType", fuelType);
                return new JsonResultBean(msg);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("根据id查询燃料类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 修改燃料类型
     *
     * @param form
     * @return
     */
    @RequestMapping(value = "/updateFuelType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateFuelType(FuelTypeForm form) {
        try {
            if (form != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return basicManagementService.updateFuelType(form, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改燃料类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据id删除燃料类型
     *
     * @param id
     * @return
     * @author tangshunyu
     */
    @RequestMapping(value = "/deleteFuelType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteFuelType(String id) {
        try {
            if (id != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
                boolean flag = basicManagementService.deleteFuelType(id, ipAddress);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除燃料类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 批量删除燃料类型
     *
     * @param ids
     * @return
     */
    @RequestMapping(value = "/deleteFuelTypeMuch", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteFuelTypeMuch(String ids) {
        try {
            if (ids != null) {
                String[] items = ids.split(",");
                List<String> list = Arrays.asList(items);
                String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
                boolean flag = basicManagementService.deleteFuelTypeMuch(list, ipAddress);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除燃料类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
