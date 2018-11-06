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

public class GoBackNReceiver implements IFileReceiver {
  private Window window;
  private PacketHandler packetHandler;
  private FileOutputStream fileToWrite;
  private int eotSeqNum;

  public GoBackNReceiver(int windowSize, int seqMod, PacketHandler packetHandler,
                         FileOutputStream fileToWrite) {
    this.window = new Window(windowSize, seqMod);
    this.packetHandler = packetHandler;
    this.fileToWrite = fileToWrite;
    this.eotSeqNum = Integer.MIN_VALUE;
  }

  @Override
  public void receive() {
    while (true) {
      Packet p = null;

      /* Receive packet */
      try {
        System.out.println("About to receive");
        p = packetHandler.receivePacket(); // Blocks
      } catch (PayloadTooLargeException e) {
        Utils.error("Caught illegal packet, discarding...");
        continue;
      } catch (IOException e) {
        Utils.error("Could not receive packet. Aborting...");
        System.exit(-1);
      }

      /* Check if EOT */
      if (p.getType() == Packet.Type.EOT) {
        eotSeqNum = p.getSeqNum();
      }

      /* If not next seq, ACK last seen */
      if (p.getSeqNum() != window.getLowerSeqNum()) {
        try {
          Packet ack = new Packet(Packet.Type.ACK, window.getPreviousSeqNum(), new byte[0]);
          packetHandler.sendPacket(ack);
        } catch (PayloadTooLargeException e) {
          Utils.error("Unexpected large payload in create packet. Aborting...");
          System.exit(-1);
        }
      }

      /* Accept next seq */
      else {
        /* Check if EOT */
        if (window.getLowerSeqNum() == eotSeqNum) {
          /* Send EOT */
          try {
            Packet eot = new Packet(Packet.Type.EOT, eotSeqNum, new byte[0]);
            packetHandler.sendPacket(eot);
          } catch (PayloadTooLargeException e) {
            Utils.error("Unexpected large payload in create packet. Aborting...");
            System.exit(-1);
          }
          /* Finish writing file */
          try {
            fileToWrite.write(0xff);
            fileToWrite.flush();
            fileToWrite.close();
          } catch (IOException e) {
            Utils.error("Could not finish writing the output file. Aborting...");
            System.exit(-1);
          }

          System.out.println("File received successfully!");
          return;
        }

        /* Expect next seq num */
        window.shiftUp();
        byte[] data = p.getPayload();
        /* Write to file */
        try {
          fileToWrite.write(data);
          fileToWrite.flush();
        } catch (IOException e) {
          Utils.error("Could not write to output file");
          System.exit(-1);
        }
      }
    }
  }
}
