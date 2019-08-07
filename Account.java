/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Victor H
 * 
 * Contains account credentials and balance, functions for withdrawing and depositing
 */

import java.util.LinkedList;

public class Account 
{
    private String cardNumber;
    private String passcode;
    private int balance;
    private boolean signedIn;
    private LinkedList<String> securityKeys;

    /**
     * The constructor of the account
     * Contains a hardcoded LinkedList of security keys to verify withdrawals
     * 
     * @param cardNumber - The card number of the account
     * @param passcode - The passcode for the account
     * @param balance - The money balance in the account
     */
    public Account(String cardNumber, String passcode, int balance)
    {
        this.cardNumber = cardNumber;
        this.passcode = passcode;
        this.balance = balance;
        this.signedIn = false;
        this.securityKeys = new LinkedList<>();
        
        // Loop for i: 0-9
        for(int i = 0; i < 10; i++)
        {
            // Loop for j: 1,3,5,7,9
            for(int j = 1; j < 10; j += 2)
            {
                // Adds key as a String variable with i for the first digit, and j for the second digit
                // Every odd number from 01-99
                securityKeys.add(i + "" + j);
            }
        }
    }
    
    /**
     * Getter function for card number
     * @return the card number of the account
     */
    public String getCardNumber()
    {
        return this.cardNumber;
    }
    
    /**
     * Getter function for passcode
     * @return the passcode used to sign in
     */
    public String getPasscode()
    {
        return this.passcode;
    }

    /**
     * Getter function for the balance
     * @return the money currently in the account
     */
    public int getBalance()
    {
        return this.balance;
    }
    
    /**
     * Getter function for the session 
     * @return whether the account is currently signed in or signed out
     */
    public boolean getSession()
    { 
        return this.signedIn; 
    }
    
    /**
     * Changes boolean value for signedIn to true
     */
    public void signIn()
    {
        this.signedIn = true;
    }
    
    /**
     * Changes boolean value for signedIn to false
     */
    public void signOut()
    {
        this.signedIn = false;
    }
    
    /**
     * Withdraws money from the account
     * 
     * @param amount - The amount of money taken from the balance
     * @param key - The hardcoded key which needs to be verified
     * @return true if verification returns true, the amount is within the balance, and the amount is positive
     */
    public boolean withdraw(int amount, String key)
    {   
        if(verifySecurityKey(key) && amount <= this.balance && amount >= 0)
        {
            this.balance -= amount;
            return true;			
        }
        return false;
    }
    
    /**
     * Checks the key with those in the hardcoded list for withdrawing money
     * Deletes the key from the hardcoded list once it's been used
     * 
     * @param key - Inputted key from client, is checked with hardcoded key list
     * @return true if the key is among the hardcoded list
     */
    
    private boolean verifySecurityKey(String key)
    {
        if(securityKeys.contains(key))
        {
            securityKeys.remove(key);
            return true;
        }
        return false;
    }
    
    /**
     * Deposits money into the account
     * 
     * @param amount - The amount of money added to the balance
     * @return true if the amount being deposited is a positive value
     */
    public boolean deposit(int amount)
    {
        if(amount >= 0)
        {
            this.balance += amount;
            return true;
        }
        return false;
    }
}