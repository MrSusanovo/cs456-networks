package t24pham.cs456.a2.receiver;

import java.util.Iterator;
import java.util.LinkedList;

import t24pham.cs456.a2.common.Packet;

public class PacketWindow {
  private int sizeLimit;
  private LinkedList<Packet> queue;

  public PacketWindow(int size) {
    this.sizeLimit = size;
    this.queue = new LinkedList<>();
  }

  public synchronized void add(Packet p) {
    while (queue.size() >= sizeLimit * 2) {
      try {
        System.out.println("Waiting to add to PacketQueue");
        wait();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    queue.addFirst(p);
    notifyAll();
  }

  public synchronized Packet remove(int seqNum) {
    while (true) {
      Iterator<Packet> it = queue.iterator();
      while (it.hasNext()) {
        Packet p = it.next();
        if (p.getSeqNum() == seqNum) {
          queue.remove(p);
          notifyAll();
          return p;
        }
      }
      try {
        wait(); // Packet not found, wait until more comes
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }
}
