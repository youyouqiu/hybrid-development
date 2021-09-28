DROP TABLE IF EXISTS `zw_m_skill_categories`;
CREATE TABLE `zw_m_skill_categories` (
  `id` varchar(64) NOT NULL COMMENT '技能类别',
  `name` varchar(64) DEFAULT NULL COMMENT '名称',
  `remark` varchar(120) DEFAULT NULL COMMENT '备注',
  `flag` smallint(6) DEFAULT '1',
  `create_data_time` datetime DEFAULT NULL,
  `create_data_username` varchar(45) DEFAULT NULL,
  `update_data_time` datetime DEFAULT NULL,
  `update_data_username` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='技能类别表';

DROP TABLE IF EXISTS `zw_m_intercom_model`;
CREATE TABLE `zw_m_intercom_model` (
  `id` varchar(64) NOT NULL COMMENT '对讲机型ID',
  `name` varchar(50) NOT NULL COMMENT '对讲机型名称',
  `original_model_id` bigint(20) DEFAULT NULL COMMENT '原始机型ID',
  `flag` smallint(6) NOT NULL DEFAULT '1',
  `create_data_time` datetime DEFAULT NULL,
  `create_data_username` varchar(64) DEFAULT NULL,
  `update_data_time` datetime DEFAULT NULL,
  `update_data_username` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='对讲机型表';

DROP TABLE IF EXISTS `zw_m_original_model`;
CREATE TABLE `zw_m_original_model` (
  `index` bigint(20) NOT NULL COMMENT '设备类型ID',
  `model_id` varchar(100) DEFAULT NULL COMMENT '设备类型',
  `model_name` varchar(100) DEFAULT NULL COMMENT '设备类型别名',
  `type` smallint(6) DEFAULT NULL COMMENT '设备类型: 0:普通对讲设备;1:调度台;2:音视频的设备;3:视频设备',
  `audio_ability` smallint(6) DEFAULT NULL COMMENT '是否支持语音对讲功能: 0:不支持;1:支持',
  `gis_ability` smallint(6) DEFAULT NULL COMMENT '是否支持GIS功能: 0:不支持;1:支持',
  `sensor_ability` smallint(6) DEFAULT NULL COMMENT '是否支持传感功能: 1:支持;0:不支持',
  `video_ability` smallint(6) DEFAULT NULL COMMENT '是否支持视频功能 :1:支持 ;0:不支持',
  `knob_num` smallint(6) DEFAULT NULL COMMENT '硬件旋钮个数',
  `max_group_num` smallint(6) DEFAULT NULL COMMENT '最大支持群组数',
  `max_friend_num` smallint(6) DEFAULT NULL COMMENT '最大支持好友数',
  `seneor_ctl_mx` int(6) DEFAULT NULL COMMENT '传感控制器支持个数',
  `serial485_num` smallint(6) DEFAULT NULL COMMENT '485串口个数',
  `serial232_num` smallint(6) DEFAULT NULL COMMENT '232串口个数',
  `temp_group_enable` smallint(6) DEFAULT NULL COMMENT '是否支持创建临时组功能:1:支持 ;0:不支持',
  `intercept_enable` smallint(6) DEFAULT NULL COMMENT '是否支持监听功能1:支持;0:不支持',
  `intercept_num` smallint(6) DEFAULT NULL COMMENT '最大支持监听组个数',
  `patrol_enable` smallint(6) DEFAULT NULL COMMENT '是否支持巡更: 1:支持; 0:不支持',
  `fence_enable` smallint(6) DEFAULT NULL COMMENT '是否支持围栏功能: 1:支持; 0:不支持',
  `audio_conference_enable` smallint(6) DEFAULT NULL COMMENT '是否支持语音会议 : 1:支持; 0:不支持',
  `video_conference_enable` smallint(6) DEFAULT NULL COMMENT '是否支持视频会议功能1:支持 0:不支持',
  `video_call_enable` smallint(6) DEFAULT NULL COMMENT '是否支持视频电话功能 1:支持 0:不支持',
  `send_text_enable` smallint(6) DEFAULT NULL COMMENT '是否支持发送IM文本消息 1:支持 0:不支持',
  `send_image_enable` smallint(6) DEFAULT NULL COMMENT '是否支持发送IM图片消息1:支持 0:不支持',
  `send_audio_enable` smallint(6) DEFAULT NULL COMMENT '是否支持发送离线语音消息1:支持 0:不支持',
  `support_locate` smallint(6) DEFAULT NULL COMMENT '是否支持定位功能',
  `video_func_enable` smallint(6) DEFAULT NULL COMMENT '是否支持实时视频功能 1:支持 0:不支持',
  `chanls_num` smallint(6) DEFAULT NULL COMMENT '视频路数',
  `comments` varchar(250) DEFAULT NULL COMMENT '备注',
  `flag` smallint(6) NOT NULL DEFAULT '1',
  `create_data_time` datetime DEFAULT NULL,
  `create_data_username` varchar(64) DEFAULT NULL,
  `update_data_time` datetime DEFAULT NULL,
  `update_data_username` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='原始机型表';


DROP TABLE IF EXISTS `zw_m_people_scheduled_info`;
CREATE TABLE `zw_m_people_scheduled_info` (
  `id` varchar(64) NOT NULL COMMENT 'id（人员排班信息）',
  `scheduled_info_id` varchar(64) NOT NULL COMMENT '排班id',
  `people_id` varchar(64) NOT NULL COMMENT '人员id',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `date_duplicate_type` varchar(20) NOT NULL COMMENT '日期重复类型（周一至周日（1至7）逗号分隔，8：每天；）',
  `flag` smallint(6) NOT NULL DEFAULT '1' COMMENT '0不显示、1显示',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `zw_m_people_designate_info`;
CREATE TABLE `zw_m_people_designate_info` (
  `id` varchar(64) NOT NULL COMMENT 'id（人员指派信息）',
  `designate_info_id` varchar(64) NOT NULL COMMENT '指派id',
  `people_id` varchar(64) NOT NULL COMMENT '人员id',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `date_duplicate_type` varchar(20) NOT NULL COMMENT '日期重复类型（周一至周日（1至7）逗号分隔，8：每天；）',
  `flag` smallint(6) NOT NULL DEFAULT '1' COMMENT '0不显示、1显示',
  PRIMARY KEY (`id`),
  KEY `inx_people_id` (`people_id`),
  KEY `idx_designate_id` (`designate_info_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `zw_m_leave_job_assignment`;
CREATE TABLE `zw_m_leave_job_assignment` (
  `id` varchar(64) NOT NULL COMMENT '离职人员之前的分组关系表',
  `assignment_id` varchar(64) DEFAULT NULL COMMENT '分组ID',
  `people_id` varchar(64) DEFAULT NULL COMMENT '人员ID',
  `flag` smallint(6) NOT NULL DEFAULT '1',
  `create_data_time` datetime DEFAULT NULL,
  `create_data_username` varchar(64) DEFAULT NULL,
  `update_data_time` datetime DEFAULT NULL,
  `update_data_username` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `zw_m_assignment_vehicle_index` (`assignment_id`,`people_id`,`flag`),
  KEY `idx_id_f` (`flag`,`people_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `zw_m_people_basic_info`;
CREATE TABLE `zw_m_people_basic_info` (
  `id` varchar(64) NOT NULL COMMENT '人员基础信息关联表',
  `people_id` varchar(64) DEFAULT NULL COMMENT '人员id',
  `basic_id` varchar(64) DEFAULT NULL COMMENT '基础信息id',
  `type` smallint(6) DEFAULT NULL COMMENT '类型  1：技能，2：驾照类别',
  `flag` smallint(6) DEFAULT '1',
  `create_data_time` datetime DEFAULT NULL,
  `create_data_username` varchar(45) DEFAULT NULL,
  `update_data_time` datetime DEFAULT NULL,
  `update_data_username` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `zw_m_call_number`;
CREATE TABLE `zw_m_call_number` (
  `call_number` int(11) NOT NULL,
  `person_call` bit(1) DEFAULT NULL COMMENT '个呼号码的状态（1代表可以使用，0代表已经被用了）',
  `group_call` bit(1) DEFAULT NULL COMMENT '组呼号码的状态（1代表可以使用，0代表已经被用了）',
  PRIMARY KEY (`call_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `zw_m_job_info`;
CREATE TABLE `zw_m_job_info` (
  `id` varchar(64) NOT NULL COMMENT '职位类别id',
  `job_name` varchar(50) NOT NULL COMMENT '职位类别名称',
  `job_icon_name` varchar(64) NOT NULL COMMENT '图标',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `flag` smallint(6) NOT NULL DEFAULT '1' COMMENT '0不显示、1显示',
  `create_data_time` datetime DEFAULT NULL,
  `create_data_username` varchar(45) DEFAULT NULL,
  `update_data_time` datetime DEFAULT NULL,
  `update_data_username` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `zw_m_skill`;
CREATE TABLE `zw_m_skill` (
  `id` varchar(64) NOT NULL COMMENT '技能',
  `name` varchar(64) DEFAULT NULL COMMENT '名称',
  `categories_id` varchar(64) DEFAULT NULL COMMENT '类别id',
  `remark` varchar(120) DEFAULT NULL COMMENT '备注',
  `flag` smallint(6) DEFAULT '1',
  `create_data_time` datetime DEFAULT NULL,
  `create_data_username` varchar(45) DEFAULT NULL,
  `update_data_time` datetime DEFAULT NULL,
  `update_data_username` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='技能表';

DROP TABLE IF EXISTS `zw_m_intercom_info`;
CREATE TABLE `zw_m_intercom_info` (
  `id` varchar(64) NOT NULL,
  `intercom_device_id` varchar(20) DEFAULT NULL COMMENT '对讲终端号(设备号)',
  `group_id` varchar(60) DEFAULT NULL COMMENT '组织ID',
  `simcard_id` varchar(60) DEFAULT NULL COMMENT 'sim卡ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '调度平台的USER_ID',
  `device_password` varchar(20) DEFAULT NULL COMMENT '对讲终端密码',
  `priority` smallint(6) DEFAULT NULL COMMENT '优先级',
  `customer_code` bigint(20) DEFAULT '1' COMMENT '客户代码: 默认为1',
  `number` varchar(20) DEFAULT NULL COMMENT '个呼号码，5位号码，开头不为0',
  `original_model_id` bigint(64) DEFAULT NULL COMMENT '原始机型ID',
  `text_enable` smallint(6) DEFAULT NULL COMMENT '文本信息: 1:支持 ;0:不支持',
  `image_enable` smallint(6) DEFAULT NULL COMMENT '是否支持图片消息 1:支持 0:不支持',
  `audio_enable` smallint(6) DEFAULT NULL COMMENT '是否支持离线语音消息 1:支持 0:不支持',
  `status` smallint(6) DEFAULT '0' COMMENT '生成状态: 0: 未生成; 1:生成成功; 2:生成失败',
  `record_enable` smallint(6) DEFAULT '0' COMMENT '是否录音: 0: 不录音; 1: 录音',
  `flag` smallint(6) DEFAULT '1',
  `create_data_time` datetime DEFAULT NULL,
  `create_data_username` varchar(45) DEFAULT NULL,
  `update_data_time` datetime DEFAULT NULL,
  `update_data_username` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `INTERCOM_OBJECT_INFO_index` (`flag`,`intercom_device_id`,`simcard_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='对讲对象';

DROP TABLE IF EXISTS `zw_m_intercom_iot_user`;
CREATE TABLE `zw_m_intercom_iot_user` (
  `user_name` varchar(255) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `call_number` int(11) DEFAULT NULL,
  `create_data_time` datetime DEFAULT NULL,
  `create_data_username` varchar(255) DEFAULT NULL,
  `update_data_time` datetime DEFAULT NULL,
  `update_data_username` varchar(255) DEFAULT NULL,
  `flag` smallint(6) DEFAULT NULL,
  PRIMARY KEY (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `zw_m_friend`;
CREATE TABLE `zw_m_friend` (
  `id` varchar(64) NOT NULL,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `friend_id` bigint(20) NOT NULL COMMENT '好友ID',
  `type` smallint(2) DEFAULT NULL COMMENT '类型: 0: 调度员; 1: 对讲对象',
  `flag` smallint(6) DEFAULT '1',
  `create_data_time` datetime DEFAULT NULL,
  `create_data_username` varchar(45) DEFAULT NULL,
  `update_data_time` datetime DEFAULT NULL,
  `update_data_username` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='好友关系表';

ALTER TABLE zw_m_config ADD COLUMN intercom_info_id varchar(64)  DEFAULT NULL COMMENT '对讲对象ID';
ALTER TABLE zw_m_assignment_vehicle ADD COLUMN knob_no  smallint(2)  DEFAULT NULL COMMENT '组旋钮位置编号';

ALTER TABLE zw_m_assignment ADD COLUMN sound_recording  smallint(2)  DEFAULT '0' COMMENT '是否录音， 1：录音 0：不';
ALTER TABLE zw_m_assignment ADD COLUMN intercom_group_id  bigint(20) DEFAULT NULL COMMENT '对讲群组id';
ALTER TABLE zw_m_assignment ADD COLUMN group_call_number  varchar(10) DEFAULT NULL COMMENT '组呼号码';
ALTER TABLE zw_m_assignment ADD COLUMN types  smallint(2) DEFAULT '0' COMMENT '分组类型（分组0，群组1）';
ALTER TABLE zw_m_people_info ADD COLUMN is_incumbency  smallint(6) DEFAULT '1' COMMENT '是否在职 0:离职； 2:在职  1:显示空白';
ALTER TABLE zw_m_people_info ADD COLUMN job_id  varchar(64) DEFAULT 'default' COMMENT '职位id';
ALTER TABLE zw_m_people_info ADD COLUMN blood_type_id  varchar(64) DEFAULT NULL COMMENT '血型id';
ALTER TABLE zw_m_people_info  CHANGE  nation  nation_id VARCHAR(60);
ALTER TABLE zw_m_people_info ADD COLUMN qualification_id varchar(64) DEFAULT NULL COMMENT '资格证id';
update zw_m_people_info  set job_id = 'default' where job_id<>'' and flag = 1;


-- 菜单管理
INSERT INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('bdd2f745-c05a-4d1d-ac01-6d06b8007b74', 'fa fa-th-list', '对讲管理', '1', NULL, 'talkbackManager', 'javascript:void(0);', NULL, '0', '17', '1', '1', NULL, NULL, NULL, NULL, NULL, '1700', '1');

INSERT INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('963289d9-6aab-11e6-8b77-86f30ca893d3', 'arrow fa fa-angle-right pull-right', '人员信息', '1', NULL, 'person', 'javascript:void(0);', NULL, 'bdd2f745-c05a-4d1d-ac01-6d06b8007b74', '171', '1', '1', NULL, NULL, NULL, NULL, NULL, '17001', '1');

INSERT INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('0e6e0b51-ff18-40e4-8fdb-fecf2ed95ddc', NULL, '技能管理', '0', NULL, 'personSkill', '/talkback/basicinfo/skill/list', NULL, '963289d9-6aab-11e6-8b77-86f30ca893d3', '1711', '1', '1', NULL, NULL, NULL, NULL, NULL, '170011', '1');

INSERT INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('b46dec1a-6a9e-11e6-8b77-86f30ca893d3', '', '对讲信息列表', '0', NULL, 'infolist', '/talkback/inconfig/infoinput/list', NULL, 'bdd2f745-c05a-4d1d-ac01-6d06b8007b74', '172', '1', '1', NULL, NULL, NULL, NULL, NULL, '17002', '1');

INSERT INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('fc4074c3-7b8a-4b77-b91b-338aedebee4d', '', '群组管理', '0', NULL, '', '/talkback/basicinfo/enterprise/assignment/list', NULL, 'bdd2f745-c05a-4d1d-ac01-6d06b8007b74', '173', '1', '1', NULL, NULL, NULL, NULL, NULL, '17003', '1');

INSERT INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('185f8a4e-fbe2-4480-b9a5-2a8a74991f15', '', '对讲机型管理', '0', NULL, '', '/talkback/intercomplatform/intercommodel/list', NULL, 'bdd2f745-c05a-4d1d-ac01-6d06b8007b74', '174', '1', '1', NULL, NULL, NULL, NULL, NULL, '17004', '1');

INSERT INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('89c8026a-ae01-44c0-811b-db33468e132c', '', '人员管理', '0', NULL, '', '/talkback/basicinfo/monitoring/personnel/list', NULL, '963289d9-6aab-11e6-8b77-86f30ca893d3', '1710', '1', '1', NULL, NULL, NULL, NULL, NULL, '170010', '1');

INSERT INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('97f7da20-7e77-48ba-9f8b-6e85ae9c4f9d', '', '职位管理', '0', NULL, '', '/talkback/basicinfo/monitoring/job/list', NULL, '963289d9-6aab-11e6-8b77-86f30ca893d3', '1712', '1', '1', NULL, NULL, NULL, NULL, NULL, '170012', '1');

INSERT INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('43fd8818-0a90-4a51-94f5-8003d2f4d7cb', 'arrow fa fa-angle-right pull-right', '业务类报表', '1', NULL, '', 'javascript:void(0);', NULL, 'bdd2f745-c05a-4d1d-ac01-6d06b8007b74', '175', '1', '1', NULL, NULL, NULL, NULL, NULL, '17005', '1');

INSERT INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('0dad3df0-2a1a-457f-b0c8-48d10c9ba021', '', '里程报表', '0', NULL, '', '/talkback/reportManagement/scheduledMileageReport/list', NULL, '43fd8818-0a90-4a51-94f5-8003d2f4d7cb', '964', '1', '1', NULL, NULL, NULL, NULL, NULL, '170051', '1');

INSERT INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('70399f53-bc42-42d6-935d-7ce08fb4cc5a', '', '出勤报表', '0', NULL, '', '/talkback/reportManagement/scheduledAttendanceReport/list', NULL, '43fd8818-0a90-4a51-94f5-8003d2f4d7cb', '9541', '1', '1', NULL, NULL, NULL, NULL, NULL, '170052', '1');

INSERT INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('a6a65e0a-5e2f-40f9-b488-f3fcf84fe1ca', '', '监控调度', '0', NULL, '', '/talkback/monitoring/dispatch/list', NULL, 'bdd2f745-c05a-4d1d-ac01-6d06b8007b74', '176', '1', '1', NULL, NULL, NULL, NULL, NULL, '17006', '1');

INSERT INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('2d253408-18d9-4bc3-971b-3a7a739f5fpj', NULL, '围栏管理', '0', NULL, 'fenceManagement', '/m/regionManagement/fenceManagement/listPage', NULL, 'bdd2f745-c05a-4d1d-ac01-6d06b8007b74', '501', '1', '1', NULL, NULL, NULL, NULL, NULL, '5011', '1');

INSERT INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('a1ccdbfa-118c-1226-8377-84430ca89002', '', '排班管理', '0', NULL, 'schedulingManagement', '/m/schedulingCenter/schedulingManagement/listPage', NULL, 'bdd2f745-c05a-4d1d-ac01-6d06b8007b74', '501', '1', '1', NULL, NULL, NULL, NULL, NULL, '5021', '1');

INSERT INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('a1ccdbfa-118c-1226-8377-84430ca89004', '', '任务管理', '0', NULL, 'taskManagement', '/a/taskManagement/list', NULL, 'bdd2f745-c05a-4d1d-ac01-6d06b8007b74', '501', '1', '1', NULL, NULL, NULL, NULL, NULL, '5031', '1');

INSERT INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('923213ba-4ddb-118e-9c2d-af7ae01abcpj', NULL, '报警查询', '0', NULL, '', '/a/businessReport/alarmSearch/list', NULL, '43fd8818-0a90-4a51-94f5-8003d2f4d7cb', '963', '1', '1', NULL, NULL, NULL, NULL, NULL, '170053', '1');

-- 初始化admin权限
INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES
('466eaa50-c789-46e2-bd95-313b87424acd', 'bdd2f745-c05a-4d1d-ac01-6d06b8007b74', 'cn=ROLE_ADMIN,ou=Groups', '2019-11-13 09:42:23', 'admin', NULL, NULL, '1', '1');

INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES
('012533db-8bb6-4c4e-9b41-9f89c56877d6', '963289d9-6aab-11e6-8b77-86f30ca893d3', 'cn=ROLE_ADMIN,ou=Groups', '2019-11-13 09:42:23', 'admin', NULL, NULL, '1', '1');

INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES
('032c7bfa-693f-49b6-8628-f3beb2112748', '0e6e0b51-ff18-40e4-8fdb-fecf2ed95ddc', 'cn=ROLE_ADMIN,ou=Groups', '2019-11-13 09:42:23', 'admin', NULL, NULL, '1', '1');

INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES
('05d16af1-8c57-4932-a2d6-2c783ed1e409', 'b46dec1a-6a9e-11e6-8b77-86f30ca893d3', 'cn=ROLE_ADMIN,ou=Groups', '2019-11-13 09:42:23', 'admin', NULL, NULL, '1', '1');

INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES
('064d84d0-e3ce-43ce-8e86-ea8ce5324798', 'fc4074c3-7b8a-4b77-b91b-338aedebee4d', 'cn=ROLE_ADMIN,ou=Groups', '2019-11-13 09:42:23', 'admin', NULL, NULL, '1', '1');

INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES
('0686ec4b-8744-4768-bfa9-f126d8fa90e9', '185f8a4e-fbe2-4480-b9a5-2a8a74991f15', 'cn=ROLE_ADMIN,ou=Groups', '2019-11-13 09:42:23', 'admin', NULL, NULL, '1', '1');

INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES
('06b158e5-61d3-4c57-8d32-f3d8c0dbc2a5', '89c8026a-ae01-44c0-811b-db33468e132c', 'cn=ROLE_ADMIN,ou=Groups', '2019-11-13 09:42:23', 'admin', NULL, NULL, '1', '1');

INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES
('0908d383-3dcb-4175-ba85-4c6c147832b3', '97f7da20-7e77-48ba-9f8b-6e85ae9c4f9d', 'cn=ROLE_ADMIN,ou=Groups', '2019-11-13 09:42:23', 'admin', NULL, NULL, '1', '1');

INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES
('0d3a1eec-cdf4-4093-a2d3-f57aa14aab5d', '43fd8818-0a90-4a51-94f5-8003d2f4d7cb', 'cn=ROLE_ADMIN,ou=Groups', '2019-11-13 09:42:23', 'admin', NULL, NULL, '1', '1');

INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES
('10999299-f64b-4362-b0c6-71b5630774bf', '0dad3df0-2a1a-457f-b0c8-48d10c9ba021', 'cn=ROLE_ADMIN,ou=Groups', '2019-11-13 09:42:23', 'admin', NULL, NULL, '1', '1');

INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES
('173cdbd4-7e0c-449e-8812-41f19934256e', '70399f53-bc42-42d6-935d-7ce08fb4cc5a', 'cn=ROLE_ADMIN,ou=Groups', '2019-11-13 09:42:23', 'admin', NULL, NULL, '1', '1');

INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES
('17cdab5c-37f2-4db8-b3b6-2270c9b59652', 'a6a65e0a-5e2f-40f9-b488-f3fcf84fe1ca', 'cn=ROLE_ADMIN,ou=Groups', '2019-11-13 09:42:23', 'admin', NULL, NULL, '1', '1');

INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES
('17ff9bd9-8dc3-49c3-bfc6-2f2f1168b069', '84113afa-df07-4247-8666-1f467acaebpj', 'cn=ROLE_ADMIN,ou=Groups', '2019-11-13 09:42:23', 'admin', NULL, NULL, '1', '1');

INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES
('183c0cee-7e90-4454-b109-c33743ce7830', '2d253408-18d9-4bc3-971b-3a7a739f5fpj', 'cn=ROLE_ADMIN,ou=Groups', '2019-11-13 09:42:23', 'admin', NULL, NULL, '1', '1');

INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES
('1843b4a8-32f8-4dff-bc81-43b093859185', 'a1ccdbfa-118c-1226-8377-84430ca89001', 'cn=ROLE_ADMIN,ou=Groups', '2019-11-13 09:42:23', 'admin', NULL, NULL, '1', '1');

INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES
('1a6a7c14-65e8-48e0-8f60-554ac554223c', 'a1ccdbfa-118c-1226-8377-84430ca89002', 'cn=ROLE_ADMIN,ou=Groups', '2019-11-13 09:42:23', 'admin', NULL, NULL, '1', '1');

INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES
('1b4ac0d6-f24d-4d84-a8f0-5afd3ae1cee9', 'a1ccdbfa-118c-1226-8377-84430ca89003', 'cn=ROLE_ADMIN,ou=Groups', '2019-11-13 09:42:23', 'admin', NULL, NULL, '1', '1');

INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES
('1be7ffbd-a06a-491f-a5b7-f7f109b7a781', 'a1ccdbfa-118c-1226-8377-84430ca89004', 'cn=ROLE_ADMIN,ou=Groups', '2019-11-13 09:42:23', 'admin', NULL, NULL, '1', '1');

INSERT INTO `clbs`.`zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES
('1c5bdad6-018e-4fc4-8b78-2c0984faf1e7', '923213ba-4ddb-118e-9c2d-af7ae01abcpj', 'cn=ROLE_ADMIN,ou=Groups', '2019-11-13 09:42:23', 'admin', NULL, NULL, '1', '1');

CREATE TABLE `zw_m_fence_type` (
  `id` varchar(64) NOT NULL COMMENT '围栏种类id',
  `fence_type_name` varchar(50) NOT NULL COMMENT '种类名称',
  `color_code` varchar(10) NOT NULL COMMENT '颜色码',
  `transparency` varchar(10) NOT NULL COMMENT '透明度',
  `draw_way` varchar(10) NOT NULL COMMENT '绘制方式 1:多边形; 2:圆; 3:路线; 4:标注; 多个逗号分隔',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `flag` smallint(6) NOT NULL DEFAULT '1' COMMENT '0不显示、1显示',
  `create_data_time` datetime DEFAULT NULL,
  `create_data_username` varchar(45) DEFAULT NULL,
  `update_data_time` datetime DEFAULT NULL,
  `update_data_username` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


ALTER TABLE zw_m_fence_info add fence_type_id varchar(64) DEFAULT NULL  COMMENT '围栏种类id';
ALTER TABLE zw_m_fence_info add area double DEFAULT '0'  COMMENT '面积';

CREATE TABLE `zw_m_monitor_scheduled_info` (
  `id` varchar(64) NOT NULL COMMENT 'id（监控对象排班信息）',
  `scheduled_info_id` varchar(64) NOT NULL COMMENT '排班id',
  `monitor_id` varchar(64) NOT NULL COMMENT '监控对象id',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `date_duplicate_type` varchar(20) NOT NULL COMMENT '日期重复类型（周一至周日（1至7）逗号分隔，8：每天；）',
  `flag` smallint(6) NOT NULL DEFAULT '1' COMMENT '0不显示、1显示',
  PRIMARY KEY (`id`),
  KEY `index_scheduled_info_id` (`scheduled_info_id`),
  KEY `index_monitor_id` (`monitor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `zw_m_scheduled_info` (
  `id` varchar(64) NOT NULL COMMENT 'id（排班信息）',
  `scheduled_name` varchar(20) NOT NULL COMMENT '排班名称',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `date_duplicate_type` varchar(20) NOT NULL COMMENT '日期重复类型（周一至周日（1至7）逗号分隔，8：每天；）',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `group_id` varchar(64) NOT NULL COMMENT '创建人所属组织id',
  `flag` smallint(6) NOT NULL DEFAULT '1' COMMENT '0不显示、1显示',
  `is_mandatory_termination` smallint(6) NOT NULL DEFAULT '0' COMMENT '0不是强制结束、1强制结束',
  `create_data_time` datetime DEFAULT NULL,
  `create_data_username` varchar(45) DEFAULT NULL,
  `update_data_time` datetime DEFAULT NULL,
  `update_data_username` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `zw_m_scheduled_item_info` (
  `id` varchar(64) NOT NULL COMMENT 'id（排班项信息）',
  `scheduled_info_id` varchar(64) NOT NULL COMMENT '排班id',
  `control_type` smallint(6) NOT NULL COMMENT '控制类别；1:围栏；2:RFID；3:NFC; 4:二维码;',
  `fence_info_id` varchar(64) NOT NULL COMMENT '围栏id',
  `start_time` varchar(10) NOT NULL COMMENT '开始时间',
  `end_time` varchar(10) NOT NULL COMMENT '结束时间',
  `relation_alarm` varchar(20) DEFAULT NULL COMMENT '关联报警（1:上班未到岗; 2:上班离岗; 3:超时长停留;）',
  `residence_time` int(2) DEFAULT NULL COMMENT '停留时间（分钟）',
  `flag` smallint(6) NOT NULL DEFAULT '1' COMMENT '0不显示、1显示',
  PRIMARY KEY (`id`),
  KEY `index_scheduled_info_id` (`scheduled_info_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `zw_m_designate_info` (
  `id` varchar(64) NOT NULL COMMENT 'id（指派信息）',
  `designate_name` varchar(20) NOT NULL COMMENT '指派名称',
  `task_id` varchar(64) NOT NULL COMMENT '任务id',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `date_duplicate_type` varchar(20) NOT NULL COMMENT '日期重复类型（周一至周日（1至7）逗号分隔，8：每天；）',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `group_id` varchar(64) NOT NULL COMMENT '创建人所属组织id',
  `flag` smallint(6) NOT NULL DEFAULT '1' COMMENT '0不显示、1显示',
  `create_data_time` datetime DEFAULT NULL,
  `create_data_username` varchar(45) DEFAULT NULL,
  `update_data_time` datetime DEFAULT NULL,
  `update_data_username` varchar(45) DEFAULT NULL,
  `forced_end` smallint(6) DEFAULT NULL COMMENT '1表示强制结束',
  PRIMARY KEY (`id`),
  KEY `inx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



CREATE TABLE `zw_m_monitor_designate_info` (
  `id` varchar(64) NOT NULL COMMENT 'id（监控对象指派信息）',
  `designate_info_id` varchar(64) NOT NULL COMMENT '指派id',
  `monitor_id` varchar(64) NOT NULL COMMENT '监控对象id',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `date_duplicate_type` varchar(20) NOT NULL COMMENT '日期重复类型（周一至周日（1至7）逗号分隔，8：每天；）',
  `flag` smallint(6) NOT NULL DEFAULT '1' COMMENT '0不显示、1显示',
  PRIMARY KEY (`id`),
  KEY `inx_monitor_id` (`monitor_id`),
  KEY `idx_designate_id` (`designate_info_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `zw_m_task_info` (
  `id` varchar(64) NOT NULL COMMENT 'id（任务信息）',
  `task_name` varchar(20) NOT NULL COMMENT '任务名称',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `group_id` varchar(64) NOT NULL COMMENT '创建人所属组织id',
  `flag` smallint(6) NOT NULL DEFAULT '1' COMMENT '0不显示、1显示',
  `create_data_time` datetime DEFAULT NULL,
  `create_data_username` varchar(45) DEFAULT NULL,
  `update_data_time` datetime DEFAULT NULL,
  `update_data_username` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `zw_m_task_item_info` (
  `id` varchar(64) NOT NULL COMMENT 'id（任务项信息）',
  `task_id` varchar(64) NOT NULL COMMENT '任务id',
  `control_type` smallint(6) NOT NULL COMMENT '控制类别；1:围栏；2:RFID；3:NFC; 4:二维码;',
  `fence_info_id` varchar(64) NOT NULL COMMENT '围栏id',
  `start_time` varchar(10) NOT NULL COMMENT '开始时间',
  `end_time` varchar(10) NOT NULL COMMENT '结束时间',
  `relation_alarm` varchar(20) DEFAULT NULL COMMENT '关联报警（1:任务未到岗; 2:任务离岗; ）',
  `flag` smallint(6) NOT NULL DEFAULT '1' COMMENT '0不显示、1显示',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `zw_m_user_defined_setting` (
  `id` varchar(64) NOT NULL COMMENT 'id',
  `relation_id` varchar(64) NOT NULL COMMENT '关联id',
  `user_id` varchar(64) NOT NULL COMMENT '用户id',
  `mark` varchar(64) NOT NULL COMMENT '标识 1、围栏显示',
  `flag` smallint(6) NOT NULL DEFAULT '1' COMMENT '0不显示、1显示',
  `create_data_time` datetime DEFAULT NULL,
  `create_data_username` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `zw_m_alarm_type` (`id`, `name`, `parent_id`, `flag`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `type`, `description`, `sendFlag`, `pos`) VALUES ('a123117c-51c7-11e8-9c2d-fa7ae01baa01', '上班未到岗', NULL, '1', NULL, NULL, NULL, NULL, 'scheduledAlarm', NULL, '0', '152');
INSERT INTO `zw_m_alarm_type` (`id`, `name`, `parent_id`, `flag`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `type`, `description`, `sendFlag`, `pos`) VALUES ('a123117c-51c7-11e8-9c2d-fa7ae01baa02', '上班离岗', NULL, '1', NULL, NULL, NULL, NULL, 'scheduledAlarm', NULL, '0', '153');
INSERT INTO `zw_m_alarm_type` (`id`, `name`, `parent_id`, `flag`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `type`, `description`, `sendFlag`, `pos`) VALUES ('a123117c-51c7-11e8-9c2d-fa7ae01baa03', '超时长停留', NULL, '1', NULL, NULL, NULL, NULL, 'scheduledAlarm', NULL, '0', '154');
INSERT INTO `zw_m_alarm_type` (`id`, `name`, `parent_id`, `flag`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `type`, `description`, `sendFlag`, `pos`) VALUES ('a123117c-51c7-11e8-9c2d-fa7ae01baa04', '任务未到岗', NULL, '1', NULL, NULL, NULL, NULL, 'taskAlarm', NULL, '0', '155');
INSERT INTO `zw_m_alarm_type` (`id`, `name`, `parent_id`, `flag`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `type`, `description`, `sendFlag`, `pos`) VALUES ('a123117c-51c7-11e8-9c2d-fa7ae01baa05', '任务离岗', NULL, '1', NULL, NULL, NULL, NULL, 'taskAlarm', NULL, '0', '156');

INSERT INTO `zw_c_dictionary` VALUES ('44zzb001-b4c7-11e9-be49-408d5cc21838', null, null, 'A1', 'DRVING_LICENCE_CATEGORY', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb002-b4c7-11e9-be49-408d5cc21838', null, null, 'A2', 'DRVING_LICENCE_CATEGORY', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb003-b4c7-11e9-be49-408d5cc21838', null, null, 'A3', 'DRVING_LICENCE_CATEGORY', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb004-b4c7-11e9-be49-408d5cc21838', null, null, 'B1', 'DRVING_LICENCE_CATEGORY', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb005-b4c7-11e9-be49-408d5cc21838', null, null, 'B2', 'DRVING_LICENCE_CATEGORY', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb006-b4c7-11e9-be49-408d5cc21838', null, null, 'C1', 'DRVING_LICENCE_CATEGORY', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb007-b4c7-11e9-be49-408d5cc21838', null, null, 'C2', 'DRVING_LICENCE_CATEGORY', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb008-b4c7-11e9-be49-408d5cc21838', null, null, 'C3', 'DRVING_LICENCE_CATEGORY', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb009-b4c7-11e9-be49-408d5cc21838', null, null, 'C4', 'DRVING_LICENCE_CATEGORY', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb010-b4c7-11e9-be49-408d5cc21838', null, null, 'D', 'DRVING_LICENCE_CATEGORY', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb011-b4c7-11e9-be49-408d5cc21838', null, null, 'E', 'DRVING_LICENCE_CATEGORY', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb012-b4c7-11e9-be49-408d5cc21838', null, null, 'F', 'DRVING_LICENCE_CATEGORY', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb013-b4c7-11e9-be49-408d5cc21838', null, null, 'M', 'DRVING_LICENCE_CATEGORY', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb014-b4c7-11e9-be49-408d5cc21838', null, null, 'N', 'DRVING_LICENCE_CATEGORY', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb015-b4c7-11e9-be49-408d5cc21838', null, null, 'P', 'DRVING_LICENCE_CATEGORY', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb016-b4c7-11e9-be49-408d5cc21838', null, null, '1级', 'CERTIFICATION_CATEGORY', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb017-b4c7-11e9-be49-408d5cc21838', null, null, '2级', 'CERTIFICATION_CATEGORY', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb018-b4c7-11e9-be49-408d5cc21838', null, null, '3级', 'CERTIFICATION_CATEGORY', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb019-b4c7-11e9-be49-408d5cc21838', null, null, '4级', 'CERTIFICATION_CATEGORY', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb020-b4c7-11e9-be49-408d5cc21838', null, null, '5级', 'CERTIFICATION_CATEGORY', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb021-b4c7-11e9-be49-408d5cc21838', null, null, '汉族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb022-b4c7-11e9-be49-408d5cc21838', null, null, '满族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb023-b4c7-11e9-be49-408d5cc21838', null, null, '蒙古族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb024-b4c7-11e9-be49-408d5cc21838', null, null, '回族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb025-b4c7-11e9-be49-408d5cc21838', null, null, '藏族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb026-b4c7-11e9-be49-408d5cc21838', null, null, '维吾尔族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb027-b4c7-11e9-be49-408d5cc21838', null, null, '苗族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb028-b4c7-11e9-be49-408d5cc21838', null, null, '彝族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb029-b4c7-11e9-be49-408d5cc21838', null, null, '壮族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb030-b4c7-11e9-be49-408d5cc21838', null, null, '布依族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb031-b4c7-11e9-be49-408d5cc21838', null, null, '侗族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb032-b4c7-11e9-be49-408d5cc21838', null, null, '瑶族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb033-b4c7-11e9-be49-408d5cc21838', null, null, '白族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb034-b4c7-11e9-be49-408d5cc21838', null, null, '土家族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb035-b4c7-11e9-be49-408d5cc21838', null, null, '哈尼族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb036-b4c7-11e9-be49-408d5cc21838', null, null, '哈萨克族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb037-b4c7-11e9-be49-408d5cc21838', null, null, '傣族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb038-b4c7-11e9-be49-408d5cc21838', null, null, '黎族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb039-b4c7-11e9-be49-408d5cc21838', null, null, '傈僳族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb040-b4c7-11e9-be49-408d5cc21838', null, null, '佤族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb041-b4c7-11e9-be49-408d5cc21838', null, null, '畲族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb042-b4c7-11e9-be49-408d5cc21838', null, null, '高山族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb043-b4c7-11e9-be49-408d5cc21838', null, null, '拉祜族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb044-b4c7-11e9-be49-408d5cc21838', null, null, '水族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb045-b4c7-11e9-be49-408d5cc21838', null, null, '东乡族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb046-b4c7-11e9-be49-408d5cc21838', null, null, '纳西族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb047-b4c7-11e9-be49-408d5cc21838', null, null, '景颇族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb048-b4c7-11e9-be49-408d5cc21838', null, null, '柯尔克孜族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb049-b4c7-11e9-be49-408d5cc21838', null, null, '土族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb050-b4c7-11e9-be49-408d5cc21838', null, null, '达斡尔族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb051-b4c7-11e9-be49-408d5cc21838', null, null, '仫佬族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb052-b4c7-11e9-be49-408d5cc21838', null, null, '羌族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb053-b4c7-11e9-be49-408d5cc21838', null, null, '布朗族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb054-b4c7-11e9-be49-408d5cc21838', null, null, '撒拉族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb055-b4c7-11e9-be49-408d5cc21838', null, null, '毛南族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb056-b4c7-11e9-be49-408d5cc21838', null, null, '仡佬族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb057-b4c7-11e9-be49-408d5cc21838', null, null, '锡伯族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb058-b4c7-11e9-be49-408d5cc21838', null, null, '阿昌族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb059-b4c7-11e9-be49-408d5cc21838', null, null, '普米族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb060-b4c7-11e9-be49-408d5cc21838', null, null, '朝鲜族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb061-b4c7-11e9-be49-408d5cc21838', null, null, '塔吉克族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb062-b4c7-11e9-be49-408d5cc21838', null, null, '怒族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb063-b4c7-11e9-be49-408d5cc21838', null, null, '乌孜别克族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb064-b4c7-11e9-be49-408d5cc21838', null, null, '俄罗斯族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb065-b4c7-11e9-be49-408d5cc21838', null, null, '鄂温克族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb066-b4c7-11e9-be49-408d5cc21838', null, null, '德昂族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb067-b4c7-11e9-be49-408d5cc21838', null, null, '保安族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb068-b4c7-11e9-be49-408d5cc21838', null, null, '裕固族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb069-b4c7-11e9-be49-408d5cc21838', null, null, '京族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb070-b4c7-11e9-be49-408d5cc21838', null, null, '塔塔尔族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb071-b4c7-11e9-be49-408d5cc21838', null, null, '独龙族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb072-b4c7-11e9-be49-408d5cc21838', null, null, '鄂伦春族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb073-b4c7-11e9-be49-408d5cc21838', null, null, '赫哲族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb074-b4c7-11e9-be49-408d5cc21838', null, null, '门巴族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb075-b4c7-11e9-be49-408d5cc21838', null, null, '珞巴族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb076-b4c7-11e9-be49-408d5cc21838', null, null, '基诺族', 'NATION', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb077-b4c7-11e9-be49-408d5cc21838', null, null, 'A', 'BLOOD_TYPE', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb078-b4c7-11e9-be49-408d5cc21838', null, null, 'B', 'BLOOD_TYPE', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb079-b4c7-11e9-be49-408d5cc21838', null, null, 'AB', 'BLOOD_TYPE', '');
INSERT INTO `zw_c_dictionary` VALUES ('44zzb080-b4c7-11e9-be49-408d5cc21838', null, null, 'O', 'BLOOD_TYPE', '');

ALTER TABLE zw_m_people_info MODIFY job_id varchar(64) DEFAULT 'default' COMMENT '职位id';
INSERT INTO `zw_m_job_info` VALUES ('default', '巡检人员', '15687979650155.png', '', '1', '2019-04-23 14:41:51', 'admin', '2019-09-18 17:12:45', 'ydy1-1');

CREATE TABLE `zw_m_temp_assignment_interlocutor` (
  `id` varchar(64) NOT NULL COMMENT '临时组与对讲对象',
  `assignment_id` varchar(64) DEFAULT NULL COMMENT '分组ID',
  `intercom_group_id` bigint(20) DEFAULT NULL COMMENT '对讲群组id',
  `interlocutor_id` varchar(64) DEFAULT NULL COMMENT '对讲对象id',
  PRIMARY KEY (`id`),
  KEY `temp_assignment_interlocutor_index` (`intercom_group_id`,`interlocutor_id`),
  KEY `temp_assignment_interlocutor_id_index` (`interlocutor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `zw_m_intercom_notification_record` (
  `id` varchar(64) NOT NULL DEFAULT '' COMMENT '调度通知记录表',
  `initiate_id` varchar(64) NOT NULL COMMENT '发起通知id',
  `receive_id` varchar(64) NOT NULL COMMENT '接收通知id',
  `content` varchar(255) DEFAULT NULL COMMENT '通知内容',
  `create_data_time` datetime DEFAULT NULL COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `initiate_receive` (`initiate_id`,`receive_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;