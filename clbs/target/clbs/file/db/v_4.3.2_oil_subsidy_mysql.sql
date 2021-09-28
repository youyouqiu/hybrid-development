#添加菜单
INSERT INTO `clbs`.`zw_c_resource`(`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('249e14be-f363-471c-a7f3-99eee350a180', 'arrow fa fa-angle-right pull-right', '河南油补转发管理', 1, NULL, 'oilSubsidiesManage', 'javascript:void(0);', NULL, 'f9c65d06-f40e-11e6-bc64-92361f002671', 115, 1, 1, NULL, NULL, NULL, NULL, NULL, '11007', 1);

INSERT INTO `clbs`.`zw_c_resource`(`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('dc67479f-9659-42c7-90ad-af2d240dd429', 'arrow fa fa-angle-right pull-right', '转发车辆管理', 0, NULL, 'forwardVehicleManage', '/m/forward/vehicle/manage/list', NULL, '249e14be-f363-471c-a7f3-99eee350a180', 1171, 1, 1, NULL, NULL, NULL, NULL, NULL, '110071', 1);

INSERT INTO `clbs`.`zw_c_resource`(`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('1778d3c0-e732-4da5-aa4e-7f5f080fd287', 'arrow fa fa-angle-right pull-right', '站点管理', 0, NULL, 'stationManage', '/m/station/manage/list', NULL, '249e14be-f363-471c-a7f3-99eee350a180', 1172, 1, 1, NULL, NULL, NULL, NULL, NULL, '110072', 1);

INSERT INTO `clbs`.`zw_c_resource`(`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('c4d4a9bb-a8a5-4532-8c63-b80bcae4c0b7', 'arrow fa fa-angle-right pull-right', '线路管理', 0, NULL, 'lineManage', '/m/line/manage/list', NULL, '249e14be-f363-471c-a7f3-99eee350a180', 1173, 1, 1, NULL, NULL, NULL, NULL, NULL, '110073', 1);

INSERT INTO `clbs`.`zw_c_resource`(`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('140d01ad-2464-46bd-b1c4-4b0eff2f76e8', 'arrow fa fa-angle-right pull-right', '上报里程统计', 0, NULL, 'reportMileageStatistics', '/m/report/mileage/statistics/list', NULL, '249e14be-f363-471c-a7f3-99eee350a180', 1174, 1, 1, NULL, NULL, NULL, NULL, NULL, '110074', 1);

INSERT INTO `clbs`.`zw_c_resource`(`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`)
VALUES ('1fce86f5-527b-4ca9-ad5c-409c27356471', 'arrow fa fa-angle-right pull-right', '定位信息统计核对', 0, NULL, 'statisticalCheckOfLocationInformation', '/m/statistics/check/locationInformation/list', NULL, '249e14be-f363-471c-a7f3-99eee350a180', 1175, 1, 1, NULL, NULL, NULL, NULL, NULL, '110075', 1);

UPDATE `clbs`.`zw_c_resource` SET `resource_name` = '定位信息统计' WHERE `id` = '1fce86f5-527b-4ca9-ad5c-409c27356471';
#添加菜单权限
INSERT INTO `clbs`.`zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('d13a1146-c132-47a1-9c9a-77d6e2921fe7', '249e14be-f363-471c-a7f3-99eee350a180', 'cn=ROLE_ADMIN,ou=Groups', '2020-08-07 10:49:22', 'admin', NULL, NULL, 1, 0);

INSERT INTO `clbs`.`zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('19fb9dd6-936e-4cce-b741-e1b0901eecd2', 'dc67479f-9659-42c7-90ad-af2d240dd429', 'cn=ROLE_ADMIN,ou=Groups', '2020-08-07 10:49:22', 'admin', NULL, NULL, 1, 1);
INSERT INTO `clbs`.`zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('96042e18-6767-4f97-9efb-8599947728ad', '1778d3c0-e732-4da5-aa4e-7f5f080fd287', 'cn=ROLE_ADMIN,ou=Groups', '2020-08-07 10:49:22', 'admin', NULL, NULL, 1, 1);
INSERT INTO `clbs`.`zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('bab2ff89-a52d-4acc-951d-4f79fbeaaa3f', 'c4d4a9bb-a8a5-4532-8c63-b80bcae4c0b7', 'cn=ROLE_ADMIN,ou=Groups', '2020-08-07 10:49:22', 'admin', NULL, NULL, 1, 1);
INSERT INTO `clbs`.`zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('b83e89f1-0f3a-4d07-ab55-e32021aedf89', '140d01ad-2464-46bd-b1c4-4b0eff2f76e8', 'cn=ROLE_ADMIN,ou=Groups', '2020-08-07 10:49:22', 'admin', NULL, NULL, 1, 1);
INSERT INTO `clbs`.`zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
 VALUES ('5c7e3ccd-2812-49b6-97c3-943d64741a64', '1fce86f5-527b-4ca9-ad5c-409c27356471', 'cn=ROLE_ADMIN,ou=Groups', '2020-08-07 10:49:22', 'admin', NULL, NULL, 1, 1);

INSERT INTO `clbs`.`zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('0ee322d8-61ce-48e6-a9a5-c654c3bc44b0', '249e14be-f363-471c-a7f3-99eee350a180', 'cn=POWER_USER,ou=Groups', '2020-08-07 10:49:22', 'admin', NULL, NULL, 1, 0);

INSERT INTO `clbs`.`zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('a779b715-0313-46da-9c5d-13899a1460c2', 'dc67479f-9659-42c7-90ad-af2d240dd429', 'cn=POWER_USER,ou=Groups', '2020-08-07 10:49:22', 'admin', NULL, NULL, 1, 1);
INSERT INTO `clbs`.`zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('31a355df-7c23-4f71-8708-11e74f2eff70', '1778d3c0-e732-4da5-aa4e-7f5f080fd287', 'cn=POWER_USER,ou=Groups', '2020-08-07 10:49:22', 'admin', NULL, NULL, 1, 1);
INSERT INTO `clbs`.`zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('e3980666-2973-4a6d-b316-65dfaa7a9756', 'c4d4a9bb-a8a5-4532-8c63-b80bcae4c0b7', 'cn=POWER_USER,ou=Groups', '2020-08-07 10:49:22', 'admin', NULL, NULL, 1, 1);
INSERT INTO `clbs`.`zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('ff0dc16d-c8f1-4043-bdb8-de5226de252d', '140d01ad-2464-46bd-b1c4-4b0eff2f76e8', 'cn=POWER_USER,ou=Groups', '2020-08-07 10:49:22', 'admin', NULL, NULL, 1, 1);
INSERT INTO `clbs`.`zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('b04a8b37-6614-4778-a182-be5cd2e303f1', '1fce86f5-527b-4ca9-ad5c-409c27356471', 'cn=POWER_USER,ou=Groups', '2020-08-07 10:49:22', 'admin', NULL, NULL, 1, 1);

#改变菜单顺序
UPDATE `clbs`.`zw_c_resource` SET `sort_order` = 117 WHERE `id` = 'f89767c1-6396-4819-8434-07f0229c8a83';

#添加河南油补协议
INSERT INTO `zw_c_dictionary` ( `id`, `pid`, `code`, `value`, `type`, `description` )
VALUES
( 'eb152bf0-0d8f-4c45-915a-52a1c4773ea0', NULL, '1603', '河南油补809-2011', '809', NULL );