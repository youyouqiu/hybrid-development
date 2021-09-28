package com.cb.platform.contorller;

import com.cb.platform.domain.TransportTimesEntity;
import com.cb.platform.domain.TransportTimesQuery;
import com.cb.platform.service.TransportTimesService;
import com.cb.platform.util.StringToList;
import com.github.pagehelper.Page;
import com.zw.platform.basic.domain.ProfessionalDO;
import com.zw.platform.commons.Auth;
import com.zw.platform.service.basicinfo.ProfessionalsService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 趟次运输
 */
@Controller
@RequestMapping("/m/monitoring/vehicle/transport")
public class TransportTimesController {

    private static final Logger log = LogManager.getLogger(TransportTimesController.class);

    @Autowired
    private TransportTimesService transportTimesService;

    @Autowired
    private ProfessionalsService professionalsService;

    private static final String LIST_PAGE = "/modules/basicinfo/monitoring/vehicle/dangerious/list";

    private static final String EDIT = "/modules/basicinfo/monitoring/vehicle/dangerious/tripEdit";

    private static final String ADD_PAGE = "/modules/basicinfo/monitoring/vehicle/dangerious/tripAdd";

    private static final String importHtml = "/modules/basicinfo/monitoring/vehicle/dangerious/tripImport";
    private static final String ERROR_PAGE = "html/errors/error_exception";

    /**
     * 修改页面
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT);
            getVehicleDetail(id, mav);
            return mav;
        } catch (Exception e) {
            log.error("修改趟次运输弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 添加页面
     */
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String add() {
        return ADD_PAGE;
    }

    /**
     * 导入页面
     */
    @RequestMapping(value = "/importHtml", method = RequestMethod.GET)
    public String importHtml() {
        return importHtml;
    }

    /**
     * 修改和详情公共方法
     * @param id  车辆id
     * @param mav mav
     * @throws Exception this
     */
    private void getVehicleDetail(@PathVariable String id, ModelAndView mav) throws Exception {
        TransportTimesEntity transportTimesEntity = transportTimesService.findById(id);
        mav.addObject("result", transportTimesEntity);
    }

    /**
     * 获取页面
     */
    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list() {
        return LIST_PAGE;
    }

    /**
     * 添加
     */
    @RequestMapping(value = "/addTransport", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addTransport(
            @Validated(ValidGroupAdd.class) final TransportTimesEntity transportTimesEntity) {
        Map<String, Object> map = check(transportTimesEntity);
        if (!(boolean) map.get("flag")) {
            return new JsonResultBean(JsonResultBean.SUCCESS, map.get("msg").toString());
        }
        try {
            if (!transportTimesService.addTransport(transportTimesEntity)) {
                return new JsonResultBean(JsonResultBean.FAULT, "添加失败");
            }
        } catch (Exception e) {
            log.error("添加危险货物运输趟次失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "添加失败");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS, "添加成功");
    }

    /**
     * 根据车辆ID校验车辆是否存在趟次记录
     */
    @RequestMapping(value = "/checkType", method = RequestMethod.POST)
    @ResponseBody
    public boolean checkType(String idList) {
        if (StringUtils.isBlank(idList)) {
            return false;
        }
        List<String> query = StringToList.stringToList(idList);
        List<TransportTimesEntity> list = transportTimesService.findByVids(query);
        return list.size() > 0;
    }

    /**
     * 根据ID删除趟次记录
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteById(@PathVariable final String id, HttpServletRequest request) {
        if (StringUtils.isBlank(id)) {
            return new JsonResultBean(JsonResultBean.FAULT, "趟次ID不能为空");
        }
        try {
            TransportTimesEntity entity = transportTimesService.findById(id);
            if (entity == null) {
                return new JsonResultBean(JsonResultBean.FAULT, "趟次记录不存在");
            }
            String ipAddress = new GetIpAddr().getIpAddr(request);
            transportTimesService.deleteTransportById(entity, ipAddress);
        } catch (Exception e) {
            log.error("删除危险货物运输趟次失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "添加失败");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS, "删除成功");
    }

    /**
     * 修改
     */
    @RequestMapping(value = "/updateTransport", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateTransport(
        @Validated(ValidGroupAdd.class) final TransportTimesEntity transportTimesEntity, HttpServletRequest request) {
        if (StringUtils.isBlank(transportTimesEntity.getId())) {
            return new JsonResultBean(JsonResultBean.FAULT, "id不能为空");
        }
        Map<String, Object> map = check(transportTimesEntity);
        if (!(boolean) map.get("flag")) {
            return new JsonResultBean(JsonResultBean.SUCCESS, map.get("msg").toString());
        }
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            transportTimesService.updateTransport(transportTimesEntity, ipAddress);
        } catch (Exception e) {
            log.error("修改危险货物运输趟次失败");
            return new JsonResultBean(JsonResultBean.FAULT, "修改失败");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS, "修改成功");
    }

    /**
     * 校验数据准确性
     */
    public Map<String, Object> check(TransportTimesEntity transportTimesEntity) {

        Map<String, Object> map = new HashMap<>(2);
        map.put("flag", true);
        if (StringUtils.isBlank(transportTimesEntity.getVehicleId())) {
            map.put("flag", false);
            map.put("msg", "车辆id不能为空");
            return map;
        }

        if (StringUtils.isBlank(transportTimesEntity.getItemNameId())) {
            map.put("flag", false);
            map.put("msg", "品名ID不能为空");
            return map;
        }
        if (transportTimesEntity.getCount() != null && transportTimesEntity.getCount().toString().length() > 9) {
            map.put("flag", false);
            map.put("msg", "数量不能超过10位");
            return map;
        }
        if (transportTimesEntity.getTransportType() != null && (transportTimesEntity.getTransportType() < 1
            || transportTimesEntity.getTransportType() > 2)) {
            map.put("flag", false);
            map.put("msg", "请选择正确的运输类型");
            return map;
        }
        if (StringUtils.isNotBlank(transportTimesEntity.getAimSite())
            && transportTimesEntity.getAimSite().length() > 20) {
            map.put("flag", false);
            map.put("msg", "目的地点不能大于20字符");
            return map;
        }
        if (StringUtils.isNotBlank(transportTimesEntity.getStartSite())
            && transportTimesEntity.getStartSite().length() > 20) {
            map.put("flag", false);
            map.put("msg", "起始地点不能大于20字符");
            return map;
        }
        if (StringUtils.isNotBlank(transportTimesEntity.getViaSite())
            && transportTimesEntity.getViaSite().length() > 20) {
            map.put("flag", false);
            map.put("msg", "途径地点不能大于20字符");
            return map;
        }
        if (StringUtils.isNotBlank(transportTimesEntity.getRemark())
            && transportTimesEntity.getRemark().length() > 50) {
            map.put("flag", false);
            map.put("msg", "备注不能大于20字符");
            return map;
        }
        //校验押送员
        if (StringUtils.isNotBlank(transportTimesEntity.getProfessinoalId())) {
            //查询所有的从业人员id
            List<String> sortGroupProfessional = professionalsService.getGroupProfessional();
            //查询所属权限内的从业人员
            List<ProfessionalDO> infoList = new ArrayList<>();
            if (sortGroupProfessional != null && sortGroupProfessional.size() > 0) {
                infoList = professionalsService.selectLicense(sortGroupProfessional);
            }
            for (ProfessionalDO info : infoList) {
                if (transportTimesEntity.getProfessinoalId().equals(info.getId())) {
                    return map;
                }
            }
            map.put("flag", false);
            map.put("msg", "押送员不存在");
            return map;
        }
        return map;

    }

    /**
     * 查询权限内的押运员
     */
    @RequestMapping(value = "/findProfessionalsInfoList", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean findProfessionalsInfoList() {
        //查询所有的从业人员id
        List<String> sortGroupProfessional = professionalsService.getGroupProfessional();
        //查询所属权限内的从业人员
        List<ProfessionalDO> list = new ArrayList<>();
        if (sortGroupProfessional != null && sortGroupProfessional.size() > 0) {
            list = professionalsService.selectLicense(sortGroupProfessional);
        }
        return new JsonResultBean(list);
    }

    /**
     * 根据ID查询详情
     */
    @RequestMapping(value = "/findById", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean findById(@RequestParam("id") String id) {
        if (StringUtils.isBlank(id)) {
            return new JsonResultBean(JsonResultBean.FAULT, "id不能为空");
        }
        TransportTimesEntity transportTimesEntity;
        try {
            transportTimesEntity = transportTimesService.findById(id);
        } catch (Exception e) {
            log.error("添加危险货物运输趟次失败");
            return new JsonResultBean(JsonResultBean.FAULT, "添加失败");
        }
        return new JsonResultBean(transportTimesEntity);
    }

    /**
     * 危险货物运输趟次查询
     */
    @RequestMapping(value = "/searchTransport", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean searchTransport(TransportTimesQuery query) {
        Page<TransportTimesEntity> list;
        try {
            list = transportTimesService.searchTransport(query);
        } catch (Exception e) {
            log.error("查询危险货物运输趟次失败", e);
            return new PageGridBean(JsonResultBean.FAULT);
        }
        return new PageGridBean(query, list, true);
    }

    /**
     * 批量删除
     */
    @RequestMapping(value = "/deleteTransport", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteTransport(String idList, HttpServletRequest request) {
        if (StringUtils.isBlank(idList)) {
            return new JsonResultBean(JsonResultBean.FAULT, "未选择危险货物运输趟次");
        }
        List<String> list = StringToList.stringToList(idList);
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            transportTimesService.deleteTransport(list, ipAddress);
        } catch (Exception e) {
            log.error("删除危险货物运输趟次失败");
            return new JsonResultBean(JsonResultBean.FAULT, "删除失败");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS, "删除成功");
    }

    /**
     * 导出
     */
    @RequestMapping(value = "/exportEnterpriseList", method = RequestMethod.GET)
    public void exportEnterpriseList(HttpServletRequest request, HttpServletResponse res, String vehicleNumber) {
        try {
            ExportExcelUtil.setResponseHead(res, "危险货物运输趟次");
            transportTimesService.export(null, 1, res, vehicleNumber);
        } catch (Exception e) {
            log.error("危险货物运输趟次导出数据异常(get)", e);
        }
    }

    /**
     * 模板
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse res) {
        try {
            ExportExcelUtil.setResponseHead(res, "危险货物运输趟次模板");
            transportTimesService.download(res);
        } catch (Exception e) {
            log.error("危险货物运输趟次导出数据异常(get)", e);
        }
    }

    /**
     * 导入
     * @param file    文件
     * @return result
     */
    @RequestMapping(value = "/importTransport", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importTransport(@RequestParam(value = "file") MultipartFile file,
        HttpServletRequest request) {
        try {
            Map<String, Object> resultMap = transportTimesService.importTransport(file);
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("导入危险货物运输趟次信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

}
