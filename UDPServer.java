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

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.util.*;
import java.nio.charset.*;

import ASN1.*;
/**
 * UDPServer
 */
public class UDPServer implements Runnable{
    final int PACKETSIZE = 1024;
    private int port = 0;
    private String file = "";
    Charset utf8 = StandardCharsets.UTF_8;

    //constructor
    public UDPServer(int prt, String fle){
        port = prt;
        file = fle;
    } //end constructor

    public void startUDPServer() {
        
        try{
         // Construct the socket
         DatagramSocket socket = new DatagramSocket( port ) ;

         System.out.println( "UDP Server ready..." ) ;

         //continuously listen for packets on UDP
         for( ;; ){
            // Create a packet
            DatagramPacket packet = new DatagramPacket( new byte[PACKETSIZE], PACKETSIZE ) ;

            // Receive a packet (blocking)
            socket.receive( packet ) ;

            //start a thread to handle the packet
            ChildUDPServer childUDPServer = new ChildUDPServer(socket, packet, file);
            Thread child_udp_thread = new Thread(childUDPServer);
            child_udp_thread.start();

        }  
     }catch( Exception e )
     {
        System.out.println( e ) ;
     }

    } //end startUDPServer

    public void run() {
        startUDPServer();
    } //end run
    
}

/**
 * ChildUDPServer
 */
class ChildUDPServer implements Runnable{
    
    DatagramSocket socket;
    DatagramPacket packet;
    String file;
    public ChildUDPServer (DatagramSocket sock, DatagramPacket pack, String fle) {
        socket = sock;
        packet = pack;
        file = fle;
    }

    public void run() {
        //execute commands
        try{
                Decoder decoder = new Decoder(packet.getData());
                int tag_val = decoder.tagVal();
                switch (tag_val) {
                    case 1:
                        Gossip gossip = new Gossip("sha", "time", "mess");
                        gossip = gossip.decode(decoder);
                        WriteToFiles.writeToFile(file, "gossip.txt", new String[]{gossip.message + ":" + gossip.timee + ":" + gossip.shaval}); 
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
                        packet.setData(mybytes4);
                        socket.send(packet);
                        break;
                    
                    case 4:
                        Leave leave = new Leave("");
                        leave = leave.decode(decoder);
                        WriteToFiles.removePeer(file, leave.peer);
                        break;

                    default:
                        break;
                }
        }catch(Exception exc){} 
    }
}