package t24pham.cs456.a2.common;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import t24pham.cs456.a2.common.exceptions.EndOfFileException;

public class FileReader {
  private final int MAX_CHUNK_SIZE;
  private final BufferedInputStream fileToRead;
  private boolean isFinished;

  public FileReader(String filename, int chunkSize) throws FileNotFoundException {
    this.MAX_CHUNK_SIZE = chunkSize;
    this.fileToRead = new BufferedInputStream(new FileInputStream(filename));
    this.isFinished = false;
  }

  public byte[] getNextChunk() throws EndOfFileException, IOException {
    if (isFinished)
      throw new EndOfFileException();

    int bytesRead;
    byte[] buffer = new byte[MAX_CHUNK_SIZE];

    /*
      Read in bytesRead bytes into buffer.
      If -1 is returned from read, end of file reached
    */
    if ((bytesRead = fileToRead.read(buffer)) != -1) {
      // Full buffer, so just send
      if (bytesRead == MAX_CHUNK_SIZE) {
        return buffer;
      }
      // Partial buffer, so trim then send
      return ByteBuffer.wrap(buffer, 0, bytesRead).array();
    }
    else { // Handle end of file case
      // Set end of file flag
      this.isFinished = true;
      // Cleanup
      fileToRead.close();
      // Signal eof
      throw new EndOfFileException();
    }
  }
}
