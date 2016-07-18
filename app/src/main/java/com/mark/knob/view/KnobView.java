package com.mark.knob.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class KnobView extends View {

    /**
     * 圆心坐标x
     */
    private float o_x;

    /**
     * 圆心坐标y
     */
    private float o_y;

    /**
     * 图片的宽度
     */
    private int width;

    /**
     * 图片的高度
     */
    private int height;

    /**
     * view的真实宽度与高度:因为是旋转，所以这个view是正方形，它的值是图片的对角线长度
     */
    private double maxwidth;

    /**
     * 圆环
     */
    private Bitmap ring;

    /**
     * 箭头
     */
    private Bitmap arrow;

    /**
     * 圆点
     */
    private Bitmap dot;

    /**
     * 目标步数
     */
    private int goalStep;

    public KnobView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 通过此方法来设置图片资源
     * @param picture_1 圆环
     * @param picture_2 箭头
     * @param picture_3 圆点
     */
    public void setRotatDrawable(int picture_1, int picture_2, int picture_3) {
        BitmapDrawable drawable = (BitmapDrawable) getContext().getDrawable(picture_1);
        if (drawable != null) {
            ring = drawable.getBitmap();
        }
        drawable = (BitmapDrawable) getContext().getDrawable(picture_2);
        if (drawable != null) {
            arrow = drawable.getBitmap();
        }
        drawable = (BitmapDrawable) getContext().getDrawable(picture_3);
        if (drawable != null) {
            dot = drawable.getBitmap();
        }
        initSize();
        postInvalidate();
    }

    private void initSize() {
        if (ring == null || dot == null) {
            return;
        }
        width = ring.getWidth();
        height = ring.getHeight();

        maxwidth = Math.sqrt(width * width + height * height);

        o_x = o_y = (float) (maxwidth / 2);// 确定圆心坐标
    }

    /**
     * 通过此方法来控制旋转度数，如果超过360，让它求余，防止，该值过大造成越界
     *
     * @param added 增加的度数
     */
    private void addDegree(float added) {
        deta_degree += added;
        if (deta_degree > 356) {
            deta_degree = 360;
        } else if (deta_degree < 0) {
            deta_degree = 0;
        }
        deta_degree = transformDegree(deta_degree);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {

        Matrix matrix = new Matrix();
        // 设置转轴位置，移动坐标原点到旋转点
        matrix.setTranslate((float) width / 2, (float) height / 2);

        // 以原点为中心旋转
        matrix.preRotate(deta_degree);
        // 转轴还原，将旋转后的图像平移回原来的坐标原点
        matrix.preTranslate(-(float) width / 2, -(float) height / 2);

        // 将位置送到view的中心
        matrix.postTranslate((float) (maxwidth - width) / 2, (float) (maxwidth - height) / 2);

        paint.setAntiAlias(true); // 消除锯齿

        canvas.drawBitmap(ring, (float) (maxwidth - width) / 2, (float) (maxwidth - height) / 2, paint);
        canvas.drawBitmap(arrow, matrix, paint);
        canvas.drawBitmap(dot, matrix, paint);

        /**
         * 画步数
         */
        paint.setStrokeWidth(0);
        paint.setColor(Color.BLUE);
        float textSize = 40;
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.DEFAULT_BOLD); // 设置字体
        //算目标步数
        goalStep = (int) (deta_degree * 20000 / 360);
        float textWidth = paint.measureText(goalStep + "步"); // 测量字体宽度，我们需要根据字体的宽度设置在圆环中间
        canvas.drawText(goalStep + "步", o_x - textWidth / 2, o_y + textSize / 2, paint);

        super.onDraw(canvas);
    }

    private Paint paint = new Paint();

    //设置View的大小
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 它的宽高不是图片的宽高，而是以宽高为直角的矩形的对角线的长度
        setMeasuredDimension((int) maxwidth, (int) maxwidth);
    }

    /**
     * 手指触屏的初始x的坐标
     */
    private float down_x;

    /**
     * 手指触屏的初始y的坐标
     */
    private float down_y;

    /**
     * 移动时的x的坐标
     */
    private float target_x;

    /**
     * 移动时的y的坐标
     */
    private float target_y;

    /**
     * 放手时的x的坐标
     */
    private float up_x;

    /**
     * 放手时的y的坐标
     */
    private float up_y;

    /**
     * 当前的弧度(以该 view 的中心为圆点)
     */
    private float current_degree;

    /**
     * 放手时的弧度(以该 view 的中心为圆点)
     */
    float up_degree;

    /**
     * 当前圆盘所转的弧度(以该 view 的中心为圆点)
     */
    private float deta_degree;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (ring == null) {
            throw new NoBitMapError("Error,No bitmap in RotatView!");
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                down_x = event.getX();
                down_y = event.getY();
                current_degree = 0;
//                current_degree = detaDegree(o_x, o_y, down_x, down_y);
//                if (current_degree > 180 && down_x > o_x) {
//                    deta_degree = current_degree - 270;
//                } else {
//                    deta_degree = current_degree + 90;
//                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                down_x = target_x = event.getX();
                down_y = target_y = event.getY();
                float degree = detaDegree(o_x, o_y, target_x, target_y);

                // 滑过的弧度增量
                float dete = degree - current_degree;
                dete = (float) (dete * 10 / 36 * 3.6);
                // 如果小于-270度说明 它跨周了，需要特殊处理
                if (dete < -270) {
                    dete = dete + 360;
                    // 如果大于270度说明 它跨周了，需要特殊处理
                } else if (dete > 270) {
                    dete = dete - 360;
                }
                addDegree(dete);
                current_degree = transformDegree(degree);
                postInvalidate();

                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                up_x = event.getX();
                up_y = event.getY();
                current_degree = detaDegree(o_x, o_y, up_x, up_y);
                current_degree = transformDegree(current_degree);
                break;
            }
        }
        return true;
    }

    /**
     * 计算以(src_x,src_y)为坐标原点，建立直角体系，求出(target_x,target_y)坐标与x轴的夹角
     * 主要是利用反正切函数的知识求出夹角
     *
     * @param src_x    原点X坐标
     * @param src_y    原点Y坐标
     * @param target_x 目标点X坐标
     * @param target_y 目标点Y坐标
     * @return 与X轴正向的夹角0~360
     */
    private float detaDegree(float src_x, float src_y, float target_x, float target_y) {

        float detaX = target_x - src_x;
        float detaY = target_y - src_y;
        double d;
        if (detaX != 0) {
            float tan = Math.abs(detaY / detaX);

            if (detaX > 0) {

                if (detaY >= 0) {
                    d = Math.atan(tan);

                } else {
                    d = 2 * Math.PI - Math.atan(tan);
                }

            } else {
                if (detaY >= 0) {

                    d = Math.PI - Math.atan(tan);
                } else {
                    d = Math.PI + Math.atan(tan);
                }
            }

        } else {
            if (detaY > 0) {
                d = Math.PI / 2;
            } else {

                d = -Math.PI / 2;
            }
        }

        return (float) ((d * 180) / Math.PI);
    }

    /**
     * 圆环有100段，将角度转换为3.6的倍数
     */
    private float transformDegree(float degree) {
        return (float) (Math.round(degree * 10 / 36) * 3.6);
    }

    /**
     * 一个异常，用来判断是否有rotatBitmap
     */
    private static class NoBitMapError extends RuntimeException {

        private static final long serialVersionUID = 1L;

        NoBitMapError(String detailMessage) {
            super(detailMessage);
        }

    }

    public int getGoalStep() {
        return goalStep;
    }

    public void setGoalStep(int goalStep) {
        this.goalStep = goalStep;
        deta_degree = goalStep * 360 / 20000;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (ring != null) {
            ring.recycle();
            ring = null;
        }
    }
}
