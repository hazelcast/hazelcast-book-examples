import com.hazelcast.core.DistributedObject;
import com.hazelcast.internal.services.ManagedService;
import com.hazelcast.spi.impl.NodeEngine;
import com.hazelcast.internal.services.RemoteService;
import com.hazelcast.spi.impl.operationservice.Operation;
import com.hazelcast.spi.partition.MigrationAwareService;
import com.hazelcast.spi.partition.MigrationEndpoint;
import com.hazelcast.spi.partition.PartitionMigrationEvent;
import com.hazelcast.spi.partition.PartitionReplicationEvent;

import java.util.Map;
import java.util.Properties;

public class CounterService implements ManagedService, RemoteService, MigrationAwareService {

    static final String NAME = "CounterService";

    Container[] containers;
    private NodeEngine nodeEngine;

    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
        containers = new Container[nodeEngine.getPartitionService().getPartitionCount()];
        for (int i = 0; i < containers.length; i++) {
            containers[i] = new Container();
        }
    }

    @Override
    public void shutdown(boolean b) {
    }

    @Override
    public DistributedObject createDistributedObject(String objectId) {
        return new CounterProxy(objectId, nodeEngine);
    }

    @Override
    public void destroyDistributedObject(String s) {
    }

    @Override
    public void beforeMigration(PartitionMigrationEvent partitionMigrationEvent) {
    }

    @Override
    public Operation prepareReplicationOperation(PartitionReplicationEvent e) {
        if (e.getReplicaIndex() > 1) {
            return null;
        }

        Container container = containers[e.getPartitionId()];
        Map<String, Integer> migrationData = container.toMigrationData();
        if (migrationData.isEmpty()) {
            return null;
        }
        return new CounterMigrationOperation(migrationData);
    }

    @Override
    public void commitMigration(PartitionMigrationEvent e) {
        if (e.getMigrationEndpoint() == MigrationEndpoint.SOURCE) {
            containers[e.getPartitionId()].clear();
        }
    }

    @Override
    public void rollbackMigration(PartitionMigrationEvent e) {
        if (e.getMigrationEndpoint() == MigrationEndpoint.DESTINATION) {
            containers[e.getPartitionId()].clear();
        }
    }

    @Override
    public void reset() {
    }
}
