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
     * 修改页面
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
            log.error("修改趟次运输弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 添加页面
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String add() {
        return ADD_PAGE;
    }

    /**
     * 导入页面
     * @return
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
        ItemNameEntity itemNameEntity = itemNameService.findById(id);
        mav.addObject("result", itemNameEntity);
    }

    /**
     * 获取页面
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list() {
        return LIST_PAGE;
    }

    /**
     * 添加
     * @param itemNameEntity
     * @return
     */
    @RequestMapping(value = "/addItemName", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addItemName(@Validated(ValidGroupAdd.class) final ItemNameEntity itemNameEntity,
        HttpServletRequest request) {
        //对数据校验
        Map<String, Object> checkMap = check(itemNameEntity);
        if (!(boolean) checkMap.get("flag")) {
            return new JsonResultBean(JsonResultBean.FAULT, checkMap.get("msg").toString());
        }
        //添加
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            itemNameService.addItemName(itemNameEntity, ipAddress);
        } catch (Exception e) {
            log.error("添加品名失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "添加失败");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS, "添加成功");
    }

    /**
     * 不分页查询所有的品名
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
            log.error("查询品名失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "查询品名失败");
        }
        return new JsonResultBean(list);
    }

    /**
     * 校验品名是否重复
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
            log.error("校验品名失败", e);
            return false;
        }
    }

    /**
     * 校验修改的品名是否重复
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
            log.error("校验品名失败", e);
            return false;
        }
    }

    /**
     * 品名管理查询
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
            log.error("查询品名失败", e);
            return new PageGridBean(JsonResultBean.FAULT);
        }
        return new PageGridBean(query, (Page<ItemNameEntity>) list, true);
    }

    /**
     * 修改
     * @return
     */
    @RequestMapping(value = "/updateItemName", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateItemName(@Validated(ValidGroupAdd.class) final ItemNameEntity itemNameEntity,
        HttpServletRequest request) {
        if (StringUtils.isBlank(itemNameEntity.getId())) {
            return new JsonResultBean(JsonResultBean.FAULT, "id不能为空");
        }
        //校验
        Map<String, Object> checkMap = check(itemNameEntity);
        if (!(boolean) checkMap.get("flag")) {
            return new JsonResultBean(JsonResultBean.FAULT, checkMap.get("msg").toString());
        }
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            itemNameService.updateItemName(itemNameEntity, ipAddress);
        } catch (Exception e) {
            log.error("修改失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "修改失败");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS, "修改成功");
    }

    /**
     * 删除
     * 趟次列表中已绑定的<品名>不允许删除，删除时提示“存在已绑定的趟次信息，不可删除”；
     * @return
     */
    @RequestMapping(value = "/deleteItemName", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteItemName(String idList, HttpServletRequest request) {
        if (StringUtils.isBlank(idList)) {
            return new JsonResultBean(JsonResultBean.FAULT, "未选择品名");
        }
        List<String> idquery = StringToList.stringToList(idList);
        String message = "删除成功";
        boolean flag = true;
        try {
            List<ItemNameEntity> list = itemNameService.findByIdList(idquery);
            if (list.size() == 0) {
                return new JsonResultBean(JsonResultBean.FAULT, "品名不存在");
            }
            //查询趟次有没有品名的存在,存在绑定关系的不删除
            List<String> itemid = transportTimesService.findByItemName(idquery);
            if (itemid.size() > 0) {
                idquery.removeIf(itemid::contains);
                message = "品名存在已绑定的危险货物运输趟次,不能进行删除";
                flag = false;
                if (itemid.size() == 0) {
                    return new JsonResultBean(flag, message);
                }
            }
            String ipAddress = new GetIpAddr().getIpAddr(request);
            itemNameService.deleteItemName(idquery, ipAddress);
        } catch (Exception e) {
            log.error("删除失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "删除失败");
        }
        return new JsonResultBean(flag, message);
    }

    /**
     * 删除
     * 趟次列表中已绑定的<品名>不允许删除，删除时提示“存在已绑定的趟次信息，不可删除”；
     * @return
     */
    @RequestMapping(value = "/deleteItemNameById/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteItemNameById(@PathVariable final String id, HttpServletRequest request) {
        if (StringUtils.isBlank(id)) {
            return new JsonResultBean(JsonResultBean.FAULT, "未选择品名");
        }
        List<String> idquery = new ArrayList<>(1);
        idquery.add(id);
        try {
            ItemNameEntity item = itemNameService.findById(id);
            if (item == null) {
                return new JsonResultBean(JsonResultBean.FAULT, "品名不存在");
            }
            //查询趟次有没有品名的存在,存在绑定关系的不删除
            List<String> itemid = transportTimesService.findByItemName(idquery);
            if (itemid.size() > 0) {
                return new JsonResultBean(JsonResultBean.FAULT, "品名存在已绑定的危险货物运输趟次,不能进行删除");
            }
            String ipAddress = new GetIpAddr().getIpAddr(request);
            itemNameService.deleteById(item, ipAddress);
        } catch (Exception e) {
            log.error("删除失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "删除失败");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS, "删除成功");
    }

    /**
     * 导出
     * @param res
     */
    @RequestMapping(value = "/exportEnterpriseList", method = RequestMethod.GET)
    public void exportEnterpriseList(HttpServletResponse res, String name, HttpServletRequest request) {
        try {
            ExportExcelUtil.setResponseHead(res, "品名管理");
            String ipAddress = new GetIpAddr().getIpAddr(request);
            itemNameService.export(null, 1, res, name, ipAddress);
        } catch (Exception e) {
            log.error("品名管理导出数据异常(get)", e);
        }
    }

    /**
     * 模板
     * @param response
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "品名管理模板");
            itemNameService.download(null, 1, response);
        } catch (Exception e) {
            log.error("品名模板导出异常(get)", e);
        }
    }

    /**
     * 导入
     * @param file        文件
     * @param httpRequest request
     * @return result
     */
    @RequestMapping(value = "/importItemName", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importItemName(@RequestParam(value = "file", required = false) MultipartFile file,
        HttpServletRequest httpRequest) {
        try {
            // 获取操作用户的IP
            String ipAddress = new GetIpAddr().getIpAddr(httpRequest);
            Map resultMap = itemNameService.importItemName(file, ipAddress);
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("导入品名管理异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * false 验证不通过
     * @param itemNameEntity
     * @return
     */
    public Map<String, Object> check(ItemNameEntity itemNameEntity) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("flag", true);
        if (StringUtils.isBlank(itemNameEntity.getName())) {
            map.put("flag", false);
            map.put("msg", "品名不能为空");
            return map;
        }
        if (itemNameEntity.getName().length() > 20) {
            map.put("flag", false);
            map.put("msg", "品名长度不能大于20");
            return map;
        }
        List<ItemNameEntity> list = null;
        try {
            list = itemNameService.findByName(itemNameEntity.getName());
            if (list.size() > 0) {
                //id不为空则校验修改
                if (StringUtils.isNotBlank(itemNameEntity.getId())) {
                    for (ItemNameEntity entity : list) {
                        //品名相等且ID不相等则品名重复
                        if (itemNameEntity.getName().equals(entity.getName()) && !itemNameEntity.getId()
                            .equals(entity.getId())) {
                            map.put("flag", false);
                            map.put("msg", "品名不能重复");
                            return map;
                        }
                    }
                } else {
                    for (ItemNameEntity entity : list) {
                        if (itemNameEntity.getName().equals(entity.getName())) {
                            map.put("flag", false);
                            map.put("msg", "品名不能重复");
                            return map;
                        }
                    }
                }

            }
        } catch (Exception e) {
            log.error("查询品名列表失败", e);
            map.put("flag", false);
            map.put("msg", "查询品名列表失败");
            return map;
        }
        //校验危险品类别
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
                        map.put("msg", "危险品类别不存在");
                        return map;
                    }
                } else {
                    map.put("flag", false);
                    map.put("msg", "危险品类别不存在");
                    return map;
                }
            } catch (Exception e) {
                log.error("查询危险品类别失败", e);
            }

        }
        if (itemNameEntity.getUnit() != null) {
            if (itemNameEntity.getUnit() > 2 || itemNameEntity.getUnit() < 1) {
                map.put("flag", false);
                map.put("msg", "请选择正确的单位");
                return map;
            }
        }
        if (StringUtils.isNotBlank(itemNameEntity.getRemark()) && itemNameEntity.getRemark().length() > 50) {
            map.put("flag", false);
            map.put("msg", "备注不能大于五十个字符");
            return map;
        }
        return map;
    }

}
