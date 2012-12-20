package com.choiboi.apps.remotebluetoothserver;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.io.StreamConnection;

public class ProcessInputConnection implements Runnable {
    
    private StreamConnection connection;
    
    // Command constants sent from the mobile device.
    private static final int EXIT_CMD = -1;
    private static final int KEY_RIGHT = 1;
    private static final int KEY_LEFT = 2;
    
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
                String cmd = new String(buffer, 0, bytes);
                
                System.out.println(cmd);
                
//                int inputCommand = inputStream.read();
                
                // Indicate that application and ended
//                if (inputCommand == EXIT_CMD) {
//                    System.out.println("==============APPLICATION ENDED==============");
//                    break;
//                }
//                
//                processCommand(inputCommand);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
    
    /*
     * Generate the proper key event from the user input on the
     * mobile device.
     * 
     * @param command the command code
     */
    private void processCommand(int inputCmd) {
        try {
            Robot robot = new Robot();
            
            switch (inputCmd) {
            case KEY_RIGHT:
                robot.keyPress(KeyEvent.VK_RIGHT);
                System.out.println("RIGHT");
                robot.keyRelease(KeyEvent.VK_RIGHT);
                break;
            case KEY_LEFT:
                robot.keyPress(KeyEvent.VK_LEFT);
                System.out.println("LEFT");
                robot.keyRelease(KeyEvent.VK_LEFT);
                break;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
                
    }
}
