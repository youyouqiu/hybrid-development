package com.zw.platform.repository.realTimeVideo;

import com.zw.platform.domain.realTimeVideo.VideoTrafficInfo;

public interface VideoFlowDao {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table zw_m_video_flow
     *
     * @mbggenerated
     */
    int deleteByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table zw_m_video_flow
     *
     * @mbggenerated
     */
    int insert(VideoTrafficInfo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table zw_m_video_flow
     *
     * @mbggenerated
     */
    int insertSelective(VideoTrafficInfo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table zw_m_video_flow
     *
     * @mbggenerated
     */
    VideoTrafficInfo selectByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table zw_m_video_flow
     *
     * @mbggenerated
     */
    int updateByPrimaryKeySelective(VideoTrafficInfo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table zw_m_video_flow
     *
     * @mbggenerated
     */
    int updateByPrimaryKey(VideoTrafficInfo record);
}