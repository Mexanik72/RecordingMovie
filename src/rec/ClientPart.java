package rec;

import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.net.*;
import java.io.*;

public class ClientPart {
 
    Socket socket;
    File selectFile;
    ClientPart(){
                
          
    }
    
    void sendFile(File selectedFile){
        //----------------------------------------------
        int port = 2154;
        String addres = "192.168.254.78";
        InetAddress ipAddress = null;
        try {
            ipAddress = InetAddress.getByName(addres);
            socket = new Socket(ipAddress, port);
            
        } 

        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        //---------------------------------------------
        
        DataOutputStream outD; 
        try{
            outD = new DataOutputStream(socket.getOutputStream());          
                                
                outD.writeLong(selectedFile.length());//מעסûכאול נאחלונ פאיכא
                outD.writeUTF(selectedFile.getName());//מעסûכאול טלÿ פאיכא
            
                FileInputStream in = new FileInputStream(selectedFile);
                byte [] buffer = new byte[64*1024];
                int count;
                
                while((count = in.read(buffer)) != -1){
                    outD.write(buffer, 0, count);
                }
                outD.flush();
                in.close();           
            socket.close();         
        }
        catch(IOException e){
            e.printStackTrace();
        }   
    }
}