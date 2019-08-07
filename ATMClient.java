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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ATMClient 
{
    private static int connectionPort = 8989;

    private static Socket ATMSocket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static FileReader file;
    private static BufferedReader rf;
    private static Scanner scanner;
    private static String lang;
    
    /**
     * Prints the message from the client to the server
     * 
     * @param msg - The message that was inputted by the client
     */
    private static void printToServer(String msg) 
    {
        // Doesn't print to server unless the message is less than 10 bytes in size
        while(msg.getBytes().length > 10)
        {
            System.out.println("Can't be sent to server, message too long");
            System.out.print("> ");
            msg = scanner.nextLine();
        }
        out.print(msg);
        out.println();
        out.flush();
    }
    
    /**
     * Prints the necessary line for the client from a language text file
     * 
     * @param lineNum - The number in which the needed line is located
     * @param var - A variable that replaces part of the String
     * @return line - The line to be shown to the client
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private static String readString(String lineNum, String var) throws IOException
    {
        try{
            file = new FileReader(lang + ".txt");
            rf = new BufferedReader(file);
            String fileLine = rf.readLine();
            String[] lines = fileLine.split(";");
            String line = lines[1];
            while(!lineNum.equals(lines[0]))
            {
                fileLine = rf.readLine();
                lines = fileLine.split(";");
                line = lines[1];
                if(lines[1].contains("(var)"))
                    line = lines[1].replace("(var)", var);
            }
            return line;
        }
        catch (FileNotFoundException fe)
        {
            lang = "en";
            return "No such language \n" + readString("6", "0");
        }
    }
    
    /**
     * The main function of the client, handles client-side input and output
     * 
     * @param args - The IP Adress and (Optional) the chosen language 
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException 
    {
        String adress = "";
        try {
                adress = args[0];
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Missing argument ip-adress");
                System.exit(1);
            }
        try {
                ATMSocket = new Socket(adress, connectionPort);
                out = new PrintWriter(ATMSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(ATMSocket.getInputStream()));
        } catch (UnknownHostException e) {
                System.err.println("Unknown host: " + adress);
                System.exit(1);
        } catch (IOException e) {
                System.err.println("Couldn't open connection to " + adress);
                System.exit(1);
        }
        scanner = new Scanner(System.in);

        // Selects Language, English is chosen unless there's a second parameter
        lang = "en";
        if(args.length == 2)
        {
            if("es".equalsIgnoreCase(args[1]))
            {
                lang = "es";
            }
            if("sv".equalsIgnoreCase(args[1]))
            {
                lang = "sv";
            }
        }   
        
        String serverMsg;
        
        // Reads user input for card number and sends it to server for verification
        serverMsg = in.readLine();
        System.out.println(readString(serverMsg, "0"));
        System.out.print("> ");
        String card = scanner.nextLine();       
        printToServer(card);

        // Loops until a valid card number is inputted
        while(in.readLine().equals("deniedC"))
        {
            serverMsg = in.readLine();
            System.out.println(readString(serverMsg, "0"));
            serverMsg = in.readLine();
            System.out.println(readString(serverMsg, "0"));
            System.out.print("> ");
            card = scanner.nextLine();
            printToServer(card);
        }

        // Reads user input for passcode and sends it to server for verification
        serverMsg = in.readLine();
        System.out.println(readString(serverMsg, "0"));
        System.out.print("> ");
        String pass = scanner.nextLine();
        printToServer(pass);

        // Loops until the correct passcode is inputted
        while (in.readLine().equals("deniedP")) 
        {
            serverMsg = in.readLine();
            System.out.println(readString(serverMsg, "0"));
            serverMsg = in.readLine();
            System.out.println(readString(serverMsg, "0"));
            System.out.print("> ");
            pass = scanner.nextLine();
            printToServer(pass);
        }

        // Variables
        int menuOption;
        int userInput;
        String banner;
        String amount;
        String serverResponse;
        
        // Outputs the banner followed by a welcoming message and the menu options for the user
        banner = in.readLine();
        if(!banner.equals(""))
            System.out.println(banner);
        serverMsg = in.readLine();
        System.out.println(readString(serverMsg, "0"));
        serverMsg = in.readLine();
        System.out.println(readString(serverMsg, "0"));
        System.out.print("> ");
        // Gets the menu option from the client and sends it to the server
        menuOption = scanner.nextInt();
        printToServer(Integer.toString(menuOption));

        // Depending on the chosen menu option, the client and server react accordingly
        // (5) is Exit, which is why it loops until that is chosen
        while (menuOption != 5) 
        {
            switch (menuOption)
            {
                case 1: // (1) Balance
                    // Gets the balance amount from the server and outputs it
                    serverMsg = in.readLine();
                    amount =  in.readLine();
                    System.out.println(readString(serverMsg, amount));
                    break;
                case 2: // (2) Withdrawal
                    // Asks for the amount to be witdrawn and sends it to server
                    serverMsg = in.readLine();
                    System.out.println(readString(serverMsg, "0"));
                    userInput = scanner.nextInt();
                    printToServer(Integer.toString(userInput));
                    // Asks for the security key and sends it to server
                    serverMsg = in.readLine();
                    System.out.println(readString(serverMsg, "0"));
                    String key = scanner.next();
                    printToServer(key);
                    // Server responds differently depending on whether it worked
                    serverResponse = in.readLine();
                    if(serverResponse.equals("withErr"))
                    {
                        // Notifies that withdrawal was unsuccessful
                        serverMsg = in.readLine();
                        System.out.println(readString(serverMsg, "0"));
                        amount =  in.readLine();
                    }
                    else
                        // If the withdrawal was successful
                        amount =  serverResponse;
                    // Prints the balance for the client
                    serverMsg = in.readLine();
                    System.out.println(readString(serverMsg, amount));
                    break;
                case 3: // (3) Deposit
                    // Asks for the amount to be deposited and sent to the server
                    serverMsg = in.readLine();
                    System.out.println(readString(serverMsg, "0"));
                    userInput = scanner.nextInt();
                    printToServer(Integer.toString(userInput));
                    // Server responds differently depending on whether it worked
                    serverResponse = in.readLine();
                    if(serverResponse.equals("depErr"))
                    {
                        // Notifies that the deposit was unsuccessful
                        serverMsg = in.readLine();
                        System.out.println(readString(serverMsg, "0"));
                        amount = in.readLine();
                    }
                    else
                        // If the deposit was successful
                        amount = serverResponse;
                    // Prints the balance for the client
                    serverMsg = in.readLine();
                    System.out.println(readString(serverMsg, amount));
                    break;
                case 4: // (4) Change Language
                    // Asks for the language the client wants
                    serverMsg = in.readLine();
                    System.out.println(readString(serverMsg, "0"));
                    System.out.print("> ");
                    lang = scanner.next();
                    break;
                default: break;
            }
            // Outputs the banner and menu again after being done with the switch case     
            banner = in.readLine();
            if(!banner.equals(""))
                System.out.println(banner);
            serverMsg = in.readLine();
            System.out.println(readString(serverMsg, "0"));
            System.out.print("> ");
            // Asks for the menu option before continuing the loop
            menuOption = scanner.nextInt();
            printToServer(Integer.toString(menuOption));
        }
        // Informs about exiting and closes all streams
        serverMsg = in.readLine();
        System.out.println(readString(serverMsg, "0"));
        scanner.close();
        out.close();
        in.close();
        ATMSocket.close();
    }
}