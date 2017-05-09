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

import java.lang.Thread;
import java.io.File;
import java.util.Arrays;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * MainServer
 */
class MainServer {

    public static void main(String[] args) {
        int port = 1234;
        String filepath = "";
        String complete_path = "";
        String connection_type = "";
        String ip_address = "";
        int port_out = 0;
        int delay = 20; //20 by default

        //parse commands using getOpt (cli)
        //add Options
        Options options = new Options();

        options.addOption("p", true, "port_to_listen_on");
        options.addOption("d", true, "directory");
        options.addOption("T", false, "TCP mode");
        options.addOption("U", false, "UDP mode");
        options.addOption("s", true, "iPAddress");
        options.addOption("P", true, "port_to_connect_to");
        options.addOption("D", true, "delay");

        CommandLineParser clp = new DefaultParser();
        try {
            CommandLine cl = clp.parse(options, args);

            //options for the server
            if(cl.hasOption("p")){
                port = Integer.parseInt(cl.getOptionValue("p"));
            }else{
                System.err.println("No valid port selected.");
                return;
            }

            if(cl.hasOption("d")){
                filepath = cl.getOptionValue("d");
                //if there a '/' in front, remove it to make it a valid directory
                if(filepath.substring(0, 1).equalsIgnoreCase("/")){
                    filepath = filepath.substring(1);
                }
            }else{
                System.err.println("No valid directory given.");
                return;
            }

            if(cl.hasOption("D")){
                delay = Integer.parseInt(cl.getOptionValue("D"));
            }

            //options for the client
            if(cl.hasOption("T")){
                connection_type = "T";
            }else if(cl.hasOption("U")){
                connection_type = "U";
            }

            if(cl.hasOption("s")){
                ip_address = cl.getOptionValue("s");
            }

            if(cl.hasOption("P")){
                port_out = Integer.parseInt(cl.getOptionValue("P"));
            }

        } catch (ParseException e) {
            //TODO: handle exception
        }

        //create directory (if it doesn't already exist)
        try {
            Files.createDirectories(Paths.get(filepath)); 
        } catch (Exception e) {
            //TODO: handle exception
            System.err.println("Couldn't create directory"); 
            System.err.println(filepath);
        }
        
        //read in required files (create them if they dont already exist)
        try {
            Files.createFile(Paths.get(filepath + "/gossip.txt"));
            Files.createFile(Paths.get(filepath + "/peers.txt"));
        } catch (Exception e) {
            //TODO: handle exception
        }
        WriteToFiles.readFiles(filepath);

        //start the servers
        TCPServerSock tcpServer = new TCPServerSock(port, filepath, delay);
        UDPServer udpServer = new UDPServer(port, filepath); 

        Thread tcpThread = new Thread(tcpServer);
        Thread udpThread = new Thread(udpServer);

        tcpThread.start();
        udpThread.start();

        //start the client
        if(!connection_type.equals("") && port_out != 0 && !ip_address.equals("")){
            Client client = new Client(ip_address, port_out, connection_type);
            Thread clientThread = new Thread(client);
            clientThread.start();
        }

        //Start thread to forget peers
        ForgetPeer forgetPeer = new ForgetPeer(filepath + "/peers.txt", delay);
        Thread forgetPeerThread = new Thread(forgetPeer);
        forgetPeerThread.start();
    }
}

/**
 * ForgetPeer
 */
class ForgetPeer implements Runnable{

    String path;
    int delay;
    private static Charset utf8 = StandardCharsets.UTF_8;
    public ForgetPeer (String flpath, int delayy) {
        path = flpath;
        delay = delayy;
    }

    public void run(){
        //continue to check and remove expired peers

        //set target time (2 days)
        long target_time = 172800;

        for(;;){
            //find the current time
            long current_time = System.currentTimeMillis()/1000;

            //get all peers and check their last seen time
            for(String x : WriteToFiles.peers_timer.keySet()){
                long tyme = WriteToFiles.peers_timer.get(x);
                if(current_time - tyme >= delay){
                    WriteToFiles.peers_timer.remove(x);
                    WriteToFiles.peers_map.remove(x);
                }
            }

            //update the peers database
            String[] new_content = new String[WriteToFiles.peers_map.size()];
            int i=0;
            for(String y : WriteToFiles.peers_map.keySet()){
                new_content[i] = y + " " + WriteToFiles.peers_map.get(y);
                i++;
            }
            try{
                Files.write(Paths.get(path), Arrays.asList(new_content), utf8);
                //Thread.sleep(3600000); //sleep for 1 hour
                Thread.sleep(1000);
            }catch(Exception efe){} 

        }
    }
}