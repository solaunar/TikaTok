package gr.aueb.distributedsystems.tikatok.backend;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

//Node class, saves the addresses of the AppNodes, Brokers and Zookeeper
public class Node implements Serializable {
    //Broker Addresses (ip, port)
    static public final ArrayList<Address> BROKER_ADDRESSES = new ArrayList<>(Arrays.asList(
            new Address("10.0.2.2", 5000),
            new Address("127.0.0.1", 5250),
            new Address("127.0.0.1", 5500)));

    //AppNode Addresses (ip, port)
    static public final ArrayList<Address> APPNODE_ADDRESSES = new ArrayList<>(Arrays.asList(
            new Address("127.0.0.1", 7000),
            new Address("127.0.0.1", 7250),
            new Address("127.0.0.1", 7500),
            new Address("127.0.0.1", 7750)));

    //Zookeeper Address (ip, port)
    static public final Address ZOOKEEPER_ADDRESS = new Address("127.0.0.1", 10000);

    //backlog
    static public final int BACKLOG = 250;
}
