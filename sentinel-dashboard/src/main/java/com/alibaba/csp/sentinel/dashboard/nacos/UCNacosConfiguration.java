package com.alibaba.csp.sentinel.dashboard.nacos;

import com.alibaba.csp.sentinel.dashboard.config.AuthProperties;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@Configuration
@EnableConfigurationProperties(UCNacosConfig.class)
public class UCNacosConfiguration {
    private Logger log = LoggerFactory.getLogger(UCNacosConfiguration.class);
    @Autowired
    private UCNacosConfig ucNacosConfig;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void initNacosDiscovery() {
        InetUtils inetUtils = new InetUtils(new InetUtilsProperties());
        String localIp = ucNacosConfig.getClientip();
        if (StringUtils.isEmpty(localIp)) {
            localIp = inetUtils.findFirstNonLoopbackAddress().getHostAddress();
        }
        int port = Integer.parseInt(environment.getProperty("server.port", "8080"));
        String serverName = environment.getProperty("spring.application.name", "sentinel-dashboard");
        log.info("nacos启动:{}, serverName={}, localIp={}, port={}", ucNacosConfig, serverName, localIp, port);
        if(ucNacosConfig.isEnabled()) {
            try {
                Instance instance = new Instance();
                instance.setIp(localIp);//IP
                instance.setPort(port);//端口
                instance.setServiceName(serverName);//服务名
                instance.setEnabled(true);//true: 上线 false: 下线
                instance.setHealthy(true);//健康状态
                instance.setWeight(1);//权重
                instance.addMetadata("nacos-sdk-java-discovery", "true");//元数据
                NamingService namingService = NamingFactory.createNamingService(ucNacosConfig.getAddress());
                namingService.registerInstance(serverName, instance);
            } catch (NacosException e) {
                log.error("", e);
            }
        }
    }
}
