INSERT INTO `zw_c_dictionary` (`id`, `pid`, `code`, `value`, `type`, `description`)
VALUES ('6ef6aea6-5c33-4ebd-80dd-05ec23a9d099', NULL, '7', '吉林-809', '809', NULL);

INSERT INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`,
                            `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`,
                            `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('fff6629c-f499-11e6-bc64-92368f002671', '', '809平台数据交互管理', '0', NULL, 'jl809platformmanagement', '/jl/vehicle/list',
        NULL, 'f9c6629c-f40e-11e6-bc64-92368f002671', '1125', '1', '1', NULL, NULL, NULL, NULL, NULL, '110027', '1');

INSERT INTO `zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`,
                                 `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('ba0a9fg4-2000-4d6c-b512-f5dcca7acf9e', 'fff6629c-f499-11e6-bc64-92368f002671', 'cn=ROLE_ADMIN,ou=Groups',
        '2020-03-23 17:27:34', 'admin', NULL, NULL, 1, 1);


DROP TABLE if EXISTS `zw_m_stopped_vehicle`;
CREATE TABLE `zw_m_stopped_vehicle`
(
    `id`              varchar(64)  NOT NULL,
    `monitor_id`      varchar(64)  NOT NULL COMMENT '监控对象ID',
    `monitor_name`    varchar(50)  NOT NULL COMMENT '监控对象名称',
    `start_date`      date         NOT NULL COMMENT '停运开始日期',
    `end_date`        date         NOT NULL COMMENT '停运结束日期',
    `stop_cause_code` smallint(6)  NOT NULL COMMENT '报停原因: 1:天气; 2:车辆故障; 3: 路阻; 4: 终端报修; 9: 其他(默认)',
    `plate_color`     smallint(20) NOT NULL COMMENT '车牌颜色：1蓝，2黄，3黑，4白，9其他,90:农蓝, 91农黄,92农绿,93黄绿色,94渐变绿色 ',
    `group_name`      varchar(50)  NOT NULL COMMENT '所属企业',
    `upload_time`     datetime     NOT NULL COMMENT '上报时间',
    `upload_state`    smallint(20) NOT NULL COMMENT '上上传状态：0: 失败; 1: 成功',
    `operator`        varchar(30)  NOT NULL COMMENT '操作人',
    `error_msg`       varchar(100)          DEFAULT NULL COMMENT '错误信息',
    PRIMARY KEY (`id`),
    KEY `monitor_id_index` (`monitor_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='停运车辆上报记录';


DROP TABLE if EXISTS `zw_m_violate_vehicle`;
CREATE TABLE `zw_m_violate_vehicle`
(
    `id`           varchar(64)  NOT NULL,
    `monitor_id`   varchar(64)  NOT NULL COMMENT '监控对象ID',
    `monitor_name` varchar(50)  NOT NULL COMMENT '监控对象名称',
    `violate_time` datetime     NOT NULL COMMENT '违规时间',
    `type`         smallint(6)  NOT NULL COMMENT '违规类型: 1:扭动镜头; 2:遮挡镜头; 3: 无照片; 4: 无定位; 5: 轨迹异常；6：超员; 7: 超速; 8:脱线运行',
    `plate_color`  smallint(20) NOT NULL COMMENT '车牌颜色：1蓝，2黄，3黑，4白，9其他,90:农蓝, 91农黄,92农绿,93黄绿色,94渐变绿色 ',
    `group_name`   varchar(50)  NOT NULL COMMENT '所属企业',
    `upload_time`  datetime     NOT NULL COMMENT '上报时间',
    `upload_state` smallint(20) NOT NULL COMMENT '上传状态：0: 失败; 1: 成功',
    `operator`     varchar(30)  NOT NULL COMMENT '操作人',
    `error_msg`    varchar(100) DEFAULT NULL COMMENT '错误信息',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='违规车辆上报记录';


DROP TABLE if EXISTS `zw_m_alarm_vehicle`;
CREATE TABLE `zw_m_alarm_vehicle`
(
    `id`           varchar(64)  NOT NULL,
    `monitor_id`   varchar(64)  NOT NULL COMMENT '监控对象ID',
    `monitor_name` varchar(50)  NOT NULL COMMENT '监控对象名称',
    `start_time`   datetime     NOT NULL COMMENT '报警开始时间',
    `end_time`     datetime     NOT NULL COMMENT '报警解除时间',
    `alarm_type`   smallint(6)  NOT NULL COMMENT '报警类型: 0:紧急报警; 10: 疲劳报警; 200:进入报警; 201 进出报警; 210: 偏航报警; 41: 超速报警; 53夜间行驶报警',
    `alarm_status` smallint(6)  NOT NULL COMMENT '报警处理状态: 1:处理中; 2:已处理; 3: 不作处理; 4: 将来处理',
    `plate_color`  smallint(20) NOT NULL COMMENT '车牌颜色：1蓝，2黄，3黑，4白，9其他,90:农蓝, 91农黄,92农绿,93黄绿色,94渐变绿色 ',
    `group_name`   varchar(50)  NOT NULL COMMENT '所属企业',
    `upload_time`  datetime     NOT NULL COMMENT '上报时间',
    `upload_state` smallint(20) NOT NULL COMMENT '上上传状态：0: 失败; 1: 成功',
    `operator`     varchar(30)  NOT NULL COMMENT '操作人',
    `error_msg`    varchar(100)          DEFAULT NULL COMMENT '错误信息',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='报警车辆上报记录';

  ALTER TABLE zw_c_logo_config ADD COLUMN video_background varchar(50)  DEFAULT 'videoLogo2.png' COMMENT '视频背景图片';



UPDATE zw_c_custom_column SET title = '终端手机号' WHERE title = 'SIM卡号';

update  zw_c_logo_config set  video_background ='videoLogo2.png' where  id = '2';


ALTER TABLE zw_c_dictionary ADD COLUMN sort int(4) DEFAULT NULL COMMENT '排序';
UPDATE `zw_c_dictionary` SET `id`='091dc97a-53eb-4bc4-81e2-e821ad40586d', `pid`=NULL, `code`='22', `value`='KKS-EV25', `type`='808', `description`=NULL, `sort`='22' WHERE (`id`='091dc97a-53eb-4bc4-81e2-e821ad40586d');
UPDATE `zw_c_dictionary` SET `id`='09d2dae9-0a68-41f8-a2c9-9c8c7a41a558', `pid`=NULL, `code`='1', `value`='交通部JT/T808-2013', `type`='808', `description`='activeSafety', `sort`='1' WHERE (`id`='09d2dae9-0a68-41f8-a2c9-9c8c7a41a558');
UPDATE `zw_c_dictionary` SET `id`='28b0a018-5cad-47c5-abd3-645287d3a7a7', `pid`=NULL, `code`='21', `value`='交通部JT/T808-2019(中位)', `type`='808', `description`='activeSafety', `sort`='3' WHERE (`id`='28b0a018-5cad-47c5-abd3-645287d3a7a7');
UPDATE `zw_c_dictionary` SET `id`='2b0d9b56-e630-4a58-b462-8a1847852b0a', `pid`=NULL, `code`='17', `value`='交通部JT/T808-2013(吉标)', `type`='808', `description`='activeSafety', `sort`='10' WHERE (`id`='2b0d9b56-e630-4a58-b462-8a1847852b0a');
UPDATE `zw_c_dictionary` SET `id`='2cf5a18a-1950-4d5c-83d5-3c31f2083aa8', `pid`=NULL, `code`='15', `value`='交通部JT/T808-2013(苏标)', `type`='808', `description`='activeSafety', `sort`='7' WHERE (`id`='2cf5a18a-1950-4d5c-83d5-3c31f2083aa8');
UPDATE `zw_c_dictionary` SET `id`='2f187ce9-341b-49ac-a005-62a8819d8017', `pid`=NULL, `code`='14', `value`='交通部JT/T808-2013(桂标)', `type`='808', `description`='activeSafety', `sort`='9' WHERE (`id`='2f187ce9-341b-49ac-a005-62a8819d8017');
UPDATE `zw_c_dictionary` SET `id`='3762ed05-86a0-4719-a925-bd5ae574818e', `pid`=NULL, `code`='20', `value`='交通部JT/T808-2019(沪标)', `type`='808', `description`='activeSafety', `sort`='13' WHERE (`id`='3762ed05-86a0-4719-a925-bd5ae574818e');
UPDATE `zw_c_dictionary` SET `id`='3a23425d-b8f3-4904-8502-c83d0972db03', `pid`=NULL, `code`='6', `value`='KKS', `type`='808', `description`=NULL, `sort`='21' WHERE (`id`='3a23425d-b8f3-4904-8502-c83d0972db03');
UPDATE `zw_c_dictionary` SET `id`='5b23e472-3e71-4b81-8181-b4eeabac1814', `pid`=NULL, `code`='8', `value`='BSJ-A5', `type`='808', `description`=NULL, `sort`='18' WHERE (`id`='5b23e472-3e71-4b81-8181-b4eeabac1814');
UPDATE `zw_c_dictionary` SET `id`='5f077b55-f39f-4200-9d6e-4ef5c9b3f2c9', `pid`=NULL, `code`='19', `value`='交通部JT/T808-2013(赣标)', `type`='808', `description`='activeSafety', `sort`='12' WHERE (`id`='5f077b55-f39f-4200-9d6e-4ef5c9b3f2c9');
UPDATE `zw_c_dictionary` SET `id`='60fb409b-6d0a-4683-8f0e-2a54c90ec77e', `pid`=NULL, `code`='16', `value`='交通部JT/T808-2013(浙标)', `type`='808', `description`='activeSafety', `sort`='8' WHERE (`id`='60fb409b-6d0a-4683-8f0e-2a54c90ec77e');
UPDATE `zw_c_dictionary` SET `id`='6df60b10-d914-46ca-8583-4ca4ae42ac28', `pid`=NULL, `code`='13', `value`='交通部JT/T808-2013(冀标)', `type`='808', `description`='activeSafety', `sort`='6' WHERE (`id`='6df60b10-d914-46ca-8583-4ca4ae42ac28');
UPDATE `zw_c_dictionary` SET `id`='784d49bf-83c6-4749-b134-37fba1e41206', `pid`=NULL, `code`='9', `value`='ASO', `type`='808', `description`=NULL, `sort`='19' WHERE (`id`='784d49bf-83c6-4749-b134-37fba1e41206');
UPDATE `zw_c_dictionary` SET `id`='7a6f33ad-bc36-4ede-9414-d2d1b028784c', `pid`=NULL, `code`='18', `value`='交通部JT/T808-2013(陕标)', `type`='808', `description`='activeSafety', `sort`='11' WHERE (`id`='7a6f33ad-bc36-4ede-9414-d2d1b028784c');
UPDATE `zw_c_dictionary` SET `id`='9bebe0ba-7707-4bc3-af43-b553f372dd20', `pid`=NULL, `code`='11', `value`='交通部JT/T808-2019', `type`='808', `description`=NULL, `sort`='2' WHERE (`id`='9bebe0ba-7707-4bc3-af43-b553f372dd20');
UPDATE `zw_c_dictionary` SET `id`='a02bea13-4236-4545-b159-676a719fa3b7', `pid`=NULL, `code`='10', `value`='F3超长待机', `type`='808', `description`=NULL, `sort`='20' WHERE (`id`='a02bea13-4236-4545-b159-676a719fa3b7');
UPDATE `zw_c_dictionary` SET `id`='a0352fd9-aac1-46b7-8e22-cd769008c693', `pid`=NULL, `code`='24', `value`='交通部JT/T808-2019(京标)', `type`='808', `description`='activeSafety', `sort`='14' WHERE (`id`='a0352fd9-aac1-46b7-8e22-cd769008c693');
UPDATE `zw_c_dictionary` SET `id`='a08a74b8-11fc-4c07-be58-4e434cae853f', `pid`=NULL, `code`='5', `value`='BDTD-SM', `type`='808', `description`=NULL, `sort`='17' WHERE (`id`='a08a74b8-11fc-4c07-be58-4e434cae853f');
UPDATE `zw_c_dictionary` SET `id`='adc69570-0458-4f2e-a955-894703c5bd7f', `pid`=NULL, `code`='0', `value`='交通部JT/T808-2011(扩展)', `type`='808', `description`=NULL, `sort`='4' WHERE (`id`='adc69570-0458-4f2e-a955-894703c5bd7f');
UPDATE `zw_c_dictionary` SET `id`='bbbe676b-53c0-4c66-8929-edd696ef905d', `pid`=NULL, `code`='23', `value`='JT/T808-2011(1078报批稿)', `type`='808', `description`=NULL, `sort`='23' WHERE (`id`='bbbe676b-53c0-4c66-8929-edd696ef905d');
UPDATE `zw_c_dictionary` SET `id`='cf7deb03-1243-48de-813e-b3ec5a8f3099', `pid`=NULL, `code`='3', `value`='天禾', `type`='808', `description`=NULL, `sort`='16' WHERE (`id`='cf7deb03-1243-48de-813e-b3ec5a8f3099');
UPDATE `zw_c_dictionary` SET `id`='d9a8b7d6-dd55-4ac1-81cd-7f7d0c62640f', `pid`=NULL, `code`='12', `value`='交通部JT/T808-2013(川标)', `type`='808', `description`='activeSafety', `sort`='5' WHERE (`id`='d9a8b7d6-dd55-4ac1-81cd-7f7d0c62640f');
UPDATE `zw_c_dictionary` SET `id`='e1b36518-d994-4e05-b281-3ba836d6439a', `pid`=NULL, `code`='2', `value`='移为', `type`='808', `description`=NULL, `sort`='15' WHERE (`id`='e1b36518-d994-4e05-b281-3ba836d6439a');

UPDATE zw_c_resource SET flag = 0 WHERE resource_name = '能源管理';

INSERT INTO `zw_m_alarm_type`(`id`, `name`, `parent_id`, `flag`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `type`, `description`, `sendFlag`, `pos`, `protocol_type`, `platform_or_device`) VALUES ('3417a643-d312-49f6-b39f-42feaa5bedb1', '24H累计疲劳驾驶', NULL, 1, NULL, NULL, NULL, NULL, 'platAlarm', NULL, '0', '203', 0, 0);


INSERT INTO `zw_m_alarm_parameter`(`id`, `param_code`, `alarm_type_id`, `default_value`, `description`, `flag`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `io_monitor_id`) VALUES ('8819795e-fdea-48ba-85a3-0e3b1af75995', 'param1', '3417a643-d312-49f6-b39f-42feaa5bedb1', NULL, NULL, 1, '2018-03-13 09:49:38', NULL, NULL, NULL, NULL);