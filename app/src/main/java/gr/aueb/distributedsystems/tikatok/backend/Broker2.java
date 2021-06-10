package gr.aueb.distributedsystems.tikatok.backend;

/**
 * AM: 3180009-3180026-3180095-3180289
 * DISTRIBUTED SYSTEMS 2020-21
 */
public class Broker2 {
    public static void main(String[] args) {
            new Broker(Node.BROKER_ADDRESSES.get(1)).init();
    }
}
