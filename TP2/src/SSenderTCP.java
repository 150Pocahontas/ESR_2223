import java.io.*;
import java.net.*;

public class SSenderTCP implements Runnable {
    private final PacketQueue queue;

    public SSenderTCP(PacketQueue queue){
        this.queue = queue;
    }

    public void run() {
        while(true){
            try{
                Packet packet = queue.remove();
                Socket sck = new Socket(packet.getDest(), 8080);
                DataOutputStream out = new DataOutputStream(sck.getOutputStream());
                DataInputStream in = new DataInputStream(new BufferedInputStream(sck.getInputStream()));
                
                Packet.send(packet,out);
                
                in.close();
                out.close();
                sck.close();
            } catch (ConnectException i) {
            } catch (IOException e){
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
}
