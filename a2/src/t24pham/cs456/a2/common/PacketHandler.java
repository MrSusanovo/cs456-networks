package t24pham.cs456.a2.common;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import t24pham.cs456.a2.common.exceptions.PayloadTooLargeException;

public class PacketHandler {
  private DatagramSocket socket;
  private boolean remoteIsKnown;

  private final ReentrantLock lock;

  public PacketHandler() throws SocketException {
    this.socket = getAvailableSocket();
    this.remoteIsKnown = false;
    this.lock = new ReentrantLock();
  }

  public PacketHandler(ConnectionInfo remote) throws SocketException {
    this.socket = getAvailableSocket();
    this.socket.connect(remote.getAddress(), remote.getPort());
    this.remoteIsKnown = true;
    this.lock = new ReentrantLock();
  }

  public String getLocalHostname() {
    return socket.getLocalAddress().getHostName();
  }

  public int getLocalPort() {
    return socket.getLocalPort();
  }

  public void sendPacket(Packet packet) {
    System.out.println(String.format("PKT SEND %s %d %d",
        packet.getType().getName(),
        packet.getLength(),
        packet.getSeqNum()));
    try {
      sendBytes(packet.toBytes());
    } catch (IOException e) {
      Utils.error("Packet " + packet.getSeqNum() + " failed to send");
    }
  }

  public Packet receivePacket() throws PayloadTooLargeException, IOException {
    lock.lock();
    try {
      byte[] packetBytes = receiveBytes();
      Packet packet = new Packet(packetBytes);
      System.out.println(String.format("PKT RECV %s %d %d",
          packet.getType().getName(),
          packet.getLength(),
          packet.getSeqNum()));
      return packet;
    } finally {
      lock.unlock();
    }
  }

  private void sendBytes(byte[] payload) throws IOException {
    DatagramPacket packet = new DatagramPacket(payload, payload.length);
    socket.send(packet);
  }

  private byte[] receiveBytes() throws IOException {
    byte[] buffer = new byte[Constants.MAX_PACKET_LEN];
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    socket.receive(packet);

    // Store remote details
    if (!remoteIsKnown) {
      socket.connect(packet.getSocketAddress());
      remoteIsKnown = true;
    }

    return ByteBuffer.wrap(packet.getData(), packet.getOffset(), packet.getLength()).array();
  }

  private static DatagramSocket getAvailableSocket() throws SocketException {
    Random rand = new Random();
    int attempts = 0;

    while (attempts < 100) {
      try {
        return new DatagramSocket(rand.nextInt(65535 - 1024 + 1) + 1024);
      } catch (SocketException e) {
        ++attempts;
      }
    }
    throw new SocketException("No available ports");
  }
}
