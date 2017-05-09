/* ------------------------------------------------------------------------- */
/*   Copyright (C) 2017 
                Author:  nagu2014@my.fit.edu, sivapilot@gmail.com
                Florida Tech, Computer Science
   
       This program is free software; you can redistribute it and/or modify
       it under the terms of the GNU Affero General Public License as published by
       the Free Software Foundation; either the current version of the License, or
       (at your option) any later version.
   
      This program is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
      GNU General Public License for more details.
  
      You should have received a copy of the GNU Affero General Public License
      along with this program; if not, write to the Free Software
      Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.              */
/* ------------------------------------------------------------------------- */

import java.io.BufferedReader;
import java.net.Socket;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.nio.file.Paths;

import ASN1.*;
/**
 * TCPServerSock
 */
public class TCPServerSock implements Runnable{
    private int port = 0;
    private String file = "";
    int delay = 20; //delay, 20 seconds by default

    //constructor
    public TCPServerSock(int prt, String fle, int delayy){
        port = prt;
        file = fle;
        delay = delayy;
    } //end constructor

    public void startTCPServer() {
        Socket sock; String input; BufferedReader reader; BufferedWriter out;
        Charset utf8 = StandardCharsets.UTF_8;
	    try {
            ServerSocket server_sock=new ServerSocket(port, 5);
            System.out.println( "TCP Server ready..." ) ;

            //continuously listen and accept new connections on TCP
	        for(;;) {
                //start a new thread for every new client
			    sock=server_sock.accept();
                ChildTCPServer child_server = new ChildTCPServer(sock, file, delay);
			    Thread child_thread = new Thread(child_server);
                child_thread.start();

            }
	    }catch(IOException e){	
            //do nothing 
            
        } 
    } //end startTCPServer

    public void run(){
        startTCPServer();
    } //end run

}

/**
 * ChildTCPServer
 */
class ChildTCPServer implements Runnable{

    int time = 0;
    int delay = 20; //20 by default

    class KillChild implements Runnable{

        public void run() {
            try{
            for( ; ; ){
                Thread.sleep(1000);
                time += 1;

                if(time == delay){
                    sock.close();
                }
            }
            }catch(Exception exc){}
        }
    }

    Socket sock;
    String file;
    public ChildTCPServer (Socket this_sock, String this_file, int delayy) {
        sock = this_sock;
        file = this_file;
        delay = delayy;
    }

    public void run() {
        String input;
        DataOutputStream out;
        DataInputStream reader;
        Charset utf8 = StandardCharsets.UTF_8;

        //handle the connection
        try{
            reader =  new DataInputStream(sock.getInputStream());
		    out = new DataOutputStream(sock.getOutputStream());

		    //try to kill the child
            KillChild killChild = new KillChild();
            Thread kill_thread = new Thread(killChild);
            kill_thread.start();

			for(;;) {
                //restart timer if there's a new connection
                time = 0;
                byte[] from_client = new byte[1024];
				if(reader.read(from_client) < 2){
                        break;
                } 
		    	//out.write(input+"\n");
                //System.out.println(input);

                //execute commands
                Decoder decoder = new Decoder(from_client);
                int tag_val = decoder.tagVal();
                switch (tag_val) {
                    case 1:
                        Thread.sleep(2000);
                        Gossip gossip = new Gossip("sha", "time", "mess");
                        gossip = gossip.decode(decoder);
                        WriteToFiles.writeToFile(file, "gossip.txt", new String[]{gossip.message + ":" + gossip.timee + ":" + gossip.shaval}); 
                        Thread.sleep(5000);
                        break;
                    
                    case 2:
                        Peer peer = new Peer("name", 0, "ipaddress");
                        peer = peer.decode(decoder);
                        WriteToFiles.writeToFile(file, "peers.txt", new String[]{peer.name + " PORT=" + peer.port + " IP=" +  peer.ip_address}); 
                        break;
                    
                    case 3:
                        Set<String> p_set = WriteToFiles.peers_map.keySet();
                        ArrayList<PeerInfo> prsss = new ArrayList<>();
                        for(String pr : p_set){
                            try{

                                PeerInfo pinf = new PeerInfo(pr, Integer.parseInt(WriteToFiles.peers_map.get(pr).split(" ")[0].substring(5)), WriteToFiles.peers_map.get(pr).split(" ")[1]);
                                prsss.add(pinf);

                            }catch(Exception eee){
                                continue;
                            }
                        }
                        PeersAnswer pans = new PeersAnswer(prsss);
                        byte[] mybytes4 = pans.encode();
                        out.write(mybytes4);
                        out.flush();
                        break;
                
                    case 4:
                        Leave leave = new Leave("");
                        leave = leave.decode(decoder);
                        WriteToFiles.removePeer(file, leave.peer);
                        break;

                    default:
                        break;
                }
                
            }
			sock.close();   //close the connection
            //System.err.println("exiting thread...");
        }catch(Exception exc){
            //error handling
        }
    }
}