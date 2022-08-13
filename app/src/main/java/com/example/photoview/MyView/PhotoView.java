package com.example.photoview.MyView;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.photoview.Utils;

public class PhotoView extends View {
    private static final float IMAGE_WIDTH = Utils.dpToPixel(300);
    private static final float OVER_SCALE_FACTOR = 1.5f;
    private Bitmap bitmap;
    private Paint paint;

    float originalOffsetX;
    float originalOffsetY;

    private float offsetX;
    private float offsetY;
    private float currentScale;

    private boolean isEnlarge;

    private GestureDetector gestureDetector;

    //支持双指缩放
    private ScaleGestureDetector scaleGestureDetector;

    private ObjectAnimator scaleAnim;

    private OverScroller overScroller;

    private float smallScale;
    private float bigScale;
    public PhotoView(Context context) {
        super(context);
        init(context);
    }

    public PhotoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PhotoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        bitmap = Utils.getPhoto(getResources(),(int) IMAGE_WIDTH);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        overScroller = new OverScroller(context);
        gestureDetector = new GestureDetector(context,new PhotoGestureDetector());

        scaleGestureDetector = new ScaleGestureDetector(context,new PhotoScaleGestureDetector());
    }

    public float getCurrentScale() {
        return currentScale;
    }

    public void setCurrentScale(float scale){
        currentScale = scale;
        invalidate();
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //为了避免图片展示的边上有空隙，最好使用float
        originalOffsetX = (getWidth() - bitmap.getWidth()) / 2f;
        originalOffsetY = (getHeight() - bitmap.getHeight()) / 2f;

        //判断是高度全屏还是宽度全屏
        if ((float)bitmap.getWidth()/bitmap.getHeight()>(float) getWidth()/getHeight()){
            smallScale = (float) getWidth()/ bitmap.getWidth();
            bigScale = (float) getHeight()/bitmap.getHeight()*OVER_SCALE_FACTOR;
        }else {
            smallScale = (float) getHeight()/bitmap.getHeight();
            bigScale = (float) getWidth()/bitmap.getWidth()*OVER_SCALE_FACTOR;
        }
        currentScale = smallScale;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //放大多少，就只能移动多少
        float scaleFaction = (currentScale-smallScale)/(bigScale-smallScale);
        canvas.translate(offsetX * scaleFaction,offsetY * scaleFaction);

        canvas.scale(currentScale,currentScale,getWidth()/2f,getHeight()/2f);
        canvas.drawBitmap(bitmap,originalOffsetX,originalOffsetY,paint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = scaleGestureDetector.onTouchEvent(event);
        if(!scaleGestureDetector.isInProgress()){
            result = gestureDetector.onTouchEvent(event);
        }
        return result;
    }

    class FlingRunner implements Runnable{
        @Override
        public void run() {
            //不断地刷新直到动画停止
            if (overScroller.computeScrollOffset()){
                offsetX = overScroller.getCurrX();
                offsetY = overScroller.getCurrY();
                invalidate();
                //下一帧动画的时候执行
                postOnAnimation(this);
            }
        }
    }
    private void fixOffsets(){
        offsetX = Math.min(offsetX, (bitmap.getWidth() * bigScale - getWidth()) / 2);
        offsetX = Math.max(offsetX, -(bitmap.getWidth() * bigScale - getWidth()) / 2);
        offsetY = Math.min(offsetY, (bitmap.getHeight() * bigScale - getHeight()) / 2);
        offsetY = Math.max(offsetY, -(bitmap.getHeight() * bigScale - getHeight()) / 2);
    }
    private ObjectAnimator getScaleAnim(float currentScale,float targetScale){
        if (scaleAnim==null){
            scaleAnim = ObjectAnimator.ofFloat(this,"currentScale",0);
        }
        scaleAnim.setFloatValues(currentScale,targetScale);
        return scaleAnim;
    }

    class PhotoScaleGestureDetector implements ScaleGestureDetector.OnScaleGestureListener {

        float initScale;
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector scaleGestureDetector) {
            if ((currentScale>smallScale && !isEnlarge)||(currentScale==smallScale && !isEnlarge)){
                isEnlarge = !isEnlarge;
            }
            //缩放因子
            currentScale = initScale * scaleGestureDetector.getScaleFactor();
            //注意：一定要刷新，否则放大缩小效果就很生硬
            invalidate();
            return false;
        }

        @Override
        public boolean onScaleBegin(@NonNull ScaleGestureDetector scaleGestureDetector) {
            initScale = currentScale;
            return true;
        }

        @Override
        public void onScaleEnd(@NonNull ScaleGestureDetector scaleGestureDetector) {

        }
    }

    class PhotoGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            isEnlarge = !isEnlarge;
            if (isEnlarge){
                //经过一次放大后缩小后点击新的位置，从该位置为中心放大，所以需要重置offset
                offsetX = (e.getX()-getWidth()/2f)-(e.getX()-getWidth()/2f) * (bigScale/smallScale);
                offsetY = (e.getY()-getHeight()/2f)-(e.getY()-getHeight()/2f)* (bigScale/smallScale);
                fixOffsets();
                currentScale = bigScale;
                getScaleAnim(smallScale,bigScale).start();
            }else {
                currentScale = smallScale;
                getScaleAnim(smallScale,bigScale).reverse();
            }
            return super.onDoubleTap(e);
        }

        /**
         *
         * @param e1 手指按下
         * @param e2 当前的事件
         * @param distanceX oldX - newX
         * @param distanceY oldY - newY ->向右滑得到的是负数，所以需要取与distance相反的值
         * @return
         */
        @Override
        public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            if (isEnlarge){
                offsetX -= distanceX;
                offsetY -= distanceY;
                fixOffsets();
                invalidate();
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            if (isEnlarge){
                overScroller.fling(
                        (int)offsetX,
                        (int)offsetY,
                        (int) velocityX,
                        (int) velocityY,
                        -(int) (bitmap.getWidth() * bigScale - getWidth()) / 2,
                        (int) (bitmap.getWidth() * bigScale - getWidth()) / 2,
                        -(int) (bitmap.getHeight() * bigScale - getHeight()) / 2,
                        (int) (bitmap.getHeight() * bigScale - getHeight()) / 2,
                        300,
                        300
                );
                postOnAnimation(new FlingRunner());

            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}
