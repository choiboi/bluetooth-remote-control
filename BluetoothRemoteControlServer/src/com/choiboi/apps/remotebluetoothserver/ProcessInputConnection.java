package com.choiboi.apps.remotebluetoothserver;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.StreamConnection;

public class ProcessInputConnection implements Runnable {
    
    // Member fields
    private StreamConnection connection;
    
    // Command constants sent from the mobile device.
    private static final String EXIT_CMD = "EXIT";
    private static final String KEY_RIGHT = "VK_RIGHT";
    private static final String KEY_LEFT = "VK_LEFT";
    
    // Regex for paring commands
    private static final String COLON = ":";
    
    public ProcessInputConnection(StreamConnection conn) {
        connection = conn;
    }

    @Override
    public void run() {
        try {
            // Open up InputStream and receive data
            InputStream inputStream = connection.openDataInputStream();
            System.out.println("Waiting for commands.....");
            while (true) {
                byte[] buffer = new byte[2048];
                int bytes = inputStream.read(buffer);
                String[] cmd = new String[]{"", ""};
                
                if (bytes != -1) {
                    cmd = parseInputCommand(new String(buffer, 0, bytes));
                }
                
                // Stop thread if application on device quits
                if (bytes == -1 || cmd[1].equals(EXIT_CMD)) {
                    System.out.println("==============APPLICATION ENDED==============");
                    break; 
                }
                       
                processCommand(cmd);  
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /*
     * Generate the proper key event from the user input on the
     * mobile device.
     * 
     * @param inputCmd command received from the connected device
     */
    private void processCommand(String[] inputCmd) {
        try {
            Robot robot = new Robot();
            
            switch (inputCmd[1]) {
            case KEY_RIGHT:
                robot.keyPress(KeyEvent.VK_RIGHT);
                System.out.println(inputCmd[0] + ": " + "RIGHT");
                robot.keyRelease(KeyEvent.VK_RIGHT);
                break;
            case KEY_LEFT:
                robot.keyPress(KeyEvent.VK_LEFT);
                System.out.println(inputCmd[0] + ": " + "LEFT");
                robot.keyRelease(KeyEvent.VK_LEFT);
                break;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
    }
    
    /*
     * Split the string between the device name and command
     * received form that device.
     * 
     * @param cmd input command received from the device
     */
    private String[] parseInputCommand(String cmd) {
        return cmd.split(COLON);
    }
}
