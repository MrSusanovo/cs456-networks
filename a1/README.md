# CS 456 - Assignment 1

Tim Pham  
t24pham  
20511526  

## Building

```bash
make
```

## Running the server

Run the server bash script. It takes 1 param, REQ_CODE, which must be an integer.

```bash
$ ./server.sh REQ_CODE
  
# Example
$ ./server.sh 1337
```

## Running the client

Run the client bash script. It takes 4 params:
  1. SERV_ADDR - The address of the server
  2. NEG_PORT - The initial port to communicate with the server
  3. REQ_CODE - Request code chosen by the server (Must be an integer)
  4. MSG - The message to send to the server (Must be surrounded be `"`)
  
```bash
$ ./client.sh SERV_ADDR NEG_PORT REQ_CODE "MSG"
  
# Example
$ ./client.sh localhost 8080 1337 "This is the message"
```

## Environment

UW Machines: ubuntu1604-002, ubuntu1604-008
make: 4.1
javac: 9-internal

