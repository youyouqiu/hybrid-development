INSERT INTO `clbs`.`zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`) VALUES ('f677c7a5-89a6-4099-be6d-e39de97872a9', NULL, '数据清理', '0', NULL, 'dataClean', '/m/dataClean/list', NULL, 'f9c65d06-f40e-11e6-bc64-92361f002671', '118', '1', '1', NULL, NULL, NULL, NULL, NULL, '11008', '1');
CREATE TABLE `zw_m_data_clean_setting` (
  `id` varchar(64) NOT NULL,
  `positional_time` smallint(4) DEFAULT NULL COMMENT '定位删除时间',
  `alarm_time` smallint(4) DEFAULT NULL COMMENT '报警删除时间',
  `media_time` smallint(4) DEFAULT NULL COMMENT '多媒体删除时间',
  `log_time` smallint(4) DEFAULT NULL COMMENT '日志删除时间',
  `spot_check_time` smallint(4) DEFAULT NULL COMMENT '视频抽查删除时间',
  `time` varchar(10) DEFAULT NULL COMMENT '设置的定时清理时间 11:22',
  `clean_type` varchar(10) DEFAULT NULL COMMENT '清理的设置（逗号分隔）1：定位 2：报警 3：多媒体 4：日志 5：视频抽查'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据清理设置';
INSERT INTO `clbs`.`zw_m_data_clean_setting` (`id`, `positional_time`, `alarm_time`, `media_time`, `log_time`, `spot_check_time`, `time`, `clean_type`) VALUES ('08fc3cbb-d053-4038-b690-eea4a9d988df', '12', '12', '12', '12', '12', '0:0', '1,2,3,4,5');
# 多媒体增加 “上传时间”，“事件项”字段（事件项字段原本已有）
ALTER TABLE `zw_m_media` ADD  `upload_time` datetime DEFAULT NULL COMMENT '多媒体上传时间' AFTER `flag`;
# 终端新增字段
ALTER TABLE `zw_m_terminal_type` MODIFY COLUMN `audio_format`  smallint(6) DEFAULT NULL COMMENT '是否支持视频选择(是)该字段才有效; 实时流音频格式 0:ADPCMA; 1:G726-8K; 2:G726-16K; 3:G726-24K; 4:G726-32K; 5:G726-40K;';
ALTER TABLE `zw_m_terminal_type` ADD COLUMN `sampling_rate`  smallint(6) DEFAULT NULL COMMENT '实时流采样率 0:8khz; 1:22.05khz; 2:44.1khz; 3:48khz';
ALTER TABLE `zw_m_terminal_type` ADD COLUMN `vocal_tract`  smallint(6) DEFAULT NULL COMMENT '实时流通道数 0：单声道  1：双声道';
ALTER TABLE `zw_m_terminal_type` ADD COLUMN `storage_audio_format`  smallint(6) DEFAULT NULL COMMENT '存储流音频格式 0:ADPCMA; 1:G726-8K; 2:G726-16K; 3:G726-24K; 4:G726-32K; 5:G726-40K;';
ALTER TABLE `zw_m_terminal_type` ADD COLUMN `storage_sampling_rate`  smallint(6) DEFAULT NULL COMMENT '存储流采样率 0:8khz; 1:22.05khz; 2:44.1khz; 3:48khz';
ALTER TABLE `zw_m_terminal_type` ADD COLUMN `storage_vocal_tract`  smallint(6) DEFAULT NULL COMMENT '存储流通道数 0：单声道  1：双声道';
#安徽晶太报表
#菜单
INSERT INTO `clbs`.`zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`) VALUES ('bef2a9d5-6e5b-4538-aa8d-c0f00803aa6e', NULL, '车辆状态报表', '0', NULL, 'alarmStatistic', '/page?listPage=modules/reportManagement/vehStateReport', NULL, '2fa6db13-1361-5f9b-f1e0-eef551c1566e', '150', '1', '1', NULL, NULL, NULL, NULL, NULL, '12005', '1');
INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES ('15c4339d-e802-487e-8b85-4c0225590ce6', 'bef2a9d5-6e5b-4538-aa8d-c0f00803aa6e', 'cn=ROLE_ADMIN,ou=Groups', '2020-06-17 17:27:34', 'admin', NULL, NULL, '1', '1');
INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES ('e7b1c28e-707a-4767-83f7-467015c66065', 'bef2a9d5-6e5b-4538-aa8d-c0f00803aa6e', 'cn=POWER_USER,ou=Groups', '2020-06-17 17:55:29', 'admin', NULL, NULL, '1', '1');
#车辆状态报表
CREATE TABLE `zw_s_orgnization_alarms` (
  `oid` char(36) NOT NULL COMMENT '车辆所属企业UUID',
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '日期',
  `vid` char(36) NOT NULL COMMENT '车辆id',
  `online_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '车辆在线状态  1: 在线  0: 离线',
  `offline_status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '车辆是否离线超过24小时  1: 是  0: 否',
  `msg_num` int(11) NOT NULL DEFAULT '0' COMMENT '下发短信数',
  `alarm_all` int(11) NOT NULL DEFAULT '0' COMMENT '报警总数',
  `alarm_speed` int(11) NOT NULL DEFAULT '0' COMMENT '超速报警数量',
  `alarm_tired` int(11) NOT NULL DEFAULT '0' COMMENT '疲劳驾驶报警数量',
  `alarm_line` int(11) NOT NULL DEFAULT '0' COMMENT '不按规定路线行驶报警数量',
  `alarm_dawn` int(11) NOT NULL DEFAULT '0' COMMENT '凌晨2-5点行驶报警数量',
  `alarm_camera` int(11) NOT NULL DEFAULT '0' COMMENT '遮挡摄像头报警数量',
  `alarm_other` int(11) NOT NULL DEFAULT '0' COMMENT '其它报警数量',
  PRIMARY KEY (`time`,`oid`,`vid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

