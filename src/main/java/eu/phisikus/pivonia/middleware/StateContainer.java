package eu.phisikus.pivonia.middleware;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a generic container able to store instances identified by their classes.
 */
public class StateContainer {

    Map<Class, Object> containerMap = new ConcurrentHashMap<>();

    /**
     * Retrieve object of a certain type. If it does not exist, return empty.
     *
     * @param type class of object that should be retrieved from the store.
     * @param <T>  generic type of object
     * @return instance wrapped in optional or empty value of that optional
     */
    public <T> Optional<T> get(Class<T> type) {
        return Optional.ofNullable((T) containerMap.get(type));
    }

    /**
     * Insert instance into state container.
     *
     * @param type class type that the instance should be associated with
     * @param stateElement instance
     * @param <T> type of inserted instance
     */
    public <T> void set(Class<T> type, T stateElement) {
        containerMap.put(type, stateElement);
    }

    /**
     * Helper method that provides empty StateContainer.
     * @return empty state container
     */
    public static StateContainer empty() {
        return new StateContainer();
    }

}
