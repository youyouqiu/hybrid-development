package com.zw.config;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * es连接配置管理
 * @author zhangjuan
 */
@Configuration
public class RestHighLevelClientConfig {
    @Value("${elasticsearch.cluster}")
    private String[] cluster;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        HttpHost[] httpPostArr = new HttpHost[cluster.length];
        for (int i = 0; i < cluster.length; i++) {
            HttpHost httpHost =
                new HttpHost(cluster[i].split(":")[0].trim(), Integer.parseInt(cluster[i].split(":")[1].trim()),
                    "http");
            httpPostArr[i] = httpHost;
        }

        RestClientBuilder builder = RestClient.builder(httpPostArr).setMaxRetryTimeoutMillis(5 * 60 * 1000);
        builder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(1000);
            requestConfigBuilder.setSocketTimeout(300000);
            requestConfigBuilder.setConnectionRequestTimeout(0);
            return requestConfigBuilder;
        });

        // 异步httpclient配置
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            RequestConfig.Builder requestConfigBuilder =
                RequestConfig.custom().setConnectTimeout(5 * 60 * 1000)//超时时间5分钟
                    .setSocketTimeout(5 * 60 * 1000)//这就是Socket超时时间设置
                    .setConnectionRequestTimeout(0);
            httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
            return httpClientBuilder;
        });
        return new RestHighLevelClient(builder);

    }

}
