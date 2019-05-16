# Pivonia
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
```java
  import eu.phisikus.pivonia.api.EmptyEnvelope;
  import eu.phisikus.pivonia.logic.MessageHandler;
  import eu.phisikus.pivonia.logic.MessageHandlers;
  import eu.phisikus.pivonia.utils.Node;
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
                          MessageHandler.create(EmptyEnvelope.class, (node, message) -> messages.onNext(message))
                  );
          // now we create instance of the framework by setting ID and message handlers
          var node = Node.builder()
                  .id(nodeId)
                  .messageHandlers(messageHandlers)
                  .build();
  
          // here we retreive TCP Server instance and bind it on port 9999
          int port = 9999;
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
  
      }
  }

```


## Architecture

The easiest way to start is to create _Node_ instance using static builder. It acts as a factory for instances of objects like _Server_ or _Client_ that allow you to listen for connections and create them yourself.
Creation of _Node_ instance forces you to pass _MessageHandlers_ which is basically a collection of lambda expressions that handle incoming messages of defined types. In a simple scenario you could also use _getMessages(type)_ on your _Client_ or _Server_ instance to get _Observable_ message source of certain type. In a more complex scenario you would keep the logic in previously defined _MessageHandlers_ and use _ConnectionManager_ which consists of following elements connected together:
- _Address Pool_ - contains information about host + port pairs. It will be used to connect clients to other nodes
- _Transmitter Pool_ - contains connected clients identified by some node ID of your choice
- _Client Heartbeat Pool_ - it uses clients from the Transmitter Pool and sends periodical messages with node ID
- _Server Pool_ - contains your node's listening server instances
- _Server Heartbeat Pool_ - responsible for responding to client heartbeat messages in timely fashion.

Basically if you add some host information to _Address Pool_ it will connect a client, put it _Transmitter Pool_ and this will trigger _Client Heartbeat Pool_ to send heartbeat messages. Responses will allow nodes to introduce themselves by ID. _Client_ instances are associated with node IDs and that information is stored in the _Transmitter Pool_. Similar situation appears when you add server to the _Server Pool_. Your server starts to respond with heartbeat messages sent by clients and it creates association by node ID in the _Transmitter Pool_. Any heartbeat timeouts force the ID association to be removed.

Have a look at integrations tests and javadocs for more details.

## Repository

Repository:
```xml
    <repositories>
        <repository>
            <id>phisikus-repo</id>
            <name>Phisikus' Maven Repository</name>
            <url>http://phisikus.eu/maven2</url>
        </repository>
    </repositories>

```     
Current version:
```xml
    <dependency>
            <groupId>eu.phisikus.pivonia</groupId>
            <artifactId>pivonia</artifactId>
            <version>0.0.9-44056df</version>
    </dependency>
```




  