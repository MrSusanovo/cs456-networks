package t24pham.cs456.a2.common.exceptions;

import t24pham.cs456.a2.common.Constants;

public class PayloadTooLargeException extends Exception {
  public PayloadTooLargeException() {
    super("Payload is larger than specified limit of " + Constants.MAX_PAYLOAD_LEN + " bytes");
  }
}
