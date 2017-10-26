package com.hazelcast.samples.jcache.timestable;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.stereotype.Component;

import com.hazelcast.cache.ICache;
import com.hazelcast.core.Client;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Commands additional to the defaults provided
 * by Spring Shell.
 * </p>
 */
@Component
@Slf4j
public class CLI implements CommandMarker {

	@Autowired
	private HazelcastInstance hazelcastInstance;

	/**
	 * <p>List the clients connected to the grid.
	 * </p>
	 */
    @CliCommand(value = "clients",
			help = "List the connected clients")
    public void clients() {
		log.info("-----------------------");

		Collection<Client> clients
			= this.hazelcastInstance.getClientService().getConnectedClients();
		
		for (Client client : clients) {
			log.info("Client => name '{}'", client);
		}
		
		if (clients.size() > 0) {
			log.info("-----------------------");
		}
		log.info("[{} client{}]", 
				clients.size(),
				(clients.size()==1 ? "": "s")
				);
		log.info("-----------------------");
    }		
		
	/**
	 * <p>List the distributed objects in the grid.
	 * </p>
	 */
    @SuppressWarnings("resource")
	@CliCommand(value = "list",
			help = "List distributed objects")
    public void list() {
		log.info("-----------------------");
		
		Collection<DistributedObject> distributedObjects
			= this.hazelcastInstance.getDistributedObjects();
		
		for (DistributedObject distributedObject : distributedObjects) {
			log.info("Distributed Object => name '{}'", distributedObject.getName());
			
			if (distributedObject instanceof ICache) {
				ICache<?, ?> iCache = (ICache<?, ?>) distributedObject;
				log.info(" -> ICACHE => size {}", iCache.size());
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
