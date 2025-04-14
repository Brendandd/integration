package integration.core.util;

import java.util.Collections;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Config for Apache Ignite. Ignite is being used as a distributed lock.
 * 
 * @author Brendan Douglas
 */
@Component
public class IgniteConfig {

    @Bean
    public Ignite igniteInstance() {
        // Preparing IgniteConfiguration using Java APIs
        IgniteConfiguration cfg = new IgniteConfiguration();

        // The node will be started as a client node.
        cfg.setClientMode(true);

        // Setting up an IP Finder to ensure the client can locate the servers.
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setAddresses(Collections.singletonList("127.0.0.1:47500..47509"));
        cfg.setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(ipFinder));

        CacheConfiguration<String, Object> cacheCfg = new CacheConfiguration<>();

        cacheCfg.setName("eventCache3");

        cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);

        cfg.setCacheConfiguration(cacheCfg);

        // Starting the node
        Ignite ignite = Ignition.start(cfg);

        return ignite;
    }
}
