package t24pham.cs456.a2.receiver;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import t24pham.cs456.a2.common.ConnectionInfo;

public class RecvInfo extends ConnectionInfo {
  private static final String FILE_NAME = "recvInfo";

  public RecvInfo(String hostname, int port) throws UnknownHostException {
    super(hostname, port);
  }

  public RecvInfo() throws IOException {
    super(FILE_NAME);
  }

  public void save() throws IOException {
    File file = new File(FILE_NAME);
    if (file.exists() && !file.delete()) {
      throw new IOException("Could not delete old recvInfo");
    }
    super.save(FILE_NAME);
  }
}
