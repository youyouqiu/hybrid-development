package com.zw.config;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * 分片配置 = 数据源 + 分片规则（都可以是多个）
 * <p>分片规则 = 逻辑表映射 + 分片算法
 * <p>逻辑表映射 = 库映射 + 表映射（数据源.[真实表名] -> 逻辑表名） + 分表键
 * <p>分片算法（库映射和表映射用到） = 算法类型 + 算法参数（或自定义算法）
 *
 * @author Zhang Yanhui
 * @since 2021/6/16 8:51
 */

@Configuration
public class ShardingConfig {

    /**
     * @see <a href="https://shardingsphere.apache.org/document/current/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/sharding/#%E6%97%B6%E9%97%B4%E8%8C%83%E5%9B%B4%E5%88%86%E7%89%87%E7%AE%97%E6%B3%95">时间范围分片算法</a>
     */
    @Bean(name = "shardingDataSource")
    public DataSource shardingDataSource(@Qualifier("mysqlDataSource") DataSource dataSource) throws SQLException {
        // 配置逻辑表映射
        Map<String, DataSource> dataSourceMap = Collections.singletonMap("ds0", dataSource);
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration("zw_log",
                // 201706-202705
                "ds0.zw_log_20170$->{6..9},"
                        + "ds0.zw_log_20171$->{0..2},"
                        + "ds0.zw_log_20$->{18..26}0$->{1..9},"
                        + "ds0.zw_log_20$->{16..26}1$->{0..2},"
                        + "ds0.zw_log_20270$->{1..5}");
        tableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration(
                "event_date", "tableShardingAlgorithm"));

        // 配置分片规则
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getTables().add(tableRuleConfig);

        // 配置分表算法
        Properties props = new Properties();
        props.setProperty("datetime-pattern", "yyyy-MM-dd HH:mm:ss");
        props.setProperty("datetime-lower", "2017-06-01 00:00:00");
        props.setProperty("datetime-upper", "2027-05-31 23:59:59");
        props.setProperty("sharding-suffix-pattern", "_yyyyMM");
        props.setProperty("datetime-interval-amount", "1");
        props.setProperty("datetime-interval-unit", "MONTHS");
        final Map<String, ShardingSphereAlgorithmConfiguration> algorithms = ruleConfig.getShardingAlgorithms();
        algorithms.put("tableShardingAlgorithm", new ShardingSphereAlgorithmConfiguration("INTERVAL", props));

        Preconditions.checkArgument(props.containsKey("datetime-pattern"), "% can not be null.", "datetime-pattern");
        return ShardingSphereDataSourceFactory.createDataSource(
                dataSourceMap, Collections.singleton(ruleConfig), new Properties());
    }

}
