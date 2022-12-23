import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;

public class BeaconSenderOtt implements Runnable {
    private final AddressingTable table;
    private final PacketQueue queue;
    private final String ip;

    public BeaconSenderOtt(AddressingTable table, PacketQueue queue, String ip) {
        this.table = table;
        this.queue = queue;
        this.ip = ip;
    }

    public void run() {
        while(true) {
            try {
                Set<String> neighbours = table.getNeighbours();
                for(String n : neighbours) {
                    try {
                        Socket s = new Socket(n, 8080);
                        DataOutputStream out = new DataOutputStream(s.getOutputStream());
                        Packet.send(new Packet(ip, n, 18, null),out);

                        if (!table.getNeighbourState(n))
                            table.setNeighbours(n, true);

                        out.close();
                        s.close();
                    } catch (IOException e) {
                        if(table.isSender(n)) {
                            Set<String> routes = table.getRoutes();
                            Set<String> nei = table.getNeighboursOn();
                            nei.remove(table.getSender());

                            for(String r : routes) {
                                nei.remove(r);
                                queue.add(new Packet(ip, r, 13, null));
                            }

                            String sender = table.getSender();
                            String senderSender = table.getSenderSender();

                            table.reset();
                            
                            if(nei.isEmpty()) {
                                queue.add(new Packet(ip, senderSender, 4, null));
                                queue.add(new Packet(ip, senderSender, 14, null));
                                queue.add(new Packet(ip, senderSender, 20, null));
                            } else {
                                for (String rn : nei)
                                    queue.add(new Packet(ip, rn, 4, null));
                                queue.add(new Packet(ip, sender, 14, null));
                            }
                        } else if(table.isRoute(n)) {
                            table.removeAddress(n);
                        }

                        if(table.getNeighbourState(n))
                            table.setNeighbours(n, false);
                    }
                }
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

