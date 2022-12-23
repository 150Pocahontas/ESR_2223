import java.util.*;
import java.nio.charset.StandardCharsets;

public class SFload implements Runnable {
    private final Bootstrapper bs;
    private final AddressingTable table;
    private final String ip;
    private final Map<Integer,String> videos;
    private final PacketQueue queue;

    public SFload(Bootstrapper bs, AddressingTable table, String ip, Map<Integer,String> videos, PacketQueue queue){
        this.bs = bs;
        this.table = table;
        this.ip = ip;
        this.videos = videos;
        this.queue = queue;
    }

    public void run(){
        System.out.println("a visitar");
        try{
            bs.isfull();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        System.out.println("tudo visitado");

        Ott.isON = true;

        Set<String> neighbours = table.getNeighbours();
        System.out.println("vizinhos obtidos: " + neighbours);
        for (String n : neighbours ){
            System.out.println("a criar pacote");
            queue.add(new Packet(ip,n,5,"1 null".getBytes(StandardCharsets.UTF_8)));
            System.out.println("pacote criado");
        }


        for(int i = 1 ; i <= Ott.streams; i++){
            Thread stream = new Thread(new SSenderUDP(i, videos.get(i), table));
            stream.start();
        }

        while(true){
            try{
                if(Ott.changed) {
                    Ott.changed = false;
                   
                    neighbours = table.getNeighboursOn();
                    for(String n : neighbours){
                        queue.add(new Packet(ip,n,11,null));
                        Thread.sleep(50);
                        queue.add(new Packet(ip, n, 5,"1 null".getBytes(StandardCharsets.UTF_8)));
                    }
                }

                Set<String> tempNeighbours = table.getTempNeighbour();
                for(String n : tempNeighbours){
                    queue.add(new Packet(ip,n,11,null));
                    Thread.sleep(50);
                    queue.add(new Packet(ip,n,5,"1 null".getBytes(StandardCharsets.UTF_8)));
                }
            }catch (InterruptedException e){
                e.printStackTrace();;
            }
        }
    }
}