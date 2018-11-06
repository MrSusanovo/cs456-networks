package t24pham.cs456.a2.common;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ConnectionInfo {
  private InetAddress address;
  private int port;

  public ConnectionInfo(String hostname, int port) throws UnknownHostException {
    this.address = InetAddress.getByName(hostname);
    this.port = port;
  }

  public ConnectionInfo(String filename) throws FileNotFoundException, IOException {
    Scanner file = new Scanner(new FileReader(filename));
    try {
      this.address = InetAddress.getByName(file.next());
      this.port = Integer.parseInt(file.next());
    } catch (Exception e) {
      throw new InvalidObjectException("Provided file has an invalid format");
    }
    file.close();
  }

  public void save(String filename) throws IOException {
    BufferedWriter file = new BufferedWriter(new FileWriter(filename));

    file.write(address.getHostName() + " " + String.valueOf(port));
    file.newLine();
    file.flush();
    file.close();
  }

  public String getHostname() {
    return address.getHostName();
  }

  public int getPort() {
    return port;
  }

  public void setHostname(String h) throws UnknownHostException {
    this.address = InetAddress.getByName(h);
  }

  public void setPort(int i) {
    this.port = i;
  }

  public InetAddress getAddress() {
    return address;
  }
}
