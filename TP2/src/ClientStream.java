import java.io.*;
import java.net.*;
import java.util.*;

public class ClientStream implements Runnable{
    private final AddressingTable table;
    private final Map<Integer,Qrtp> queueMap;

    public ClientStream(AddressingTable table, Map<Integer,Qrtp> queueMap) {
        this.table = table;
        this.queueMap = queueMap;
    }

    public void run() {

        try {
            int RTP_RCV_PORT = 25000;
            DatagramSocket rtpSocket = new DatagramSocket(RTP_RCV_PORT);

            while(true) {
                byte[] buf = new byte[15000];
                DatagramPacket received = new DatagramPacket(buf, buf.length);

                rtpSocket.receive(received);

                Prtp rtp_packet = new Prtp(received.getData(), received.getLength());

                Set<String> streamIPs = table.getStreamIp(rtp_packet.StreamID);
                for (String ip : streamIPs) {
                    int RTP_dest_port = 25000;
                    DatagramPacket senddp = new DatagramPacket(received.getData(), received.getData().length, InetAddress.getByName(ip), RTP_dest_port);
                    rtpSocket.send(senddp);
                }

                if (table.isClientStream(rtp_packet.StreamID)) {
                    queueMap.get(rtp_packet.StreamID).add(rtp_packet);
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
