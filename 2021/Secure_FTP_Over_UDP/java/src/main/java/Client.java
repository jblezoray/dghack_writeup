import model.Packet;
import org.apache.commons.lang3.mutable.MutableObject;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;

public class Client implements Closeable {

    private DatagramSocket socket;
    private InetAddress distAddress;
    private int distPort;

    public Client(String distAddress, int distPort) throws IOException {
        this.distAddress = InetAddress.getByName(distAddress);
        this.distPort = distPort;
        this.socket = new DatagramSocket();
        this.socket.connect(this.distAddress, distPort);
    }

    public Packet send(Packet p, int tries, int timeoutms) throws IOException, InterruptedException {

        MutableObject<Packet> responsePacket = new MutableObject<>();
        int counter = 0;
        while (responsePacket.getValue()==null && counter++ < tries) {
            var t = new Thread(()->{
                var respBytes = new byte[1024];
                DatagramPacket response = new DatagramPacket(respBytes, respBytes.length);;
                try {
                    System.out.println("waiting response");
                    socket.setSoTimeout(timeoutms-500);
                    socket.receive(response);
                    responsePacket.setValue(PacketFactory.parse(response.getData()));
                    System.out.println("response : (" + responsePacket.getValue().getContenuString() + ")");
                } catch (SocketTimeoutException e) {
                    System.out.println("response timeout ...");
                } catch (PacketFactory.InvalidCrcException | IOException e) {
                    System.out.println("invalid response : " + Hex.fromBytes(response.getData()));
                    e.printStackTrace();
                }
            });
            t.start();
            Thread.sleep(200);

            var packetBytes = p.getBytes();
            var datagramPacket = new DatagramPacket(packetBytes, packetBytes.length, distAddress, distPort);

            socket.send(datagramPacket);
            t.join(timeoutms);

            // failure ...
            if (responsePacket.getValue()==null) {
                System.out.println("No result ... waiting a bit and retrying... ");
                Thread.sleep(500);
            }
        }

        if (responsePacket.getValue()==null) {
            throw new RuntimeException("could not make request.");
        }

        return responsePacket.getValue();
    }

    @Override
    public void close() throws IOException {
        if (!socket.isClosed()) {
            socket.close();
        }
    }
}
