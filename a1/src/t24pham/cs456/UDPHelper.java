package t24pham.cs456;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Random;

public class UDPHelper {
  private static final int INT_BYTES = Integer.SIZE / Byte.SIZE;

  public static void sendInt(int i, DatagramSocket socket, InetAddress remoteAddr, int remotePort)
      throws IOException {
    byte[] buffer = ByteBuffer.allocate(INT_BYTES).putInt(i).array();
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, remoteAddr, remotePort);
    socket.send(packet);
  }

  public static int receiveInt(DatagramSocket socket) throws IOException {
    byte[] buffer = new byte[INT_BYTES];
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    socket.receive(packet);

    return ByteBuffer.wrap(packet.getData()).getInt();
  }

  public static DatagramSocket getAvailableSocket() throws SocketException {
    Random rand = new Random();
    int attempts = 0;

    while (attempts < 100) {
      try {
        return new DatagramSocket(rand.nextInt(65535) + 1024);
      } catch (SocketException e) {
        continue;
      }
    }
    throw new SocketException("No available ports");
  }
}
