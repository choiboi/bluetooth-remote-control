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
    private static final String EXIT_CMD = "EXIT";
    private static final String KEY_LEFT = "LEFT";
    private static final String KEY_DOWN = "DOWN";
    private static final String KEY_UP = "UP";
    private static final String KEY_RIGHT = "RIGHT";
    private static final String GO_FULLSCREEN = "GO_FULLSCREEN";
    private static final String EXIT_FULLSCREEN = "EXIT_FULLSCREEN";
    private static final String APP_STARTED = "APP_STARTED";
    
    // Acknowledgment to the device
    private static final String ACKNOWLEDGE_SENDING_IMG = "SENDING_IMG";
    private static final String ACKNOWLEDGE_IMG_SENT = "IMG_SENT";
    
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
            
            while (true) {
                byte[] buffer = new byte[2048];
                int bytes = mInputStream.read(buffer);
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
		if (inputCmd[0].equals(DEVICE_CONNECTED)) {
			System.out.println("\nThis Device is Connected to: " + inputCmd[1]);
			System.out.println("Waiting for commands.....");
		} else {
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
				sendSlideScreenshot();
				break;
			}
		}
	}
    
    /*
     * Key events for up, down, left, and right arrow.
     * 
     * @param inputCmd command received from the connected device
     * @param key either VK_UP or VK_DOWN or VK_LEFT or VK_RIGHT constants
     */
    private void processArrowCmd(String[] inputCmd, int key) {
        try {
            Robot robot = new Robot();
            robot.keyPress(key);
            robot.keyRelease(key);
            
            System.out.println(inputCmd[0] + ": " + inputCmd[1]);
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
        sendSlideScreenshot();
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
        sendSlideScreenshot();
    }
    
    /*
     * Key events to make Microsoft Powerpoint presentations go
     * fullscreen.
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
     * Key events to make Microsoft Powerpoint presentations exit
     * fullscreen.
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
     * Key events to make Adobe Reader presentations to go or 
     * exit fullscreen.
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
     * Key events to make browser presentations go or exit
     * fullscreen on Windows machines. Browsers include
     * Google Chrome, Mozilla Firefox, and Microsoft Internet
     * Explorer.
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
     * Key events to make browser presentations go or exit
     * fullscreen on MacOS machines. Browsers include
     * Google Chrome, and Mozilla Firefox.
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
     * Send out a screenshot of the slide to the device.
     */
    private void sendSlideScreenshot() {
		try {
			// Send acknowledgment that an image will be sent
			mOutputStream.write(ACKNOWLEDGE_SENDING_IMG.getBytes());

			// Wait until all the animation on the slides have been completed.
			Thread.sleep(800);

			// Take a screenshot of the primary screen
			Robot r = new Robot();
			Rectangle captureSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
			BufferedImage bImg = r.createScreenCapture(captureSize);

			// Send image to device via Bluetooth
			ImageIO.write(bImg, "png", mOutputStream);
			mOutputStream.flush();

			// Send acknowledgment that transfer is done
			mOutputStream.write(ACKNOWLEDGE_IMG_SENT.getBytes());

		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (Exception e) {
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
