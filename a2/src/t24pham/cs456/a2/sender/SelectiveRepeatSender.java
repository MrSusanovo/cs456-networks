package t24pham.cs456.a2.sender;

import java.io.IOException;

import t24pham.cs456.a2.common.FileReader;
import t24pham.cs456.a2.common.IFileSender;
import t24pham.cs456.a2.common.Packet;
import t24pham.cs456.a2.common.PacketHandler;
import t24pham.cs456.a2.common.Utils;
import t24pham.cs456.a2.common.Window;
import t24pham.cs456.a2.common.exceptions.EndOfFileException;
import t24pham.cs456.a2.common.exceptions.PayloadTooLargeException;

public class SelectiveRepeatSender implements IFileSender {
  private Window window;
  private TimedPacketWindow queue;
  private PacketHandler packetHandler;
  private FileReader inputFile;
  private int timeout;


  public SelectiveRepeatSender(int windowSize, int seqMod, PacketHandler packetHandler,
                               FileReader inputFile, int timeout) {
    this.window = new Window(windowSize, seqMod);
    this.queue = new TimedPacketWindow(window.getWindowSize());
    this.packetHandler = packetHandler;
    this.inputFile = inputFile;
    this.timeout = timeout;
  }

  @Override
  public void send() {
    Thread fileToPacketWorker = new Thread(new FileToPacketTask());
    Thread ackerWorker = new Thread(new AckerTask());

    ackerWorker.start();
    fileToPacketWorker.start();

    try {
      // Wait for file to finish being sent
      System.out.println("Waiting for file to be sent...");
      fileToPacketWorker.join(); // Blocks
      // Send eot
      Packet eot = new Packet(Packet.Type.EOT, window.assignNextSeqNum(), new byte[0]);
      packetHandler.sendPacket(eot);
      // Wait for eot confirmation
      System.out.println("Waiting for EOT reply...");
      ackerWorker.join(); // Blocks
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    catch (PayloadTooLargeException e) {
      Utils.error("Unexpected payload too large exception caught");
      System.exit(-1);
    }

    System.out.println("File sent!");
  }


  private class FileToPacketTask implements Runnable {

    @Override
    public void run() {
      while (true) {
        try {
          System.out.println("Getting chunk from file");
          byte[] payload = inputFile.getNextChunk();
          Packet packet = new Packet(Packet.Type.DATA_PACKET, window.assignNextSeqNum(), payload);
          TimedPacket tp = new TimedPacket(packetHandler, packet, timeout);
          queue.add(tp); // Blocks
          tp.send();
        } catch (EndOfFileException e) {
          return;
        } catch (IOException e) {
          Utils.error("Unexpected IOException caught");
          System.exit(-1);
        } catch (PayloadTooLargeException e) {
          Utils.error("Unexpected payload too large exception caught");
          System.exit(-1);
        }
      }
    }
  }

  private class AckerTask implements Runnable {

    @Override
    public void run() {
      while (true) {
        try {
          Packet ack = packetHandler.receivePacket(); // Blocks
          if (ack.getType() == Packet.Type.ACK) {
            queue.acknowledge(ack.getSeqNum());
          } else if (ack.getType() == Packet.Type.EOT) {
            return;
          }
        } catch (PayloadTooLargeException e) {
          Utils.error("Unexpected payload too large exception caught");
          System.exit(-1);
        } catch (IOException e) {
          Utils.error("Unexpected IOException caught");
          System.exit(-1);
        }
      }
    }
  }
}
