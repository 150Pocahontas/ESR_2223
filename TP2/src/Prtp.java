
public class Prtp {
    
    //size of the RTP header:
    static int HEADER_SIZE = 12;
    
    //Fields that compose the RTP header
    public int Version;
    public int Padding;
    public int Extension;
    public int CC;
    public int Marker;
    public int PayloadType;
    public int SequenceNumber;
    public int TimeStamp;
    public int Ssrc;
    public int StreamID;
    
    //Bitstream of the RTP header
    public byte[] header;
    
    //size of the RTP payload
    public int payload_size;
    //Bitstream of the RTP payload
    public byte[] payload;
    
    //--------------------------
    //Constructor of an RTPpacket object from header fields and payload bitstream
    //--------------------------
    public Prtp(int PType, int Framenb, int Time, int strId, byte[] data, int data_length) {
        //fill by default header fields:
        Version = 2;
        Padding = 0;
        Extension = 0;
        CC = 0;
        Marker = 0;
        Ssrc = 0;
    
        //fill changing header fields:
        SequenceNumber = Framenb;
        TimeStamp = Time;
        PayloadType = PType;
        StreamID = strId;

        //build the header bistream:
        //--------------------------
        header = new byte[HEADER_SIZE];
    
        //fill the header array of byte with RTP header fields
        header[0] = (byte) (Version << 6 | Padding << 5 | Extension << 4 | CC);
        header[1] = (byte) (Marker << 7 | PayloadType & 0x000000FF);
        header[2] = (byte) (SequenceNumber >> 8);
        header[3] = (byte) (SequenceNumber & 0x00FF);
        header[4] = (byte) (TimeStamp >> 24);
        header[5] = (byte) (TimeStamp >> 16);            
        header[6] = (byte) (TimeStamp >> 8);
        header[7] = (byte) (TimeStamp & 0x00FF);
        header[8] = (byte) (Ssrc >> 24);
        header[9] = (byte) (Ssrc >> 16);
        header[10] = (byte) (Ssrc >> 8);
        header[11] = (byte) (Ssrc & 0x00FF);
        header[12] = (byte)(StreamID);
        payload_size = data_length;
        payload = new byte[data_length];
        for (int i=0; i < data_length; i++)
            payload[i] = data[i];
    }

    //--------------------------
    //Constructor of an RTPpacket object from the packet bistream
    //--------------------------
    public Prtp(byte[] packet, int packet_size) {
        //fill default fields:
        Version = 2;
        Padding = 0;
        Extension = 0;
        CC = 0;
        Marker = 0;
        Ssrc = 0;
        StreamID = 0;
    
        //check if total packet size is lower than the header size
        if (packet_size >= HEADER_SIZE) {
            //get the header bitsream:
            header = new byte[HEADER_SIZE];
            for (int i=0; i < HEADER_SIZE; i++)
                header[i] = packet[i];
    
            //get the payload bitstream:
            payload_size = packet_size - HEADER_SIZE;
            payload = new byte[payload_size];
            for (int i=HEADER_SIZE; i < packet_size; i++)
                payload[i-HEADER_SIZE] = packet[i];
    
            //interpret the changing fields of the header:
            PayloadType = header[1] & 127;
            SequenceNumber = unsigned_int(header[3]) + 256*unsigned_int(header[2]);
            TimeStamp = unsigned_int(header[7]) + 256*unsigned_int(header[6]) + 65536*unsigned_int(header[5]) + 16777216*unsigned_int(header[4]);
            StreamID = unsigned_int(header[12]);
        }
    }

    public int getpayload(byte[] data){
        for (int i=0; i < payload_size; i++)
            data[i] = payload[i];
        return(payload_size);
    }

    public int getpayload_length() {
        return(payload_size);
    }

    public int getlength() {
        return(payload_size + HEADER_SIZE);
    }

    public int getpacket(byte[] packet) {
        //construct the packet = header + payload
        for (int i=0; i < HEADER_SIZE; i++)
            packet[i] = header[i];
        for (int i=0; i < payload_size; i++)
            packet[i+HEADER_SIZE] = payload[i];
    
        //return total size of the packet
        return(payload_size + HEADER_SIZE);
    }

    public int gettimestamp() {
        return(TimeStamp);
    }

    public int getsequencenumber() {
        return(SequenceNumber);
    }

    public int getpayloadtype() {
        return(PayloadType);
    }

    public int getStreamID() {
        return(StreamID);
    }

    public void headerToString() {
        System.out.println("[RTP Packet Header]:");
        System.out.println("Version: " + Version);
        System.out.println("Padding: " + Padding);
        System.out.println("Extension: " + Extension);
        System.out.println("CC: " + CC);
        System.out.println("Marker: " + Marker);
        System.out.println("PayloadType: " + PayloadType);
        System.out.println("SequenceNumber: " + SequenceNumber);
        System.out.println("TimeStamp: " + TimeStamp);
        System.out.println("Ssrc: " + Ssrc);
        System.out.println("StreamID: " + StreamID);
    }

    static int unsigned_int(int nb){
        if (nb >= 0)
            return(nb);
        else
            return(256+nb);
    }
}
