package com.choiboi.apps.bluetoothremote.presentationmode;

import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import com.choiboi.apps.bluetoothremote.BluetoothService;

public class SwipeGestureDetector implements OnGestureListener {
    
    // Debugging
    private static final String TAG = "GestureDetector";
    
    // Member fields
    private final BluetoothService mBtService;
    private String mLocalDeviceName;
    
    // Constant for swipe gesture
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    
    // Constants that indicate command to computer
    private static final String CMD = "CMD";
    private static final String LEFT = "LEFT";
    private static final String DOWN = "DOWN";
    private static final String UP = "UP";
    private static final String RIGHT = "RIGHT";
    
    public SwipeGestureDetector(BluetoothService btService, String deviceName) {
        Log.i(TAG, "++ CustomGestureDetector ++");
        
        mBtService = btService;
        mLocalDeviceName = deviceName;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.i(TAG, "++ onFling ++");
        
        String command = CMD + ":" + mLocalDeviceName + ":";

        if (rightLeftSwipe(e1, e2)) {
            command += RIGHT;
            mBtService.write(command.getBytes());
        } else if (leftRightSwipe(e1, e2)) {
            command += LEFT;
            mBtService.write(command.getBytes());
        } else if (upDownSwipe(e1, e2)) {
            command += UP;
            mBtService.write(command.getBytes());
        } else if (downUpSwipe(e1, e2)) {
            command += DOWN;
            mBtService.write(command.getBytes());
        }

        return false;
    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        Log.i(TAG, "++ onDown ++");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
        Log.i(TAG, "++ onLongPress ++");
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        Log.i(TAG, "++ onScroll ++");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
        Log.i(TAG, "++ onShowPress ++");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        Log.i(TAG, "++ onSingleTapUp ++");
        
        String command = CMD + ":" + mLocalDeviceName + ":" + RIGHT;
        mBtService.write(command.getBytes());

        return false;
    }
    
    /*
     * Detects right to left swipe and return true if it is. Detection is 
     * done by checking the initial contact location along the x-axis and 
     * release location on the x-axis. Checking is also done along the 
     * y-axis that way it does not respond to noticeable diagonal swipe
     * made by the user.
     */
    private boolean rightLeftSwipe(MotionEvent e1, MotionEvent e2) {
        Log.i(TAG, "--- rightLeftSwipe ---");

        if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
            if (Math.abs(e1.getY() - e2.getY()) < SWIPE_MAX_OFF_PATH) {
                return true;
            }
        }
        return false;
    }
    
    /*
     * Detects left to right swipe and return true if it does. Idea is similar
     * to right to left swipe detection.
     */
    private boolean leftRightSwipe(MotionEvent e1, MotionEvent e2) {
        Log.i(TAG, "--- leftRightSwipe ---");

        if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
            if (Math.abs(e1.getY() - e2.getY()) < SWIPE_MAX_OFF_PATH) {
                return true;
            }
        }
        return false;
    }
    
    /*
     * Detects up to down swipe and return true if it does. Detection is done
     * by checking the initial contact location along the y-axis and release
     * location on the y-axis. Check is also done along the x-axis that way it
     * does not respond to noticeable diagonal swipe made by the user.
     */
    private boolean upDownSwipe(MotionEvent e1, MotionEvent e2) {
        Log.i(TAG, "--- upDownSwipe ---");

        if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE) {
            if (Math.abs(e1.getX() - e2.getX()) < SWIPE_MAX_OFF_PATH) {
                return true;
            }
        }
        return false;
    }
    
    /*
     * Detects down to up swipe and return true if it does. Idea is similar to
     * up to down swipe detection.
     */
    private boolean downUpSwipe(MotionEvent e1, MotionEvent e2) {
        Log.i(TAG, "--- downUpSwipe ---");

        if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE) {
            if (Math.abs(e1.getX() - e2.getX()) < SWIPE_MAX_OFF_PATH) {
                return true;
            }
        }
        return false;
    }
}
