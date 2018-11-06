package t24pham.cs456.a2.sender;

import t24pham.cs456.a2.common.Packet;
import t24pham.cs456.a2.common.PacketHandler;

public class TimedPacket {
  private PacketHandler packetHandler;
  private Packet packet;
  private int timeout;
  private boolean acked;
  private Thread worker;

  public TimedPacket(PacketHandler packetHandler, Packet packet, int timeout) {
    this.packetHandler = packetHandler;
    this.packet = packet;
    this.timeout = timeout;
    this.acked = false;
    this.worker = new Thread(new Task());
  }

  public void send() {
    worker.start();
  }

  public synchronized void acknowledge() {
    if (!acked) {
      this.acked = true;
      worker.interrupt();
      System.out.println("Acknowledged " + packet.getSeqNum());
    }
  }

  public synchronized boolean isAcked() {
    return acked;
  }

  public Packet getPacket() {
    return packet;
  }

  private class Task implements Runnable {
    @Override
    public void run() {
      while (!isAcked()) {
        System.out.println("Sending packet");
        packetHandler.sendPacket(packet);
        try {
          Thread.sleep(timeout);
        }
        catch(InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }
}
