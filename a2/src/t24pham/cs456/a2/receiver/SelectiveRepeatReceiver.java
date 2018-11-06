package t24pham.cs456.a2.receiver;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import t24pham.cs456.a2.common.IFileReceiver;
import t24pham.cs456.a2.common.Packet;
import t24pham.cs456.a2.common.PacketHandler;
import t24pham.cs456.a2.common.Utils;
import t24pham.cs456.a2.common.Window;
import t24pham.cs456.a2.common.exceptions.PayloadTooLargeException;

public class SelectiveRepeatReceiver implements IFileReceiver {
  private Window window;
  private PacketWindow buffer;
  private PacketHandler packetHandler;
  private FileOutputStream outputFile;

  public SelectiveRepeatReceiver(int windowSize, int seqMod, PacketHandler packetHandler,
                                 FileOutputStream outputFile) {
    this.window = new Window(windowSize, seqMod);
    this.buffer = new PacketWindow(windowSize);
    this.packetHandler = packetHandler;
    this.outputFile = outputFile;
  }

  @Override
  public void receive() {
    Thread receiverWorker = new Thread(new ReceiverTask());
    Thread fileWriteWorker = new Thread(new FileWriterTask());

    receiverWorker.start();
    fileWriteWorker.start();

    /* Wait for file to finish writing */
    try {
      // Wait for file to finish writing
      fileWriteWorker.join(); // Blocks
      System.out.println("Writer finished");
      receiverWorker.interrupt();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    System.out.println("File received!");
  }

  private class ReceiverTask implements Runnable {

    @Override
    public void run() {
      /* Receive data and send acks until EOT */
      while (true) {
        try {
          Packet p = packetHandler.receivePacket(); // Blocks
          /* Add packet to buffer if in window */
          buffer.add(p); // Blocks
          /* ACK packet */
          if (p.getType() == Packet.Type.DATA_PACKET) {
            Packet ack = new Packet(Packet.Type.ACK, p.getSeqNum(), new byte[0]);
            packetHandler.sendPacket(ack);
          }
        } catch (PayloadTooLargeException e) {
          Utils.error("Illegal packet received");
          System.exit(-1);
        } catch (IOException e) {
          e.printStackTrace();
          System.exit(-1);
        }
      }
    }
  }

  /**
   * Remove packets from buffer in sequence order and write to file
   * If required not found, block
   */
  private class FileWriterTask implements Runnable {

    @Override
    public void run() {
      while (true) {
        try {
          System.out.println("Trying to get " + window.getLowerSeqNum());
          Packet p = buffer.remove(window.getLowerSeqNum()); // Blocks
          /* Check EOT */
          if (p.getType() == Packet.Type.EOT) {
            // Return EOT signal
            try {
              Packet eot = new Packet(Packet.Type.EOT, p.getSeqNum(), new byte[0]);
              packetHandler.sendPacket(eot);
            } catch (PayloadTooLargeException e) {
              Utils.error("Unexpected payload too large exception caught");
              System.exit(-1);
            }
            // Finish writing
            outputFile.close();
            System.out.println("Finished writing file");
            return;
          }
          if (p.getType() == Packet.Type.DATA_PACKET) {
            outputFile.write(p.getPayload());
            outputFile.flush();
            window.shiftUp();
          }
        } catch (IOException e) {
          Utils.error("Could not properly write file");
          System.exit(-1);
        }
      }
    }
  }
}
