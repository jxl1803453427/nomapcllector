package com.example.signalcollection.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.example.signalcollection.R;

public class SideBarView extends View {

    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
    private String[] b = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
    private int choose = -1;
    private Paint paint = new Paint();
    private boolean showBkg = false;

    private int textSize;

    private int textColor;

    public SideBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SideBarView);
        textSize = array.getDimensionPixelSize(R.styleable.SideBarView_letterSize, 26);
        textColor = array.getColor(R.styleable.SideBarView_letterColor, Color.parseColor("#8c8c8c"));

    }

    public SideBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public SideBarView(Context context) {
        this(context, null);
    }


    private TextView mTvLetter;


    public void setTextView(TextView tvLetter) {
        this.mTvLetter = tvLetter;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showBkg) {
            canvas.drawColor(Color.parseColor("#40000000"));
        }
        int height = getHeight();
        int width = getWidth();
        int singleHeight = height / b.length;
        for (int i = 0; i < b.length; i++) {
            paint.setColor(textColor);
            paint.setTextSize(textSize);
            // paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setAntiAlias(true);
            /*if (i == choose) {
                paint.setColor(Color.parseColor("#3399ff"));
				paint.setFakeBoldText(true);
			}*/
            float xPos = width / 2 - paint.measureText(b[i]) / 2;
            float yPos = singleHeight * i + singleHeight;
            canvas.drawText(b[i], xPos, yPos, paint);
            paint.reset();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();
        final int oldChoose = choose;
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
        final int c = (int) (y / getHeight() * b.length);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                showBkg = true;
                if (oldChoose != c && listener != null) {
                    if (c >= 0 && c < b.length) {
                        listener.onTouchingLetterChanged(b[c]);
                        choose = c;
                        showText(b[c]);
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (oldChoose != c && listener != null) {
                    if (c >= 0 && c < b.length) {
                        listener.onTouchingLetterChanged(b[c]);
                        choose = c;
                        showText(b[c]);
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                showBkg = false;
                choose = -1;
                closeText();
                invalidate();
                break;
        }
        return true;
    }


    private void showText(String s) {
        if (mTvLetter != null) {
            mTvLetter.setVisibility(VISIBLE);
            mTvLetter.setText(s);
        }
    }

    private void closeText() {
        if (mTvLetter != null) {
            mTvLetter.setVisibility(GONE);

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void setOnTouchingLetterChangedListener(OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    public interface OnTouchingLetterChangedListener {
        public void onTouchingLetterChanged(String s);
    }
}
