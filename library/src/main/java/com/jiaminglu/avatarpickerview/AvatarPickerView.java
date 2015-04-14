package com.jiaminglu.avatarpickerview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;

/**
 * Created by jiaming on 14-12-2.
 */
public class AvatarPickerView extends ImageView implements ScaleGestureDetector.OnScaleGestureListener{
    public AvatarPickerView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public AvatarPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public AvatarPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public AvatarPickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    int borderWidth;
    void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        detector = new ScaleGestureDetector(context, this);
        borderWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.AvatarPickerView, defStyleAttr, defStyleRes);

        borderWidth = a.getDimensionPixelSize(R.styleable.AvatarPickerView_borderWidth, borderWidth);

        a.recycle();
    }

    ScaleGestureDetector detector;

    float lastX;
    float lastY;
    @Override
    public boolean onTouchEvent(@NotNull MotionEvent e) {
        detector.onTouchEvent(e);
        if (e.getAction() == MotionEvent.ACTION_DOWN && e.getPointerCount() == 1)
            scaling = false;
        if (e.getAction() == MotionEvent.ACTION_MOVE && e.getPointerCount() == 1 && !scaling) {
            matrix.postTranslate(e.getX() - lastX, e.getY() - lastY);
            setImageMatrix(matrix);
        }
        lastX = e.getX();
        lastY = e.getY();
        return true;
    }

    Matrix matrix;
    Paint paint = new Paint();{
        paint.setColor(0xaf000000);
    }
    Paint linePaint = new Paint();{
        linePaint.setColor(0xafffffff);
    }
    Rect bound = new Rect();
    int avatarSize;
    int horizPadding;
    int vertPadding;

    public void resetMatrix() {
        matrix = null;
    }

    @Override
    public void onDraw(@NotNull Canvas canvas) {
        if (getDrawable() == null) {
            super.onDraw(canvas);
            return;
        }
        canvas.getClipBounds(bound);
        avatarSize = bound.width() * 3 / 4;
        horizPadding = (bound.width() - avatarSize) / 2;
        vertPadding = (bound.height() - avatarSize) / 2;
        if (matrix == null) {
            RectF drawableRect = new RectF(getDrawable().getBounds());
            RectF viewRect = new RectF(bound);
            matrix = new Matrix();
            matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
            setImageMatrix(matrix);
        }

        super.onDraw(canvas);

        canvas.drawRect(
                bound.left + horizPadding,              bound.top,
                bound.left + horizPadding + avatarSize, bound.top + vertPadding,
                paint);
        canvas.drawRect(
                bound.left + horizPadding,              bound.top + vertPadding + avatarSize,
                bound.left + horizPadding + avatarSize, bound.bottom,
                paint);
        canvas.drawRect(
                bound.left,                             bound.top,
                bound.left + horizPadding,              bound.bottom,
                paint);
        canvas.drawRect(
                bound.left + horizPadding + avatarSize, bound.top,
                bound.right,                            bound.bottom,
                paint);

        canvas.drawRect(
                bound.left + horizPadding - borderWidth,              bound.top + vertPadding - borderWidth,
                bound.left + horizPadding + avatarSize + borderWidth, bound.top + vertPadding,
                linePaint);
        canvas.drawRect(
                bound.left + horizPadding - borderWidth,              bound.top + vertPadding + avatarSize,
                bound.left + horizPadding + avatarSize + borderWidth, bound.top + vertPadding + avatarSize + borderWidth,
                linePaint);
        canvas.drawRect(
                bound.left + horizPadding - borderWidth,              bound.top + vertPadding,
                bound.left + horizPadding,                          bound.top + vertPadding + avatarSize,
                linePaint);
        canvas.drawRect(
                bound.left + horizPadding + avatarSize,             bound.top + vertPadding,
                bound.left + horizPadding + avatarSize + borderWidth, bound.top + vertPadding + avatarSize,
                linePaint);
    }

    Matrix invert = new Matrix();
    public Bitmap getClippedBitmap(int maxSize) {
        RectF src = new RectF(horizPadding, vertPadding, horizPadding + avatarSize, vertPadding + avatarSize);
        RectF dst = new RectF();
        matrix.invert(invert);
        invert.mapRect(dst, src);

        if (maxSize == 0 || dst.width() < maxSize) {
            Bitmap bitmap = Bitmap.createBitmap((int) dst.width(), (int) dst.width(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.translate(-dst.left, -dst.top);
            getDrawable().draw(canvas);
            return bitmap;
        } else {
            Bitmap bitmap = Bitmap.createBitmap(maxSize, maxSize, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.scale(maxSize / dst.width(), maxSize / dst.width());
            canvas.translate(-dst.left, -dst.top);
            getDrawable().draw(canvas);
            return bitmap;
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (matrix == null)
            return false;
        matrix.set(scaleMatrix);
        matrix.postScale(detector.getScaleFactor(), detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
        setImageMatrix(matrix);
        return false;
    }

    boolean scaling = false;
    Matrix scaleMatrix = new Matrix();
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        if (matrix == null)
            return false;
        scaleMatrix.set(matrix);
        scaling = true;
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }
}
