import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ReceiverOtt implements Runnable {
    private final ServerSocket ss;
    private final AddressingTable table;
    private final PacketQueue queue;
    private final String ip;

    public ReceiverOtt(ServerSocket ss, AddressingTable table, PacketQueue queue, String ip) {
        this.ss = ss;
        this.table = table;
        this.queue = queue;
        this.ip = ip;
    }

    public void run() {
        while(true) {
            try {
                Socket s = ss.accept();

                DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                Packet p = Packet.receive(in);

                if (p.getType() == 4) {
                    int hops = table.getHops() + 1;
                    String msg = hops + " " + table.getSender();
                    queue.add(new Packet(ip, p.getOrg(), 5, msg.getBytes(StandardCharsets.UTF_8)));
                } else if (p.getType() == 5) {
                    String[] lines = new String(p.getData(), StandardCharsets.UTF_8).split(" ");
                    int hops = Integer.parseInt(lines[0]);
                    String senderSender = lines[1];

                    if (hops < table.getHops()) {
                        String sender = table.getSender();
                        table.setSender(p.getOrg());

                        if (senderSender.equals("null"))
                            table.setSenderSender(null);
                        else
                            table.setSenderSender(senderSender);

                        table.setHops(hops);

                        Map<Integer, Boolean> isClientStream = table.isClientStreamMap();

                        if (sender != null) {
                            for (Map.Entry<Integer, Boolean> e : isClientStream.entrySet()) {
                                if (e.getValue()) {
                                    queue.add(new Packet(p.getDest(), sender, 10, String.valueOf(e.getKey()).getBytes(StandardCharsets.UTF_8)));
                                }
                            }
                            queue.add(new Packet(ip, sender, 7, null));
                        }

                        queue.add(new Packet(p.getDest(), p.getOrg(), 6, null));


                        for (Map.Entry<Integer, Boolean> e : isClientStream.entrySet()) {
                            if (e.getValue()) {
                                queue.add(new Packet(p.getDest(), p.getOrg(), 9, String.valueOf(e.getKey()).getBytes(StandardCharsets.UTF_8)));
                            }
                        }

                        Set<String> neighbours = table.getNeighboursOn();
                        hops++;

                        for (String n : neighbours)
                            if (!n.equals(p.getOrg())) {
                                String msg = hops + " " + table.getSender();
                                queue.add(new Packet(ip, n, 5, msg.getBytes(StandardCharsets.UTF_8)));
                            }

                        Set<String> neighboursTemp = table.getTempNeighbour();

                        for (String n : neighboursTemp)
                            if (!n.equals(p.getOrg())) {
                                String msg = hops + " " + table.getSender();
                                queue.add(new Packet(ip, n, 5, msg.getBytes(StandardCharsets.UTF_8)));
                            }

                    }
                } else if (p.getType() == 6) {
                    table.addAddress(p.getOrg());
                } else if (p.getType() == 7) {
                    table.removeAddress(p.getOrg());
                } else if (p.getType() == 8) {
                    Set<String> routes = table.getRoutes();
                    Set<String> nei = table.getNeighboursOn();
                    nei.remove(table.getSender());

                    for (String r : routes) {
                        nei.remove(r);
                        queue.add(new Packet(ip, r, 11, null));
                    }

                    String sender = table.getSender();
                    String senderSender = table.getSenderSender();

                    table.reset();

                    if (nei.isEmpty()) {
                        queue.add(new Packet(ip, senderSender, 4, null));
                        queue.add(new Packet(ip, senderSender, 12, null));
                        queue.add(new Packet(ip, senderSender, 17, null));
                    } else {
                        for (String rn : nei)
                            queue.add(new Packet(ip, rn, 4, null));
                        queue.add(new Packet(ip, sender, 12, null));
                    }
                } else if (p.getType() == 9) {
                    int streamID = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));

                    if (table.isNotStreaming(streamID)) {
                        queue.add(new Packet(ip, table.getSender(), 9, p.getData()));
                    }

                    table.setNeighbourState(p.getOrg(), true, streamID);
                } else if (p.getType() == 10) {
                    int streamID = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));

                    table.setNeighbourState(p.getOrg(), false, streamID);
                    if (table.isNotStreaming(streamID)) {
                        queue.add(new Packet(ip, table.getSender(), 10, p.getData()));
                    }
                } else if (p.getType() == 11) {
                    Set<String> routes = table.getRoutes();

                    for (String n : routes) {
                        queue.add(new Packet(ip, n, 11, null));
                    }

                    table.reset();
                } else if (p.getType() == 12) {
                    queue.add(new Packet(ip, table.getSender(), 12, null));
                } else if (p.getType() == 13) {
                    table.ping();

                    Set<String> routes = table.getRoutes();
                    for (String r : routes) {
                        queue.add(new Packet(ip, r, 13, null));
                    }
                } else if (p.getType() == 14) {
                    int hops = table.getHops() + 1;
                    String msg = hops + " " + table.getSender();
                    queue.add(new Packet(ip, p.getOrg(), 15, msg.getBytes(StandardCharsets.UTF_8)));
                } else if (p.getType() == 15) {
                    String[] lines = new String(p.getData(), StandardCharsets.UTF_8).split(" ");
                    int hops = Integer.parseInt(lines[0]);
                    String senderSender = lines[1];

                    if (hops < table.getHops()) {
                        String sender = table.getSender();
                        table.setSender(p.getOrg());
                        if (senderSender.equals("null"))
                            table.setSenderSender(null);
                        else
                            table.setSenderSender(senderSender);
                        table.setHops(hops);

                        if (sender != null) {
                            queue.add(new Packet(ip, sender, 7, null));
                        }

                        queue.add(new Packet(p.getDest(), p.getOrg(), 6, null));
                    }
                } else if(p.getType() == 16) {
                    Set<String> neighbours_temp = table.getTempNeighbour();
                    Set<String> nei = new TreeSet<>(List.of(new String(p.getData(), StandardCharsets.UTF_8).split(",")));

                    for(String n : neighbours_temp) {
                        for(String ne : nei) {
                            if(n.equals(ne)) {
                                table.removeNeighbour(ne);
                            }
                        }
                    }

                    queue.add(new Packet(ip, table.getSender(), 16, p.getData()));
                } else if(p.getType() == 17) {
                    table.addNeighbour(p.getOrg());
                }

                in.close();
                out.close();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}