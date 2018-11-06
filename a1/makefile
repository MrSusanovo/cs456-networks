CODE_DIR = ./src/t24pham/cs456
BUILD_DIR = ./build

build: Server Client UDPHelper

Server: UDPHelper $(CODE_DIR)/Server.java
	javac -d $(BUILD_DIR) -classpath $(BUILD_DIR) src/t24pham/cs456/Server.java

Client: UDPHelper $(CODE_DIR)/Client.java
	javac -d $(BUILD_DIR) -classpath $(BUILD_DIR) src/t24pham/cs456/Client.java

UDPHelper: $(CODE_DIR)/UDPHelper.java
	javac -d $(BUILD_DIR) src/t24pham/cs456/UDPHelper.java

.PHONY: clean

clean:
	rm -rf $(BUILD_DIR)/*