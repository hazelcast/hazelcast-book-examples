import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import static java.util.concurrent.TimeUnit.SECONDS;

public class MaxIdleIgnoreMapConfigLiveForever {

    public static void main(String[] args)
            throws InterruptedException {
        Config config = new Config();
        config.addMapConfig(new MapConfig("default").setMaxIdleSeconds(5));

        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        IMap<Integer, String> map = hazelcastInstance.getMap("default");

        map.put(0, "Number Zero");
        // 0 overrides the map config and make the entry not expirable
        map.put(1, "Number One", 0, SECONDS, 0, SECONDS);
        System.out.println("Entry 0: " + map.get(0) + ", Entry 1: " + map.get(1));

        Thread.sleep(SECONDS.toMillis(5));

        if (map.get(0) != null) {
            System.err.println("Entry 0 is still alive!");
        }

        if (map.get(1) != null) {
            System.out.println("Entry 1 is still alive!");
        }

        System.exit(0);
    }

}
