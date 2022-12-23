import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class SReceiverTCP implements Runnable{
    private final ServerSocket socket;
    private final Bootstrapper bs;
    private final AddressingTable table;
    private final PacketQueue queue;

    public SReceiverTCP(ServerSocket socket, Bootstrapper bs, AddressingTable table, PacketQueue queue){
        this.socket = socket; 
        this.bs = bs;
        this.table = table;
        this.queue = queue;
    }

    public void run(){
        while(true){
            try{
                Socket sck = socket.accept();
                DataInputStream in = new DataInputStream(new BufferedInputStream(sck.getInputStream()));
                DataOutputStream out = new DataOutputStream(sck.getOutputStream());
                Packet packet = Packet.receive(in);

                if(packet.getType() == 1){
                    String data = Ott.streams + " " + bs.getNeighbours(packet.getOrg());
                    if(!Ott.isON){
                        Packet.send(new Packet(packet.getDest(), packet.getOrg(), 2, data.getBytes(StandardCharsets.UTF_8)),out);
                    } else { 
                        Packet.send(new Packet(packet.getDest(), packet.getOrg(), 3, data.getBytes(StandardCharsets.UTF_8)), out);
                        Ott.changed = true;
                    }
                } else if (packet.getType() == 4){
                    queue.add(new Packet(packet.getDest(), packet.getOrg(), 5, "1 null".getBytes(StandardCharsets.UTF_8)));
                } else if (packet.getType() == 6) {
                    table.addAddress(packet.getOrg());
                } else if (packet.getType() == 9){
                    int streamID = Integer.parseInt(new String(packet.getData(), StandardCharsets.UTF_8));
                    table.setNeighbourState(packet.getOrg(), true, streamID);
                } else if(packet.getType() == 10) {
                    int streamID = Integer.parseInt(new String(packet.getData(), StandardCharsets.UTF_8));
                    table.setNeighbourState(packet.getOrg(), false, streamID);
                } else if(packet.getType() == 12) {
                    Ott.changed = true;
                } else if (packet.getType() == 14) {
                    int hops = table.getHops() + 1;
                    String msg = hops + " " + table.getSender();
                    queue.add(new Packet(packet.getDest(), packet.getOrg(), 17, msg.getBytes(StandardCharsets.UTF_8)));
                } else if(packet.getType() == 16) {
                    Set<String> tNeighbours = table.getNeighbours();
                    Set<String> neighbours = new TreeSet<>(List.of(new String(packet.getData(),StandardCharsets.UTF_8).split(",")));
                
                    for(String n : tNeighbours)
                        for(String ne: neighbours)
                            if(n.equals(ne))
                                table.removeNeighbour(ne);
                           
                } else if(packet.getType() == 17)
                    table.addNeighbour(packet.getOrg());

                out.close();
                in.close();
                sck.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}