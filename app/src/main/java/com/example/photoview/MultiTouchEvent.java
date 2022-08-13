package com.example.photoview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 最后一个按下的手指处理事件
 */
public class MultiTouchEvent extends View {
    private static final float IMAGE_WIDTH = Utils.dpToPixel(300);
    private Bitmap bitmap;
    private Paint paint;

    // 手指滑动偏移值
    private float offsetX;
    private float offsetY;

    // 按下时的x,y坐标
    private float downX;
    private float downY;

    // 上一次的偏移值
    private float lastOffsetX;
    private float lastOffsetY;

    int currentPointId;
    public MultiTouchEvent(Context context) {
        super(context);
        init();
    }

    public MultiTouchEvent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MultiTouchEvent(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bitmap = Utils.getPhoto(getResources(), (int) IMAGE_WIDTH);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, offsetX, offsetY, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()){
            //Down和Up都只触发一次
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                lastOffsetX = offsetX;
                lastOffsetY = offsetY;

                currentPointId = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                // 通过id 拿index
                if (event.getPointerCount()!=0){
                    int index = event.findPointerIndex(currentPointId);
                    // event.getX()默认 index = 0的坐标 --- move操作的是后按下的手指
                    offsetX = lastOffsetX + event.getX(index) - downX;
                    offsetY = lastOffsetY + event.getY(index) - downY;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //获取当前（最后一次）按下的index
                if (event.getPointerCount()>1){
                    int actionIndex = event.getActionIndex();
                    currentPointId = event.getPointerId(actionIndex);

                    downX = event.getX(actionIndex);
                    downY = event.getY(actionIndex);
                    lastOffsetX = offsetX;
                    lastOffsetY = offsetY;
                }
                break;
            case MotionEvent.ACTION_UP:
                    int upIndex = event.getActionIndex();
                    int pointId = event.getPointerId(upIndex);
                    //非活跃的手指不用处理
                    if (pointId==currentPointId){
                        //更新活跃手指
                        if (upIndex == event.getPointerCount()-1){
                            //抬起的手指是活跃手指并且是最后一个按下的手指，那么活跃手指更新为抬起手指的前一个
                            upIndex = event.getPointerCount() -2;
                        }else{
                            //如果抬起手指是中间的并且是活跃手指，那么活跃手指更新为下一个
                            upIndex++;
                        }
                        currentPointId = event.getPointerId(upIndex);
                        downX = event.getX(upIndex);
                        downY = event.getY(upIndex);
                        lastOffsetX = offsetX;
                        lastOffsetY = offsetY;
                    }
                break;
        }
        return true;
    }
}
