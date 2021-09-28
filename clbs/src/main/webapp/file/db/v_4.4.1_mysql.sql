USE clbs;
# 路线偏离报警统计
UPDATE `zw_c_resource` SET `sort_order` = 1310, `code_num` = '1310' WHERE `id` = 'c6034934-9686-11ea-bb37-0242ac130002';
INSERT INTO `zw_c_resource`(`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`) VALUES ('c60343ef-9686-11ea-bb37-0242ac130002', NULL, '路线偏离报警统计', 0, NULL, '', '/cb/cbReportManagement/offRoute/list', NULL, 'd41f83a4-4dd9-11e8-9c2d-fa7ae01bbebc', 1309, 1, 1, NULL, NULL, NULL, NULL, NULL, '1309', 1);
INSERT INTO `zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES ('006c99ee-0e1c-4483-a031-443218ae6e8b', 'c60343ef-9686-11ea-bb37-0242ac130002', 'cn=ROLE_ADMIN,ou=Groups', '2021-03-19 09:25:13', 'admin', NULL, NULL, 1, 1);
INSERT INTO `zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES ('006c99ee-0e1c-4483-a031-443218ae6e8c', 'c60343ef-9686-11ea-bb37-0242ac130002', 'cn=POWER_USER,ou=Groups', '2021-03-19 09:25:13', 'admin', NULL, NULL, 1, 1);

# 监控对象备注长度调整
ALTER TABLE `zw_m_vehicle_info` MODIFY COLUMN `remark` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '车辆备注' AFTER `vehicle_icon`;
ALTER TABLE `zw_m_thing_info` MODIFY COLUMN `remark` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注信息' AFTER `thing_photo`;
ALTER TABLE `zw_m_people_info` MODIFY COLUMN `remark` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注信息' AFTER `people_number`;
ALTER TABLE `zw_m_vehicle_info` MODIFY COLUMN `registration_remark` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注' AFTER `registration_remind_days`;

# 报警参数-疑似人为屏蔽
INSERT INTO `zw_m_alarm_type` (`id`, `name`, `parent_id`, `flag`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `type`, `description`, `sendFlag`, `pos`, `protocol_type`, `platform_or_device`) VALUES ('0334e0bc-bb84-11e6-a4a6-cec0c932ce02', '疑似人为屏蔽报警', NULL, 1, NULL, NULL, NULL, NULL, 'platAlarm', '', '0', '209', 0, 0);
INSERT INTO `zw_m_alarm_parameter` (`id`, `param_code`, `alarm_type_id`, `default_value`, `description`, `flag`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `io_monitor_id`)
VALUES ('bccd2eaa-667f-11e9-9cb1-000c297cf509', 'param1', '0334e0bc-bb84-11e6-a4a6-cec0c932ce02', 10, '点位信息间隔时间，单位：分钟', 1, '2021-03-22 17:46:18', '', NULL, NULL, NULL),
       ('bccd2eaa-667f-11e9-9cb1-000c297cf510', 'param2', '0334e0bc-bb84-11e6-a4a6-cec0c932ce02', 10, '点位信息间隔里程，单位：km', 1, '2021-03-22 17:46:18', '', NULL, NULL, NULL),
       ('bccd2eaa-667f-11e9-9cb1-000c297cf511', 'param3', '0334e0bc-bb84-11e6-a4a6-cec0c932ce02', 1, '连续次数阈值，大于等于此阈值时触发报警', 1, '2021-03-22 17:46:18', '', NULL, NULL, NULL);

# 系统管理/油补补发管理菜单下（转发车辆管理与站点管理之间）,新增“补传管理”
UPDATE `zw_c_resource` SET `sort_order` = 1173, `code_num` = '110073' WHERE `id` = '851b050b-68f4-4529-b461-46c882e3e0ef';
UPDATE `zw_c_resource` SET `sort_order` = 1174, `code_num` = '110074' WHERE `id` = 'b4ec118a-a949-437e-a91b-eb08a950ca53';
UPDATE `zw_c_resource` SET `sort_order` = 1175, `code_num` = '110075' WHERE `id` = '535c1d76-b624-41a6-8560-1d91414ace57';
UPDATE `zw_c_resource` SET `sort_order` = 1176, `code_num` = '110076' WHERE `id` = '2a149552-b5b8-42bd-8d4d-b07fdd556888';

# 油补菜单，放到pay.sql
INSERT INTO `zw_c_resource`(`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`) VALUES ('25e89331-6724-d368-ba4e-a30e5ff7c2e2', 'arrow fa fa-angle-right pull-right', '补传管理', 0, NULL, 'subsidyManage', '/m/subsidy/manage/list', NULL, 'f9602388-02ec-4d1a-b40f-92aee2ebea46', 1172, 1, 1, NULL, NULL, NULL, NULL, NULL, '110072', 1);
# 油补菜单权限，放到pay.sql
INSERT INTO `zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES ('3a9b38b2-2777-b501-26ec-531ad9256444', '25e89331-6724-d368-ba4e-a30e5ff7c2e2', 'cn=ROLE_ADMIN,ou=Groups', '2021-03-25 16:54:13', 'admin', NULL, NULL, 1, 1);
INSERT INTO `zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES ('2ad868a2-bf97-f435-9b28-18835b1c9255', '25e89331-6724-d368-ba4e-a30e5ff7c2e2', 'cn=POWER_USER,ou=Groups', '2021-03-25 16:54:13', 'admin', NULL, NULL, 1, 1);

DROP TABLE IF EXISTS `zw_c_people_group`;
DROP TABLE IF EXISTS `zw_c_professionals_group`;
DROP TABLE IF EXISTS `zw_c_vehicle_group`;
DROP TABLE IF EXISTS `zw_m_assignment_group`;
DROP TABLE IF EXISTS `zw_m_device_group`;
DROP TABLE IF EXISTS `zw_m_sim_group`;

ALTER TABLE `zw_m_vehicle_spot_check` ADD INDEX `idx_time`(`spot_check_time`);

ALTER TABLE `zw_m_output_setting`
    ADD COLUMN `control_status` smallint(6) NOT NULL  DEFAULT '0' COMMENT '控制状态 0:断开 1:闭合' AFTER `outlet_set`;

#河南809代码移植
INSERT INTO `zw_c_dictionary` (`id`, `pid`, `code`, `value`, `type`, `description`, `sort`) VALUES ('93186b9a-f885-15da-527a-24f971b64dbe', NULL, '16', '豫标-809', '809', NULL, NULL);

#zw_m_vehicle_card_num 表字段类型等修改
ALTER TABLE `zw_m_vehicle_card_num`
    MODIFY COLUMN `card_number` mediumtext CHARACTER SET utf8mb4 NULL COMMENT '从业资格证号' AFTER `vid`,
    MODIFY COLUMN `identify_number` mediumtext CHARACTER SET utf8mb4 NULL COMMENT '4.40新增身份证唯一标识字段' AFTER `card_number`,
    CHARACTER SET = utf8mb4;