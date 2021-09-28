package com.zw.platform.controller.common;


import com.zw.platform.domain.share.BaudRateUtil;
import com.zw.platform.domain.share.CompEnUtil;
import com.zw.platform.domain.share.FilterFactorUtil;
import com.zw.platform.domain.share.ParityCheckUtil;
import com.zw.platform.domain.share.ShapeUtil;
import com.zw.platform.domain.share.UploadTimeUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * <p> Title:通讯参数 <p> Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年07月06日 15:48
 */
@Controller
@RequestMapping("/m/sensorConfig/commParamController")
public class CommParamController {

    /**
     * 获取波特率Util
     * @param
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = {"/getBaudRateUtil.gsp"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getBaudRateUtil()
        throws BusinessException {
        return new JsonResultBean(BaudRateUtil.getMap().toJSONString());
    }

    /**
     * 获取油箱形状Util-share
     * @param
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = {"/getShareUtil.gsp"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getShareUtil()
        throws BusinessException {
        return new JsonResultBean(ShapeUtil.getMap().toJSONString());
    }

    /**
     * 获取补偿使能Util
     * @param
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = {"/getCompEnUtil.gsp"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getCompEnUtil()
        throws BusinessException {
        return new JsonResultBean(CompEnUtil.getMap().toJSONString());
    }

    /**
     * 获取滤波系数
     * @param
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = {"/getFilterFactorUtil.gsp"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getFilterFactorUtil()
        throws BusinessException {
        return new JsonResultBean(FilterFactorUtil.getMap().toJSONString());
    }

    /**
     * 获取奇偶校验Util
     * @param
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = {"/getParityCheckUtil.gsp"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getParityCheckUtil()
        throws BusinessException {
        return new JsonResultBean(ParityCheckUtil.getMap().toJSONString());
    }

    /**
     * 获取自动上传时间Util
     * @param
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = {"/getUploadTimeUtil.gsp"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getUploadTimeUtil()
        throws BusinessException {
        return new JsonResultBean(UploadTimeUtil.getMap().toJSONString());
    }

}
