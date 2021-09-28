-- 疲劳-车-日统计表
drop table if EXISTS tire_monitor_day_${yyyymm};
create table if not exists tire_monitor_day_${yyyymm}(
 monitor_id BINARY(16) not null,
 day bigint not null,
 monitor_name varchar ,
 org_id BINARY(16) ,
 org_name VARCHAR ,
 PLATE_COLOR VARCHAR,
 vehicle_Type varchar,
 cnt_sum integer,
 cnt_day integer,
 cnt_night integer,
 cnt_acc integer
 CONSTRAINT PK PRIMARY KEY (monitor_id,day)
)COLUMN_ENCODED_BYTES= 0,
COMPRESSION='SNAPPY';

-- 疲劳-企业-日统计表
drop table if EXISTS tire_org_day_${yyyymm};
create table if not exists tire_org_day_${yyyymm}(
 org_id BINARY(16) not null,
 day bigint not null,
 org_name VARCHAR ,
 cnt_sum integer,
 cnt_day integer,
 cnt_night integer,
 cnt_acc integer,
 cnt_monitor integer,
 CONSTRAINT PK PRIMARY KEY (org_id,day)
)COLUMN_ENCODED_BYTES= 0,
COMPRESSION='SNAPPY';

-- 超速-车-日统计表
drop table if EXISTS overspeed_monitor_day_${yyyymm};
create table if not exists overspeed_monitor_day_${yyyymm}(
 monitor_id BINARY(16) not null,
 day bigint not null,
 monitor_name varchar ,
 org_id BINARY(16) ,
 org_name VARCHAR ,
 PLATE_COLOR VARCHAR,
 vehicle_Type varchar,
 cnt_sum integer,
 cnt_p_20_t_5 integer,
 cnt_p_20_t_10 integer,
 cnt_p_20_t_max integer,
 cnt_p_50_t_5 integer,
 cnt_p_50_t_10 integer,
 cnt_p_50_t_max integer,
 cnt_p_max_5 integer,
 cnt_p_max_10 integer,
 cnt_p_max_t_max integer
 CONSTRAINT PK PRIMARY KEY (monitor_id,day)
)COLUMN_ENCODED_BYTES= 0,
COMPRESSION='SNAPPY';

-- 超速-企业-日统计表
drop table if EXISTS overspeed_org_day_${yyyymm};
create table if not exists overspeed_org_day_${yyyymm}(
 org_id BINARY(16) not null,
 day bigint not null,
 org_name VARCHAR ,
 cnt_sum integer,
 cnt_p_20_t_5 integer,
 cnt_p_20_t_10 integer,
 cnt_p_20_t_max integer,
 cnt_p_50_t_5 integer,
 cnt_p_50_t_10 integer,
 cnt_p_50_t_max integer,
 cnt_p_max_5 integer,
 cnt_p_max_10 integer,
 cnt_p_max_t_max integer,
 cnt_monitor integer
 CONSTRAINT PK PRIMARY KEY (org_id,day)
)COLUMN_ENCODED_BYTES= 0,
COMPRESSION='SNAPPY';
