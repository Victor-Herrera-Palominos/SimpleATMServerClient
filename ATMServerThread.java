/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Victor H
 * 
 * Thread created by Server, one for each connected Client
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ATMServerThread extends Thread 
{
    private Socket socket = null;
    private BufferedReader in;
    private PrintWriter out;
    private Account acc;
    private Account[] accs;
    private String banner;
    public ATMServerThread(Socket socket, Account[] accs, String banner) 
    {
            super("ATMServerThread");
            this.socket = socket;
            this.accs = accs;
            this.banner = banner;
    }
    
    /**
     * Finds the Account based on the card number
     * 
     * @param card - The card number that the client inputted
     * @return true if the inputted card number corresponds to an account, and it's not currently signed in
     */
    private boolean selectAcc(String card)
    {
        for (Account account : accs) 
        {
            if (account.getCardNumber().equals(card)) 
            {
                acc = account;
                if(!acc.getSession())
                    return true;
            }
        }
        return false;
    }
    
    /**
     * Verifies that the account passcode is correct
     * 
     * @param pass - The passcode that the client inputted
     * @return true if the account passcode equals the inputted passcode
     */
    private boolean verifyAcc(String pass) 
    {
        return acc.getPasscode().equals(pass);
    }
    
    /**
     * Changes the default banner
     * 
     * @param banner - The banner that was inputted server-side
     */
    public void setBanner(String banner)
    {
        this.banner = banner;
    }
    
    /**
     * Prints messages to the client from the server with PrintWriter
     * 
     * @param msg - the message from the server
     */
    private void printToClient(String msg) 
    {
        if(msg.getBytes().length > 10)
            out.print("Error: Message must be under 10 bytes");
        else
            out.print(msg);
        out.println();
        out.flush();
    }
    
    /**
     * Prints the banner to the client from the server with PrintWriter
     * Doesn't have the 10 byte restriction
     * 
     * @param banner - the banner inputted from the server
     */
    private void printBanner(String banner) 
    {
        out.print(banner);
        out.println();
        out.flush();
    }
    
    /**
     * Reads and returns client input from BufferedReader
     * 
     * @return the String that was inputted by the client
     * @throws IOException 
     */
    private String readLine() throws IOException 
    {
        String str = in.readLine();
        //System.out.println(""  + socket + " : " + str);
        return str;
    }
    
    /*
    * Line Number Reminder
    * 
    * 1;Enter card number:
    * 2;Account is already signed in, or the card number doesn't exist.
    * 3;Enter passcode:
    * 4;Incorrect passcode
    * 5;Welcome to the Bank
    * 6;(1)Balance, (2)Withdraw, (3)Deposit, (4)Change Language, (5)Exit
    * 7;Current balance is (var) dollars
    * 8;Enter amount:
    * 9;Enter security code:
    * 10;Withdrawal error: Check security code or the amount withdrawn
    * 11;Deposit error: That amount can't be deposited
    * 12;(1)English, (2)Spanish
    * 13;Goodbye
    */
    
    @Override
    public void run() 
    {
        try 
        {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String clientInput;
            String currentBanner = "";
            int amount;
            
            //Tells client to input card number
            printToClient("1");
            //Card Number
            clientInput = readLine();
            //Checks that card number exists and is not signed in
            while(!selectAcc(clientInput))
            {
                // Keeps client in a while-loop
                printToClient("deniedC");
                // Tells client of error
                printToClient("2");
                printToClient("1");
                clientInput = readLine();
            }
            printToClient("grantedC");
            
            //Tells client to input passcode
            printToClient("3");
            //Passcode
            clientInput = readLine();
            //Check if the user entered the correct passcode
            while(!verifyAcc(clientInput))
            {
                // Keeps client in a while-loop
                printToClient("deniedP");
                // Tells client of error
                printToClient("4");
                printToClient("3");
                clientInput = readLine();
            }
            printToClient("grantedP");

            // After verification, sets account to signed in
            acc.signIn();
            // Banner is sent to client for display, and updated if changed
            if(!currentBanner.equals(banner))
            {
                printBanner(banner);
                currentBanner = banner;
            }
            else
            {
                printBanner(currentBanner);
            }
            // Welcomes client
            printToClient("5");
            // Shows menu options
            printToClient("6");
            // Input from client containing the number of the menu option
            clientInput = readLine();
            int serverChoice = Integer.parseInt(clientInput);
            // (5) is Exit, which is why it loops until that is chosen
            while (serverChoice != 5) 
            {
                switch (serverChoice) 
                {
                    case 1: // (1) Balance
                        // Shows balance
                        printToClient("7");
                        printToClient(Integer.toString(acc.getBalance()));
                        break;
                    case 2: // (2) Withdraw
                        // Asks for amount
                        printToClient("8");
                        // clientInput contains the amount to be deposited, stored in Integer
                        clientInput = readLine();
                        amount = Integer.parseInt(clientInput);
                        // Asks for security key
                        printToClient("9");
                        // clientInput now contains the key used to verify the withdrawal
                        clientInput = readLine();
                        // If the withdraw function returns true, then the balance is already updated
                        if(acc.withdraw(amount, clientInput))
                            printToClient(Integer.toString(acc.getBalance())); 
                        else // If it returns false, the client is notified and the balance remains unchanged
                        {
                            printToClient("withErr");
                            // Notifies of error
                            printToClient("10");
                            printToClient(Integer.toString(acc.getBalance()));
                        }
                        // Shows balance
                        printToClient("7");
                        break;
                    case 3: // (3) Deposit
                        // Asks for amount
                        printToClient("8");
                        // Variable contains the amount to be deposited
                        clientInput = readLine();
                        amount = Integer.parseInt(clientInput);
                        // If the deposit function returns true, then the balance is already updated
                        if(acc.deposit(amount))
                        {
                            printToClient(Integer.toString(acc.getBalance()));
                        }
                        else //If it returns false, the client is notified and the balance remains unchanged
                        {
                            printToClient("depErr");
                            // Notifies of error
                            printToClient("11");
                            printToClient(Integer.toString(acc.getBalance()));
                        }
                        // Shows balance
                        printToClient("7");
                        break;
                    case 4: // (4) Change Language
                        // Shows available language options
                        printToClient("12");
                        break;
                    default: break;
                }
                if(!currentBanner.equals(banner))
                {
                    printBanner(banner);
                    currentBanner = banner;
                }
                else
                {
                    printBanner(currentBanner);
                }
                // Shows menu
                printToClient("6");
                clientInput = readLine();
                serverChoice = Integer.parseInt(clientInput);
            }
            // Tells client goodbye
            printToClient("13");
            // Signs out the account and closes the streams
            System.out.println("Client " + acc.getCardNumber() + " has disconnected");
            acc.signOut();
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
                e.printStackTrace();
        } 
    }
}