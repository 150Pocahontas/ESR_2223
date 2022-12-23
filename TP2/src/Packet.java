import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Packet {
    private final String org;
    private final String dest;
    private final int type;
    private final byte[] data;

    public Packet(String org, String dest, int type, byte[] data) {
        this.org = org;
        this.dest = dest;
        this.type = type;
        if (data == null) this.data = new byte[0];
        else this.data = data;
    }

    public byte[] serialize() throws UnknownHostException {
        
        byte[] buffer = new byte[12 + this.data.length];

        int pos = 0;

        ByteBuffer bytebuffer = ByteBuffer.allocate(4);
        System.arraycopy(bytebuffer.putInt(type).array(), 0, buffer, pos,4);
        pos += 4;

        System.arraycopy(InetAddress.getByName(this.org).getAddress(),0,buffer,pos,4);
        pos += 4;
        System.arraycopy(InetAddress.getByName(this.dest).getAddress(),0,buffer,pos,4);
        pos += 4;
        System.arraycopy(this.data,0,buffer,pos, this.data.length);

        return buffer;
    }

    public Packet(byte[] data) throws UnknownHostException {
        byte[] buffer = new byte[4];
        int pos = 0;

        this.type = ByteBuffer.wrap(data, pos, 4).getInt();
        pos += 4;

        System.arraycopy(data, pos, buffer,0,4);
        this.org = InetAddress.getByAddress(buffer).getHostAddress();
        pos += 4;
        System.arraycopy(data, pos, buffer,0,4);
        this.dest = InetAddress.getByAddress(buffer).getHostAddress();
        pos += 4;

        if (data.length <= pos) this.data = new byte[0];
        else {
            byte[] aux = new byte[data.length - pos];
            System.arraycopy(data, pos, aux, 0, aux.length);
            this.data = aux;
        }
    }

    public byte[] getData() {
        return data;
    }

    public int getType() {
        return type;
    }

    public String getDest() {
        return dest;
    }

    public String getOrg() {
        return org;
    }

    public static Packet receive(DataInputStream in) throws IOException {
        byte[] buffer = new byte[4096];
        int read = in.read(buffer, 0, 4096);
        byte[] content = new byte[read];
        System.arraycopy(buffer, 0, content, 0, read);

        Packet packet = new Packet(content);

        packet.receiveIO();

        return packet;
    }

    public static void send(Packet packet, DataOutputStream out) throws IOException {
        packet.sendIO();
        out.write(packet.serialize());
        out.flush();
    }

    public void receiveIO() {
        if(this.type < 18) {
            String msg = "[RECB] ";
            msg += "Tipo: " + type + " ";
            msg += "Fonte: " + org + "\n";
            msg += "MSG=";

            switch (type) {
                case 1 -> msg += "Pedido de tabela de vizinhos recebida";
                case 2 -> msg += "Stream desligada, tabela de vizinhos entregue";
                case 3 -> msg += "Stream ligada, tabela de vizinhos entregue";
                case 4 -> msg += "Pedido de fload recebido";
                case 5 -> msg += "Fload recebido com " + new String(data, StandardCharsets.UTF_8).split(" ")[0] + "hops";
                case 6 -> msg += "Caminho aceite";
                case 7 -> msg += "Caminho substituido";
                case 8 -> msg += "Recebi informação que o servidor vai sair";
                case 9 -> msg += "Recebi pedido de stream " + new String(data, StandardCharsets.UTF_8);
                case 10 -> msg += "Pedido de cancelamento de stream " + new String(data, StandardCharsets.UTF_8);
                case 11 -> msg += "Informação para limpar as rotas";
                case 12 -> msg += "Pedido de informação ao servidor que o nodo vai sair";
                case 13 -> msg += "Pedido de escrita para ficheiro de log";
                case 14 -> msg += "Pedido de fload sem redirecionamento";
                case 15 -> msg += "Fload sem redirecionamento com " + new String(data, StandardCharsets.UTF_8).split(" ")[0] + "hops";
                case 16 -> msg += "Pedido para remover vizinhos temporários";
                case 17 -> msg += "Pedido para adicionar vizinhos temporários";
                default -> {}
            }

            System.out.println(msg);
        }
    }

    public void sendIO() {
        if(this.type < 18) {
            String msg = "[ENV] ";
            msg += "Tipo: " + type + " ";
            msg += "Destino: " + dest + "\n";
            msg += "MSG=";

            switch (type) {
                case 1 -> msg += "Pedido de tabela de vizinhos recebida";
                case 2 -> msg += "Stream desligada, tabela de vizinhos entregue";
                case 3 -> msg += "Stream ligada, tabela de vizinhos entregue";
                case 4 -> msg += "Pedido de fload recebido";
                case 5 -> msg += "Fload recebido com " + new String(data, StandardCharsets.UTF_8).split(" ")[0] + "hops";
                case 6 -> msg += "Caminho aceite";
                case 7 -> msg += "Caminho substituido";
                case 8 -> msg += "Enviei informação que o servidor vai sair";
                case 9 -> msg += "Enviei pedido de stream " + new String(data, StandardCharsets.UTF_8);
                case 10 -> msg += "Pedido de cancelamento de stream " + new String(data, StandardCharsets.UTF_8);
                case 11 -> msg += "Informação para limpar as rotas";
                case 12 -> msg += "Pedido de informação ao servidor que o nodo vai sair";
                case 13 -> msg += "Pedido de escrita para ficheiro de log";
                case 14 -> msg += "Pedido de fload sem redirecionamento";
                case 15 -> msg += "Fload sem redirecionamento com " + new String(data, StandardCharsets.UTF_8).split(" ")[0] + "hops";
                case 16 -> msg += "Pedido para remover vizinhos temporários";
                case 17 -> msg += "Pedido para adicionar vizinhos temporários";
                default -> {}
            }

            System.out.println(msg);
        }
    }

}
