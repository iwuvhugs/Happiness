package com.iwuvhugs.happiness;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class FaceView extends View {

    private FaceDrawer drawer;

    private float happiness = 100;

    private float centerX, centerY;
    private float y1;
    private float faceRadius;

    private final static float HAPPINESS_GESTURE_SCALE = 4f;
    private final static float SCALE = 0.9f;
    private final static float FACE_RADIUS_TO_EYE_RADIUS_RATIO = 10f;
    private final static float FACE_RADIUS_TO_EYE_OFFSET_RATIO = 3f;
    private final static float FACE_RADIUS_TO_EYE_SEPARATION_RATIO = 1.5f;
    private final static float FACE_RADIUS_TO_MOUTH_WIDTH_RATIO = 1f;
    private final static float FACE_RADIUS_TO_MOUTH_HEIGHT_RATIO = 3f;
    private final static float FACE_RADIUS_TO_MOUTH_OFFSET_RATIO = 3f;

    private enum Eye {
        LEFT,
        RIGHT
    }

    public FaceView(Context context) {
        this(context, null);
    }

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeFace();
    }

    private void initializeFace() {
        drawer = new FaceDrawer();
    }

    public void setHappiness(float happiness) {
        this.happiness = happiness;
        postInvalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        FaceState faceState = new FaceState(parcelable);
        faceState.state = happiness;
        return faceState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        FaceState faceState = (FaceState) state;
        super.onRestoreInstanceState(faceState.getSuperState());
        setHappiness(faceState.state);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawer.drawFace(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                y1 = event.getY();
                return true;
            case (MotionEvent.ACTION_MOVE):
                float deltaY = -(event.getY() - y1) / HAPPINESS_GESTURE_SCALE;
                y1 = event.getY();
                if (deltaY != 0) {
                    changeHappiness(deltaY);
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private void changeHappiness(float happinessChange) {
        happiness = Math.min(Math.max(happiness + happinessChange, 0), 100);
        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        faceRadius = Math.min(w, h) / 2 * SCALE;
        centerX = w / 2;
        centerY = h / 2;
    }

    private class FaceDrawer {

        private Paint paint;

        FaceDrawer() {
            paint = new Paint();
            paint.setColor(Color.BLUE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(20);
        }

        void drawFace(Canvas canvas) {
            canvas.save();
            canvas.translate(centerX, centerY);

            drawFaceOval(canvas);
            drawEye(canvas, Eye.LEFT);
            drawEye(canvas, Eye.RIGHT);
            drawMouth(canvas);

            canvas.restore();
            canvas.save();
        }

        private void drawFaceOval(Canvas canvas) {
            canvas.drawCircle(0, 0, faceRadius, paint);
        }

        private void drawEye(Canvas canvas, Eye eye) {
            float eyeRadius = faceRadius / FACE_RADIUS_TO_EYE_RADIUS_RATIO;
            float eyeVerticalOffset = faceRadius / FACE_RADIUS_TO_EYE_OFFSET_RATIO;
            float eyeHorizontalSeparation = faceRadius / FACE_RADIUS_TO_EYE_SEPARATION_RATIO;

            float eyeY = -eyeVerticalOffset;
            float eyeX = (eye == Eye.LEFT ? -1 : 1) * eyeHorizontalSeparation / 2;

            canvas.drawCircle(eyeX, eyeY, eyeRadius, paint);
        }

        private void drawMouth(Canvas canvas) {
            float mouthWidth = faceRadius / FACE_RADIUS_TO_MOUTH_WIDTH_RATIO;
            float mouthHeight = faceRadius / FACE_RADIUS_TO_MOUTH_HEIGHT_RATIO;
            float mouthVerticalOffset = faceRadius / FACE_RADIUS_TO_MOUTH_OFFSET_RATIO;

            float fractionOfMaxSmile = (happiness - 50) / 50;
            float smileHeight = Math.max(Math.min(fractionOfMaxSmile, 1), -1) * mouthHeight;

            Path path = new Path();
            path.moveTo(-mouthWidth / 2, mouthVerticalOffset);
            path.cubicTo(-mouthWidth / 3, mouthVerticalOffset + smileHeight,
                    mouthWidth / 3, mouthVerticalOffset + smileHeight,
                    mouthWidth / 2, mouthVerticalOffset);
            canvas.drawPath(path, paint);
        }
    }

    private static class FaceState extends BaseSavedState {

        float state;

        FaceState(Parcelable superState) {
            super(superState);
        }

        private FaceState(Parcel source) {
            super(source);
            state = source.readFloat();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(state);
        }

        public static final Parcelable.Creator<FaceState> CREATOR = new Creator<FaceState>() {
            @Override
            public FaceState createFromParcel(Parcel source) {
                return new FaceState(source);
            }

            @Override
            public FaceState[] newArray(int size) {
                return new FaceState[size];
            }
        };
    }
}
