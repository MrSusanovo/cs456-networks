package t24pham.cs456.a2.sender;

import java.io.IOException;
import java.net.UnknownHostException;

import t24pham.cs456.a2.common.ConnectionInfo;

public class ChannelInfo extends ConnectionInfo {
  private static final String FILE_NAME = "channelInfo";

  public ChannelInfo(String hostname, int port) throws UnknownHostException {
    super(hostname, port);
  }

  public ChannelInfo() throws IOException {
    super(FILE_NAME);
  }

  public void save() throws IOException {
    super.save(FILE_NAME);
  }
}
