package t24pham.cs456.a2.common.exceptions;

public class EndOfFileException extends Exception {
  public EndOfFileException() {
    super("No more bytes to read from file.");
  }
}
