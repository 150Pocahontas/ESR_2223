import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.stream.Collectors;

/*
 * AddressingTable class is used to store the information about the neighbours and the streams 
 * that are being served by the server. It also stores the information about the clients that are
 * connected to the server.
 *  table is a map that stores the information about the streams that are being served by the server.
 *  The key of the map is the ip of the neighbour and the value is a map that stores the information 
 *  about the streams that are being served by the neighbour. The key of the map is the stream id and
 * the value is a boolean that indicates if the stream is being served by the neighbour.
 * isClientStream is a map that stores the information about the streams that are being served by the
 * server. The key of the map is the stream id and the value is a boolean that indicates if the stream
 * is being served by the server.
 * neighbours is a map that stores the information about the neighbours of the server. The key of the
 * map is the ip of the neighbour and the value is a boolean that indicates if the neighbour is a
 * temporary neighbour.
 * tempNeighbours is a set that stores the ip of the temporary neighbours of the server.
 * ip is the ip of the server.
 * sender is the ip of the neighbour that sent the packet that is being processed.
 * senderSender is the ip of the neighbour that sent the packet that is being processed.
 * hops is the number of hops that the packet that is being processed has travelled.
 * numStreams is the number of streams that the server is serving.
 * lock is a lock that is used to synchronize the access to the data structures.
 */
public class AddressingTable {

    private Map<String, Map<Integer, Boolean>> table;
    private final Map<Integer, Boolean> isClientStream;
    private final Map<String, Boolean> neighbours;
    private final Set<String> tempNeighbours;
    private final String ip;
    private String sender;
    private String senderSender;
    private int hops;
    private final int numStreams;
    private final ReentrantLock lock;
    
    AddressingTable(String ip, int numStreams) {
        this.ip = ip;
        this.numStreams = numStreams;
        this.isClientStream = new HashMap<>();
        for(int i= 1; i <= numStreams; i++ )
            isClientStream.put(i, false);
        this.table = new HashMap<>();
        this.neighbours = new HashMap<>();
        this.tempNeighbours = new TreeSet<>();
        this.sender =  null;
        this.senderSender = null;
        this.hops = Integer.MAX_VALUE;
        this.lock = new ReentrantLock();
    }
    /*
    * This method is used to add a neighbour to the table.
    *  neighbours is a set that stores the ip of the neighbours of the server.
    * The method adds the neighbours to the table.
    * The method is synchronized.
    */
    public void addNeighbours(Set<String> neighbours) {
        lock.lock();
        try {
            for(String n : neighbours)
                this.neighbours.put(n , false);
        } finally {
            lock.unlock();
        }
    }

    /*  
    * This method is used to add a neighbour to the table.
    * neighbour is the ip of the neighbour that is being added to the table.
    * The method adds the neighbour to the table.
    * The method is synchronized.
    */
    public void addNeighbour(String neighbour) {
        lock.lock();
        try {
            this.tempNeighbours.add(neighbour);
        } finally {
            lock.unlock();
        }
    }
    /*  
     * This method is used to remove a neighbour from the table.
     * neighbour is the ip of the neighbour that is being removed from the table.
     * The method removes the neighbour from the table.
     * The method is synchronized.
     */
    public void removeNeighbour(String neighbour){
        lock.lock();
        try {
            this.tempNeighbours.remove(neighbour);
        } finally {
            lock.unlock();
        }
    }
    
    public Set<String> getNeighbours() {
        lock.lock();
        try {
            return new TreeSet<>(neighbours.keySet());
        } finally {
            lock.unlock();
        }
    }

    public String neighbourToString(){
        lock.lock();
        try {
            StringBuilder res = new StringBuilder();
            Iterator<String> it = tempNeighbours.iterator();
            if(tempNeighbours.isEmpty()) return null;
            while(it.hasNext()){
                res.append(it.next());
                if(it.hasNext()) res.append(",");
            }
            return res.toString();
        } finally {
            lock.unlock();
        }
    }

    public Map<Integer, Boolean> isClientStreamMap() {
        lock.lock();
        try {
            return new HashMap<>(isClientStream);
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getNeighboursOn() {
        lock.lock();
        try {
            Set<String> res = new TreeSet<>();
            for(Map.Entry<String,Boolean> e : neighbours.entrySet())
                if(e.getValue())
                    res.add(e.getKey());
            return res;
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getTempNeighbour() {
        lock.lock();
        try {
            return new TreeSet<>(tempNeighbours);
        } finally {
            lock.unlock();
        }
    }

    public boolean isSender(String neighbour){
        lock.lock();
        try {
            if(sender == null) return false;
            return neighbour.equals(sender);
        } finally {
            lock.unlock();
        }
    }

    public boolean getNeighbourState(String neighbour) {
        lock.lock();
        try {
            return neighbours.get(neighbour);
        } finally {
            lock.unlock();
        }
    }

    public void addAddress(String neighbour){
        lock.lock();
        try {
            Map<Integer, Boolean> map = new HashMap<>();
            for(int i = 1; i <= numStreams; i++)
                map.put(i, false);
            table.put(neighbour, map);
        } finally {
            lock.unlock();
        }
    }

    public void removeAddress(String neighbour){
        lock.lock();
        try {
            table.remove(neighbour);
        } finally {
            lock.unlock();
        }
    }   

    public int getHops(){
        lock.lock();
        try {
            return hops;
        } finally {
            lock.unlock();
        }
    }

    public void setHops(int hops){
        lock.lock();
        try {
            this.hops = hops;
        } finally {
            lock.unlock();
        }
    }

    public String getSender(){
        lock.lock();
        try {
            return sender;
        } finally {
            lock.unlock();
        }
    }

    public void setSender(String sender){
        lock.lock();
        try {
            this.sender = sender;
        } finally {
            lock.unlock();
        }
    }

    public String getSenderSender(){
        lock.lock();
        try {
            return senderSender;
        } finally {
            lock.unlock();
        }
    }

    public void setSenderSender(String senderSender){
        lock.lock();
        try {
            this.senderSender = senderSender;
        } finally {
            lock.unlock();
        }
    }

    public void setNeighbours(String ip, Boolean state) {
        lock.lock();
        try {
            this.neighbours.put(ip, state);
        } finally {
            lock.unlock();
        }
    }

    public void setNeighbourState(String neighbour, boolean state, int streamId) {
        lock.lock();
        try {
            table.get(neighbour).put(streamId, state);
        } finally {
            lock.unlock();
        }
    }

    public boolean isClientStream(int streamId){
        lock.lock();
        try {
            return isClientStream.get(streamId);
        } finally {
            lock.unlock();
        }
    }

    public boolean isNotStreaming(int streamID) {
        lock.lock();
        try {
            return !table.values().stream().map(m -> m.get(streamID)).collect(Collectors.toSet()).contains(true) && !isClientStream.get(streamID);
        } finally {
            lock.unlock();
        }
    }

    public void setClientStream(int streamId, boolean state){
        lock.lock();
        try {
            isClientStream.put(streamId, state);
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getStreamIp(int streamId){
        lock.lock();
        try {
            Set<String> res = new TreeSet<>();
            for(Map.Entry<String, Map<Integer, Boolean>> e : table.entrySet())
                if(e.getValue().get(streamId))
                    res.add(e.getKey());
            return res;
        } finally {
            lock.unlock();
        }
    }

    public boolean isRoute(String neighbour) {
        lock.lock();
        try {
            return getRoutes().contains(neighbour);
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getRoutes() {
        lock.lock();
        try{ 
            return table.keySet();
        } finally {
            lock.unlock();
        }
    }



    public void reset(){
        lock.lock();
        try{
            this.sender = null;
            this.senderSender = null;
            this.hops = Integer.MAX_VALUE;
            this.table = new HashMap<>();
        } finally {
            lock.unlock();
        }
    }

    public int getNumStreams(){
        lock.lock();
        try {
            return numStreams;
        } finally {
            lock.unlock();
        }
    }

    public void ping() {
        lock.lock();
        try {

            String name = "../files/log_" + ip;

            File file = new File(name);
            if(!file.exists()) {
                file.delete();
            }

            FileOutputStream out = new FileOutputStream(name);
            StringBuilder content = new StringBuilder("ip: " + ip + "\n");
            content.append("sender: ").append(sender).append("\n").append("senderSender: ").append(senderSender).append("\n");
            
            for(Map.Entry<Integer, Boolean> f : isClientStream.entrySet()) {
                content.append(" - Stream ").append(f.getKey()).append(" :").append(f.getValue()).append("\n");
            }

            content.append("\nNeighbours:\n");
            for(Map.Entry<String, Map<Integer, Boolean>> e : table.entrySet()) {
                content.append("\n").append(e.getKey()).append(":\n");
                for(Map.Entry<Integer, Boolean> f : e.getValue().entrySet()) {
                    content.append(" - Stream ").append(f.getKey()).append(" :").append(f.getValue()).append("\n");
                }
                System.out.println("\n");
            }

            out.write(content.toString().getBytes(StandardCharsets.UTF_8));
            out.close();
        }catch(IOException e){
            e.printStackTrace();
        
        } finally {
            lock.unlock();
        }
    }
}
