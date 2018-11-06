package t24pham.cs456;

import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Client {

  public static void main(String[] args) throws Exception {
    // Arguments
    InetAddress serverAddr;
    int serverNegPort;
    int reqCode;
    String message;
    // Sockets
    DatagramSocket udpSocket;
    Socket tcpSocket;
    // Server info
    int serverTransPort;

    /*
     * Get arguments
     */
    if (args.length != 4) {
      System.err.println("Invalid number of arguments.");
      System.err.println("Expected:\n- Address\n- Port\n- Request code\n- Message");
      return;
    }
    String serverAddrStr = args[0];
    String negPortStr = args[1];
    String reqCodeStr = args[2];
    message = args[3];
    // Convert arguments to proper type
    serverAddr = InetAddress.getByName(serverAddrStr);
    serverNegPort = Integer.parseInt(negPortStr);
    reqCode = Integer.parseInt(reqCodeStr);


    /*
     * Create UDP for negotiation
     */
    udpSocket = new DatagramSocket();
    udpSocket.setSoTimeout(10000);

    /*
     * Send req_code
     */
    UDPHelper.sendInt(reqCode, udpSocket, serverAddr, serverNegPort);

    /*
     * Receive r_port
     */
    try {
      serverTransPort = UDPHelper.receiveInt(udpSocket);
    }
    catch (SocketTimeoutException e) {
      System.err.println("Request timed out. Check if the request code is correct.");
      return;
    }

    /*
     * Send r_port confirmation
     */
    UDPHelper.sendInt(serverTransPort, udpSocket, serverAddr, serverNegPort);

    /*
     * Receive ACK
     */
    int ack = UDPHelper.receiveInt(udpSocket);
    if (ack == 0) {
      System.out.println("Received bad r_port.");
      return;
    }

    /*
     * Close UDP
     */
    udpSocket.close();

    /*
     * Create TCP for transaction
     */
    tcpSocket = new Socket(serverAddr, serverTransPort);

    /*
     * Send message
     */
    DataOutputStream toServer = new DataOutputStream(tcpSocket.getOutputStream());
    toServer.writeBytes(message + "\n");

    /*
     * Receive and print reversed message
     */
    BufferedReader fromServer = new BufferedReader(
        new InputStreamReader(tcpSocket.getInputStream()));
    String messageRev = fromServer.readLine();
    System.out.println(messageRev);

    /*
     * Close connection
     */
    fromServer.close();
    tcpSocket.close();
  }
}
