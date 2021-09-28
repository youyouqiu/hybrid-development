package com.zw.adas.service.defineSetting.impl;

import com.github.pagehelper.Page;
import com.zw.adas.domain.riskManagement.AdasRiskEvent;
import com.zw.adas.domain.riskManagement.form.AdasRiskEventForm;
import com.zw.adas.domain.riskManagement.query.AdasRiskEventQuery;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasRiskEventDao;
import com.zw.adas.service.defineSetting.AdasRiskEventService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ExportRiskEventExcel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Tdz on 2016/7/25.
 */
@Service
public class AdasRiskEventServiceImpl implements AdasRiskEventService {

    private static final Integer RISKEVENT_NUMBER_MAXLENGTH = 20; // 风险事件最大长度

    private static final Integer DESCRIBE_NUMBER_MAXLENGTH = 200; // 风险描述最大长度

    private static final Integer RISK_TYPE_NUMBER_MAXLENGTH = 20; // 风险类型最大长度

    private static Logger log = LogManager.getLogger(AdasRiskEventServiceImpl.class);

    private static final String TEMPLATE_COMMENT = "注：红色标注为必填；风险id组合规则：外设id+风险类型，如：“6403” 代表车距过近；(另：整理前请删除示例数据，谢谢)";

    @Autowired
    private AdasRiskEventDao adasRiskEventDao;

    @Override
    public AdasRiskEvent get(String id) throws Exception {
        if (StringUtils.isNotBlank(id)) {
            return adasRiskEventDao.get(id);
        }
        return null;
    }

    @Override
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<String>();
        List<String> requiredList = new ArrayList<String>();
        List<Object> exportList = new ArrayList<Object>();
        // 表头
        // headList.add("事件id");
        headList.add("风险事件");
        headList.add("风险类型*");
        headList.add("描述");

        // 必填字段
        // requiredList.add("事件id");
        requiredList.add("风险事件");
        requiredList.add("风险类型");
        // 默认设置一条数据
        // exportList.add("6403");
        exportList.add("车距过近");
        exportList.add("碰撞危险");
        exportList.add("车距过近碰撞危险");

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<String, String[]>();
        // 风险类别
        String[] riskType = { "碰撞危险", "注意力分散", "疑似疲劳", "违规异常" };
        selectMap.put("风险类型*", riskType);

        // ExportExcel export = new ExportExcel(headList,
        // requiredList,selectMap);
        ExportRiskEventExcel export = new ExportRiskEventExcel(TEMPLATE_COMMENT, headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // 输出导文件
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }

    @Override
    public Page<AdasRiskEvent> findByPage(AdasRiskEventQuery query) throws Exception {
        return PageHelperUtil.doSelect(query, () -> adasRiskEventDao.find(query));
    }

    @Override
    public AdasRiskEvent findByRiskEvent(String riskEvent) throws Exception {
        return adasRiskEventDao.findByRiskEvent(riskEvent);
    }

    @Override
    public AdasRiskEvent findById(String id) throws Exception {
        return adasRiskEventDao.findById(id);
    }

    @Override
    public int delete(String id) throws Exception {
        return adasRiskEventDao.delete(id);
    }

    @Override
    public List<AdasRiskEvent> find(AdasRiskEventQuery query) {
        return adasRiskEventDao.find(query);
    }

    @Override
    public AdasRiskEvent findByRiskEvent(String riskType, String riskEvent) {
        return adasRiskEventDao.isExist(riskType, riskEvent);
    }

    @Override
    public void add(AdasRiskEventForm form) throws Exception {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        adasRiskEventDao.add(form);

    }

    @Override
    public int update(AdasRiskEventForm form) throws Exception {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        form.setCreateDataTime(new Date());
        return adasRiskEventDao.update(form);
    }

    @Override
    public boolean isRepeate(AdasRiskEventForm form) {

        int result = adasRiskEventDao.isRepeate(form);
        return result > 0;
    }

    @Override
    public boolean exportInfo(String title, int type, HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(title, AdasRiskEventForm.class, 1, null);
        List<AdasRiskEventForm> exportList = new ArrayList<AdasRiskEventForm>();
        List<AdasRiskEvent> list = adasRiskEventDao.find(null);
        for (AdasRiskEvent info : list) {
            AdasRiskEventForm form = new AdasRiskEventForm();
            BeanUtils.copyProperties(info, form);
            exportList.add(form);
        }
        export.setDataList(exportList);
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }

}