package t24pham.cs456.a2.sender;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;

import t24pham.cs456.a2.common.Constants;
import t24pham.cs456.a2.common.FileReader;
import t24pham.cs456.a2.common.IFileSender;
import t24pham.cs456.a2.common.PacketHandler;
import t24pham.cs456.a2.common.Utils;

public class SenderMain {

  /**
   * @param args * 0: protocol selector (0 or 1)
   *             * 1: timeout (positive int)
   *             * 2: filename of file to send
   */
  public static void main(String[] args) {
    int protocol;
    int timeout;
    String filename;
    FileReader fileToSend;
    ChannelInfo channelInfo;
    PacketHandler packetHandler;
    IFileSender sender;

    /* Check number of arguments */
    if (args.length != 3) {
      Utils.error("Expected 3 arguments. Instead, got " + args.length);
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
    // Timeout
    try {
      timeout = Integer.parseInt(args[1]);
    } catch (Exception e) {
      Utils.error("Expected a positive integer for timeout");
      return;
    }
    // Filename
    filename = args[2];
    try {
      fileToSend = new FileReader(filename, Constants.MAX_PAYLOAD_LEN);
    } catch (FileNotFoundException e) {
      Utils.error("Could not find file " + filename);
      return;
    }

    /* Get recvInfo from file */
    try {
      channelInfo = new ChannelInfo();
    } catch (IOException e) {
      Utils.error("Could not get channel details from 'channelInfo'");
      return;
    }

    /* Create sending socket to channel with recvInfo */
    try {
      packetHandler = new PacketHandler(channelInfo);
    } catch (SocketException e) {
      Utils.error("No socket was available");
      return;
    }

    /* Call appropriate protocol */
    if (protocol == 0) { // Go-Back-N
      sender = new GoBackNSender(Constants.SEND_WINDOW_LEN, Constants.SEQ_MOD, packetHandler, fileToSend, timeout);
    } else if (protocol == 1) { // Selective Repeat
      sender = new SelectiveRepeatSender(Constants.SEND_WINDOW_LEN, Constants.SEQ_MOD, packetHandler, fileToSend, timeout);
    } else {
      Utils.error("Unknown protocol selector received");
      return;
    }

    try {
      sender.send();
    } catch (Exception e) {
      Utils.error("An error occurred while sending the file");
    }
  }
}
