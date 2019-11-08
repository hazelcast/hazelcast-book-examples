import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.HotRestartPersistenceConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.nio.IOUtil;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.io.File;

import static com.hazelcast.cache.HazelcastCachingProvider.propertiesByInstanceItself;
import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;

/**
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 */
public class JCacheHotRestart {

    private static final String HOT_RESTART_ROOT_DIR = System.getProperty("java.io.tmpdir")
            + File.separatorChar + "hazelcast-hot-restart";

    public static void main(String[] args) {
        IOUtil.delete(new File(HOT_RESTART_ROOT_DIR));

        Config config = new Config();
        config.setLicenseKey(ENTERPRISE_LICENSE_KEY);

        config.getNetworkConfig().setPort(5701).setPortAutoIncrement(false);
        JoinConfig join = config.getNetworkConfig().getJoin();
        join.getMulticastConfig().setEnabled(false);
        join.getTcpIpConfig().setEnabled(true).clear().addMember("127.0.0.1");

        HotRestartPersistenceConfig hotRestartConfig = config.getHotRestartPersistenceConfig();
        hotRestartConfig.setEnabled(true).setBaseDir(new File(HOT_RESTART_ROOT_DIR));

        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);

        Cache<Integer, String> cache = createCache(instance);
        for (int i = 0; i < 10; i++) {
            cache.put(i, "value" + i);
        }

        instance.shutdown();

        instance = Hazelcast.newHazelcastInstance(config);
        cache = createCache(instance);

        for (int i = 0; i < 10; i++) {
            System.out.println("cache.get(" + i + ") = " + cache.get(i));
        }

        Hazelcast.shutdownAll();
    }

    static Cache<Integer, String> createCache(HazelcastInstance instance) {
        CachingProvider cachingProvider = Caching.getCachingProvider(HazelcastServerCachingProvider.class.getName());
        CacheManager cacheManager = cachingProvider.getCacheManager(null, null,
                propertiesByInstanceItself(instance));

        CacheConfig<Integer, String> cacheConfig = new CacheConfig<>("cache");
        cacheConfig.getHotRestartConfig().setEnabled(true);

        return cacheManager.createCache("cache", cacheConfig);
    }
}
