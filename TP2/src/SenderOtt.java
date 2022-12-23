import java.io.*;
import java.net.*;

public class SenderOtt implements Runnable {
    private final PacketQueue queue;

    public SenderOtt(PacketQueue queue) {
        this.queue = queue;
    }

    public void run() {
        while(true) {
            try {
                Packet p = queue.remove();

                Socket s = new Socket(p.getDest(), 8080);
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));

                Packet.send(p,out);

                in.close();
                out.close();
                s.close();
            } catch (ConnectException ignored) {
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}