package eu.phisikus.pivonia.pool.server;

import eu.phisikus.pivonia.api.Server;
import eu.phisikus.pivonia.pool.ServerPool;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Log4j2
public class ServerPoolImpl implements ServerPool {
    @Getter
    private final List<Server> servers = new CopyOnWriteArrayList<>();
    private final Subject<ServerPoolEvent> serverChanges = PublishSubject.create();
    @Getter
    private boolean isDisposed = false;

    @Override
    public void add(Server server) {
        servers.add(server);
        var additionEvent = new ServerPoolEvent(server, ServerPoolEvent.Operation.ADD);
        serverChanges.onNext(additionEvent);
    }

    @Override
    public void remove(Server server) {
        servers.remove(server);
        var deletionEvent = new ServerPoolEvent(server, ServerPoolEvent.Operation.REMOVE);
        serverChanges.onNext(deletionEvent);
    }

    @Override
    public Observable<ServerPoolEvent> getChanges() {
        return serverChanges;
    }

    @Override
    public void dispose() {
        if (!isDisposed) {
            servers.forEach(server -> {
                try {
                    server.close();
                } catch (Exception exception) {
                    log.debug(exception);
                }
            });
        }
        isDisposed = true;
    }

}
