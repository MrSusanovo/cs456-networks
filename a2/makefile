SRC_ROOT_DIR = src
SRC_DIR = $(SRC_ROOT_DIR)/t24pham/cs456/a2
BUILD_DIR = build
JC = javac
JFLAGS = -d $(BUILD_DIR) -sourcepath $(SRC_ROOT_DIR) -classpath $(BUILD_DIR)

sender_src = $(SRC_DIR)/sender/SenderMain.java
receiver_src = $(SRC_DIR)/receiver/ReceiverMain.java

all: Sender Receiver scripts

Sender : $(sender_src)
	$(JC) $(JFLAGS) $(sender_src)

Receiver : $(receiver_src)
	$(JC) $(JFLAGS) $(receiver_src)

scripts:
	chmod +x Sender
	chmod +x Receiver

_PHONY:
	clean

clean :
	rm -rf $(BUILD_DIR)/*
	rm -f recvInfo
	rm -f channelInfo
