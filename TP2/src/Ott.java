import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Ott {
    public static boolean isON = false;
    public static boolean changed = false;
    public static int streams = 0;

    public static void main(String[] args) throws IOException {
        String ip = InetAddress.getLocalHost().getHostAddress();
        ServerSocket ss = new ServerSocket(8080);
        if(args.length==1 && args[0].equals("-server")) {
           server(ip, ss, new PacketQueue());
        } else if(args.length==2 && args[1].equals("-client")) {
            client(ip, ss, args[0], new PacketQueue());
        } else if(args.length==1) {
            ott(ip, ss, args[0], new PacketQueue());
        } else {
            System.out.println("Wrong number of arguments");
        }
    }

    public static void server(String ip, ServerSocket ss , PacketQueue queueTCP) throws IOException {
        File file = new File("../files/movie.Mjpeg");
        Scanner s = new Scanner(file);
        Map<Integer, String> movies = new HashMap<>();

        Bootstrapper bs = new Bootstrapper("../files/bootstrapper");

        while(s.hasNextLine()) {
            String[] args = s.nextLine().split(" ");
            movies.put(Integer.parseInt(args[0]), args[1]);
            Ott.streams++;
        }

        AddressingTable at = new AddressingTable(ip,Ott.streams);
        at.addNeighbours(new TreeSet<>(List.of(bs.getNeighbours(ip).split(","))));

        Thread senderTCP = new Thread(new SSenderTCP(queueTCP));
        Thread receiverTCP = new Thread(new SReceiverTCP(ss, bs, at, queueTCP));
        Thread serverFload = new Thread(new SFload(bs, at, ip, movies, queueTCP));
        Thread beacon = new Thread(new BeaconSenderOtt(at, queueTCP, ip));

        senderTCP.start();
        receiverTCP.start();
        serverFload.start();
        beacon.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;

        while((line = in.readLine())!= null) {
            if(line.equals("ping")) {
                at.ping();
                Set<String> routes = at.getRoutes();
                for(String r : routes) {
                    queueTCP.add( new Packet(ip, r, 13, null));
                }
            }
        }
    }

    public static void ott(String ip, ServerSocket ss, String bootstrapperIP, PacketQueue queueTCP) throws IOException {
        AddressingTable at = neighbours(queueTCP, ip, bootstrapperIP);

        Thread ottStream = new Thread(new StreamOtt(at));
        Thread senderTCP = new Thread(new SenderOtt(queueTCP));
        Thread receiverTCP = new Thread(new ReceiverOtt(ss, at, queueTCP, ip));
        Thread beacon = new Thread(new BeaconSenderOtt(at, queueTCP, ip));

        senderTCP.start();
        receiverTCP.start();
        ottStream.start();
        beacon.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;

        while((line = in.readLine())!= null) {
            if(line.equals("exit")) {
                Set<String> neighbours = at.getRoutes();
                for(String n : neighbours) {
                    queueTCP.add(new Packet(ip, n, 8, null));
                }
                queueTCP.add(new Packet(ip, at.getSender(), 7, null));

                System.exit(0);
            }
        }
    }

    public static void client(String ip, ServerSocket ss, String bootstrapperIP, PacketQueue queueTCP) throws IOException {
        AddressingTable at = neighbours(queueTCP, ip, bootstrapperIP);

        Map<Integer, Qrtp> queueMap = new HashMap<>();
        for(int i=1; i<=at.getNumStreams(); i++)
            queueMap.put(i, new Qrtp());


        Thread senderTCP = new Thread(new SenderOtt(queueTCP));
        Thread receiverTCP = new Thread(new ReceiverOtt(ss, at, queueTCP, ip));
        Thread clientStream = new Thread(new ClientStream(at, queueMap));
        Thread beacon = new Thread(new BeaconSenderOtt(at, queueTCP, ip));

        senderTCP.start();
        receiverTCP.start();
        clientStream.start();
        beacon.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;
        int streamID;

        while((line = in.readLine())!= null) {
            if(lerInt(1, at.getNumStreams(), line)) {
                streamID = Integer.parseInt(line);
                if(!at.isClientStream(streamID)) {
                    if (at.isNotStreaming(streamID)) {
                        queueTCP.add(new Packet(ip, at.getSender(), 9, String.valueOf(streamID).getBytes(StandardCharsets.UTF_8)));
                    }
                    at.setClientStream(streamID, true);

                    Thread display = new Thread(new ClientDisplay(at, queueMap.get(streamID), queueTCP, streamID, ip));
                    display.start();
                }
            } else if(line.equals("exit")) {
                Set<String> neighbours = at.getRoutes();
                for(String n : neighbours) {
                    queueTCP.add(new Packet(ip, n, 8, null));
                }
                queueTCP.add(new Packet(ip, at.getSender(), 7, null));

                System.exit(0);
            }
        }
    }

    public static boolean lerInt(int min, int max, String msg) {
        boolean flag = false;
        for(int i=min; i<=max; i++)
            if(msg.equals(String.valueOf(i)))
                flag = true;
        return flag;
    }

    public static AddressingTable neighbours(PacketQueue queue, String ip, String bootstrapperIP) throws IOException {
        Packet p = new Packet(ip, bootstrapperIP, 1, " ".getBytes(StandardCharsets.UTF_8));
        Socket s = new Socket(p.getDest(), 8080);

        DataOutputStream out = new DataOutputStream(s.getOutputStream());
        DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));

        Packet.send(p,out);
        Packet rp = Packet.receive(in);

        in.close();
        out.close();
        s.close();

        String data = new String(rp.getData(), StandardCharsets.UTF_8);
        String[] args = data.split(" ");
        Set<String> neighbours = new TreeSet<>(List.of(args[1].split(",")));
        AddressingTable at = new AddressingTable( ip, Integer.parseInt(args[0]));
        at.addNeighbours(neighbours);

        if(rp.getType() == 3) {
            for(String n : neighbours) {
                queue.add(new Packet(ip, n, 14, null));
                String temp = at.neighbourToString();
                if(temp!=null)
                    queue.add(new Packet(ip, p.getOrg(), 16, temp.getBytes(StandardCharsets.UTF_8)));
            }
        }
        return at;
    }
}
