package com.zw.adas.service.riskdisposerecord;

import com.zw.adas.domain.report.inspectuser.InspectUserDTO;
import com.zw.adas.domain.report.inspectuser.InspectUserQuery;
import com.zw.platform.util.common.PageGridBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author wanxing
 * @Title: 巡检用户
 * @date 2020/12/3017:39
 */
public interface InspectUserService {
    /**
     * 分页接口
     * @param query
     * @return
     */
    PageGridBean getListByKeyWord(InspectUserQuery query);

    /**
     * 导出
     * @param response
     * @param query
     * @throws IOException
     */
    void export(HttpServletResponse response, InspectUserQuery query) throws IOException;

    /**
     * 通过Id获取
     * @param id
     * @return
     */
    InspectUserDTO getById(String id);

    /**
     * 更新过期标识
     * @param id
     */
    void updateAnswerById(String id);

    /**
     * 更新数据库
     * @param inspectUser
     */
    void update(InspectUserDTO inspectUser);

    /**
     * 添加
     * @param inspectUserDTO
     */
    void add(InspectUserDTO inspectUserDTO);

    /**
     * 巡检应答
     * @param type type 应答类型，type:1弹窗应答（多个人可以应答），2：列表应答（只允许应答一次）
     * @param inspectUserDTO
     * @param image
     * @throws IOException
     */
    void updateAndAnswer(Integer type, InspectUserDTO inspectUserDTO, MultipartFile image)
        throws IOException;

    /**
     * 组装用户信息
     * @param inspectUserDTO
     * @return
     */
    InspectUserDTO generateUserInfo(InspectUserDTO inspectUserDTO);
}
