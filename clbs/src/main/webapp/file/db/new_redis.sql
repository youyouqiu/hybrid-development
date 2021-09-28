#废弃企业中间表直接新增添加企业ID
ALTER table zw_m_people_info add org_id varchar(64) COMMENT'企业ID' DEFAULT NULL;
ALTER table zw_m_device_info add org_id varchar(64) COMMENT'企业ID' DEFAULT NULL;
ALTER table zw_m_sim_card_info add org_id varchar(64) COMMENT'企业ID' DEFAULT NULL;
ALTER table zw_m_professionals_info add org_id varchar(64) COMMENT'企业ID' DEFAULT NULL;
ALTER table zw_m_assignment add org_id varchar(64) COMMENT'企业ID' DEFAULT NULL;

#终端信息表企业ID历史数据处理
UPDATE zw_m_device_info d
LEFT JOIN zw_m_device_group g on d.id = g.device_id and g.flag=1
set d.org_id = g.group_id
where d.flag = 1 and ISNULL(d.org_id);

UPDATE zw_m_sim_card_info d
LEFT JOIN zw_m_sim_group g on d.id = g.sim_id and g.flag=1
set d.org_id = g.group_id
where d.flag = 1 and ISNULL(d.org_id);


UPDATE zw_m_people_info d
LEFT JOIN zw_c_people_group g on d.id = g.people_id and g.flag=1
set d.org_id = g.group_id
where d.flag = 1 and ISNULL(d.org_id);

UPDATE zw_m_professionals_info d
LEFT JOIN zw_c_professionals_group g on d.id = g.professionals_id and g.flag=1
set d.org_id = g.group_id
where d.flag = 1 and ISNULL(d.org_id);

UPDATE zw_m_assignment d
    LEFT JOIN zw_m_assignment_group g on d.id = g.assignment_id and g.flag=1
set d.org_id = g.group_id
where d.flag = 1 and ISNULL(d.org_id);

create index index_orgId_flag   on clbs.zw_m_assignment(org_id,flag);


DROP TABLE IF EXISTS zw_c_people_group;
DROP TABLE IF EXISTS zw_c_professionals_group;
DROP TABLE IF EXISTS zw_c_vehicle_group;
DROP TABLE IF EXISTS zw_m_assignment_group;
DROP TABLE IF EXISTS zw_m_device_group;
DROP TABLE IF EXISTS zw_m_sim_group;