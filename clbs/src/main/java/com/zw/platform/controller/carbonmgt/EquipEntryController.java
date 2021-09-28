package com.zw.platform.controller.carbonmgt;

import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.vas.carbonmgt.form.EquipForm;
import com.zw.platform.domain.vas.carbonmgt.query.EquipQuery;
import com.zw.platform.service.carbonmgt.EquipEntryService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 设备录入
 * ZhongWeiTeam </p>
 * @version 1.0
 */
@Controller
@RequestMapping("/v/carbonmgt/equipEntry")
public class EquipEntryController {
    @Autowired
    private EquipEntryService equipEntryService;

    @Autowired
    private UserService userService;

    private static final String LIST_PAGE = "vas/carbonmgt/equipEntry/list";

    private static final String ADD_PAGE = "vas/carbonmgt/equipEntry/add";

    private static final String EDIT_PAGE = "vas/carbonmgt/equipEntry/edit";

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage()
        throws BusinessException {
        return LIST_PAGE;
    }

    /**
     * 分页查询
     */
    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final EquipQuery query) {
        if (query == null) {
            return new PageGridBean(PageGridBean.FAULT);
        }
        Page<EquipForm> result =
                (Page<EquipForm>) equipEntryService.findBenchmark(userService.getOrgByUser(), query, true);
        if (CollectionUtils.isNotEmpty(result)) {
            Pattern splitter = Pattern.compile("#");
            for (EquipForm form : result) {
                if (StringUtil.isNotEmpty(form.getGroupId())) {
                    StringBuilder groupName = new StringBuilder();
                    String[] groupIds = splitter.split(form.getGroupId());
                    for (String groupId : groupIds) {
                        OrganizationLdap organization = userService.findOrganization(groupId);
                        if (organization != null) {
                            groupName.append(organization.getName()).append(",");
                        }
                    }
                    groupName = new StringBuilder(
                        groupName.toString().endsWith(",") ? groupName.substring(0, groupName.length() - 1)
                            : groupName.toString());
                    form.setGroupName(groupName.toString());
                }
            }
        }
        return new PageGridBean(query, result, true);
    }

    @RequestMapping(value = {"/add"}, method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }
    /*
    */

    /**
     * 基准信息录入
     * @author fanlu
     */
    @RequestMapping(value = {"/add"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final EquipForm form,
        final BindingResult bindingResult) {
        // 数据校验
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        } else {
            form.setFlag(1);
            form.setCreateDataTime(new Date());
            form.setCreateDataUsername(SystemHelper.getCurrentUsername());
            boolean flag = equipEntryService.addBenchmark(form);
            if (flag) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }

        }

    }

    /**
     * 根据id删除 基准信息
     */
    @Transactional
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) throws BusinessException {
        EquipForm form = new EquipForm();
        form.setId(id);
        form.setFlag(0);
        boolean flag = equipEntryService.deleteBenchmark(form);
        if (flag) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    /**
     * 批量删除
     */
    @Transactional
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(HttpServletRequest request) {
        String items = request.getParameter("deltems");
        String[] item = items.split(",");
        for (String s : item) {
            EquipForm form = new EquipForm();
            form.setId(s);
            form.setFlag(0);
            equipEntryService.deleteBenchmark(form);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 修改基准信息
     */

    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        ModelAndView mav = new ModelAndView(EDIT_PAGE);
        EquipForm form = equipEntryService.findBenchmarkById(id);
        mav.addObject("result", form);
        return mav;
    }

    /**
     * 修改基准信息
     * @author fanlu
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final EquipForm form,
        final BindingResult bindingResult) {
        // 数据校验
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        } else {
            form.setUpdateDataTime(new Date());
            form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
            boolean flag = equipEntryService.updateBenchmark(form);
            if (flag) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        }

    }

    /**
     * 根据所选组织返回车辆列表
     * @param ids 组织id
     * @author fan lu
     */

    @RequestMapping(value = "/findVehicle", method = RequestMethod.POST)
    @ResponseBody
    public String findVehicle(String ids, String type) {
        List<String> group = new ArrayList<>();
        group.add(ids);
        boolean flag = "assignment".equals(type);
        List<VehicleInfo> vehicle = equipEntryService.findVehicleByUser(group, flag);
        JSONArray result = new JSONArray();
        result.addAll(vehicle);
        return result.toJSONString();
    }

    /**
     * 车辆已存在基准信息
     * @param brand 车牌号
     * @author fan lu
     */

    @RequestMapping(value = "/checkExsit", method = RequestMethod.POST)
    @ResponseBody
    public boolean checkExsit(String brand) {
        List<EquipForm> result = equipEntryService.findBenchmark(userService.getOrgByUser(), null, false);
        if (CollectionUtils.isNotEmpty(result)) {
            for (EquipForm eq : result) {
                if (brand.equals(eq.getBrand())) {
                    return false;
                }
            }
        }
        return true;
    }
}
