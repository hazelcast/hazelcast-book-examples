package com.hazelcast.samples.spi;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

import lombok.extern.slf4j.Slf4j;

/**
 * <P>A server in the cluster. It doesn't do anything
 * with the data, except a passive inspection on the
 * current distributed objects (won't create any).
 * </p>
 */
@Slf4j
@SpringBootApplication
public class Application implements CommandLineRunner {

	/**
	 * <p>Start Spring. Indirectly, a 
	 * Hazelcast server instance is created in
	 * this JVM by {@link com.hazelcast.samples.spi.ApplicationConfig ApplicationConfig}.
	 * As the Hazelcast instance won't end
	 * unless signalled to, {@code main()}
	 * won't return and the JVM won't shut down.
	 * </p>
	 * 
	 * @param args From command line
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Autowired
	private HazelcastInstance hazelcastInstance;

	/**
	 * <p>Look up the {@link com.hazelcast.core.DistributedObject DistributedObject}
	 * that are currently defined in the cluster. As we are not
	 * accessing by name, this won't trigger the lazy creation.
	 * It only shows the ones currently present.
	 * </p>
	 */
	@Override
	public void run(String... arg0) throws Exception {

		log.info("-----------------------");
		
		Collection<DistributedObject> distributedObjects
			= this.hazelcastInstance.getDistributedObjects();
		
		// Find the distributed queue a different way than by name
		for (DistributedObject distributedObject : distributedObjects) {

			String distributedObjectName = distributedObject.getName();
			String distributedObjectServiceName = distributedObject.getServiceName();

			log.info("Distributed Object, name '{}', service '{}'",
					distributedObjectName,
					distributedObjectServiceName
					);

			// If it's our queue, use one of the operations defined for it
			if (distributedObjectServiceName.equals(MyPriorityQueue.SERVICE_NAME)) {
				MyPriorityQueue<?> myPriorityQueue
					= (MyPriorityQueue<?>) distributedObject;

				log.info(" -> queue size {}", myPriorityQueue.size());
			}
			
			if (distributedObject instanceof IQueue) {
				IQueue<?> iQueue
					= (IQueue<?>) distributedObject;

					log.info(" -> queue size {}", iQueue.size());
			}
		}
		
		if (distributedObjects.size() > 0) {
			log.info("-----------------------");
		}
		
		log.info("[{} distributed object{}]", 
				distributedObjects.size(),
				(distributedObjects.size()==1 ? "": "s")
				);
		
		log.info("-----------------------");
		
	}

}