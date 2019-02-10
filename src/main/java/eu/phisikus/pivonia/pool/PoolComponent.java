package eu.phisikus.pivonia.pool;

import dagger.Component;
import eu.phisikus.pivonia.tcp.TCPComponent;

@Component(modules = PoolModule.class, dependencies = TCPComponent.class)
public interface PoolComponent {
    ConnectionManager getConnectionManager();
}
