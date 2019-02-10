package eu.phisikus.pivonia.pool;

import dagger.Component;

@Component(modules = PoolModule.class)
public interface PoolComponent {
    ConnectionManager getConnectionManager();
}
