package integration.core.util;

import java.util.Collections;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.events.EventType;
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
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setClientMode(true);

        String igniteHostIP = System.getenv("IGNITE_HOST");
        String ignitePort = System.getenv("IGNITE_PORT");

        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setAddresses(Collections.singletonList(igniteHostIP + ":" + ignitePort));
        cfg.setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(ipFinder));

        CacheConfiguration<String, Object> cacheCfg = new CacheConfiguration<>();
        cacheCfg.setName("eventCache3");
        cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);

        cfg.setCacheConfiguration(cacheCfg);

        Ignite ignite = Ignition.start(cfg);

        // Registering client reconnect listener
        ignite.events().localListen(event -> {
            System.out.println("🔥 Ignite client has reconnected to the cluster.");
            // You can reload cache references or reinit any cluster-dependent services here.
            return true;
        }, EventType.EVT_CLIENT_NODE_RECONNECTED);

        return ignite;
    }
}
