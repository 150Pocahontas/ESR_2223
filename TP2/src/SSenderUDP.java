public class SSenderUDP implements Runnable{
    private final int streamID;
    private final String name;
    private final AddressingTable table;

    public SSenderUDP(int s, String name, AddressingTable t){
        this.streamID = s;
        this.name = name;
        this.table = t;
    }

    public void run(){
        SStream.execute(streamID,name,table);
    }
    
}
