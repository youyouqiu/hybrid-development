package com.zw.platform.service.reportManagement;

import com.github.pagehelper.Page;
import com.zw.platform.domain.multimedia.Media;
import com.zw.platform.domain.multimedia.form.MediaForm;
import com.zw.platform.domain.multimedia.query.MediaQuery;
import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletResponse;

/**
 * @author wangying
 */
public interface MediaService {
    /**
     * 查询
     * @param query
     * @return
     */
    Page<Media> findMedia(MediaQuery query);

    /**
     * 新增多媒体
     * @param form
     * @return
     */
    boolean addMedia(MediaForm form);

    /**
     * 根据id删除多媒体
     * @param id
     * @return
     */
    boolean deleteById(String id) throws Exception;

    boolean addZipMedia(MediaForm mediaForm);

    /**
     * 根据多媒体ID,查询数据
     * @param id id
     * @return Media
     */
    Media getMedia(String id);

    /**
     * @param id          多媒体ID
     * @param description 描述
     * @return
     */
    JsonResultBean updateMediaDescription(String id, String description);

    void downMedia(String mediaUrl, String fileName, HttpServletResponse response);
}
