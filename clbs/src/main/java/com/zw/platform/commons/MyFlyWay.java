package com.zw.platform.commons;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Profile({ "dev", "37" })
@Component
@DependsOn(value = "mysqlDataSource")
public class MyFlyWay {

    @Autowired
    private DruidDataSource dataSource;

    private Logger logger = LogManager.getLogger(MyFlyWay.class);

    @PostConstruct
    public void flyWayConfig() {
        Properties config = new Properties();
        try {
            config.load(MyFlyWay.class.getClassLoader().getResourceAsStream("flyway.properties"));
            if ("true".equals(config.getProperty("flyway.enabled"))) {
                // 创建flyway实例和指向数据库
                Flyway flyway = Flyway.configure().baselineOnMigrate(true)
                    .baselineVersion(config.getProperty("flyway.baselineVersion"))
                    .locations(config.getProperty("flyway.locations"))
                    .outOfOrder("true".equals(config.getProperty("flyway.outOfOrder"))).dataSource(dataSource)
                    .encoding("UTF-8").load();
                // 开始迁移
                flyway.migrate();
            }
        } catch (Exception e) {
            logger.error("flyway初始异常", e);
        }
    }
}
