UPDATE `zw_c_resource`
SET `code_num` = '30016'
WHERE `id` = '83ddac3c-5404-11e8-9c2d-fa7ae01bbebc';
UPDATE `zw_c_resource`
SET `sort_order` = 318,
    `code_num`   = '30018'
WHERE `id` = 'a71d239a-5421-11e8-9c2d-fa7ae01bbebc';
UPDATE `zw_c_resource`
SET `sort_order` = 319,
    `code_num`   = '30019'
WHERE `id` = '67303322-9970-4d3b-b951-ede02a5df269';
INSERT INTO `zw_c_resource`(`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`,
                            `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`,
                            `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`,
                            `flag`)
VALUES ('74bbac3c-5a04-a1e8-9d2c-fa7ae01aeebc', NULL, '驾驶员识别管理', 0, NULL, '', '/m/driver/discern/manage/list', NULL,
        '963283ce-6aab-11e6-8b77-86f30ca893d3', 317, 1, 1, NULL, NULL, NULL, NULL, NULL, '30017', 1);
INSERT INTO `zw_c_resource`(`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`,
                            `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`,
                            `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`,
                            `flag`)
VALUES ('98f26a8a-5545-64f6-a6b1-dac14b15c112', NULL, '终端驾驶员识别统计', 0, NULL, '', '/m/driver/discern/statistics/list',
        NULL, '2234bd3e-191e-2216-d43f-78df4c92c74d', 14013, 1, 1, NULL, NULL, NULL, NULL, NULL, '90131', 1);
UPDATE `zw_c_resource`
SET `sort_order` = 14014,
    `code_num`   = '90132'
WHERE `id` = 'a6db362a-df48-11e9-a9a6-000c294a3301';

ALTER TABLE `zw_m_809_setting`
    ADD COLUMN `data_filter_status` smallint(6) NOT NULL DEFAULT '0' COMMENT '是否开启过滤 0:关闭 1:开启' AFTER `server_status`;

-- 报警联动策略-下发短信
ALTER TABLE zw_m_msg_setting
    MODIFY `id` varchar(64) NOT NULL COMMENT '联动策略-下发短信设置表主键id';
ALTER TABLE zw_m_msg_setting
    add column text_type SMALLINT(6) NOT NULL DEFAULT '1' COMMENT '1:通知; 2: 服务(2019协议专用)';
ALTER TABLE zw_m_msg_setting
    add column message_type_one SMALLINT(6) NOT NULL DEFAULT '1' COMMENT '1:通知;2:服务;3:紧急(2019协议专用)';
ALTER TABLE zw_m_msg_setting
    add column message_type_two SMALLINT(6) NOT NULL DEFAULT '0' COMMENT '0: 中心导航信息; 1: CAN故障码信息 (2019协议专用)';
ALTER TABLE zw_m_msg_setting
    modify `marks` varchar(20) DEFAULT NULL COMMENT '[1,2,3,4] 1:紧急；2:终端显示器显示；3:终端TTS语音；4:广告屏显示';
-- 报警联动策略特殊报警
ALTER TABLE zw_m_special_alarm
    MODIFY `id` varchar(64) NOT NULL COMMENT '监控对象联动策略设置表主键id';
ALTER TABLE zw_m_special_alarm
    add column alarm_handle_type SMALLINT(6) DEFAULT NULL COMMENT '处理方式: 1: 下发短信; 2: 拍照; 3: 不作处理;';
ALTER TABLE zw_m_special_alarm
    add column alarm_handle_result SMALLINT(6) DEFAULT NULL COMMENT '处理结果: 1:处理中; 2:已处理完毕; 3: 不作处理; 4: 将来处理;';
ALTER TABLE zw_m_special_alarm
    add column handle_username varchar(30) DEFAULT NULL COMMENT '处理人姓名, 用于809上传使用';
ALTER TABLE zw_m_special_alarm
    add column alarm_handle_linkage_check SMALLINT(6) NOT NULL DEFAULT '0' COMMENT '报警处理联动是否勾选, 0: 未勾选; 1: 勾选';
-- 拍照和录像
ALTER TABLE zw_m_photo_setting
    MODIFY `way_id` VARCHAR(100) NOT NULL COMMENT '通道ID, 多个逗号分隔, 最多16个';
ALTER TABLE zw_m_output_setting
    MODIFY `id` varchar(64) NOT NULL COMMENT '联动策略-输出控制设置表主键id';
-- 字典数据
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35067', '道路旅客运输', 'BUSINESS_SCOPE', '', '01000');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35068', '班车客运', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35067', '01100');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35069', '县内班车客运', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35068', '01101');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35070', '县际班车客运', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35068', '01102');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35071', '市际班车客运', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35068', '01103');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35072', '省际班车客运', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35068', '01104');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35073', '包车客运', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35067', '01200');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35074', '县内包车客运', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35073', '01201');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35075', '县际包车客运', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35073', '01202');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35076', '市际包车客运', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35073', '01203');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35077', '省际包车客运', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35073', '01204');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35078', '定线旅游', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35067', '01300');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35079', '县内定线旅游', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35078', '01301');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35080', '县际定线旅游', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35078', '01302');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35081', '市际定线旅游', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35078', '01303');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35082', '省际定线旅游', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35078', '01304');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35083', '非定线旅游', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35067', '01400');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35084', '县内非定线旅游', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35083', '01401');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35085', '县际非定线旅游', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35083', '01402');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35086', '市际非定线旅游', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35083', '01403');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35087', '省际非定线旅游', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35083', '01404');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35088', '道路普通货运', 'BUSINESS_SCOPE', '', '02000');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35089', '道路普通货物运输', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35088', '02100');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35090', '道路普通货物运输', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35089', '02101');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35091', '货物专用运输', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35088', '02200');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35092', '货物专用运输(集装箱)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35091', '02201');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35093', '货物专用运输(冷藏保鲜设备)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35091',
        '02202');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35094', '货物专用运输(罐式容器)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35091',
        '02203');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35095', '大型物件运输', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35088', '02300');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35096', '大型物件运输(一类)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35095', '02301');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35097', '大型物件运输(二类)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35095', '02302');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35098', '大型物件运输(三类)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35095', '02303');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba35099', '大型物件运输(四类)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba35095', '02304');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350100', '道路危险货物运输', 'BUSINESS_SCOPE', '', '03000');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350101', '经营性道路危险货物运输', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350100',
        '03100');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350102', '危险货物运输(1类1项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350101',
        '03111');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350103', '危险货物运输(1类2项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350101',
        '03112');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350104', '危险货物运输(1类3项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350101',
        '03113');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350105', '危险货物运输(1类4项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350101',
        '03114');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350106', '危险货物运输(1类5项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350101',
        '03115');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350107', '危险货物运输(1类6项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350101',
        '03116');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350108', '危险货物运输(2类1项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350101',
        '03121');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350109', '危险货物运输(2类2项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350101',
        '03122');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350110', '危险货物运输(2类3项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350101',
        '03123');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350111', '危险货物运输(3类)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350101',
        '03131');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350112', '危险货物运输(4类1项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350101',
        '03141');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350113', '危险货物运输(4类2项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350101',
        '03142');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350114', '危险货物运输(4类3项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350101',
        '03143');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350115', '危险货物运输(5类1项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350101',
        '03151');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350116', '危险货物运输(5类2项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350101',
        '03152');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350117', '危险货物运输(6类1项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350101',
        '03161');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350118', '危险货物运输(6类2项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350101',
        '03162');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350119', '危险货物运输(7类)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350101',
        '03170');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350120', '危险货物运输(8类)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350101',
        '03181');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350121', '危险货物运输(9类)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350101',
        '03191');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350122', '非经营性危险货物运输', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350100',
        '03200');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350123', '危险货物运输(1类1项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350122',
        '03211');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350124', '危险货物运输(1类2项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350122',
        '03212');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350125', '危险货物运输(1类3项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350122',
        '03213');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350126', '危险货物运输(1类4项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350122',
        '03214');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350127', '危险货物运输(1类5项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350122',
        '03215');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350128', '危险货物运输(1类6项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350122',
        '03216');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350129', '危险货物运输(2类1项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350122',
        '03221');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350130', '危险货物运输(2类2项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350122',
        '03222');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350131', '危险货物运输(2类3项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350122',
        '03223');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350132', '危险货物运输(3类)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350122',
        '03231');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350133', '危险货物运输(4类1项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350122',
        '03241');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350134', '危险货物运输(4类2项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350122',
        '03242');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350135', '危险货物运输(4类3项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350122',
        '03243');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350136', '危险货物运输(5类1项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350122',
        '03251');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350137', '危险货物运输(5类2项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350122',
        '03252');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350138', '危险货物运输(6类1项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350122',
        '03261');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350139', '危险货物运输(6类2项)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350122',
        '03262');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350140', '危险货物运输(7类)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350122',
        '03270');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350141', '危险货物运输(8类)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350122',
        '03281');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350142', '危险货物运输(9类)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350122',
        '03291');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350143', '机动车维修', 'BUSINESS_SCOPE', '', '04000');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350144', '一类汽车维修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350143', '04100');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350145', '大中型客车维修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350144', '04101');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350146', '大中型货车维修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350144', '04102');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350147', '小型车辆维修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350144', '04103');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350148', '危险货物运输车辆维修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350144',
        '04104');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350149', '二类汽车维修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350143', '04200');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350150', '大中型客车维修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350149', '04201');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350151', '大中型货车维修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350149', '04202');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350152', '小型车辆维修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350149', '04203');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350153', '三类汽车维修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350143', '04300');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350154', '发动机维修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350153', '04301');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350155', '车身维修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350153', '04302');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350156', '电气系统维修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350153', '04303');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350157', '自动变速器维修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350153', '04304');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350158', '车身清洁维护', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350153', '04305');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350159', '涂漆', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350153', '04306');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350160', '轮胎动平衡及修补', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350153', '04307');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350161', '四轮定位检测调整', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350153', '04308');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350162', '供油系统维护及油品更换', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350153',
        '04309');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350163', '喷油泵和喷油嘴维修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350153', '04310');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350164', '曲轴修磨', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350153', '04311');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350165', '气缸膛磨', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350153', '04312');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350166', '散热器(水箱)修理', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350153', '04313');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350167', '空调维修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350153', '04314');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350168', '车辆装潢(篷布、坐垫及内饰)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350153',
        '04315');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350169', '车辆玻璃安装', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350153', '04316');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350170', '一类摩托车维修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350143', '04400');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350171', '总成大修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350170', '04401');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350172', '维护', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350170', '04402');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350173', '小修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350170', '04403');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350174', '二类摩托车维修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350143', '04500');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350175', '维护', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350174', '04501');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350176', '小修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350174', '04502');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350177', '其他机动车维修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350143', '04600');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350178', '其他机动车维修', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350174', '04601');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350179', '机动车驾驶员培训', 'BUSINESS_SCOPE', '', '05000');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350180', '普通机动车驾驶员培训', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350179',
        '05100');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350181', 'A1', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350180', '05111');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350182', 'A2', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350180', '05112');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350183', 'A3', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350180', '05113');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350184', 'B1', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350180', '05121');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350185', 'B2', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350180', '05122');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350186', 'C1', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350180', '05131');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350187', 'C2', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350180', '05132');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350188', 'C3', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350180', '05133');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350189', 'C4', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350180', '05134');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350190', 'D', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350180', '05140');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350191', 'E', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350180', '05150');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350192', 'F', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350180', '05160');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350193', 'M', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350180', '05170');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350194', '道路运输驾驶员从业资格培训', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350179',
        '05200');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350195', '道路运输驾驶员从业资格培训(客运)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350194',
        '05201');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350196', '道路运输驾驶员从业资格培训(货运)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350194',
        '05202');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350197', '道路运输驾驶员从业资格培训(危险货运)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350194',
        '05203');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350198', '道路运输驾驶员从业资格培训(其他)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350194',
        '05204');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350199', '机动车驾驶员培训教练场', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350179',
        '05300');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350200', '机动车驾驶员培训教练场', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350199',
        '05301');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350201', '道路货运站(场)', 'BUSINESS_SCOPE', '', '06200');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350202', '道路货运站(场)', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350201', '06201');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350203', '国际道路运输', 'BUSINESS_SCOPE', '', '07000');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350204', '国际道路旅客运输', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350203', '07100');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350205', '定期国际道路旅客运输', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350204',
        '07101');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350206', '不定期国际道路旅客运输', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350204',
        '07102');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350207', '国际道路货物运输', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350203', '07200');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350208', '国际道路货物运输', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350207', '07201');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350209', '国际道路危险货物运输', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350207',
        '07202');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350210', '公交运输', 'BUSINESS_SCOPE', '', '08000');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350211', '公交运输', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350210', '08100');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350212', '公交运输', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350211', '08101');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350213', '出租运输', 'BUSINESS_SCOPE', '', '09000');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350214', '客运出租运输', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350213', '09100');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350215', '客运出租运输', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350214', '09101');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350216', '货运出租运输', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350213', '09200');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350217', '货运出租运输', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350216', '09201');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350218', '汽车租赁', 'BUSINESS_SCOPE', '', '10000');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350219', '客运汽车租赁', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350218', '10100');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350220', '客运汽车租赁', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350219', '10101');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350221', '货运汽车租赁', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350218', '10200');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-8b9d-bcca-8bc4-ba350222', '货运汽车租赁', 'BUSINESS_SCOPE', '078261db-8b9d-bcca-8bc4-ba350221', '10201');



INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350188', '北京市', 'BUSINESS_LICENSE_TYPE', '', '京');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350189', '天津市', 'BUSINESS_LICENSE_TYPE', '', '津');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350190', '河北省', 'BUSINESS_LICENSE_TYPE', '', '冀');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350191', '山西省', 'BUSINESS_LICENSE_TYPE', '', '晋');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350192', '内蒙古自治区', 'BUSINESS_LICENSE_TYPE', '', '蒙');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350193', '辽宁省', 'BUSINESS_LICENSE_TYPE', '', '辽');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350194', '吉林省', 'BUSINESS_LICENSE_TYPE', '', '吉');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350195', '黑龙江省', 'BUSINESS_LICENSE_TYPE', '', '黑');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350196', '上海市', 'BUSINESS_LICENSE_TYPE', '', '沪');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350197', '江苏省', 'BUSINESS_LICENSE_TYPE', '', '苏');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350198', '浙江省', 'BUSINESS_LICENSE_TYPE', '', '浙');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350199', '安徽省', 'BUSINESS_LICENSE_TYPE', '', '皖');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350200', '福建省', 'BUSINESS_LICENSE_TYPE', '', '闽');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350201', '江西省', 'BUSINESS_LICENSE_TYPE', '', '赣');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350202', '山东省', 'BUSINESS_LICENSE_TYPE', '', '鲁');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350203', '河南省', 'BUSINESS_LICENSE_TYPE', '', '豫');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350204', '湖北省', 'BUSINESS_LICENSE_TYPE', '', '鄂');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350205', '湖南省', 'BUSINESS_LICENSE_TYPE', '', '湘');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350206', '广东省', 'BUSINESS_LICENSE_TYPE', '', '粤');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350207', '广西壮族自治区', 'BUSINESS_LICENSE_TYPE', '', '桂');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350208', '海南省', 'BUSINESS_LICENSE_TYPE', '', '琼');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350209', '重庆市', 'BUSINESS_LICENSE_TYPE', '', '渝');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350210', '四川省', 'BUSINESS_LICENSE_TYPE', '', '川');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350211', '贵州省', 'BUSINESS_LICENSE_TYPE', '', '贵');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350212', '云南省', 'BUSINESS_LICENSE_TYPE', '', '云');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350213', '西藏自治区', 'BUSINESS_LICENSE_TYPE', '', '藏');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350214', '陕西省', 'BUSINESS_LICENSE_TYPE', '', '陕');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350215', '甘肃省', 'BUSINESS_LICENSE_TYPE', '', '甘');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350216', '青海省', 'BUSINESS_LICENSE_TYPE', '', '青');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350217', '宁夏回族自新区', 'BUSINESS_LICENSE_TYPE', '', '宁');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350218', '新疆维吾尔自治区', 'BUSINESS_LICENSE_TYPE', '', '新');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350219', '台湾省', 'BUSINESS_LICENSE_TYPE', '', '台');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350220', '香港特别行政区', 'BUSINESS_LICENSE_TYPE', '', '港');
INSERT INTO `zw_c_dictionary` (`id`, `value`, `type`, `pid`, `code`)
VALUES ('078261db-5443-bcca-8bc4-ba350221', '澳门特别行政区', 'BUSINESS_LICENSE_TYPE', '', '澳');


CREATE TABLE `zw_m_business_scope_config`
(
    `id`                varchar(64) NOT NULL COMMENT '企业 或者 车id',
    `business_scope_id` varchar(64) DEFAULT NULL COMMENT '运营范围id',
    `type`              varchar(10) DEFAULT NULL COMMENT '类型 1 企业  2 车',
    KEY `id` (`id`),
    KEY `scopeid` (`business_scope_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `zw_m_vehicle_driver_identification`
(
    `id`                    varchar(64) NOT NULL,
    `monitor_id`            varchar(64) NOT NULL COMMENT '车辆id',
    `driver_id`             varchar(64)  DEFAULT NULL COMMENT '驾驶员id',
    `identification_time`   datetime    NOT NULL COMMENT '比对时间',
    `identification_type`   smallint(6) NOT NULL COMMENT '比对类型，0插卡比对 1巡检比对 2点火比对 3离开返回比对',
    `identification_result` smallint(6) NOT NULL COMMENT '比对结果，0匹配成功 1匹配失败 2超时 3没有启用该功能 4连接异常 5无指定人脸图片 6无人脸库',
    `match_rate`            varchar(10)  DEFAULT NULL COMMENT '比对相似度，单位%',
    `match_threshold`       smallint(5)  DEFAULT NULL COMMENT '比对相似度阈值，单位%',
    `latitude`              varchar(50)  DEFAULT NULL COMMENT '纬度',
    `longitude`             varchar(50)  DEFAULT NULL COMMENT '经度',
    `image_url`             varchar(255) DEFAULT NULL COMMENT '图像url',
    `photo_flag`            smallint(6)  DEFAULT '1' COMMENT '照片是否已删除（一般定义7天删除）',
    PRIMARY KEY (`id`),
    KEY `idx_vid` (`monitor_id`),
    KEY `idx_time` (`identification_time`),
    KEY `idx_type` (`identification_type`),
    KEY `idx_result` (`identification_result`),
    KEY `idx_photo` (`photo_flag`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='车辆驾驶员识别记录表';

-- 离线位移日报表
INSERT INTO `zw_c_resource`
VALUES ('60dde35a-ff51-4833-a6df-9217697c9931', NULL, '离线位移日报表', '0', NULL, '',
        '/m/reportManagement/offlineDisplacement/list', NULL, 'c792a247-fa3a-f455-9e39-2d15ad22f5cb', '150', '1', '1',
        NULL, NULL, NULL, NULL, NULL, '120015', '1');
INSERT INTO `zw_c_role_resource`
VALUES ('27c56bce-c586-4ac3-b3e1-ce7cbcd0f080', '60dde35a-ff51-4833-a6df-9217697c9931', 'cn=ROLE_ADMIN,ou=Groups',
        '2020-10-20 11:49:34', 'admin', NULL, NULL, '1', '1');

-- 809从链路逻辑修改，初始化原809转发设置从链路字段SQL
UPDATE zw_m_809_setting
SET ip_branch = '0.0.0.0'
WHERE (ip_branch IS NULL OR ip_branch = '');

CREATE TABLE `zw_m_device_driver_discern_manage`
(
    `id`                   varchar(64) NOT NULL,
    `vehicle_id`           varchar(64) NOT NULL COMMENT '车辆id',
    `latest_query_time`    datetime    DEFAULT NULL COMMENT '最近查询时间',
    `query_success_time`   datetime    DEFAULT NULL COMMENT '查询成功时间',
    `latest_issue_time`    datetime    DEFAULT NULL COMMENT '最近下发时间',
    `issue_status`         smallint(6) DEFAULT NULL COMMENT '下发状态 0:等待下发; 1:下发失败; 2:下发中; 3:下发成功',
    `issue_result`         smallint(6) DEFAULT NULL COMMENT '下发结果 0:终端已应答，1:终端未应答，2:终端离线',
    `query_result`         smallint(6) DEFAULT NULL COMMENT '查询结果 0:终端已应答，1:终端未应答，2:终端离线',
    `issue_username`       varchar(20) DEFAULT NULL COMMENT '下发人',
    `flag`                 smallint(6) DEFAULT '1',
    `create_data_time`     datetime    DEFAULT NULL,
    `create_data_username` varchar(45) DEFAULT NULL,
    `update_data_time`     datetime    DEFAULT NULL,
    `update_data_username` varchar(45) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_vid` (`vehicle_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='终端驾驶员识别管理表';

CREATE TABLE `zw_m_vehicle_device_driver`
(
    `vehicle_id`       varchar(64) NOT NULL COMMENT '车辆ID',
    `professionals_id` varchar(64) NOT NULL COMMENT '从业人员ID',
    PRIMARY KEY (`vehicle_id`, `professionals_id`),
    KEY `idx_vid` (`vehicle_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='终端驾驶员与车辆关联表';

RENAME TABLE zw_m_809_message TO zw_m_809_message_old;
ALTER TABLE zw_m_809_message_old ADD INDEX idx_time(`time`);

CREATE TABLE `zw_m_809_message` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'id',
  `platform_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '转发平台id',
  `group_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收消息的企业id',
  `type` tinyint(4) NOT NULL COMMENT '消息类型，0：标准809查岗，1：标准809督办 2:西藏809查岗 3:西藏809督办',
  `brand` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色',
  `warn_type` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '报警类型(809)',
  `alarm_type` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '报警类型(808)',
  `warn_time` bigint(20) DEFAULT NULL COMMENT '报警时间（上报时间）',
  `warn_src` smallint(6) DEFAULT NULL COMMENT '报警来源',
  `supervision_level` smallint(6) DEFAULT NULL COMMENT '督办等级',
  `supervisor` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '督办/查岗人',
  `supervision_tel` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '督办联系电话',
  `supervision_email` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '督办联系邮件',
  `info_content` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '查岗/督办内容',
  `enterprise` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '西藏运输企业名称',
  `time` datetime NOT NULL COMMENT '查岗/督办时间',
  `ack_time` datetime DEFAULT NULL COMMENT '处理时间',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `result` tinyint(4) DEFAULT NULL COMMENT '处理结果 0：未处理，1：已处理，2：已过期',
  `ack_content` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '应答内容',
  `dealer` varchar(50) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '处理人',
  `alarm_start_time` bigint(20) DEFAULT NULL COMMENT '报警开始时间',
  `source_msg_sn` int(10) DEFAULT NULL COMMENT '源消息编号',
  `source_data_type` int(10) DEFAULT NULL COMMENT '源数据类型',
  `event_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '事件id',
  `info_id` int(10) DEFAULT NULL COMMENT '信息id',
  `monitor_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '监控对象id',
  `object_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '对象id',
  `object_type` int(10) DEFAULT NULL COMMENT '对象类型',
  `supervision_id` int(10) DEFAULT NULL,
  `data_type` int(10) DEFAULT NULL COMMENT '数据类型',
  `handle_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `msg_gnss_center_id` int(10) DEFAULT NULL,
  `msg_id` int(10) DEFAULT NULL COMMENT '消息类型',
  `msg_sn` int(10) DEFAULT NULL COMMENT '消息编号',
  `protocol_type` int(10) DEFAULT NULL COMMENT '协议类型',
  `server_ip` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '上级ip',
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_time` (`time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上级平台消息处理表';