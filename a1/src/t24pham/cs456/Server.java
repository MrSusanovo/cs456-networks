package t24pham.cs456;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Random;

public class Server {

  public static void main(String[] args) throws Exception {
    // Arguments
    int reqCode;
    // Sockets
    DatagramSocket udpSocket;
    ServerSocket tcpSocket;
    // Server info
    int myNegPort;
    int myTransPort;
    // Client info
    InetAddress clientAddr;
    int clientNegPort;
    Socket clientTransSocket;

    /*
     * Get arguments
     */
    if (args.length != 1) {
      System.err.println("Invalid number of arguments.");
      System.err.println("Expected:\n- Request code");
      return;
    }
    String reqCodeStr = args[0];
    // Store reqCode
    reqCode = Integer.parseInt(reqCodeStr);

    /*
     * Create UDP for negotiation
     */
    udpSocket = UDPHelper.getAvailableSocket();

    /*
     * Store and print neg_port
     */
    myNegPort = udpSocket.getLocalPort();
    System.out.println("SERVER_PORT=" + myNegPort);

    while (true) {
      /*
       * Receive req_code (manually in order to read client info)
       */
      byte[] negBuffer = new byte[4];
      DatagramPacket negPacket = new DatagramPacket(negBuffer, negBuffer.length);
      udpSocket.receive(negPacket);
      int recvReqCode = ByteBuffer.wrap(negPacket.getData()).getInt();

      /*
       * Check req_code
       */
      if (reqCode != recvReqCode) continue;

      /*
       * Get client info
       */
      clientAddr = negPacket.getAddress();
      clientNegPort = negPacket.getPort();

      /*
       * Create TCP for transaction
       */
      tcpSocket = getAvailableTCPSocket();

      /*
       * Store and print r_port
       */
      myTransPort = tcpSocket.getLocalPort();
      System.out.println("SERVER_TCP_PORT=" + myTransPort);

      /*
       * Send r_port
       */
      UDPHelper.sendInt(myTransPort, udpSocket, clientAddr, clientNegPort);

      /*
       * Receive r_port confirmation
       */
      int confirmMyTransPort = UDPHelper.receiveInt(udpSocket);

      /*
       * Check confirmation and send ACK (0 is no, 1 is ok)
       */
      if (confirmMyTransPort != myTransPort) {
        UDPHelper.sendInt(0, udpSocket, clientAddr, clientNegPort);
        continue;
      }
      UDPHelper.sendInt(1, udpSocket, clientAddr, clientNegPort);

      /*
       * Accept incoming TCP connection
       */
      clientTransSocket = tcpSocket.accept();

      /*
       * Read message
       */
      BufferedReader fromClient = new BufferedReader(
          new InputStreamReader(clientTransSocket.getInputStream()));
      String msg = fromClient.readLine();

      /*
       * Print message
       */
      System.out.println("SERVER_RCV_MSG=" + msg);

      /*
       * Create and send reversed message
       */
      DataOutputStream toClient = new DataOutputStream(clientTransSocket.getOutputStream());
      String msgRev = new StringBuilder(msg).reverse().toString();
      toClient.writeBytes(msgRev + "\n");
    }
  }

  public static ServerSocket getAvailableTCPSocket() throws SocketException {
    Random rand = new Random();
    int attempts = 0;

    while (attempts < 100) {
      try {
        attempts++;
        return new ServerSocket(rand.nextInt(65535) + 1024);
      } catch (IOException e) {
        continue;
      }
    }
    throw new SocketException("No available ports");
  }
}
