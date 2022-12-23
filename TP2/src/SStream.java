import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;

public class SStream extends JFrame implements ActionListener {
    private DatagramSocket rtpSocket;

    private final int streamID;
    private final String name;
    private final AddressingTable table;

    private int image = 0;
    private static final int framePeriod = 100;
    private Display videoS;

    private final Timer timer;
    private final byte[] buffer;

    public SStream(int s, String name, AddressingTable t) throws Exception{
        super("Server");
        this.streamID = s;
        this.name = name;
        this.table = t;

        this.timer = new Timer(framePeriod,this);
        this.timer.setInitialDelay(0);
        this.timer.setCoalesce(true);
        this.buffer = new byte[15000];

        try{
            rtpSocket = new DatagramSocket();
            videoS = new Display(name);
        } catch (Exception e){}

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                timer.stop();
                rtpSocket.close();
                System.exit(0);
            }
        });
        
        JLabel lbl = new JLabel("Send frame # ", JLabel.CENTER);
        getContentPane().add(lbl, BorderLayout.CENTER);

        this.timer.start();
    }

    public static void execute(int s, String name, AddressingTable t) {
        try {
            SStream stream = new SStream(s, name, t);
            stream.pack();
            stream.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void actionPerformed(ActionEvent e){

        int vlength = 500;
        if(image < vlength){
            image++;
            try{
                vlength = videoS.getnextframe(buffer);
                int mjpeg = 26;
                Prtp rtPpacket = new Prtp(mjpeg, image, image*framePeriod, streamID, buffer, vlength);

                int packet_length = rtPpacket.getlength();

                byte[] packet_bits = new byte[packet_length];
                rtPpacket.getpacket(packet_bits);

                Set<String> stream = table.getStreamIp(streamID);
                for(String ip : stream){
                    DatagramPacket datagramPacket = new DatagramPacket(packet_bits, packet_length, InetAddress.getByName(ip), 25000);
                    rtpSocket.send(datagramPacket);
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }else{
            try{
                videoS = new Display(name);
                image = 0;
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }   
}