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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

/**
 * WriteToFiles
 */
public class WriteToFiles {
    public static ConcurrentHashMap<String, String> message_map = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> peers_map = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Long> peers_timer = new ConcurrentHashMap<>();
    private static Charset utf8 = StandardCharsets.UTF_8;

    public static void readFiles(String path) {
        String mess_path = path + "/" + "gossip.txt"; //gossips file
        String peers_path = path + "/" + "peers.txt"; //peers file

        try{
            //read all known messages into Map for faster access
            List<String> messages = new ArrayList<>();
            messages = Files.readAllLines(Paths.get(mess_path), utf8);
            for(String x : messages){
                String[] y = x.split(":");
                if(y.length >= 3){
                    message_map.put(y[0], y[1] + " " + y[2]);
                }
            }

            //read all known peers into a map for faster access
            List<String> messages2 = new ArrayList<>();
            messages2 = Files.readAllLines(Paths.get(peers_path), utf8);
            for(String x : messages2){
                String[] y = x.split(" ");
                if(y.length >= 3){
                    peers_map.put(y[0], y[1] + " " + y[2]);

                    //read all peers from file and start counter
                    peers_timer.put(y[0], System.currentTimeMillis()/1000);
                }
            }
        }catch(Exception e){
            System.err.println(e);
        }
        

    } //end readFiles

    public static synchronized void writeToFile(String path, String file, String[] data) {
        String complete_path = path + "/" + file; //file to write to
        
        if(file.equals("gossip.txt")){
            try{
                String test = message_map.get(data[0].split(":")[0]);
                //if its not there, add it to the file
                if(test == null){ throw new NullPointerException(); }
                //if its there, say so
                System.err.println("DISCARDED");
                //System.err.println(test);
            }catch(NullPointerException e){
                //add it to the messages map
                message_map.put(data[0].split(":")[0], data[0].split(":")[1] + " " + data[0].split(":")[2]);
                //add it to the messages file
                try {
                    Files.write(Paths.get(complete_path), Arrays.asList(data), utf8, StandardOpenOption.CREATE, StandardOpenOption.APPEND); 
                } catch (Exception ee) {
                    System.err.println(ee);
                }
                //encode to ASN1 and broadcast it to all known peers
                String[] mssg = data[0].split(":");
                Gossip gossip = new Gossip(mssg[2], mssg[1], mssg[0]);
                byte[] mess = gossip.encode();
                ProcessMessage.sendToPeers(mess, peers_map);
                System.err.println(data[0].split(":")[0]); //print to the std err 
            }
        }else{
            try{
                String test = peers_map.get(data[0].split(" ")[0]);
                //if its not there, add it to the file
                if(test == null){ throw new NullPointerException(); }
                //if its there, update its address
                peers_map.put(data[0].split(" ")[0], data[0].split(" ")[1] + " " + data[0].split(" ")[2]);
                peers_timer.put(data[0].split(" ")[0], System.currentTimeMillis()/1000);

                String[] new_content = new String[peers_map.size()];
                int i=0;
                for(String y : peers_map.keySet()){
                    new_content[i] = y + " " + peers_map.get(y);
                    i++;
                }
                try{
                    Files.write(Paths.get(complete_path), Arrays.asList(new_content), utf8);
                }catch(Exception efe){} 

            }catch(NullPointerException e){
                //add it to the peers map
                peers_map.put(data[0].split(" ")[0], data[0].split(" ")[1] + " " + data[0].split(" ")[2]);
                peers_timer.put(data[0].split(" ")[0], System.currentTimeMillis()/1000);

                //add it to the peers file
                try {
                    Files.write(Paths.get(complete_path), Arrays.asList(data), utf8, StandardOpenOption.CREATE, StandardOpenOption.APPEND); 
                } catch (Exception ee) {
                    System.err.println(ee);
                }
            }
        }
        
        
    } //end writeToFile

    public static synchronized void removePeer(String path, String peerr){
        String complete_path = path + "/peers.txt"; //file to amend
        try{
            //remove it from the peers map
            peers_map.remove(peerr);

            //update the peers file
            String[] new_content = new String[peers_map.size()];
            int i=0;
            for(String y : peers_map.keySet()){
                new_content[i] = y + " " + peers_map.get(y);
                i++;
            }
            Files.write(Paths.get(complete_path), Arrays.asList(new_content), utf8);
            
        }catch(Exception e){

        }
    }//end remove peer
}