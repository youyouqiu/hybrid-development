package com.zw.platform.controller.offlineExport;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.domain.basicinfo.query.OfflineExportQuery;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 离线报表导出
 * @author XK 2020/05/26
 */
@Controller
@RequestMapping("/offline/export")
public class OfflineExportController {
    private static Logger logger = LogManager.getLogger(OfflineExportController.class);


    @Autowired
    private OfflineExportService offlineExportService;

    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getPage(OfflineExportQuery query) {
        try {
            Page<OfflineExportInfo> result = offlineExportService.getPageOfflineExport(query);
            return new PageGridBean(query, result, PageGridBean.SUCCESS);
        } catch (Exception e) {
            logger.error("分页查询离线报表异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = {"/delete"}, method = RequestMethod.POST)
    public JsonResultBean deleteOfflineExport() {

        return new JsonResultBean();
    }

}
