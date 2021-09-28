package com.zw.platform.controller.basicinfo;


import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.CardReaderInfo;
import com.zw.platform.domain.basicinfo.form.CardReaderInfoForm;
import com.zw.platform.domain.basicinfo.query.CardReaderInfoQuery;
import com.zw.platform.service.basicinfo.CardReaderInfoService;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
import java.io.UnsupportedEncodingException;
import java.util.Map;


/**
 * 读卡器管理控制器 <p>Title: CardReaderInfoService.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 *
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年7月21日下午5:09:45
 */
@Controller
@RequestMapping("/m/cardreader")
public class CardReaderInfoController {

    private static final String LIST_PAGE = "modules/basicinfo/equipment/cardreader/list";

    private static final String ADD_PAGE = "modules/basicinfo/equipment/cardreader/add";

    private static final String EDIT_PAGE = "modules/basicinfo/equipment/cardreader/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/equipment/cardreader/import";

    @Autowired
    private CardReaderInfoService cardReaderInfoService;

    /**
     * 读卡器列表页面
     *
     * @return String
     * @throws BusinessException
     * @Title: listPage
     * @author Liubangquan
     */
    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    /**
     * 分页查询读卡器列表
     *
     * @param query
     * @return String
     * @throws BusinessException
     * @Title: list
     * @author Liubangquan
     */
    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(final CardReaderInfoQuery query) throws BusinessException {
        Page<CardReaderInfo> result = cardReaderInfoService.findByPage(query);
        return new PageGridBean(query, result, true);
    }

    /**
     * 读卡器新增界面
     *
     * @return String
     * @throws BusinessException
     * @Title: add
     * @author Liubangquan
     */
    @RequestMapping(value = {"/add"}, method = RequestMethod.GET)
    public String add() throws BusinessException {
        return ADD_PAGE;
    }

    /**
     * 新增读卡器
     *
     * @return JsonResultBean
     * @throws @author Liubangquan
     * @Title: add
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({ValidGroupAdd.class})
                              @ModelAttribute("form") final CardReaderInfoForm form,
                              final BindingResult bindingResult) throws BusinessException {

        cardReaderInfoService.add(form);

        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 根据id删除 CardReaderInfo
     *
     * @return JsonResultBean
     * @throws @author Liubangquan
     * @Title: delete
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) throws BusinessException {
        cardReaderInfoService.delete(id);
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 批量删除
     *
     * @return JsonResultBean
     * @throws @author Liubangquan
     * @Title: deleteMore
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(HttpServletRequest request) throws BusinessException {
        String items = request.getParameter("deltems");
        String[] item = items.split(",");
        for (int i = 0; i < item.length; i++) {
            cardReaderInfoService.delete(item[i]);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 修改CardReaderInfo 页面
     *
     * @return ModelAndView
     * @throws @author Liubangquan
     * @Title: editPage
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        ModelAndView mav = new ModelAndView(EDIT_PAGE);
        mav.addObject("result", cardReaderInfoService.get(id));
        return mav;
    }

    /**
     * 修改CardReaderInfo操作
     *
     * @return JsonResultBean
     * @throws @author Liubangquan
     * @Title: edit
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ValidGroupUpdate.class})
                               @ModelAttribute("form") final CardReaderInfoForm form,
                               final BindingResult bindingResult) throws BusinessException {
        cardReaderInfoService.update(form);
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 导出到excel表格
     *
     * @return void
     * @throws UnsupportedEncodingException
     * @Title: export
     * @author Liubangquan
     */
    @RequestMapping(value = "/export.gsp", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response, HttpServletRequest request)
            throws UnsupportedEncodingException {
        ExportExcelUtil.setResponseHead(response, "读卡器信息列表");
        cardReaderInfoService.exportCardReaderInfo(null, 1, response);
    }

    /**
     * 点击导入跳转的页面
     *
     * @return String
     * @throws BusinessException
     * @Title: importPage
     * @author Liubangquan
     */
    @RequestMapping(value = {"/import"}, method = RequestMethod.GET)
    public String importPage() throws BusinessException {
        return IMPORT_PAGE;
    }

    /**
     * 模板下载
     *
     * @param response
     * @param request
     * @return void
     * @throws UnsupportedEncodingException
     * @Title: download
     * @author Liubangquan
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response, HttpServletRequest request) throws UnsupportedEncodingException {
        ExportExcelUtil.setResponseHead(response, "读卡器信息列表模板");
        cardReaderInfoService.generateTemplate(response);
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importCardReaderInfo(@RequestParam(value = "file", required = false) MultipartFile file,
                                               HttpServletRequest request) throws BusinessException {
        Map resultMap = cardReaderInfoService.importCardReaderInfo(file);
        String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
        JsonResultBean result = new JsonResultBean(true, msg);
        return result;
    }

    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("cardReaderNumber") String cardReaderNumber) {
        CardReaderInfo vt = null;

        vt = cardReaderInfoService.findByCardReaderInfo(cardReaderNumber);

        if (vt == null) {
            return true;
        } else {
            return false;
        }
    }
}
