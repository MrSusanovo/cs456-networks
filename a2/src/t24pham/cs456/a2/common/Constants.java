package t24pham.cs456.a2.common;

public class Constants {
  public static final int INT_LEN = 4;
  public static final int HEADER_LEN = INT_LEN * 3;
  public static final int MAX_PAYLOAD_LEN = 500;
  public static final int MAX_PACKET_LEN = HEADER_LEN + MAX_PAYLOAD_LEN;
  public static final int SEND_WINDOW_LEN = 10;
  public static final int SEQ_MOD = 256;
}
