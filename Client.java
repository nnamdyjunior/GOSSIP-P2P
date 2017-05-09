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

import java.io.*;
import java.net.*;
import java.io.BufferedReader;
import java.time.*;
import java.util.*;
import java.security.*;
import java.net.InetAddress;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

//import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import ASN1.*;
/**
 * Client
 */
class Client implements Runnable{
    String ip_address, connection_type;
    int port;

    public Client(String ipad, int prt, String connectiontype){
        ip_address = ipad;
        port = prt;
        connection_type = connectiontype;
    }

    public void run() {
        
        try{
            String completemessage = "";
            String timestamp = getTimeStamp();

            if(connection_type.equals("U")){
                UDPClient(ip_address, port, completemessage, timestamp);
            }
            else if(connection_type.equals("T")){
                TCPClient(ip_address, port, completemessage, timestamp);
            }
            else{
                return;
            }
        } catch (Exception e) {
            //TODO: handle exception
        }
    }

    public static void TCPClient(String ipad, int port, String msg, String tstmp) {
        String sentence, modifiedSentence;
        DataOutputStream outToServer;
        DataInputStream inFromServer;
        String[] words;

        try {
            //start the client and let it connect to the specified port
            InetAddress ipadd = InetAddress.getByName(ipad);
            Socket clientSocket = new Socket(ipadd, port);
            
            //if there's an initial message, process it
            if(msg.length() > 0){
                outToServer = new DataOutputStream(clientSocket.getOutputStream());
                inFromServer = new DataInputStream(clientSocket.getInputStream());
                byte[] cbuf = new byte[1024];
                inFromServer.read(cbuf);
                modifiedSentence = "";

            }
            //start a thread that continuously listens for messages from the server
            ContinuousListener clis = new ContinuousListener(new DataInputStream(clientSocket.getInputStream()));
            Thread listThread = new Thread(clis);
            listThread.start();

            //continuously accept messages to be sent to the server until connection is closed
            for(;;){
                    BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
                    outToServer = new DataOutputStream(clientSocket.getOutputStream());

                    sentence = inFromUser.readLine();
                    //get all commands in message
                    String[] comms = getCommands(sentence);
                    //translate each command
                    byte[] sentence_bytes;
                    for(String comm : comms){
                        sentence_bytes = translate(comm, tstmp);
                        outToServer.write(sentence_bytes);
                    }
                    //words = sentence.split("%");
                    
            }
      
            //clientSocket.close();
            } catch (Exception e) {
                //TODO: handle exception
            }
    }//end TCPClient

    public static void UDPClient(String ipad, int port, String msg, String tstmp) {
        try{
            InetAddress iPAddress = InetAddress.getByName(ipad);
            DatagramPacket sendPacket, receivePacket;
            String sentence; 
            String[] words;
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            DatagramSocket clientSocket = new DatagramSocket();
            
            byte[] sendData = new byte[1024];
            byte[] receiveData = new byte[1024];
            
            //if there's an initial message, process it
            if(msg.length() > 0){
                sentence = msg;
                words = sentence.split("%");
                
                sendData = sentence.getBytes();
                sendPacket = new DatagramPacket(sendData, sendData.length, iPAddress, port);
                clientSocket.send(sendPacket);
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                //read all the replies from the UDP server
                for(int k=0; k<words.length; k++){
                    clientSocket.receive(receivePacket);
                    String modifiedSentence = new String(receivePacket.getData());
                    System.out.println("FROM SERVER:" + modifiedSentence);
                }
            }

            //start a thread that continuously listens for messages from the server
            ContinuousUDPListener culis = new ContinuousUDPListener(clientSocket);
            Thread ulistThread = new Thread(culis);
            ulistThread.start();

            //continuously accept messages to be sent to the server until connection is closed
            for(;;){
                sentence = inFromUser.readLine();
                //get all commands in message
                String[] comms = getCommands(sentence);
                //translate each command
                sentence = "";
                for(String comm : comms){
                    byte[] sentence_bytes = translate(comm, tstmp);
                    sendPacket = new DatagramPacket(sentence_bytes, sentence_bytes.length, iPAddress, port);
                    clientSocket.send(sendPacket);
                }
                
            }
            
       }catch(Exception e){
            //System.err.println("Something terrible happened...");
            //e.printStackTrace();
       }
    }//end UDPClient

public static String formatInput(String input, String tstamp) {
     String ans = "";
     
     String parse_str = tstamp + ":" + input;       
     //calculate the sha-256 digest value
     try {
         MessageDigest md = MessageDigest.getInstance("SHA-256");
         md.update(parse_str.getBytes("UTF-8")); 
         byte[] digest = md.digest();
         String sha = Base64.getEncoder().encodeToString(digest);
         String fin = sha + ":" + parse_str;
         ans += fin;
     } catch (Exception e) {
        //TODO: handle exception
     }

   return ans;  
 } //end formatInput

 public static String[] getCommands(String message) { //separate chained commands
    String[] commands = message.split("(%|\n)");
    return commands;
 } //end getCommands

 public static byte[] translate(String message, String tstamp) { //translate each command
        PeersQuery pq; byte[] pq_bytes;

        if(message.equals("PEERS?\\n") || message.equals("PEERS?")){
            pq = new PeersQuery();
            pq_bytes = pq.encode();
            return pq_bytes;
        }

        String[] mess = message.split(":");

        switch (mess[0]) {
            case "GOSSIP":
                String new_mess = formatInput(mess[1], tstamp);
                //byte[] gossip_bytes = new byte[1024];
                String[] content = new_mess.split(":");
                Gossip gossip = new Gossip(content[0], content[1], content[2]);
                byte[] gossip_bytes = gossip.encode();
                return gossip_bytes;

            case "PEER":
                Peer peer = new Peer(mess[1], Integer.parseInt(mess[2].substring(5)), mess[3].substring(3));
                byte[] peer_bytes = peer.encode(); 
                return peer_bytes;
                
            case "PEERS?\\n":
                pq = new PeersQuery();
                pq_bytes = pq.encode(); 
                return pq_bytes;

            case "PEERS?":
                pq = new PeersQuery();
                pq_bytes = pq.encode();
                return pq_bytes;
                    
            case "LEAVE":
                Leave leave = new Leave(mess[1]);
                byte[] leave_bytes = leave.encode();
                return leave_bytes;

            default:
                byte[] empty = new byte[1];
                return empty;
        }
 } //end translate

 public static String getTimeStamp() {
     return (LocalDateTime.now() + "").replace(':', '-');
 }//end getTimeStamp

}

class ContinuousListener implements Runnable{
    DataInputStream xxx;

    public ContinuousListener(DataInputStream dis){
        xxx = dis;
    }

    public void run() {
        for(;;){

        try{
        byte[] cbuf = new byte[1024];
        int yy = xxx.read(cbuf);
        //decode message
        ArrayList<PeerInfo> initial = new ArrayList<>();
        PeersAnswer pans = new PeersAnswer(initial);//initialize new PeersAnswer

        Decoder dec = new Decoder(cbuf);
        PeersAnswer prans = pans.decode(dec);
        ArrayList<PeerInfo> answer = prans.peers;

        String result = "PEERS|" + answer.size() + "|";
        for(PeerInfo pi : answer){
            result += pi.name + ":PORT=" + pi.port + ":" + pi.ip_address + "|";
        }
        System.err.println(result);

        }catch(Exception ex){}
        }
    }
}

class ContinuousUDPListener implements Runnable{
    DatagramSocket datagramSocket;

    public ContinuousUDPListener(DatagramSocket dSock){
        datagramSocket = dSock;
    }

    public void run() {
        for(;;){

        try{
        byte[] cbuf = new byte[1024];
        DatagramPacket receive = new DatagramPacket(cbuf, cbuf.length);
        datagramSocket.receive(receive);
        cbuf = receive.getData();

        //decode message
        ArrayList<PeerInfo> initial = new ArrayList<>();
        PeersAnswer pans = new PeersAnswer(initial);//initialize new PeersAnswer

        Decoder dec = new Decoder(cbuf);
        PeersAnswer prans = pans.decode(dec);
        ArrayList<PeerInfo> answer = prans.peers;

        String result = "PEERS|" + answer.size() + "|";
        for(PeerInfo pi : answer){
            result += pi.name + ":PORT=" + pi.port + ":" + pi.ip_address + "|";
        }
        System.err.println(result);
        }catch(Exception ex){}
        }
    }
}
