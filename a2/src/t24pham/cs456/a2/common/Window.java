package t24pham.cs456.a2.common;

import java.util.concurrent.locks.ReentrantLock;

public class Window {
  private final int WINDOW_SIZE;
  private final int SEQ_MOD;
  private int minSeqNum;
  private int currentSeqNum;
  private final ReentrantLock lock;

  public Window(int windowSize, int seqMod) {
    this.WINDOW_SIZE = windowSize;
    this.SEQ_MOD = seqMod;
    this.minSeqNum = 0;
    this.currentSeqNum = 0;
    this.lock = new ReentrantLock();
  }

  public int getWindowSize() {
    return WINDOW_SIZE;
  }

  public int assignNextSeqNum() {
    lock.lock();
    try {
      int oldSeqNum = currentSeqNum;
      currentSeqNum = (currentSeqNum + 1) % SEQ_MOD;
      return oldSeqNum;
    } finally {
      lock.unlock();
    }
  }

  public int getLowerSeqNum() {
    lock.lock();
    try {
      return minSeqNum;
    } finally {
      lock.unlock();
    }
  }
  public int getSeqNumAfter(int i) {
    lock.lock();
    try {
      return (i + 1) % SEQ_MOD;
    } finally {
      lock.unlock();
    }
  }

  public int getPreviousSeqNum() {
    lock.lock();
    try {
      return (minSeqNum - 1 + SEQ_MOD) % SEQ_MOD;
    } finally {
      lock.unlock();
    }
  }

  public void shiftUp() {
    lock.lock();
    try {
      minSeqNum = (minSeqNum + 1) % SEQ_MOD;
    } finally {
      lock.unlock();
    }
  }

  public void shiftUpTo(int i) {
    lock.lock();
    try {
      while (minSeqNum != i) {
        minSeqNum = (minSeqNum + 1) % SEQ_MOD;
      }
    } finally {
      lock.unlock();
    }
  }

  public boolean isSeqNumWithinWindow(int seqNum) {
    lock.lock();
    try {
      final int upperSeqLimit = minSeqNum + WINDOW_SIZE;

      // Window does not wrap
      if (upperSeqLimit <= SEQ_MOD) {
        return (seqNum >= minSeqNum && seqNum < upperSeqLimit);
      }
      // Window wraps due to mod
      return (seqNum >= minSeqNum && seqNum < SEQ_MOD) // Within range before mod wrap
          || (seqNum >= 0 && seqNum < (upperSeqLimit % SEQ_MOD)); // Within range after mod wrap
    } finally {
      lock.unlock();
    }
  }
}
