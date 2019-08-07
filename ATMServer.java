/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Victor H
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class ATMServer 
{
    private static int connectionPort = 8989;
    private static ArrayList<ATMServerThread> atmServers;
    private static String banner = ""; // Default banner

    public static void main(String[] args) throws IOException 
    {
        // Hardcoded accounts that persist as long as the server is running
        // Each account has a card number, passcode and balance
        Account[] accs = { new Account("1111", "1234", 10000),
                               new Account("1112", "12345", 250000), 
                               new Account("1113", "123456", 1000000) };

        ServerSocket serverSocket = null;
        int threadnum = 0;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        boolean listening = true;

        try {
                serverSocket = new ServerSocket(connectionPort);
        } catch (IOException e) {
                System.err.println("Could not listen on port: " + connectionPort);
                System.exit(1);
        }

        System.out.println("Bank started listening on port: " + connectionPort);
        serverSocket.setSoTimeout(5000);
        Socket socket;
        atmServers = new ArrayList<ATMServerThread>(); 
        while (listening) 
        {
            try 
            {
                socket = serverSocket.accept();
                // Creates thread for each client that connects to the socket
                if (socket.isConnected()) 
                {
                    System.out.println("A client has connected");
                    System.out.println("Type any message under 80 characters to update the banner \nType 'shutdown' to shut down the server");
                    atmServers.add(new ATMServerThread(socket, accs, banner));
                    atmServers.get(threadnum).start();
                    threadnum = threadnum + 1;
                }
            } catch (SocketTimeoutException e) {}
            if(in.ready())
            {
                String msg = in.readLine();
                // Updates the banner or shuts down the server if the right string is inputted
                if(msg.length() <= 80)
                {
                    if(msg.equals("shutdown"))
                        listening = false;
                    else
                    {
                        banner = msg;
                        for(ATMServerThread atmst : atmServers)
                        {
                            atmst.setBanner(banner); 
                        }                     
                        System.out.println("Banner has been updated to " + banner);
                    }
                }
                else
                {
                    System.out.println("Banner can't be over 80 characters");
                }				
            }
        }
        serverSocket.close();
        System.exit(0);
    }
}