package com.cb.platform.contorller;

import com.cb.platform.domain.ItemNameEntity;
import com.cb.platform.domain.ItemNameQuery;
import com.cb.platform.service.ItemNameService;
import com.cb.platform.service.TransportTimesService;
import com.cb.platform.util.StringToList;
import com.github.pagehelper.Page;
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

@Controller
@RequestMapping("/m/monitoring/vehicle/itemName")
public class ItemNameController {

    private static final Logger log = LogManager.getLogger(ItemNameController.class);

    @Autowired
    private ItemNameService itemNameService;
    @Autowired
    private TransportTimesService transportTimesService;

    private static final String LIST_PAGE = "/modules/basicinfo/monitoring/vehicle/dangerious/list";

    private static final String EDIT = "/modules/basicinfo/monitoring/vehicle/dangerious/productNameEdit";

    private static final String ADD_PAGE = "/modules/basicinfo/monitoring/vehicle/dangerious/productNameAdd";

    private static final String importHtml = "/modules/basicinfo/monitoring/vehicle/dangerious/productNameImport";
    private static final String ERROR_PAGE = "html/errors/error_exception";

    /**
     * ????????????
     * @param id
     * @return
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT);
            getVehicleDetail(id, mav);
            return mav;
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ????????????
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String add() {
        return ADD_PAGE;
    }

    /**
     * ????????????
     * @return
     */
    @RequestMapping(value = "/importHtml", method = RequestMethod.GET)
    public String importHtml() {
        return importHtml;
    }

    /**
     * ???????????????????????????
     * @param id  ??????id
     * @param mav mav
     * @throws Exception this
     */
    private void getVehicleDetail(@PathVariable String id, ModelAndView mav) throws Exception {
        ItemNameEntity itemNameEntity = itemNameService.findById(id);
        mav.addObject("result", itemNameEntity);
    }

    /**
     * ????????????
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list() {
        return LIST_PAGE;
    }

    /**
     * ??????
     * @param itemNameEntity
     * @return
     */
    @RequestMapping(value = "/addItemName", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addItemName(@Validated(ValidGroupAdd.class) final ItemNameEntity itemNameEntity,
        HttpServletRequest request) {
        //???????????????
        Map<String, Object> checkMap = check(itemNameEntity);
        if (!(boolean) checkMap.get("flag")) {
            return new JsonResultBean(JsonResultBean.FAULT, checkMap.get("msg").toString());
        }
        //??????
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            itemNameService.addItemName(itemNameEntity, ipAddress);
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "????????????");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS, "????????????");
    }

    /**
     * ??????????????????????????????
     * @param
     * @return
     */
    @RequestMapping(value = "/findList", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean findList() {
        List<ItemNameEntity> list = null;
        try {
            list = itemNameService.findList();
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????");
        }
        return new JsonResultBean(list);
    }

    /**
     * ????????????????????????
     * @param name
     * @return
     */
    @RequestMapping(value = "/chechItem", method = RequestMethod.GET)
    @ResponseBody
    public boolean chechItem(String name) {
        if (StringUtils.isBlank(name)) {
            return false;
        }
        try {
            List<ItemNameEntity> list = itemNameService.findByName(name);
            if (list.size() == 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("??????????????????", e);
            return false;
        }
    }

    /**
     * ?????????????????????????????????
     * @param name
     * @return
     */
    @RequestMapping(value = "/chechItemById", method = RequestMethod.GET)
    @ResponseBody
    public boolean chechItemById(String name, String id) {
        if (StringUtils.isBlank(name)) {
            return false;
        }
        if (StringUtils.isBlank(id)) {
            return false;
        }
        try {
            List<ItemNameEntity> list = itemNameService.findByName(name);
            boolean flag = true;
            for (ItemNameEntity itemNameEntity : list) {
                if (!id.equals(itemNameEntity.getId()) && name.equals(itemNameEntity.getName())) {
                    flag = false;
                }
            }
            return flag;
        } catch (Exception e) {
            log.error("??????????????????", e);
            return false;
        }
    }

    /**
     * ??????????????????
     * @param query
     * @return
     */
    @RequestMapping(value = "/searchItemName", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean searchItemName(ItemNameQuery query) {
        Page<ItemNameEntity> list = null;
        try {
            list = itemNameService.searchItemName(query);
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new PageGridBean(JsonResultBean.FAULT);
        }
        return new PageGridBean(query, (Page<ItemNameEntity>) list, true);
    }

    /**
     * ??????
     * @return
     */
    @RequestMapping(value = "/updateItemName", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateItemName(@Validated(ValidGroupAdd.class) final ItemNameEntity itemNameEntity,
        HttpServletRequest request) {
        if (StringUtils.isBlank(itemNameEntity.getId())) {
            return new JsonResultBean(JsonResultBean.FAULT, "id????????????");
        }
        //??????
        Map<String, Object> checkMap = check(itemNameEntity);
        if (!(boolean) checkMap.get("flag")) {
            return new JsonResultBean(JsonResultBean.FAULT, checkMap.get("msg").toString());
        }
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            itemNameService.updateItemName(itemNameEntity, ipAddress);
        } catch (Exception e) {
            log.error("????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "????????????");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS, "????????????");
    }

    /**
     * ??????
     * ???????????????????????????<??????>???????????????????????????????????????????????????????????????????????????????????????
     * @return
     */
    @RequestMapping(value = "/deleteItemName", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteItemName(String idList, HttpServletRequest request) {
        if (StringUtils.isBlank(idList)) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????");
        }
        List<String> idquery = StringToList.stringToList(idList);
        String message = "????????????";
        boolean flag = true;
        try {
            List<ItemNameEntity> list = itemNameService.findByIdList(idquery);
            if (list.size() == 0) {
                return new JsonResultBean(JsonResultBean.FAULT, "???????????????");
            }
            //????????????????????????????????????,??????????????????????????????
            List<String> itemid = transportTimesService.findByItemName(idquery);
            if (itemid.size() > 0) {
                idquery.removeIf(itemid::contains);
                message = "????????????????????????????????????????????????,??????????????????";
                flag = false;
                if (itemid.size() == 0) {
                    return new JsonResultBean(flag, message);
                }
            }
            String ipAddress = new GetIpAddr().getIpAddr(request);
            itemNameService.deleteItemName(idquery, ipAddress);
        } catch (Exception e) {
            log.error("????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "????????????");
        }
        return new JsonResultBean(flag, message);
    }

    /**
     * ??????
     * ???????????????????????????<??????>???????????????????????????????????????????????????????????????????????????????????????
     * @return
     */
    @RequestMapping(value = "/deleteItemNameById/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteItemNameById(@PathVariable final String id, HttpServletRequest request) {
        if (StringUtils.isBlank(id)) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????");
        }
        List<String> idquery = new ArrayList<>(1);
        idquery.add(id);
        try {
            ItemNameEntity item = itemNameService.findById(id);
            if (item == null) {
                return new JsonResultBean(JsonResultBean.FAULT, "???????????????");
            }
            //????????????????????????????????????,??????????????????????????????
            List<String> itemid = transportTimesService.findByItemName(idquery);
            if (itemid.size() > 0) {
                return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????????????????????????????,??????????????????");
            }
            String ipAddress = new GetIpAddr().getIpAddr(request);
            itemNameService.deleteById(item, ipAddress);
        } catch (Exception e) {
            log.error("????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "????????????");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS, "????????????");
    }

    /**
     * ??????
     * @param res
     */
    @RequestMapping(value = "/exportEnterpriseList", method = RequestMethod.GET)
    public void exportEnterpriseList(HttpServletResponse res, String name, HttpServletRequest request) {
        try {
            ExportExcelUtil.setResponseHead(res, "????????????");
            String ipAddress = new GetIpAddr().getIpAddr(request);
            itemNameService.export(null, 1, res, name, ipAddress);
        } catch (Exception e) {
            log.error("??????????????????????????????(get)", e);
        }
    }

    /**
     * ??????
     * @param response
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "??????????????????");
            itemNameService.download(null, 1, response);
        } catch (Exception e) {
            log.error("????????????????????????(get)", e);
        }
    }

    /**
     * ??????
     * @param file        ??????
     * @param httpRequest request
     * @return result
     */
    @RequestMapping(value = "/importItemName", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importItemName(@RequestParam(value = "file", required = false) MultipartFile file,
        HttpServletRequest httpRequest) {
        try {
            // ?????????????????????IP
            String ipAddress = new GetIpAddr().getIpAddr(httpRequest);
            Map resultMap = itemNameService.importItemName(file, ipAddress);
            String msg = "???????????????" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * false ???????????????
     * @param itemNameEntity
     * @return
     */
    public Map<String, Object> check(ItemNameEntity itemNameEntity) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("flag", true);
        if (StringUtils.isBlank(itemNameEntity.getName())) {
            map.put("flag", false);
            map.put("msg", "??????????????????");
            return map;
        }
        if (itemNameEntity.getName().length() > 20) {
            map.put("flag", false);
            map.put("msg", "????????????????????????20");
            return map;
        }
        List<ItemNameEntity> list = null;
        try {
            list = itemNameService.findByName(itemNameEntity.getName());
            if (list.size() > 0) {
                //id????????????????????????
                if (StringUtils.isNotBlank(itemNameEntity.getId())) {
                    for (ItemNameEntity entity : list) {
                        //???????????????ID????????????????????????
                        if (itemNameEntity.getName().equals(entity.getName()) && !itemNameEntity.getId()
                            .equals(entity.getId())) {
                            map.put("flag", false);
                            map.put("msg", "??????????????????");
                            return map;
                        }
                    }
                } else {
                    for (ItemNameEntity entity : list) {
                        if (itemNameEntity.getName().equals(entity.getName())) {
                            map.put("flag", false);
                            map.put("msg", "??????????????????");
                            return map;
                        }
                    }
                }

            }
        } catch (Exception e) {
            log.error("????????????????????????", e);
            map.put("flag", false);
            map.put("msg", "????????????????????????");
            return map;
        }
        //?????????????????????
        if (itemNameEntity.getDangerType() != null) {
            List<Map<String, Object>> typeList = null;
            try {
                typeList = itemNameService.selectType();
                if (typeList.size() > 0) {
                    boolean flag = false;
                    for (Map<String, Object> typemap : typeList) {
                        if (itemNameEntity.getDangerType().toString().equals(typemap.get("code").toString())) {
                            flag = true;
                        }
                    }
                    if (!flag) {
                        map.put("flag", false);
                        map.put("msg", "????????????????????????");
                        return map;
                    }
                } else {
                    map.put("flag", false);
                    map.put("msg", "????????????????????????");
                    return map;
                }
            } catch (Exception e) {
                log.error("???????????????????????????", e);
            }

        }
        if (itemNameEntity.getUnit() != null) {
            if (itemNameEntity.getUnit() > 2 || itemNameEntity.getUnit() < 1) {
                map.put("flag", false);
                map.put("msg", "????????????????????????");
                return map;
            }
        }
        if (StringUtils.isNotBlank(itemNameEntity.getRemark()) && itemNameEntity.getRemark().length() > 50) {
            map.put("flag", false);
            map.put("msg", "?????????????????????????????????");
            return map;
        }
        return map;
    }

}
