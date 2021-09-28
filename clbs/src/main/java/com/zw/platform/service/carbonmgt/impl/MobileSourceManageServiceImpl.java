package com.zw.platform.service.carbonmgt.impl;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.vas.carbonmgt.MobileSourceManage;
import com.zw.platform.domain.vas.carbonmgt.form.MobileSourceManageForm;
import com.zw.platform.domain.vas.carbonmgt.query.MobileSourceManageQuery;
import com.zw.platform.repository.vas.MobileSourceManageDao;
import com.zw.platform.service.carbonmgt.MobileSourceManageService;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 王健宇 on 2017/2/21.
 */
@Service
public class MobileSourceManageServiceImpl implements MobileSourceManageService {

    @Autowired
    private MobileSourceManageDao mobileSourceManageDao;

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public List<Positional> mobileSourceManage(String band, String startTime, String endTime) throws Exception {
        List<Positional> list;

        long stime = 0;
        long ntime = 0;
        stime = DateUtils.parseDate(startTime, DATE_FORMAT).getTime() / 1000;
        ntime = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
        list = getList(band, stime, ntime);
        return list;
    }

    private List<Positional> getList(String band, long stime, long ntime) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("brand", band);
        params.put("startTime", String.valueOf(stime));
        params.put("endTime", String.valueOf(ntime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.MOBILE_SOURCE_MANAGE, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    @Override
    public boolean add(JSONObject msg, String band, String startTime, String endTime) throws Exception {
        MobileSourceManageForm mobileSourceManageform = new MobileSourceManageForm();
        mobileSourceManageform.setTotalMileage(msg.getString("totalMileage"));
        mobileSourceManageform.setSpeed(msg.getString("speed"));
        mobileSourceManageform.setTotalFuelConsumption(msg.getString("totalFuelConsumption"));
        mobileSourceManageform.setBTotalFuelConsumption(msg.getString("bTotalFuelConsumption"));
        mobileSourceManageform.setCo2(msg.getString("co2"));
        mobileSourceManageform.setBco2(msg.getString("co2"));
        mobileSourceManageform.setBrand(band);
        mobileSourceManageform.setStartTime(startTime);
        mobileSourceManageform.setEndTime(endTime);
        boolean flag = mobileSourceManageDao.addMobile(mobileSourceManageform);
        return flag;
    }

    @Override
    public boolean find(String band, String startTime, String endTime) throws Exception {
        String id = mobileSourceManageDao.find(band, startTime, endTime);
        if (id == null) {
            return false;
        } else if (id != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<MobileSourceManageForm> findList(String brand) throws Exception {
        return mobileSourceManageDao.findList(brand);
    }

    @Override
    public boolean del(String band, String startTime, String endTime) {
        boolean flag = mobileSourceManageDao.del(band, startTime, endTime);
        return flag;
    }

    @Override
    @MethodLog(name = "导出", description = "导出")
    public boolean exportMobileSourceManage(String title, int type, HttpServletResponse response,
        List<MobileSourceManage> mobileInfo) throws Exception {
        ExportExcel export = new ExportExcel(title, MobileSourceManageQuery.class, 1, null);
        List<MobileSourceManageQuery> exportList = new ArrayList<MobileSourceManageQuery>();
        // List<MobileSourceManage> list = null;
        for (MobileSourceManage info : mobileInfo) {
            MobileSourceManageQuery form = new MobileSourceManageQuery();
            BeanUtils.copyProperties(info, form);
            exportList.add(form);
        }
        export.setDataList(exportList);
        OutputStream out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }
}
