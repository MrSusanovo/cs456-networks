package t24pham.cs456.a2.sender;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import t24pham.cs456.a2.common.FileReader;
import t24pham.cs456.a2.common.IFileSender;
import t24pham.cs456.a2.common.Packet;
import t24pham.cs456.a2.common.PacketHandler;
import t24pham.cs456.a2.common.Utils;
import t24pham.cs456.a2.common.Window;
import t24pham.cs456.a2.common.exceptions.EndOfFileException;
import t24pham.cs456.a2.common.exceptions.PayloadTooLargeException;

public class GoBackNSender implements IFileSender {
  private Window window;
  private PacketHandler packetHandler;
  private FileReader fileToSend;
  private int timeout;

  private final ReentrantLock queueLock;
  private final Condition queueIsNotEmpty;
  private final Condition queueIsNotFull;
  private LinkedList<Packet> queue;

  private final ReentrantLock statusLock;
  private final Condition ackReceived;
  private boolean fileStillSending;
  private int lastSeqNum;

  public GoBackNSender(int windowSize, int seqMod, PacketHandler packetHandler, FileReader
      fileToSend, int timeout) {
    this.window = new Window(windowSize, seqMod);
    this.packetHandler = packetHandler;
    this.fileToSend = fileToSend;
    this.timeout = timeout;
    this.queueLock = new ReentrantLock();
    this.queueIsNotEmpty = this.queueLock.newCondition();
    this.queueIsNotFull = this.queueLock.newCondition();

    this.queue = new LinkedList<>();
    this.statusLock = new ReentrantLock();
    this.ackReceived = this.statusLock.newCondition();
    this.fileStillSending = true;
    this.lastSeqNum = Integer.MIN_VALUE;
  }

  @Override
  public void send() {
    Thread acker = new Thread(new AckerTask(), "Acker");
    Thread sender = new Thread(new SenderTask(), "Sender");

    acker.start();
    sender.start();

    try {
      sender.join();
    } catch (Exception e) {
      Utils.error("Something happened while waiting ");
      System.exit(-1);
    }

    System.out.println("File successfully sent!");
  }

  /**
   * Helper functions
   */

  private boolean isFileStillSending() {
    statusLock.lock();
    try {
      return fileStillSending;
    } finally {
      statusLock.unlock();
    }
  }

  private void setSendComplete() {
    statusLock.lock();
    try {
      fileStillSending = false;
    } finally {
      statusLock.unlock();
    }
  }

  private int getLastSeqNum() {
    statusLock.lock();
    try {
      return lastSeqNum;
    } finally {
      statusLock.unlock();
    }
  }

  private void setLastSeqNum(int i) {
    statusLock.lock();
    try {
      lastSeqNum = i;
    } finally {
      statusLock.unlock();
    }
  }

  private void sendWindow() {
    Object[] window;
    queueLock.lock();
    try {
      window = queue.toArray();
    } finally {
      queueLock.unlock();
    }
    /* Send each */
    for (int i = 0; i < window.length; ++i) {
      Packet p = (Packet) window[i];
      packetHandler.sendPacket(p);
    }
  }

  private void waitForTimeoutOrAck() {
    statusLock.lock();
    try {
      if (fileStillSending) {
        System.out.println("Waiting for Timeout or ACK");
        ackReceived.await(timeout, TimeUnit.MILLISECONDS);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      statusLock.unlock();
    }
  }

  private void notifyAck() {
    statusLock.lock();
    try {
      ackReceived.signal();
    } finally {
      statusLock.unlock();
    }
  }

  private int getQueueSize() {
    queueLock.lock();
    try {
      return queue.size();
    } finally {
      queueLock.unlock();
    }
  }

  private void queueUp(Packet p) {
    queueLock.lock();
    try {
      while (queue.size() >= window.getWindowSize()) {
        try {
          System.out.println("Waiting until queue is not full");
          queueIsNotFull.await();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }

      queue.addLast(p);
      queueIsNotEmpty.signalAll();
    } finally {
      queueLock.unlock();
    }
  }

  private void removeUpTo(int i) {
    queueLock.lock();
    try {
      while (queue.isEmpty()) {
        try {
          queueIsNotEmpty.await();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }

      if (window.isSeqNumWithinWindow(i)) {
        /* Move window up */
        window.shiftUpTo(window.getSeqNumAfter(i));
        int oldSize = queue.size();
        /* Remove anything that is no longer in window */
        Packet p = queue.getFirst();
        while (!window.isSeqNumWithinWindow(p.getSeqNum())) {
          queue.removeFirst();
          if (queue.isEmpty()) break;
          p = queue.getFirst();
        }

        if (queue.size() < oldSize) {
          queueIsNotFull.signalAll();
        }
      }
    } finally {
      queueLock.unlock();
    }
  }

  private class SenderTask implements Runnable {

    @Override
    public void run() {
      boolean eofReached = false;
      int lastSeqNumSeen = Integer.MIN_VALUE;
      while (!eofReached) {
        /* Load queue */
        while (getQueueSize() < window.getWindowSize()) {
          try {
            byte[] chunk = fileToSend.getNextChunk();
            Packet p = new Packet(Packet.Type.DATA_PACKET, window.assignNextSeqNum(), chunk);
            lastSeqNumSeen = p.getSeqNum();
            queueUp(p); // Blocks if full
          } catch (PayloadTooLargeException e) {
            Utils.error("Unexpected large payload in create packet. Aborting...");
            System.exit(-1);
          } catch (IOException e) {
            Utils.error("Could not read from file. Aborting...");
            System.exit(-1);
          } catch (EndOfFileException e) {
            eofReached = true;
            break;
          }
        }
        sendWindow();
        waitForTimeoutOrAck();
      }

      /* Signal last seq num */
      setLastSeqNum(lastSeqNumSeen);

      /* Continue sending packets in window in case of loss */
      while (isFileStillSending()) {
        sendWindow();
        waitForTimeoutOrAck();
      }

      /* All ACKs received, send EOT */
      try {
        Packet eot = new Packet(Packet.Type.EOT, window.assignNextSeqNum(), new byte[0]);
        packetHandler.sendPacket(eot);
      } catch (PayloadTooLargeException e) {
        Utils.error("Unexpected large payload in create packet. Aborting...");
        System.exit(-1);
      }
    }
  }

  private class AckerTask implements Runnable {

    @Override
    public void run() {
      while (true) {
        /* Receive packet */
        Packet p = null;
        try {
          p = packetHandler.receivePacket();
        } catch (PayloadTooLargeException e) {
          Utils.error("Illegal packet caught. Discarding...");
          continue;
        } catch (IOException e) {
          Utils.error("Could not receive packet. Aborting...");
          System.exit(-1);
        }

        /* Process ACK */
        if (p.getType() == Packet.Type.ACK) {
          /* Check if last seq num */
          if (p.getSeqNum() == getLastSeqNum()) {
            /* Receiver received everything, so close */
            setSendComplete();
            return;
          }

          /* Otherwise ack packets in queue */
          removeUpTo(p.getSeqNum());
        }

        /* Notify to add more packets */
        notifyAck();
      }
    }
  }
}
