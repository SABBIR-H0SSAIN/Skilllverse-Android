package com.example.skillverse_android.utils;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class SwipeGestureHelper {
    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_THRESHOLD_VELOCITY = 100;
    private static final float SWIPE_DIRECTION_TOLERANCE = 0.5f;
    
    private final GestureDetector gestureDetector;
    private final OnSwipeListener listener;
    private boolean isSwipeInProgress = false;
    private float startX = 0;
    private float startY = 0;

    public interface OnSwipeListener {
        void onSwipeRight();
    }

    public SwipeGestureHelper(Context context, OnSwipeListener listener) {
        this.listener = listener;
        this.gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                isSwipeInProgress = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float diffX = event.getX() - startX;
                float diffY = Math.abs(event.getY() - startY);
                if (diffX > 30 && diffX > diffY * 1.5f) {
                    isSwipeInProgress = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                boolean wasSwipe = isSwipeInProgress;
                isSwipeInProgress = false;
                if (wasSwipe) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
                break;
        }
        
        gestureDetector.onTouchEvent(event);
        return isSwipeInProgress;
    }

    public boolean isSwipeInProgress() {
        return isSwipeInProgress;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;
            
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();
            
            float absDiffX = Math.abs(diffX);
            float absDiffY = Math.abs(diffY);
            
            if (absDiffX > absDiffY * (1 + SWIPE_DIRECTION_TOLERANCE)) {
                if (diffX > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    if (listener != null) {
                        listener.onSwipeRight();
                    }
                    return true;
                }
            }
            return false;
        }
    }
}
