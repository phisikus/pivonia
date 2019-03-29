package eu.phisikus.pivonia.pool.server;

import eu.phisikus.pivonia.api.Server;
import eu.phisikus.pivonia.pool.ServerPool;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerPoolImpl implements ServerPool {
    @Getter
    private final List<Server> servers = new CopyOnWriteArrayList<>();
    private final Subject<ServerEvent> serverChanges = PublishSubject.create();

    @Override
    public void add(Server server) {
        servers.add(server);
        var additionEvent = new ServerEvent(server, ServerEvent.Operation.ADD);
        serverChanges.onNext(additionEvent);
    }

    @Override
    public void remove(Server server) {
        servers.remove(server);
        var deletionEvent = new ServerEvent(server, ServerEvent.Operation.REMOVE);
        serverChanges.onNext(deletionEvent);
    }

    @Override
    public Observable<ServerEvent> getServerChanges() {
        return serverChanges;
    }
}
