package com.choiboi.apps.bluetoothremote.presentationmode;

import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

public class SwipeGestureDetector implements OnGestureListener {
    
    // Debugging
    private static final String TAG = "GestureDetector";
    
    // Member fields
    private PresentationMode mPresMode;
    
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    
    public SwipeGestureDetector(PresentationMode presMode) {
        Log.i(TAG, "++ CustomGestureDetector ++");
        
        mPresMode = presMode;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.i(TAG, "++ onFling ++" + e1.getX() + "---" + e2.getX());

//        // Right to left swipe
//        if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
//                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//            mPresMode.rightArrow(null);
//            // Left to right swipe
//        } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
//                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//            mPresMode.rightArrow(null);
//        }
        
        if (rightLeftSwipe(e1, e2)) {
            mPresMode.rightArrow(null);
        } else if (leftRightSwipe(e1, e2)) {
            mPresMode.leftArrow(null);
        }

        return false;
    }
    
    /*
     * 
     */
    private boolean rightLeftSwipe(MotionEvent e1, MotionEvent e2) {
        Log.i(TAG, "--- rightLeftSwipe ---");
        
        if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
            if (Math.abs(e1.getY() - e2.getY()) < SWIPE_MIN_DISTANCE) {
                return true;
            }
        }
        return false;
    }
    
    /*
     * 
     */
    private boolean leftRightSwipe(MotionEvent e1, MotionEvent e2) {
        Log.i(TAG, "--- leftRightSwipe ---");
        
        if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
            if (Math.abs(e1.getY() - e2.getY()) < SWIPE_MIN_DISTANCE) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        Log.i(TAG, "++ onDown ++");
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
        // TODO Auto-generated method stub
        Log.i(TAG, "++ onLongPress ++");
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
            float arg3) {
        // TODO Auto-generated method stub
        Log.i(TAG, "++ onScroll ++");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
        // TODO Auto-generated method stub
        Log.i(TAG, "++ onShowPress ++");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        // TODO Auto-generated method stub
        Log.i(TAG, "++ onSingleTapUp ++");
        return false;
    }
}
