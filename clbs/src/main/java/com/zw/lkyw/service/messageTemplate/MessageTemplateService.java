package com.zw.lkyw.service.messageTemplate;

import com.github.pagehelper.Page;
import com.zw.lkyw.domain.messageTemplate.MessageTemplateBean;
import com.zw.lkyw.domain.messageTemplate.MessageTemplateForm;
import com.zw.lkyw.domain.messageTemplate.MessageTemplateInfo;
import com.zw.lkyw.domain.messageTemplate.MessageTemplateQuery;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 下发消息模板业务层
 * @author XK on 2019/12/26
 */

public interface MessageTemplateService {

    Page<MessageTemplateInfo> findMessageTemplate(MessageTemplateQuery query);

    JsonResultBean addTemplate(MessageTemplateForm info, String ipAddress);

    JsonResultBean deleteTemplate(List<String> templateIds, String ipAddress);

    JsonResultBean updateTemplate(MessageTemplateBean form, String ipAddress);

    List<MessageTemplateInfo> findTemplateById(String id);

    /**
     * 生成导入模板
     */
    void generateMessageTemplate(HttpServletResponse response) throws Exception;

    Map<String, Object> importTemplate(MultipartFile multipartFile, String ipAddress) throws Exception;

    void exportTemplate(String fuzzyParam, HttpServletResponse response) throws Exception;
}
