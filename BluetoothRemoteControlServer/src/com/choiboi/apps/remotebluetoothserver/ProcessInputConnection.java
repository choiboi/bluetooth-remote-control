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

    // Acknowledge from the server
    private static final String ACKNOWLEDGE = "<ACK>";
    private static final String ACKNOWLEDGE_CMD_SENDING = "<ACK-SENDING-CMD>";
    private static final String ACKNOWLEDGE_SENDING_IMG = "<ACK-SENDING-IMG>";
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

            // Read for connected device name
            bytes = mInputStream.read(buffer);
            String[] inputCmd = parseInputCommand(new String(buffer, 0, bytes));
            if (inputCmd[0].equals(DEVICE_CONNECTED))
                System.out.println("\nThis Device is Connected to: " + inputCmd[1]);

            System.out.println("Waiting for commands.....");

            while (true) {
                bytes = mInputStream.read(buffer);
                
                if (bytes == -1) {
                    System.out.println("==============APPLICATION ENDED==============");
                    break;
                } else if (ACKNOWLEDGE_CMD_SENDING.equals(new String(buffer, 0, bytes))) {
                    receivingCommand();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * The communication to the device when it is receiving a command from the
     * user. It consist of receiving the command string and sending the device a
     * screenshot of the screen.
     */
    private void receivingCommand() {
        byte[] buffer = new byte[1024];
        int bytes;
        String[] inputCmd;

        try {
            // Send acknowledge that it is about to receive a command
            mOutputStream.write(ACKNOWLEDGE.getBytes());

            // Receive command from device
            bytes = mInputStream.read(buffer);
            inputCmd = parseInputCommand(new String(buffer, 0, bytes));
            processCommand(inputCmd);

            // Send acknowledgment that image will be sent
            mOutputStream.write(ACKNOWLEDGE_SENDING_IMG.getBytes());

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

            // Send acknowledge
            mOutputStream.write(ACKNOWLEDGE.getBytes());
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
        switch (inputCmd[1]) {
        case KEY_RIGHT:
            processArrowCmd(inputCmd, KeyEvent.VK_RIGHT);
            break;
        case KEY_LEFT:
            processArrowCmd(inputCmd, KeyEvent.VK_LEFT);
            break;
        case KEY_UP:
            processArrowCmd(inputCmd, KeyEvent.VK_UP);
            break;
        case KEY_DOWN:
            processArrowCmd(inputCmd, KeyEvent.VK_DOWN);
            break;
        case GO_FULLSCREEN:
        case EXIT_FULLSCREEN:
            handleFullScreenCmd(inputCmd);
            break;
        case APP_STARTED:
            break;
        case EXIT_CMD:
            System.out.println("==============APPLICATION ENDED==============");
            break;
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

            System.out.println(inputCmd[0] + ": " + inputCmd[1]);
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
        if (inputCmd[2].equals(MICROSOFT_POWERPOINT)) {
            switch (inputCmd[1]) {
            case GO_FULLSCREEN:
                microPPTkeyEventGoFullscreen();
                break;
            case EXIT_FULLSCREEN:
                microPPTKeyEventExitFullscreen();
                break;
            }
        } else if (mOS.startsWith(WINDOWS)) {
            switch (inputCmd[2]) {
            case ADOBE_READER:
                adobePDFKeyEventFullscreen(KeyEvent.VK_CONTROL);
                break;
            case BROWSER:
                browserKeyEventWinFullscreen();
                break;
            }
        } else if (mOS.startsWith(MAC_OS)) {
            switch (inputCmd[2]) {
            case ADOBE_READER:
                adobePDFKeyEventFullscreen(KeyEvent.VK_META);
                break;
            case BROWSER:
                browserKeyEventMacFullscreen();
                break;
            }
        }

        System.out.println(inputCmd[0] + ": " + inputCmd[1]);
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
