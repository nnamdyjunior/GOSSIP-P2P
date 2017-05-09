#!/bin/bash

#adding a peer (the witness) and sending a gossip message via the TCP client
#this adds the witness server to the list of known peers so it can receive the forwarded messages.
#The second message is sent after a delay of 7 seconds, and is not received by the witness.

cmd="PEER:JOHN:PORT=50000:IP=127.0.0.1%GOSSIP:This should be seen%GOSSIP:This should not be seen"


echo $cmd | ./run.sh -p 46000 -d /path/folder3 -T -s "localhost" -P 45000