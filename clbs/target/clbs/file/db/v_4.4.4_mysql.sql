USE clbs;

-- 主动安全-风险监管 移至 增值目录下
UPDATE `zw_c_resource` SET parent_id = '8dd33331-2016-4f85-9e8d-958b4ee79535', perm_value = 'javascript:void(0);', sort_order = 1136 WHERE id = 'baf66d4e-0a6e-11ea-9469-000c29920fdc';
-- 屏蔽"燃料管理"菜单
UPDATE `zw_c_resource` SET flag = 1 where id = 'fc87ab62-7d62-11e6-lb12-56b6b6499611';
UPDATE `zw_c_role_resource` SET flag = 1 where resource_id = 'fc87ab62-7d62-11e6-lb12-56b6b6499611' and flag = 1;