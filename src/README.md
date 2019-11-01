# EECS 325 Project #1
By: David Blincoe

## Overview
I decided to implement the UDP discovery program for this project. 

To execute on the servers:
```shell script
cd p2p
java p2p
```

## Structure
Everything is based around the: 
- p2p
    - Reads command line input and feeds input to ConnectionManager

- Discovery
    - Manages discovery protocol

- ConnectionManager
    - Starts ConnectionThread
    - Manages concurrence of all connections

- TransferManager
    - Starts TransferServer
    - Manages listening for the transfers

## Handling issues
- Query storms are prevented by keeping a hashset of the query ids
- The first response or queries are the only ones that hosts consider. After the first message relating to a specific message id, all other messages relating to that type are ignored.