# Pivonia [![Build Status](https://travis-ci.org/phisikus/pivonia.svg?branch=master)](https://travis-ci.org/phisikus/pivonia)
It is a small communication framework that gives you the ability to connect nodes over TCP with optional encryption and manage pools of connections with different nodes identified by ID and managed with heartbeat protocol.

Used technologies:

- Java 11
- Jackson with BSON support (serialization to binary json)
- RxJava 2 (for exposing events)
- Spock + Groovy (testing)
- Dagger 2 (dependency injection)
- Google Tink (symmetrical encryption of messages)
- Resilience4j (for retrying operations)
- vavr (for functional features)
- Lombok

## Basic Usage
In this example an instance of the framework is created. Factory functions are used to provide server and client instances. The two are connected and predefined message handler is used to notify if message is sent.
```java
  import eu.phisikus.pivonia.api.EmptyEnvelope;
  import eu.phisikus.pivonia.logic.MessageHandler;
  import eu.phisikus.pivonia.logic.MessageHandlers;
  import eu.phisikus.pivonia.node.Node;
  import io.reactivex.subjects.PublishSubject;
  
  import java.util.UUID;
  
  public class Main {
      public static void main(String[] args) throws Exception {
      
          // generate ID of some type for node that we are creating
          var nodeId = UUID.randomUUID();        
  
          // we are going to save a stream of received messages
          var messages = PublishSubject.create();
  
          // you can register message handlers for different types of messages
          var messageHandlers = MessageHandlers.create()
                  .withHandler(
                          MessageHandler.create(EmptyEnvelope.class, (node, event) -> messages.onNext(event.getMessage()))
                  );
          // now we create instance of the framework by setting ID and message handlers
          var node = Node.builder()
                  .id(nodeId)
                  .messageHandlers(messageHandlers)
                  .build();
  
          // here we retrieve TCP Server instance and bind it on port 8888
          int port = 8888;
          var server = node.getServer().bind(port).get();
  
          // here we are creating client and making a connection
          var client = node.getClient().connect("localhost", port).get();
  
          // time to send some dummy message
          var newMessage = new EmptyEnvelope<>(nodeId, nodeId);
          client.send(newMessage);
  
          // here we wait for message that should be saved in the stream by message handler above
          assert messages.blockingFirst() == newMessage;
  
          // both client and server shared message handlers
          // we can dispose their resources
          client.close();
          server.close();
          node.dispose();
  
      }
  }

```


## Repository

Packages are published in a GitHub artifactory which requires authentication.
To generate key/token go to _Settings -> Developer settings -> Personal access token_
```groovy
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/phisikus/pivonia")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
        }
    }
}
```     
Current version dependency:
```groovy
implementation "eu.phisikus.pivonia:pivonia:0.0.29"
```

If you have issues with configuration you can read more in the [GitHub Packages official documentation](https://help.github.com/en/packages/using-github-packages-with-your-projects-ecosystem) 


## Architecture

The easiest way to start is to create _Node_ instance using static builder. It acts as a factory for instances of objects like _Server_ or _Client_ that allow you to listen for connections and create them yourself.
Creation of _Node_ instance forces you to pass _MessageHandlers_ which is basically a collection of lambda expressions that handle incoming messages of defined types. In a simple scenario you could also use _getMessages(type)_ on your _Client_ or _Server_ instance to get _Observable_ message source of certain type. In a more complex scenario you would keep the logic in previously defined _MessageHandlers_ and use _ConnectionManager_ which consists of following elements connected together:
- _Address Pool_ - contains information about host + port pairs. It will be used to connect clients to other nodes
- _Transmitter Pool_ - contains connected clients identified by some node ID of your choice
- _Client Heartbeat Pool_ - it uses clients from the Transmitter Pool and sends periodical messages with node ID
- _Server Pool_ - contains your node's listening server instances
- _Server Heartbeat Pool_ - responsible for responding to client heartbeat messages in timely fashion.

Basically if you add some host information to _Address Pool_ it will connect a client, put it in _Transmitter Pool_ and this will trigger _Client Heartbeat Pool_ to send heartbeat messages. Responses will allow nodes to introduce themselves by ID. _Client_ instances are associated with node IDs and that information is stored in the _Transmitter Pool_. Similar situation appears when you add server to the _Server Pool_. Your server starts to respond with heartbeat messages sent by clients and it creates association by node ID in the _Transmitter Pool_. Any heartbeat timeouts force the ID association to be removed.

Have a look at integrations tests and javadocs for more details.

## Pool usage example
In this example server is created and added to the pool which causes it to respond to heartbeat messages properly. That server's address is added to the pool which causes client to be connected, heartbeat message to be sent, response to be returned by the server and as a result of that handshake dummy application message is sent.
```java
public class Main {
    public static void main(String[] args) throws Exception {

        // generate ID of some type for node that we are creating
        var nodeId = UUID.randomUUID();

        // we are going to save a stream of received messages
        var messages = PublishSubject.create();

        // you can register message handlers for different types of messages
        var messageHandlers = MessageHandlers.create()
                .withHandler(
                        MessageHandler.create(EmptyEnvelope.class, (node, event) -> messages.onNext(event.getMessage()))
                );
        // now we create instance of the framework by setting ID and message handlers
        var node = Node.builder()
                .id(nodeId)
                .messageHandlers(messageHandlers)
                .build();

        var connectionManager = node.getConnectionManager();

        // here we retrieve TCP Server instance and bind it on port 9999
        int port = 9999;
        var server = node.getServer().bind(port).get();

        // and we add it to the server pool
        connectionManager.getServerPool().add(server);

        // let's prepare test message
        var newMessage = new EmptyEnvelope<>(nodeId, nodeId);
        var clientHeartbeatPool = connectionManager.getClientHeartbeatPool();

        // ... and send that message once heartbeat message is returned to the client
        var subscription = clientHeartbeatPool.getHeartbeatChanges()
                .filter(event -> event instanceof ReceivedEvent)
                .subscribe(event -> event.getTransmitter().send(newMessage));

        // here we are adding address to the pool so a client is created
        // this will cause the client to be added to heartbeat pool
        // heartbeat message will be sent and server will reply
        // appropriate event will be emitted and test message will be sent
        connectionManager.getAddressPool().add("localhost", port);

        // here we wait for message that should reach the server
        assert messages.blockingFirst() == newMessage;

        // let's clean up the resources
        node.dispose();
        subscription.dispose();
    }
}

```


  
