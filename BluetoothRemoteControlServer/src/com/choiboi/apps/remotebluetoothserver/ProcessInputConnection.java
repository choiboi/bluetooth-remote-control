package com.choiboi.apps.remotebluetoothserver;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.microedition.io.StreamConnection;

public class ProcessInputConnection implements Runnable {

    // Member fields
    private StreamConnection connection;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private String mOS = "";
    private String mConnectedDeviceName = "";

    // Command constants sent from the mobile device.
    private static final String DEVICE_CONNECTED = "DEVICE_CONNECTED";
    private static final String KEY_LEFT = "LEFT";
    private static final String KEY_DOWN = "DOWN";
    private static final String KEY_UP = "UP";
    private static final String KEY_RIGHT = "RIGHT";
    private static final String GO_FULLSCREEN = "GO_FULLSCREEN";
    private static final String EXIT_FULLSCREEN = "EXIT_FULLSCREEN";
    private static final String APP_STARTED = "APP_STARTED";
    private static final String EXIT_CMD = "EXIT";
    private static final String CMD = "CMD";

    // Acknowledge from the server
    private static final String ACKNOWLEDGE = "<ACK>";
    private static final String ACKNOWLEDGE_CMD_RECEIVED = "<ACK-CMD-RECEIVED>";
    private static final String ACKNOWLEDGE_IMG_CAN_RECEIVE = "<ACK-IMG-CAN-RECEIVE>";
    private static final String ACKNOWLEDGE_IMG_SENDING = "<ACK-IMG-SENDING>";
    private static final String ACKNOWLEDGE_IMG_RECEIVED = "<ACK-IMG-RECEIVED>";

    // Regex for parsing commands
    private static final String COLON = ":";

    // Operating Systems
    private static final String WINDOWS = "window";
    private static final String MAC_OS = "mac";

    // Presentation programs
    private static final String BROWSER = "BROWSER";
    private static final String MICROSOFT_POWERPOINT = "MICRO_PPT";
    private static final String ADOBE_READER = "ADOBE_PDF";

    public ProcessInputConnection(StreamConnection conn) {
        connection = conn;
        mOS = System.getProperty("os.name").toLowerCase();
    }

    @Override
    public void run() {
        try {
            // Open up InputStream and OutputStream to send and receive data
            mInputStream = connection.openDataInputStream();
            mOutputStream = connection.openDataOutputStream();
            byte[] buffer = new byte[1024];
            int bytes;
            String[] inputCmd;

            // Read for connected device name
            bytes = mInputStream.read(buffer);
            inputCmd = parseInputCommand(new String(buffer, 0, bytes));
            if (inputCmd[0].equals(DEVICE_CONNECTED)) {
                mConnectedDeviceName = inputCmd[1];
                System.out.println("\nThis Device is Connected to: " + inputCmd[1]);
            }

            System.out.println("Waiting for commands.....");

            while (true) {
                bytes = mInputStream.read(buffer);
                
                if (bytes == -1) {
                    System.out.println("==============APPLICATION ENDED==============");
                    break;
                } if (ACKNOWLEDGE_IMG_CAN_RECEIVE.equals(new String(buffer, 0, bytes))) {
                    sendScreenshot();
                } else {
                    inputCmd = parseInputCommand(new String(buffer, 0, bytes));
                    if (inputCmd[0].equals(CMD)) {
                        processCommand(inputCmd);
                        mOutputStream.write(ACKNOWLEDGE_CMD_RECEIVED.getBytes());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void sendScreenshot() {
        byte[] buffer = new byte[512];
        int bytes;
        
        try {
            // Send acknowledgment that image will be sent
            mOutputStream.write(ACKNOWLEDGE_IMG_SENDING.getBytes());
            
            // Receive acknowledgment
            bytes = mInputStream.read(buffer);
            if (!ACKNOWLEDGE.equals(new String(buffer, 0, bytes)))
                return;
            
            // Send image
            Thread.sleep(800);
            BufferedImage bImg = sendSlideScreenshot();
            ImageIO.write(bImg, "png", mOutputStream);
            mOutputStream.flush();
            
            // Receive acknowledgment that image has been received
            bytes = mInputStream.read(buffer);
            if (!ACKNOWLEDGE_IMG_RECEIVED.equals(new String(buffer, 0, bytes)))
                return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    /*
     * Generate the proper key event from the user input on the mobile device.
     * 
     * @param inputCmd command received from the connected device
     */
    private void processCommand(String[] inputCmd) {
        if (inputCmd[2].equals(KEY_RIGHT)) {
            processArrowCmd(inputCmd, KeyEvent.VK_RIGHT);
        } else if (inputCmd[2].equals(KEY_LEFT)) {
            processArrowCmd(inputCmd, KeyEvent.VK_LEFT);
        } else if (inputCmd[2].equals(KEY_UP)) {
            processArrowCmd(inputCmd, KeyEvent.VK_UP);
        } else if (inputCmd[2].equals(KEY_DOWN)) {
            processArrowCmd(inputCmd, KeyEvent.VK_DOWN);
        } else if (inputCmd[2].equals(GO_FULLSCREEN) || inputCmd[2].equals(EXIT_FULLSCREEN)) {
            handleFullScreenCmd(inputCmd);
        } else if(inputCmd[2].equals(APP_STARTED)) {
            System.out.println(mConnectedDeviceName + " is in Presentation Mode!!\n");
        } else if (inputCmd[2].equals(EXIT_CMD)) {
            System.out.println("==============APPLICATION ENDED==============");
        }
    }

    /*
     * Key events for up, down, left, and right arrow.
     * 
     * @param inputCmd command received from the connected device
     * 
     * @param key either VK_UP or VK_DOWN or VK_LEFT or VK_RIGHT constants
     */
    private void processArrowCmd(String[] inputCmd, int key) {
        try {
            Robot robot = new Robot();
            robot.keyPress(key);
            robot.keyRelease(key);

            System.out.println(inputCmd[1] + ": " + inputCmd[2]);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    /*
     * Whenever any fullscreen command has been given generate the proper
     * KeyEvent depending on the program used and the OS.
     * 
     * @param inputCmd command received from the connected device
     */
    private void handleFullScreenCmd(String[] inputCmd) {
        if (inputCmd[3].equals(MICROSOFT_POWERPOINT)) {
            if (inputCmd[2].equals(GO_FULLSCREEN)) {
                microPPTkeyEventGoFullscreen();
            } else if (inputCmd[2].equals(EXIT_FULLSCREEN)) {
                microPPTKeyEventExitFullscreen();
            }
        } else if (mOS.startsWith(WINDOWS)) {
            if (inputCmd[3].equals(ADOBE_READER)) {
                adobePDFKeyEventFullscreen(KeyEvent.VK_CONTROL);
            } else if (inputCmd[3].equals(BROWSER)) {
                browserKeyEventWinFullscreen();
            }
        } else if (mOS.startsWith(MAC_OS)) {
            if (inputCmd[3].equals(ADOBE_READER)) {
                adobePDFKeyEventFullscreen(KeyEvent.VK_META);
            } else if (inputCmd[3].equals(BROWSER)) {
                browserKeyEventMacFullscreen();
            }
        }

        System.out.println(inputCmd[1] + ": " + inputCmd[2]);
    }

    /*
     * Key events to make Microsoft Powerpoint presentations go fullscreen.
     */
    private void microPPTkeyEventGoFullscreen() {
        try {
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_F5);
            robot.keyRelease(KeyEvent.VK_F5);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    /*
     * Key events to make Microsoft Powerpoint presentations exit fullscreen.
     */
    private void microPPTKeyEventExitFullscreen() {
        try {
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_ESCAPE);
            robot.keyRelease(KeyEvent.VK_ESCAPE);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    /*
     * Key events to make Adobe Reader presentations to go or exit fullscreen.
     * 
     * @param key either VK_META or VK_CONTROL constants
     */
    private void adobePDFKeyEventFullscreen(int key) {
        try {
            Robot robot = new Robot();
            robot.keyPress(key);
            robot.keyPress(KeyEvent.VK_L);
            robot.keyRelease(KeyEvent.VK_L);
            robot.keyRelease(key);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    /*
     * Key events to make browser presentations go or exit fullscreen on Windows
     * machines. Browsers include Google Chrome, Mozilla Firefox, and Microsoft
     * Internet Explorer.
     */
    private void browserKeyEventWinFullscreen() {
        try {
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_F11);
            robot.keyRelease(KeyEvent.VK_F11);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    /*
     * Key events to make browser presentations go or exit fullscreen on MacOS
     * machines. Browsers include Google Chrome, and Mozilla Firefox.
     */
    private void browserKeyEventMacFullscreen() {
        try {
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_META);
            robot.keyPress(KeyEvent.VK_SHIFT);
            robot.keyPress(KeyEvent.VK_F);
            robot.keyRelease(KeyEvent.VK_F);
            robot.keyRelease(KeyEvent.VK_SHIFT);
            robot.keyRelease(KeyEvent.VK_META);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    /*
     * Take a screenshot of the screen to be sent to the device.
     */
    private BufferedImage sendSlideScreenshot() {
        try {
            Robot r = new Robot();
            Rectangle captureSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            return r.createScreenCapture(captureSize);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * Split the string between the device name and command received form that
     * device.
     * 
     * @param cmd input command received from the device
     */
    private String[] parseInputCommand(String cmd) {
        return cmd.split(COLON);
    }
}
