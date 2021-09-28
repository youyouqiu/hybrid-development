package com.zw.talkback.service.baseinfo.impl;

import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.talkback.domain.intercom.form.IntercomModelForm;
import com.zw.talkback.domain.intercom.info.IntercomModelInfo;
import com.zw.talkback.domain.intercom.info.OriginalModelInfo;
import com.zw.talkback.domain.intercom.query.IntercomModelQuery;
import com.zw.talkback.repository.mysql.IntercomModelDao;
import com.zw.talkback.service.baseinfo.IntercomModelService;
import com.zw.talkback.util.excel.ExportExcelParam;
import com.zw.talkback.util.excel.ExportExcelUtil;
import joptsimple.internal.Strings;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


/**
 * 对讲机型实现类
 */
@Service
public class IntercomModelServiceImpl implements IntercomModelService {

    @Autowired
    private IntercomModelDao intercomModelDao;
    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    @Override
    public JsonResultBean addIntercomModel(IntercomModelForm form, String ipAddress) {
        if (checkModelNameIsExists(form.getName(), null)) {
            return new JsonResultBean(false, form.getName() + "该对讲机型名称在平台已经存在!");
        }
        List<String> names = intercomModelDao.getNameByOriginalModelId(form.getOriginalModelId(), null);
        boolean originalModelIdIsBind = CollectionUtils.isNotEmpty(names);
        if (originalModelIdIsBind) {
            return new JsonResultBean(false, names.get(0) + "该原始机型在平台已经被绑定了!");
        }
        intercomModelDao.addIntercomModel(form);
        addLog(form.getName(), ipAddress, "新增对讲机型: ");
        return new JsonResultBean(true);
    }

    private void addLog(String name, String ipAddress, String operate) {
        logSearchServiceImpl.addLog(ipAddress, operate + name, "3", "", "-", "");
    }

    @Override
    public JsonResultBean updateIntercomModel(IntercomModelForm form, String ipAddress) {
        if (checkModelNameIsExists(form.getName(), form.getId())) {
            return new JsonResultBean(false, form.getName() + "该对讲名称在平台已经存在!");
        }

        List<String> names = intercomModelDao.getNameByOriginalModelId(form.getOriginalModelId(), form.getId());
        boolean originalModelIdIsBind = CollectionUtils.isNotEmpty(names);
        if (originalModelIdIsBind) {
            return new JsonResultBean(false, names.get(0) + "该原始机型在平台已经被绑定了!");
        }
        intercomModelDao.updateIntercomModel(form);

        addLog(form.getName(), ipAddress, "更新对讲机型: ");
        return new JsonResultBean(true);
    }

    private boolean checkModelNameIsExists(String name, String id) {
        return StrUtil.isNotBlank(intercomModelDao.getModelNameByIdAndName(name, id));
    }

    @Override
    public void exportIntercomModels(IntercomModelQuery query, HttpServletResponse response) throws IOException {

        ExportExcelUtil.export(
            new ExportExcelParam(getIntercomModels(query), OriginalModelInfo.class, response.getOutputStream()));
    }

    @Override
    public List<OriginalModelInfo> getIntercomModels(IntercomModelQuery query) {

        if (query != null && StringUtils.isNotBlank(query.getSimpleQueryParam())) {
            //特殊字符转译
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        }
        List<OriginalModelInfo> intercomModels = intercomModelDao.getIntercomModels(query);
        intercomModels.forEach(OriginalModelInfo::initExportData);
        return intercomModels;
    }

    @Override
    public JsonResultBean deleteIntercomModelById(String id, String ipAddress) {
        return deleteIntercomModelByIds(id, ipAddress);
    }

    @Override
    public JsonResultBean deleteIntercomModelByIds(String ids, String ipAddress) {
        List<String> bindIntercomModel = intercomModelDao.getBindIntercomModelByIds(ids);
        if (CollectionUtils.isNotEmpty(bindIntercomModel)) {
            return new JsonResultBean(false, Strings.join(bindIntercomModel, ",") + "这些对讲机型已经被绑定了！");
        }
        intercomModelDao.deleteIntercomModelByIds(ids);
        if (ids.length() == 1) {
            addLog(bindIntercomModel.get(0), ipAddress, "删除对讲机型");
        } else {
            addLog("", ipAddress, "批量删除对讲机型");
        }
        return new JsonResultBean(true);
    }

    @Override
    public IntercomModelInfo getIntercomModelById(String id) {
        return intercomModelDao.getIntercomModelById(id);
    }

}
