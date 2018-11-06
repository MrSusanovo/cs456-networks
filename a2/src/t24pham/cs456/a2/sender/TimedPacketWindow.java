package t24pham.cs456.a2.sender;

import java.util.Iterator;
import java.util.LinkedList;

public class TimedPacketWindow {
  private int sizeLimit;
  private LinkedList<TimedPacket> queue;

  public TimedPacketWindow(int size) {
    this.sizeLimit = size;
    this.queue = new LinkedList<>();
  }

  public synchronized void add(TimedPacket p) {
    while (queue.size() >= sizeLimit) {
      try {
        System.out.println("Waiting to add to TimedPacketWindow");
        wait();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    System.out.println("Adding to TimedPacketWindow");
    queue.addLast(p);
    notifyAll();
  }

  public synchronized void acknowledge(int seqNum) {
    boolean nonAckedSeen = false;
    Iterator<TimedPacket> it = queue.iterator();
    TimedPacket toRemove = null;
    while (it.hasNext()) {
      TimedPacket tp = it.next();
      // Found packet, acknowledge
      if (tp.getPacket().getSeqNum() == seqNum) {
        tp.acknowledge();
      }
      // Clean up acknowledged packets if in sequence
      if (!nonAckedSeen) {
        if (tp.isAcked()) {
          toRemove = tp;
        } else {
          nonAckedSeen = true;
        }
      }
    }
    if (toRemove != null) {
      queue.remove(toRemove);
      notifyAll();
    }
  }
}
