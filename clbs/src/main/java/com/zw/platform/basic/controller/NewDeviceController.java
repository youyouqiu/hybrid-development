package com.zw.platform.basic.controller;

import com.zw.platform.basic.dto.DeviceDTO;
import com.zw.platform.basic.dto.query.DeviceQuery;
import com.zw.platform.basic.service.DeviceService;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.form.DeviceForm;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.ImportErrorUtil;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.talkback.common.ControllerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * Device Controller
 * @author wangying
 */

@Controller
@RequestMapping("/m/basicinfo/equipment/device")
public class NewDeviceController {


    @Autowired
    private DeviceService deviceService;

    @Autowired
    private HttpServletRequest request;

    private static final String LIST_PAGE = "modules/basicinfo/equipment/device/list";

    private static final String ADD_PAGE = "modules/basicinfo/equipment/device/add";

    private static final String EDIT_PAGE = "modules/basicinfo/equipment/device/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/equipment/device/import";

    /**
     * ??????????????????
     * @return list page
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * ????????????
     * @param query query
     * @return PageGridBean
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final DeviceQuery query) {

        return ControllerTemplate.getPageGridBean(() -> deviceService.getListByKeyWord(query), query, "??????????????????????????????");

    }

    /**
     * ??????????????????
     * @param map null
     * @return add page
     */
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public String getAddPage(ModelMap map) {
        return ADD_PAGE;
    }

    /**
     * ????????????
     * @param form          form
     * @param bindingResult bindingResult
     * @return JsonResultBean
     * @author wangying
     * @Title: ????????????
     */
    @AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addDevice(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final DeviceForm form,
        final BindingResult bindingResult) {
        return ControllerTemplate
            .getResultBean(() -> deviceService.add(DeviceDTO.getAddInstance(form)), "??????????????????", bindingResult);

    }

    /**
     * ??????id?????? ??????
     * @param id id
     * @return JsonResultBean
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        return ControllerTemplate.getResultBean(() -> deviceService.delete(id), "??????????????????");

    }

    /**
     * ????????????
     * @return JsonResultBean
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        return ControllerTemplate
            .getResultBean(() -> deviceService.deleteBatch(Arrays.asList(request.getParameter("deltems").split(","))),
                "????????????????????????");
    }

    /**
     * ????????????
     * @param id id
     * @return ModelAndView
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        return ControllerTemplate.editPage(EDIT_PAGE, () -> deviceService.findById(id));
    }

    /**
     * ????????????
     * @param form          form
     * @param bindingResult bindingResult
     * @return JsonResultBean
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final DeviceForm form,
        final BindingResult bindingResult) {
        return ControllerTemplate
            .getResultBean(() -> deviceService.updateNumber(DeviceDTO.getUpdateInstance(form)), "??????????????????",
                bindingResult);

    }

    /**
     * ??????
     * @param response response
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response) {
        ControllerTemplate.export(() -> deviceService.exportDevice(), "??????????????????", response, "????????????????????????");

    }

    /**
     * ????????????
     * @param response response
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        ControllerTemplate.export(() -> deviceService.generateTemplate(response), "????????????????????????", response, "????????????????????????????????????");
    }

    /**
     * ??????????????????
     * @return String
     * @author wangying
     * @Title: ??????
     */
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    /**
     * importDevice
     * @param file file
     * @return JsonResultBean
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importDevice(@RequestParam(value = "file", required = false) MultipartFile file) {
        return ControllerTemplate.getResultBean(() -> deviceService.importData(file), "????????????????????????");
    }

    @RequestMapping(value = "/exportError", method = RequestMethod.GET)
    public void exportDeviceError(HttpServletResponse response) {
        ControllerTemplate
            .execute(() -> ImportErrorUtil.generateErrorExcel(ImportModule.DEVICE, "????????????????????????", null, response),
                "??????????????????????????????");

    }

    /**
     * repetition
     * @param deviceNumber deviceNumber
     * @return boolean
     */
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public Boolean repetition(@RequestParam("deviceNumber") String deviceNumber) {
        return ControllerTemplate.execute(() -> !deviceService.checkIsExist(deviceNumber, null), "????????????????????????", false);

    }

}
