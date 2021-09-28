#超速统计报表
INSERT INTO `clbs`.`zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('c60343ee-9686-11ea-bb37-0242ac130002', NULL, '超速统计报表', '0', NULL, '', '/cb/cbReportManagement/overSpeed/list', NULL, 'd41f83a4-4dd9-11e8-9c2d-fa7ae01bbebc', '1308', '1', '1', NULL, NULL, NULL, NULL, NULL, '1308', '1');

INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('c6034650-9686-11ea-bb37-0242ac130002', 'c60343ee-9686-11ea-bb37-0242ac130002', 'cn=ROLE_ADMIN,ou=Groups', '2020-05-15 16:44:08', 'admin', NULL, NULL, '1', '1');


INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('c603477c-9686-11ea-bb37-0242ac130002', 'c60343ee-9686-11ea-bb37-0242ac130002', 'cn=POWER_USER,ou=Groups', '2020-05-15 16:44:08', 'admin', NULL, NULL, '1', '1');

#疲劳驾驶报警统计报表

INSERT INTO `clbs`.`zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('c6034934-9686-11ea-bb37-0242ac130002', NULL, '疲劳驾驶报警统计报表', '0', NULL, '', '/cb/cbReportManagement/fatigueDriving/list', NULL, 'd41f83a4-4dd9-11e8-9c2d-fa7ae01bbebc', '1309', '1', '1', NULL, NULL, NULL, NULL, NULL, '1309', '1');

INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('c6035dca-9686-11ea-bb37-0242ac130002', 'c6034934-9686-11ea-bb37-0242ac130002', 'cn=ROLE_ADMIN,ou=Groups', '2020-05-15 16:44:08', 'admin', NULL, NULL, '1', '1');


INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('c6035f8c-9686-11ea-bb37-0242ac130002', 'c6034934-9686-11ea-bb37-0242ac130002', 'cn=POWER_USER,ou=Groups', '2020-05-15 16:44:08', 'admin', NULL, NULL, '1', '1');

#四川离线导出报表中心sql
CREATE TABLE `zw_m_offline_export` (
  `username` varchar(255) NOT NULL COMMENT '用户名',
  `module` varchar(255) NOT NULL COMMENT '模块名称',
  `business_id` varchar(11) NOT NULL COMMENT '业务ID,由paas_cloud提供',
  `query_condition` text NOT NULL COMMENT '查询条件',
  `file_name` varchar(255) DEFAULT NULL COMMENT '文件名',
  `create_date_time` datetime NOT NULL COMMENT '导出时间',
  `digest_id` varchar(64) NOT NULL COMMENT '摘要导出ID',
  `status` tinyint(255) NOT NULL COMMENT ' 0 待执行 1 执行中 2成功 3 失败',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `real_path` varchar(255) DEFAULT NULL COMMENT '文件路径',
  `file_size` int(11) DEFAULT NULL COMMENT '文件大小,单位(Byte)',
  PRIMARY KEY (`username`,`create_date_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



#交通部JT/T808-2011(扩展)协议的描述修改为null
UPDATE `clbs`.`zw_c_dictionary` SET `description` = null WHERE `id` = 'adc69570-0458-4f2e-a955-894703c5bd7f';
#2013中位  主动安全参数设置对应protocolo为1
UPDATE `clbs`.`zw_c_dictionary` SET `description` = 'activeSafety' WHERE `id` = '09d2dae9-0a68-41f8-a2c9-9c8c7a41a558';

alter table  zw_m_professionals_info modify column photograph VARCHAR(100);
#京标(平台参数设置)
alter table  zw_m_adas_platform_param_setting add column three_level_remind  tinyint(4) DEFAULT NULL COMMENT '三级报警提醒方式 0无,1闪烁,2提示音,3闪烁加提示音,4弹窗提醒,5短信提醒';
alter table  zw_m_adas_platform_param_setting add column three_level_deal_time_interval  int(11) DEFAULT NULL COMMENT '三级报警处理间隔时间';
alter table  zw_m_adas_platform_param_setting add column one_level_automatic_get  tinyint(4) DEFAULT NULL COMMENT '一级报警自动获取 （0未开启，1开启）';
alter table  zw_m_adas_platform_param_setting add column two_level_automatic_get  tinyint(4) DEFAULT NULL COMMENT '二级报警自动获取 （0未开启，1开启）';
alter table  zw_m_adas_platform_param_setting add column three_level_automatic_get  tinyint(4) DEFAULT NULL COMMENT '三级报警自动获取 （0未开启，1开启）';
alter table  zw_m_adas_platform_param_setting add column one_level_automatic_deal  tinyint(4) DEFAULT NULL COMMENT '一级报警自动处理 （0未开启，1开启）';
alter table  zw_m_adas_platform_param_setting add column two_level_automatic_deal  tinyint(4) DEFAULT NULL COMMENT '二级报警自动处理 （0未开启，1开启）';
alter table  zw_m_adas_platform_param_setting add column three_level_automatic_deal  tinyint(4) DEFAULT NULL COMMENT '三级报警自动处理 （0未开启，1开启）';
#京标(主动安全参数设置表添加)
DROP TABLE IF EXISTS `zw_m_adas_jing_alarm_param_setting`;
CREATE TABLE `zw_m_adas_jing_alarm_param_setting` (
  `id` varchar(255) NOT NULL COMMENT '表id',
  `risk_function_id` int(11) DEFAULT NULL COMMENT '风险时间(function_id)'',',
  `parameter_id` varchar(255) DEFAULT NULL COMMENT '参数id（下发8103）',
  `vehicle_id` varchar(255) DEFAULT NULL COMMENT '车辆id',
  `alarm_level` varchar(32) DEFAULT NULL COMMENT '报警级别',
  `alarm_volume` varchar(32) DEFAULT NULL COMMENT '报警提示音量',
  `speech` tinyint(10) DEFAULT NULL COMMENT '语音播报：0不播报，1播报，默认1',
  `alarm_video_duration` int(11) DEFAULT NULL COMMENT '报警视频时长 单位秒，取值范围0~300：0不采集视频，默认值6',
  `video_resolution` varchar(32) DEFAULT NULL COMMENT '报警视频分辨率',
  `photograph_number` int(11) DEFAULT NULL COMMENT '报警照片张数',
  `camera_resolution` varchar(32) DEFAULT NULL COMMENT '照片分辨率',
  `photograph_time` int(11) DEFAULT NULL COMMENT '拍照间隔',
  `speed_limit` int(11) DEFAULT NULL COMMENT '速度阈值',
  `duration_threshold` int(11) DEFAULT NULL COMMENT '判断持续时长阀值',
  `protocol_type` int(11) DEFAULT NULL COMMENT '协议类型',
  `param_type` int(11) DEFAULT NULL COMMENT '报警指令类型',
  `flag` bigint(10) DEFAULT NULL COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#京标(参数默认设置)
INSERT INTO `clbs`.`zw_m_adas_jing_alarm_param_setting`(`id`, `risk_function_id`, `parameter_id`, `vehicle_id`, `alarm_level`, `alarm_volume`, `speech`, `alarm_video_duration`, `video_resolution`, `photograph_number`, `camera_resolution`, `photograph_time`, `speed_limit`, `duration_threshold`, `protocol_type`, `param_type`, `flag`) VALUES ('0be2f2ac-394f-4c7c-8cd6-5cd95092a683', 246505, '0xF517', 'default', '1', '7', 1, 6, '0x01', 4, '0x01', 2, 50, 0, 24, 51, 1);
INSERT INTO `clbs`.`zw_m_adas_jing_alarm_param_setting`(`id`, `risk_function_id`, `parameter_id`, `vehicle_id`, `alarm_level`, `alarm_volume`, `speech`, `alarm_video_duration`, `video_resolution`, `photograph_number`, `camera_resolution`, `photograph_time`, `speed_limit`, `duration_threshold`, `protocol_type`, `param_type`, `flag`) VALUES ('113f62ba-881f-4164-a18b-36e26387d532', 246407, '0xF527', 'default', '1', '7', 1, 6, '0x01', 4, '0x01', 2, 50, 0, 24, 52, 1);
INSERT INTO `clbs`.`zw_m_adas_jing_alarm_param_setting`(`id`, `risk_function_id`, `parameter_id`, `vehicle_id`, `alarm_level`, `alarm_volume`, `speech`, `alarm_video_duration`, `video_resolution`, `photograph_number`, `camera_resolution`, `photograph_time`, `speed_limit`, `duration_threshold`, `protocol_type`, `param_type`, `flag`) VALUES ('203ea1b2-241d-4739-8d29-8553e07041cb', 246404, '0xF524', 'default', '1', '7', 1, 6, '0x01', 4, '0x01', 2, 50, 0, 24, 52, 1);
INSERT INTO `clbs`.`zw_m_adas_jing_alarm_param_setting`(`id`, `risk_function_id`, `parameter_id`, `vehicle_id`, `alarm_level`, `alarm_volume`, `speech`, `alarm_video_duration`, `video_resolution`, `photograph_number`, `camera_resolution`, `photograph_time`, `speed_limit`, `duration_threshold`, `protocol_type`, `param_type`, `flag`) VALUES ('21b3a95c-80ea-4c32-bd44-50645f1a342e', 246502, '0xF512', 'default', '1', '7', 1, 6, '0x01', 4, '0x01', 2, 50, 0, 24, 51, 1);
INSERT INTO `clbs`.`zw_m_adas_jing_alarm_param_setting`(`id`, `risk_function_id`, `parameter_id`, `vehicle_id`, `alarm_level`, `alarm_volume`, `speech`, `alarm_video_duration`, `video_resolution`, `photograph_number`, `camera_resolution`, `photograph_time`, `speed_limit`, `duration_threshold`, `protocol_type`, `param_type`, `flag`) VALUES ('5245aab5-5899-4861-b47c-5e9ca4df6d0b', 246506, '0xF516', 'default', '1', '7', 1, 6, '0x01', 4, '0x01', 2, 50, 0, 24, 51, 1);
INSERT INTO `clbs`.`zw_m_adas_jing_alarm_param_setting`(`id`, `risk_function_id`, `parameter_id`, `vehicle_id`, `alarm_level`, `alarm_volume`, `speech`, `alarm_video_duration`, `video_resolution`, `photograph_number`, `camera_resolution`, `photograph_time`, `speed_limit`, `duration_threshold`, `protocol_type`, `param_type`, `flag`) VALUES ('56bd3c9f-3839-4ccf-8e68-a3ca71fc46df', 246503, '0xF514', 'default', '1', '7', 1, 6, '0x01', 4, '0x01', 2, 50, 0, 24, 51, 1);
INSERT INTO `clbs`.`zw_m_adas_jing_alarm_param_setting`(`id`, `risk_function_id`, `parameter_id`, `vehicle_id`, `alarm_level`, `alarm_volume`, `speech`, `alarm_video_duration`, `video_resolution`, `photograph_number`, `camera_resolution`, `photograph_time`, `speed_limit`, `duration_threshold`, `protocol_type`, `param_type`, `flag`) VALUES ('645a09ea-d694-42c4-937d-117c64b7c8b0', 246501, '0xF511', 'default', '1', '7', 1, 6, '0x01', 4, '0x01', 2, 50, 0, 24, 51, 1);
INSERT INTO `clbs`.`zw_m_adas_jing_alarm_param_setting`(`id`, `risk_function_id`, `parameter_id`, `vehicle_id`, `alarm_level`, `alarm_volume`, `speech`, `alarm_video_duration`, `video_resolution`, `photograph_number`, `camera_resolution`, `photograph_time`, `speed_limit`, `duration_threshold`, `protocol_type`, `param_type`, `flag`) VALUES ('6f4acef2-f395-493c-b89a-5795d2043ccd', 246409, '0xF529', 'default', '1', '7', 1, 6, '0x01', 4, '0x01', 2, 50, 0, 24, 52, 1);
INSERT INTO `clbs`.`zw_m_adas_jing_alarm_param_setting`(`id`, `risk_function_id`, `parameter_id`, `vehicle_id`, `alarm_level`, `alarm_volume`, `speech`, `alarm_video_duration`, `video_resolution`, `photograph_number`, `camera_resolution`, `photograph_time`, `speed_limit`, `duration_threshold`, `protocol_type`, `param_type`, `flag`) VALUES ('8626f8f3-bcf6-43f2-ac1f-1904b41fa1f9', 246518, '0xF515', 'default', '1', '7', 1, 6, '0x01', 4, '0x01', 2, 50, 0, 24, 51, 1);
INSERT INTO `clbs`.`zw_m_adas_jing_alarm_param_setting`(`id`, `risk_function_id`, `parameter_id`, `vehicle_id`, `alarm_level`, `alarm_volume`, `speech`, `alarm_video_duration`, `video_resolution`, `photograph_number`, `camera_resolution`, `photograph_time`, `speed_limit`, `duration_threshold`, `protocol_type`, `param_type`, `flag`) VALUES ('8794cf3c-c44b-46e2-bfbc-c59b17a1cd1c', 246402, '0xF523', 'default', '1', '7', 1, 6, '0x01', 4, '0x01', 2, 50, 0, 24, 52, 1);
INSERT INTO `clbs`.`zw_m_adas_jing_alarm_param_setting`(`id`, `risk_function_id`, `parameter_id`, `vehicle_id`, `alarm_level`, `alarm_volume`, `speech`, `alarm_video_duration`, `video_resolution`, `photograph_number`, `camera_resolution`, `photograph_time`, `speed_limit`, `duration_threshold`, `protocol_type`, `param_type`, `flag`) VALUES ('af70e9bf-8c22-4685-bb46-66ecd1a3b4c0', 246403, '0xF522', 'default', '1', '7', 1, 6, '0x01', 4, '0x01', 2, 50, 0, 24, 52, 1);
INSERT INTO `clbs`.`zw_m_adas_jing_alarm_param_setting`(`id`, `risk_function_id`, `parameter_id`, `vehicle_id`, `alarm_level`, `alarm_volume`, `speech`, `alarm_video_duration`, `video_resolution`, `photograph_number`, `camera_resolution`, `photograph_time`, `speed_limit`, `duration_threshold`, `protocol_type`, `param_type`, `flag`) VALUES ('bf6af10d-f5a1-4a88-bd26-5672b3d19eca', 246504, '0xF513', 'default', '1', '7', 1, 6, '0x01', 4, '0x01', 2, 50, 0, 24, 51, 1);
INSERT INTO `clbs`.`zw_m_adas_jing_alarm_param_setting`(`id`, `risk_function_id`, `parameter_id`, `vehicle_id`, `alarm_level`, `alarm_volume`, `speech`, `alarm_video_duration`, `video_resolution`, `photograph_number`, `camera_resolution`, `photograph_time`, `speed_limit`, `duration_threshold`, `protocol_type`, `param_type`, `flag`) VALUES ('dfd25b1e-b9c0-414d-ae91-e09cc35b8b2c', 246406, '0xF526', 'default', '1', '7', 1, 6, '0x01', 4, '0x01', 2, 50, 0, 24, 52, 1);
INSERT INTO `clbs`.`zw_m_adas_jing_alarm_param_setting`(`id`, `risk_function_id`, `parameter_id`, `vehicle_id`, `alarm_level`, `alarm_volume`, `speech`, `alarm_video_duration`, `video_resolution`, `photograph_number`, `camera_resolution`, `photograph_time`, `speed_limit`, `duration_threshold`, `protocol_type`, `param_type`, `flag`) VALUES ('dfea9473-7c13-4b2a-a0c2-8e8e3ca770d6', 246408, '0xF528', 'default', '1', '7', 1, 6, '0x01', 4, '0x01', 2, 50, 0, 24, 52, 1);
INSERT INTO `clbs`.`zw_m_adas_jing_alarm_param_setting`(`id`, `risk_function_id`, `parameter_id`, `vehicle_id`, `alarm_level`, `alarm_volume`, `speech`, `alarm_video_duration`, `video_resolution`, `photograph_number`, `camera_resolution`, `photograph_time`, `speed_limit`, `duration_threshold`, `protocol_type`, `param_type`, `flag`) VALUES ('e7465600-2f84-4ef7-b06b-eb4a73ed2ea4', 246405, '0xF525', 'default', '1', '7', 1, 6, '0x01', 4, '0x01', 2, 50, 0, 24, 52, 1);
INSERT INTO `clbs`.`zw_m_adas_jing_alarm_param_setting`(`id`, `risk_function_id`, `parameter_id`, `vehicle_id`, `alarm_level`, `alarm_volume`, `speech`, `alarm_video_duration`, `video_resolution`, `photograph_number`, `camera_resolution`, `photograph_time`, `speed_limit`, `duration_threshold`, `protocol_type`, `param_type`, `flag`) VALUES ('fece05dd-9e52-4de3-bdbc-2421c1b3076d', 246401, '0xF521', 'default', '1', '7', 1, 6, '0x01', 4, '0x01', 2, 50, 0, 24, 52, 1);


#离线下载中心报表
CREATE TABLE `zw_m_offline_export` (
 `username` varchar(255) NOT NULL COMMENT '用户名',
 `module` varchar(255) NOT NULL COMMENT '模块名称',
 `business_id` varchar(11) NOT NULL COMMENT '业务ID,由paas_cloud提供',
 `query_condition` text NOT NULL COMMENT '查询条件',
 `file_name` varchar(255) DEFAULT NULL COMMENT '文件名',
 `create_date_time` datetime NOT NULL COMMENT '导出时间',
 `digest_id` varchar(64) NOT NULL COMMENT '摘要导出ID',
 `status` tinyint(255) NOT NULL COMMENT ' 0 待执行 1 执行中 2成功 3 失败',
 `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
 `real_path` varchar(255) DEFAULT NULL COMMENT '文件路径',
 `file_size` int(11) DEFAULT NULL COMMENT '文件大小,单位(Byte)',
 PRIMARY KEY (`username`,`create_date_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


#京标新增事件
ALTER TABLE zw_m_vehicle_adas_event ADD COLUMN `vehicle_offset` int(11) DEFAULT 0 COMMENT '车道偏离';
ALTER TABLE zw_m_vehicle_adas_event ADD COLUMN `blind_spot_monitoring` int(11) DEFAULT 0 COMMENT '盲区监测';

#京标协议添加
INSERT INTO `clbs`.`zw_c_dictionary` (`id`, `pid`, `code`, `value`, `type`, `description`) VALUES ('a0352fd9-aac1-46b7-8e22-cd769008c693', NULL, '24', '交通部JT/T808-2019(京标)', '808', 'activeSafety');

#主动安全按需求调整报警等级初始化值
UPDATE  `zw_m_risk_event` SET  `one_level` = 5, `two_level` = 8 WHERE `id` = '078bf512-abb4-11ea-bdcd-000c29920fdc';
UPDATE  `zw_m_risk_event` SET `interval_time` = 30,  `one_level` = 4, `two_level` = 9 WHERE `id` = '078e7900-abb4-11ea-bdcd-000c29920fdc';
UPDATE  `zw_m_risk_event` SET  `one_level` = 12 WHERE `id` = '0796e134-abb4-11ea-bdcd-000c29920fdc';
UPDATE  `zw_m_risk_event` SET  `one_level` = 12 WHERE `id` = '079c4fb6-abb4-11ea-bdcd-000c29920fdc';
UPDATE  `zw_m_risk_event` SET  `interval_time` = 20, `one_level` = 12  WHERE `id` = '07a7ab61-abb4-11ea-bdcd-000c29920fdc';
UPDATE  `zw_m_risk_event` SET  `one_level` = 12 WHERE `id` = '07a489c1-abb4-11ea-bdcd-000c29920fdc';
UPDATE  `zw_m_risk_event` SET  `one_level` = 12, `two_level` = 12 WHERE `id` = '0780ae8b-abb4-11ea-bdcd-000c29920fdc';
UPDATE  `zw_m_risk_event` SET  `one_level` = 12, `two_level` = 12 WHERE `id` = '07837f78-abb4-11ea-bdcd-000c29920fdc';
UPDATE  `zw_m_risk_event` SET  `one_level` = 12, `two_level` = 12 WHERE `id` = '078684fa-abb4-11ea-bdcd-000c29920fdc';
UPDATE  `zw_m_risk_event` SET  `one_level` = 12, `two_level` = 12 WHERE `id` = '078952fc-abb4-11ea-bdcd-000c29920fdc';
UPDATE  `zw_m_risk_event` SET  `one_level` = 9 WHERE `id` = '077db7e2-abb4-11ea-bdcd-000c29920fdc';



