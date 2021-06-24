package com.example.firebaseabtest;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.textfield.TextInputLayout;

/**
 * A {@link MaterialShapeDrawable} that can draw a cutout for the label in {@link TextInputLayout}'s
 * outline mode.
 */
class CutoutDrawable extends MaterialShapeDrawable {
    @NonNull private final Paint cutoutPaint;
    @NonNull private final RectF cutoutBounds;
    private int savedLayer;

    CutoutDrawable() {
        this(null);
    }

    CutoutDrawable(@Nullable ShapeAppearanceModel shapeAppearanceModel) {
        super(shapeAppearanceModel != null ? shapeAppearanceModel : new ShapeAppearanceModel());
        cutoutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setPaintStyles();
        cutoutBounds = new RectF();
    }

    private void setPaintStyles() {
        cutoutPaint.setStyle(Style.FILL_AND_STROKE);
        cutoutPaint.setColor(Color.RED);
        cutoutPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
    }

    boolean hasCutout() {
        return !cutoutBounds.isEmpty();
    }

    void setCutout(float left, float topOriginal, float right, float bottomOriginal) {
        float top = Math.max(0, topOriginal);
        float bottom = top+this.getStrokeWidth();
        // Avoid expensive redraws by only calling invalidateSelf if one of the cutout's dimensions has
        // changed.
        if (left != cutoutBounds.left
                || top != cutoutBounds.top
                || right != cutoutBounds.right
                || bottom != cutoutBounds.bottom) {
            cutoutBounds.set(left, top, right, bottom);
            invalidateSelf();
        }
    }

    void setCutout(@NonNull RectF bounds) {
        setCutout(bounds.left, bounds.top, bounds.right, bounds.bottom);
    }

    void removeCutout() {
        // Call setCutout with empty bounds to remove the cutout.
        setCutout(0, 0, 0, 0);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        preDraw(canvas);
        super.draw(canvas);

        // Draw mask for the cutout.
//        canvas.drawRect(cutoutBounds, cutoutPaint);

//        Paint yellow = new Paint();
//        yellow.setStyle(Style.STROKE);
//        yellow.setColor(Color.YELLOW);
//        cutoutPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
//        canvas.drawArc(cutoutBounds.right-10, cutoutBounds.top, cutoutBounds.right+10, cutoutBounds.bottom, 90f, 180f, false, yellow);

//        Paint green = new Paint();
//        green.setStyle(Style.FILL_AND_STROKE);
//        green.setColor(Color.GREEN);


        Path p = new Path();
        p.moveTo(cutoutBounds.left, cutoutBounds.top);
        p.lineTo(cutoutBounds.right, cutoutBounds.top);
        p.arcTo(cutoutBounds.right-10, cutoutBounds.top, cutoutBounds.right+10, cutoutBounds.bottom, -90f, -180f, false);
        p.lineTo(cutoutBounds.left, cutoutBounds.bottom);
        p.arcTo(cutoutBounds.left-10, cutoutBounds.top, cutoutBounds.left+10, cutoutBounds.bottom, 90, -180f, false);

        canvas.drawPath(p, cutoutPaint);

        postDraw(canvas);
    }

    private void preDraw(@NonNull Canvas canvas) {
        Callback callback = getCallback();

        if (useHardwareLayer(callback)) {
            View viewCallback = (View) callback;
            // Make sure we're using a hardware layer.
            if (viewCallback.getLayerType() != View.LAYER_TYPE_HARDWARE) {
                viewCallback.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }
        } else {
            // If we're not using a hardware layer, save the canvas layer.
            saveCanvasLayer(canvas);
        }
    }

    private void saveCanvasLayer(@NonNull Canvas canvas) {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            savedLayer = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null);
        } else {
            savedLayer =
                    canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
        }
    }

    private void postDraw(@NonNull Canvas canvas) {
        if (!useHardwareLayer(getCallback())) {
            canvas.restoreToCount(savedLayer);
        }
    }

    private boolean useHardwareLayer(Callback callback) {
        return callback instanceof View;
    }
}

