package eu.phisikus.pivonia;

import io.vavr.control.Try;

public interface Client {
    Try<Integer> send(Message message);
}
