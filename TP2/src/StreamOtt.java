import java.io.*;
import java.net.*;
import java.util.*;

public class StreamOtt implements Runnable {
    private final AddressingTable table;

    public StreamOtt(AddressingTable table ) {
        this.table = table;
    }

    public void run() {

        try {
            int RTP_RCV_PORT = 25000;
            DatagramSocket rtpSocket = new DatagramSocket(RTP_RCV_PORT);

            while(true) {
                byte[] buf = new byte[15000];
                DatagramPacket received = new DatagramPacket(buf, buf.length);

                rtpSocket.receive(received);

                Prtp rtpPacket = new Prtp(received.getData(), received.getLength());

                Set<String> streamIPs = table.getStreamIp(rtpPacket.StreamID);
                for (String ip : streamIPs) {
                    int RTP_dest_port = 25000;
                    DatagramPacket senddp = new DatagramPacket(received.getData(), received.getData().length, InetAddress.getByName(ip), RTP_dest_port);
                    rtpSocket.send(senddp);
                }
            }
        }
        catch (InterruptedIOException iioe){
            System.out.println("Nothing to read");
        }
        catch (IOException ioe) {
            System.out.println("Exception caught: "+ioe);
        }
    }
}