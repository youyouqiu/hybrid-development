package com.zw.lkyw.repository.mysql.messageTemplate;

import com.zw.lkyw.domain.messageTemplate.MessageTemplateBean;
import com.zw.lkyw.domain.messageTemplate.MessageTemplateForm;
import com.zw.lkyw.domain.messageTemplate.MessageTemplateInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author XK
 * @date 2019/12/26
 */
public interface MessageTemplateDao {

    /**
     * 新增模板
     * @param messageTemplateForm info
     */
    void addMessageTemplate(MessageTemplateForm messageTemplateForm);

    /**
     * 搜索消息模板
     * @param simpleQueryParam 模糊搜索消息内容
     * @return list
     */
    List<MessageTemplateInfo> findMessageTemplate(@Param("simpleQueryParam") String simpleQueryParam,
                                                  @Param("status") Integer status);

    /**
     * 精确查找消息内容相同的下发消息模板
     * @param simpleQueryParam
     * @return
     */
    List<MessageTemplateInfo> accurateFindMessageTemplate(@Param("simpleQueryParam") String simpleQueryParam);

    /**
     * 批量新增模板
     * @param messageTemplateForms list
     * @return bool
     */
    boolean addTemplateList(@Param("messageTemplateForms") List<MessageTemplateForm> messageTemplateForms);

    /**
     * 删除模板
     * @param templateIds list
     * @return bool
     */
    boolean deleteTemplate(@Param("templateIds") List<String> templateIds);

    /**
     * 修改消息模板信息
     * @param info 修改info
     * @return bool
     */
    boolean updateTemplate(@Param("info") MessageTemplateBean info);


    /**
     * 通过id 查找消息模板
     * @param ids 模板id
     * @return list
     */
    List<MessageTemplateInfo> findTemplatesById(@Param("ids") List<String> ids);
}
