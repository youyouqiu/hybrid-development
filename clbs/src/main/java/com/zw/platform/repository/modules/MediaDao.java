package com.zw.platform.repository.modules;

import com.zw.platform.domain.multimedia.Media;
import com.zw.platform.domain.multimedia.form.MediaForm;
import com.zw.platform.domain.multimedia.query.MediaQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * <p> Title: 多媒体Dao </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team: ZhongWeiTeam
 * </p>
 *
 * @version 1.0
 * @author: wangying
 * @date 2017年4月7日上午9:07:35
 */
public interface MediaDao {

    /**
     * 查询多媒体
     */
    List<Media> findMedia(MediaQuery query);

    /**
     * 新增多媒体
     *
     * @param form
     * @return
     */
    boolean addMedia(MediaForm form);

    /**
     * 根据id删除多媒体
     *
     * @param id
     * @return
     */
    boolean deleteById(String id);

    /**
     * 查询风控证据文件路径
     *
     * @param riskId
     * @return
     */
    String queryMediaPathByRiskId(String riskId);

    /**
     * 转码成功后,更新status,表示视频可以用
     *
     * @param mediaName 文件名称
     * @return result
     */
    boolean updateMediaStatus(String mediaName);

    /**
     * 获取已经打包的终端证据
     *
     * @param downLoadId 风险id OR 事件Id
     * @param isEvent    是否事件
     * @return result
     */
    Map<String, String> hasTerminalEvidece(@Param("downLoadId") String downLoadId, @Param("isEvent") boolean isEvent);

    List<MediaForm> queryAll();

    String queryMediaUrlById(String id);

    String queryMediaUrlNewById(String id);

    List<MediaForm> queryRiskEvidenceByRiskId(String riskId);

    MediaForm queryZipRiskEvidenceByRiskId(String riskId);

    MediaForm findById(String id);

    /**
     * 获取多媒体数据
     * @param id id
     * @return Media
     */
    Media getMediaByMediaId(String id);

    /**
     * 修改多媒体备注
     * @param id id
     * @param description description
     * @return true: 成功, false: 失败
     */
    boolean updateMediaDescription(@Param("id") String id, @Param("description") String description);

}
