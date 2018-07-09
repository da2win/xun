package com.da2win.xunwu.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author Darwin
 * @date 2018/7/9
 */
@Configuration
public class ElasticSearchConfiguration {
    @Bean
    public TransportClient esClient() throws UnknownHostException {
        Settings settings = Settings.builder()
                .put("cluster.name", "da2win")
                .put("client.transport.sniff", true)
                .build();
        InetSocketTransportAddress master = new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300);
        TransportClient client = new PreBuiltTransportClient(setting);
    }
}
