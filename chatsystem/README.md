
# A Chat System


Start the server  `./r1_httpd.sh`

Start Java RMI daemon  `./r2_rmid.sh` 

Start Jini look up service  `./r3_reggie.sh`


## The Server

Start the Server `./chatserver.sh`
   Observe logging of clients
  
## The Clients  

Start the client `./chatclient.sh`

* Name a chat user `.name` <given a name e.g .name  client1>
* Connect to the server `.connect server1`
* Send a text message to the chat `.text <Type your chat message here>`
* List currently active chat users. `listclients`
* Leave the chat room `.disconnect`


## Exit

RMI Deamon `rmid -stop`