USE clbs;

ALTER TABLE `clbs`.`zw_m_video_channel_setting`
    ADD COLUMN `panoramic` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否360全景 0:否 1:是;(通道类型为0和2时，此字段有效)' AFTER `connection_flag`;

CREATE TABLE `zw_log_201706` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201707` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201708` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201709` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201710` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201711` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201712` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201801` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201802` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201803` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201804` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201805` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201806` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201807` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201808` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201809` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201810` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201811` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201812` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201901` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201902` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201903` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201904` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201905` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201906` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201907` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201908` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201909` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201910` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201911` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_201912` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202001` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202002` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202003` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202004` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202005` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202006` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202007` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202008` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202009` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202010` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202011` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202012` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202101` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202102` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202103` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202104` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202105` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202106` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202107` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202108` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202109` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202110` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202111` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202112` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202201` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202202` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202203` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202204` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202205` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202206` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202207` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202208` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202209` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202210` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202211` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202212` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202301` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202302` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202303` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202304` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202305` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202306` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202307` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202308` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202309` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202310` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202311` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202312` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202401` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202402` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202403` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202404` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202405` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202406` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202407` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202408` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202409` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202410` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202411` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202412` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202501` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202502` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202503` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202504` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202505` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202506` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202507` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202508` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202509` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202510` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202511` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202512` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202601` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202602` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202603` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202604` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202605` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202606` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202607` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202608` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202609` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202610` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202611` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202612` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202701` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202702` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202703` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202704` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';
CREATE TABLE `zw_log_202705` (
  `id` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '仅用于分页排序，不用作主键(减少碎片化)',
  `log_type` varchar(10) DEFAULT NULL COMMENT '类型',
  `message` longtext CHARACTER SET utf8mb4 COMMENT '内容',
  `exception` varchar(100) DEFAULT NULL COMMENT '异常',
  `event_date` datetime NOT NULL COMMENT '时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP',
  `username` varchar(45) DEFAULT NULL COMMENT '操作用户',
  `org_id` varchar(100) DEFAULT NULL COMMENT '日志所属企业',
  `module` varchar(25) DEFAULT NULL COMMENT '操作日志模块：MONITORING:实时监控页面',
  `log_source` varchar(25) DEFAULT NULL COMMENT '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作',
  `monitoring_operation` varchar(50) DEFAULT NULL COMMENT '监控对象操作',
  `brand` varchar(45) DEFAULT NULL COMMENT '监控对象名称',
  `plate_color` smallint(6) DEFAULT NULL COMMENT '车牌颜色（1蓝，2黄，3黑，4白，9其他）',
  KEY `INDEX_LOG` (`event_date`,`module`,`org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';

# 自定义列增加是否补传字段
UPDATE `zw_c_custom_column` SET sort = sort + 1 WHERE id IN ("6676748c-a450-11e9-9469-000c29920fdc", "667a6764-a450-11e9-9469-000c29920fdc", "667e1c97-a450-11e9-9469-000c29920fdc");
INSERT INTO `zw_c_custom_column`(`id`, `title`, `name`, `mark`, `status`, `sort`, `column_module`, `init_value`) VALUES ('63b3f5ae-14cc-11eb-a5a9-000c294a3302', '是否补传', 'reissue', 'TRACKPLAY_DATA', 0, 16, 'TRACKPLAY', NULL);

# 新增后处理报警图片标识位
ALTER TABLE `zw_m_config` ADD COLUMN `pic_postprocess` bit(1) NULL DEFAULT b'0' COMMENT '是否后处理主动安全报警图片' AFTER `access_network`;


# 以下SQL让增值服务菜单对admin和管理员角色可见，但启用与否不会变（已启用的不受影响，不可见的变为可见且未启用）
INSERT IGNORE INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`) VALUES ('535c1d76-b624-41a6-8560-1d91414ace57', 'arrow fa fa-angle-right pull-right', '上报里程统计', '0', NULL, 'reportMileageStatistics', 'javascript:void(0);', NULL, 'f9602388-02ec-4d1a-b40f-92aee2ebea46', '1174', '1', '1', NULL, NULL, NULL, NULL, NULL, '110074', '1');
INSERT IGNORE INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`) VALUES ('851b050b-68f4-4529-b461-46c882e3e0ef', 'arrow fa fa-angle-right pull-right', '站点管理', '0', NULL, 'stationManage', 'javascript:void(0);', NULL, 'f9602388-02ec-4d1a-b40f-92aee2ebea46', '1172', '1', '1', NULL, NULL, NULL, NULL, NULL, '110072', '1');
INSERT IGNORE INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`) VALUES ('2a149552-b5b8-42bd-8d4d-b07fdd556888', 'arrow fa fa-angle-right pull-right', '定位信息统计', '0', NULL, 'statisticalCheckOfLocationInformation', 'javascript:void(0);', NULL, 'f9602388-02ec-4d1a-b40f-92aee2ebea46', '1175', '1', '1', NULL, NULL, NULL, NULL, NULL, '110075', '1');
INSERT IGNORE INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`) VALUES ('b4ec118a-a949-437e-a91b-eb08a950ca53', 'arrow fa fa-angle-right pull-right', '线路管理', '0', NULL, 'lineManage', 'javascript:void(0);', NULL, 'f9602388-02ec-4d1a-b40f-92aee2ebea46', '1173', '1', '1', NULL, NULL, NULL, NULL, NULL, '110073', '1');
INSERT IGNORE INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`) VALUES ('d648c829-df51-4770-9207-31a948bacfcc', 'arrow fa fa-angle-right pull-right', '转发车辆管理', '0', NULL, 'forwardVehicleManage', 'javascript:void(0);', NULL, 'f9602388-02ec-4d1a-b40f-92aee2ebea46', '1171', '1', '1', NULL, NULL, NULL, NULL, NULL, '110071', '1');
REPLACE INTO `zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('58ef2095-c09a-4ffc-bde7-4f0536cc1003', '535c1d76-b624-41a6-8560-1d91414ace57', 'cn=ROLE_ADMIN,ou=Groups', '2021-01-07 10:49:22', 'admin', NULL, NULL, 1, 1);
REPLACE INTO `zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('9a8df61c-b8df-494a-a447-6e44dd33bcc4', '851b050b-68f4-4529-b461-46c882e3e0ef', 'cn=ROLE_ADMIN,ou=Groups', '2021-01-07 10:49:22', 'admin', NULL, NULL, 1, 1);
REPLACE INTO `zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('6ad8810c-a713-42fc-8660-cf728e444c87', '2a149552-b5b8-42bd-8d4d-b07fdd556888', 'cn=ROLE_ADMIN,ou=Groups', '2021-01-07 10:49:22', 'admin', NULL, NULL, 1, 1);
REPLACE INTO `zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('2f3d86ee-812b-4436-9263-b43046917330', 'b4ec118a-a949-437e-a91b-eb08a950ca53', 'cn=ROLE_ADMIN,ou=Groups', '2021-01-07 10:49:22', 'admin', NULL, NULL, 1, 1);
REPLACE INTO `zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`)
VALUES ('186c5350-24bd-4e54-b500-71f2cf7ca58c', 'd648c829-df51-4770-9207-31a948bacfcc', 'cn=ROLE_ADMIN,ou=Groups', '2021-01-07 10:49:22', 'admin', NULL, NULL, 1, 1);
INSERT IGNORE INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`) VALUES ('37108d89-3fea-11e9-9b4f-00163e2cf72b', NULL, '温湿度监控', '0', NULL, 'humitureMonitor', 'javascript:void(0);', NULL, '8dd33331-2016-4f85-9e8d-958b4ee79535', '1106', '1', '1', NULL, NULL, NULL, NULL, NULL, '110006', '1');
REPLACE INTO `zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES ('a4d46b62-8677-4f81-b179-acb9678a39d0', '37108d89-3fea-11e9-9b4f-00163e2cf72b', 'cn=ROLE_ADMIN,ou=Groups', '2021-01-25 10:52:55', 'admin', NULL, NULL, '1', '1');
INSERT IGNORE INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`) VALUES ('7c558b1d-2377-472f-bd08-f29d0a1f83ad', NULL, '网约车转发管理', '0', NULL, 'netCarForwarding', 'javascript:void(0);', NULL, '8dd33331-2016-4f85-9e8d-958b4ee79535', '1114', '1', '1', NULL, NULL, NULL, NULL, NULL, '110014', '1');
INSERT IGNORE INTO `zw_c_resource` (`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`) VALUES ('f9c7729c-f40e-11e7-bc74-92361f003671', NULL, '浙江入网证明转发', '0', NULL, 'netAccessProveForward', 'javascript:void(0);', NULL, '8dd33331-2016-4f85-9e8d-958b4ee79535', '1128', '1', '1', NULL, NULL, NULL, NULL, NULL, '110028', '1');
REPLACE INTO `zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES ('34293f24-37b0-431b-8810-581948ecbcfc', '7c558b1d-2377-472f-bd08-f29d0a1f83ad', 'cn=ROLE_ADMIN,ou=Groups', '2021-03-09 15:17:24', 'admin', NULL, NULL, '1', '1');
REPLACE INTO `zw_c_role_resource` (`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES ('a4d1c867-80af-11eb-8872-000c29dcff33', 'f9c7729c-f40e-11e7-bc74-92361f003671', 'cn=ROLE_ADMIN,ou=Groups', '2021-03-09 15:17:24', 'admin', NULL, NULL, '1', '1');
INSERT IGNORE INTO `zw_c_resource`(`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`) VALUES ('25e89331-6724-d368-ba4e-a30e5ff7c2e2', 'arrow fa fa-angle-right pull-right', '补传管理', 0, NULL, 'subsidyManage', 'javascript:void(0);', NULL, 'f9602388-02ec-4d1a-b40f-92aee2ebea46', 1172, 1, 1, NULL, NULL, NULL, NULL, NULL, '110072', 1);
REPLACE INTO `zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES ('3a9b38b2-2777-b501-26ec-531ad9256444', '25e89331-6724-d368-ba4e-a30e5ff7c2e2', 'cn=ROLE_ADMIN,ou=Groups', '2021-03-25 16:54:13', 'admin', NULL, NULL, 1, 1);
REPLACE INTO `zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES ('2ad868a2-bf97-f435-9b28-18835b1c9255', '25e89331-6724-d368-ba4e-a30e5ff7c2e2', 'cn=POWER_USER,ou=Groups', '2021-03-25 16:54:13', 'admin', NULL, NULL, 1, 1);
INSERT IGNORE INTO `zw_c_resource`(`id`, `icon_cls`, `resource_name`, `type`, `code`, `permission`, `perm_value`, `description`, `parent_id`, `sort_order`, `editable`, `enabled`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `domain`, `code_num`, `flag`) VALUES ('f9c7729c-f40e-11e7-bc74-92361f003672', NULL, '报警图片处理管理', 0, NULL, 'adasPicPostprocess', 'javascript:void(0);', NULL, '8dd33331-2016-4f85-9e8d-958b4ee79535', 1132, 1, 1, NULL, NULL, NULL, NULL, NULL, '110032', 1);
REPLACE INTO `zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES ('3a9b38b2-2777-b501-26ec-531ad9256444', 'f9c7729c-f40e-11e7-bc74-92361f003672', 'cn=ROLE_ADMIN,ou=Groups', '2021-03-25 16:54:13', 'admin', NULL, NULL, 1, 1);
REPLACE INTO `zw_c_role_resource`(`id`, `resource_id`, `role_id`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`, `flag`, `editable`) VALUES ('2ad868a2-bf97-f435-9b28-18835b1c9255', 'f9c7729c-f40e-11e7-bc74-92361f003672', 'cn=POWER_USER,ou=Groups', '2021-03-25 16:54:13', 'admin', NULL, NULL, 1, 1);

CREATE TABLE `zw_m_vehicle_ic_history` (
  `id` bigint(10) NOT NULL AUTO_INCREMENT,
  `vehicle_id` varchar(64) CHARACTER SET ascii NOT NULL COMMENT '车辆id',
  `driver_name` varchar(255) NOT NULL COMMENT '驾驶员姓名',
  `identification_number` varchar(255) NOT NULL COMMENT '身份唯一标识',
  `create_data_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniqueUnionId` (`vehicle_id`,`identification_number`)
) ENGINE=InnoDB AUTO_INCREMENT=4941 DEFAULT CHARSET=utf8mb4;

UPDATE `zw_m_vehicle_purpose` SET purpose_category='营运性危险货物运输' WHERE id = '65f52144-2e9a-496e-afb9-5853157a4411';
UPDATE `zw_m_vehicle_purpose` SET purpose_category='非营运性危险货物运输' WHERE id = '65f52144-2e9a-496e-afb9-5853157a4412';
