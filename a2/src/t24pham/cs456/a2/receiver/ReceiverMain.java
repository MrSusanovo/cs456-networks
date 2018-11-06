package t24pham.cs456.a2.receiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import t24pham.cs456.a2.common.Constants;
import t24pham.cs456.a2.common.IFileReceiver;
import t24pham.cs456.a2.common.PacketHandler;
import t24pham.cs456.a2.common.Utils;

public class ReceiverMain {

  public static void main(String[] args) {
    int protocol;
    String filename;
    FileOutputStream fileToWrite;
    RecvInfo recvInfo;
    PacketHandler packetHandler;
    IFileReceiver receiver;

    /* Check number of arguments */
    if (args.length != 2) {
      Utils.error("Expected 2 arguments. Instead, got " + args.length);
      return;
    }

    /* Parse and store arguments */
    // Protocol Selector
    try {
      protocol = Integer.parseInt(args[0]);
    } catch (Exception e) {
      Utils.error("Expected 0 or 1 for protocol selector");
      return;
    }
    // Filename
    filename = args[1];

    /* Verify that file does not exist */
    File file = new File(filename);
    if (file.exists()) {
      Utils.error("Output file '" + filename + "' already exists");
      return;
    }
    /* Create file */
    try {
      file.createNewFile();
    } catch (IOException e) {
      Utils.error("Could not create output file");
      return;
    }
    try {
      fileToWrite = new FileOutputStream(filename);
    } catch (FileNotFoundException e) {
      Utils.error("Could not find file " + filename);
      return;
    }

    /* Get host info and create recvInfo */
    try {
      packetHandler = new PacketHandler();
    } catch (SocketException e) {
      Utils.error("Could not get an available socket");
      return;
    }
    try {
      recvInfo = new RecvInfo(InetAddress.getLocalHost().getHostName(), packetHandler.getLocalPort());
      recvInfo.save(); // Write recvInfo file
    } catch(UnknownHostException e) {
      Utils.error("Unexpected bad local address");
      return;
    } catch (IOException e) {
      Utils.error("Could not write recvInfo file");
      return;
    }

    /* Call appropriate protocol */
    if (protocol == 0) { // Go-Back-N
      receiver = new GoBackNReceiver(Constants.SEND_WINDOW_LEN, Constants.SEQ_MOD,
          packetHandler, fileToWrite);
    } else if (protocol == 1) { // Selective Repeat
      receiver = new SelectiveRepeatReceiver(Constants.SEND_WINDOW_LEN, Constants.SEQ_MOD,
          packetHandler, fileToWrite);
    } else {
      Utils.error("Unknown protocol selector received");
      return;
    }

    try {
      receiver.receive();
    } catch (Exception e) {
      Utils.error("An error occurred while receiving the file");
    }
  }
}
