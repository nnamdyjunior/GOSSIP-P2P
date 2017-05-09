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

import java.util.*;
import java.net.InetAddress;
import java.net.Socket;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.DataOutputStream;

/**
 * name
 */
public class ProcessMessage {

    public static String[] getCommands(String message) { //separate chained commands
        String[] commands = message.split("%");
        return commands;
    } //end getCommands

    public static String[] translate(String message) { //translate each command
        if(message.equals("PEERS?\\n") || message.equals("PEERS?")){
            //System.out.println("no peers yet");
            return new String[] {"-1"};
        }

        String[] mess = message.split(":");

        switch (mess[0]) {
            case "GOSSIP":
                return new String[] {"gossip.txt", mess[3] + ":"+ mess[2] + ":" + mess[1]};
                //break;

            case "PEER":
                //System.out.println("Receiving Peer...");
                return new String[] {"peers.txt", mess[1] + " " + mess[2] + " " + mess[3]};
                //break;
                
            case "PEERS?\\n":
                return new String[] {"-1"};

            case "PEERS?":
                return new String[] {"-1"};
                    
            default:
                System.out.println("Invalid message");
                return new String[] {};
                //break;
        }
    } //end translate

    //broadcast message to all known peers
    public static void sendToPeers(byte[] message, Map<String, String> aMap) {
        Set<String> names = aMap.keySet();

        for(String name : names){
            try{
                String[] details = aMap.get(name).split(" ");
                int port = Integer.parseInt(details[0].substring(5));
                InetAddress ip_address = InetAddress.getByName(details[1].substring(3));

              //check if address is valid, before attempting to send message  
              if(ip_address.isReachable(3000)){ 
                try{
                    Socket sock = new Socket(ip_address, port);sock.setSoTimeout(3); 
                    OutputStream os = sock.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(os);
                    dos.write(message);
                    dos.flush();

                    //update the time last seen
                    WriteToFiles.peers_timer.put(name, System.currentTimeMillis()%1000);

                    Thread.sleep(1000);
                    sock.close();
                }catch(Exception e){
                    continue;
                }
              }else{

              }

            }catch(Exception ee){
                continue;
            }

        }
        System.err.println(); 
    } //end sendToPeers
    
}