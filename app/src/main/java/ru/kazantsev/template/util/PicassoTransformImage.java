package ru.kazantsev.template.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import com.squareup.picasso.Transformation;

/**
 * Created by Dmitry on 22.07.2015.
 */
public class PicassoTransformImage implements Transformation {

    final int width;
    final int height;
    final int maxWidth;
    final String tag;


    public PicassoTransformImage(int width, int height, int maxWidth, String tag) {
        this.width = width;
        this.height = height;
        this.maxWidth = maxWidth;
        this.tag = tag;
    }



    @Override
    public Bitmap transform(Bitmap bitmap) {
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        float scaleX = 1;
        float scaleY = 1;
        if (width < 0 && height > 0) {
            scaleX = scaleY = ((float) height / bitmapHeight);
        }
        if (width > 0 && height < 0) {
            scaleX = scaleY = ((float) width / bitmapWidth);
        }
        if(width > 0 && height > 0) {
            scaleY = ((float) height / bitmapHeight);
            scaleX = ((float) width / bitmapWidth);
        }
        if (scaleX * bitmapWidth > maxWidth) {
            scaleX = scaleY = ((float) maxWidth / bitmapWidth);
        }
        Bitmap scaledBitmap;
        if (scaleX != 1 || scaleY != 1) {
            Matrix matrix = new Matrix();
            matrix.postScale(scaleX, scaleY);
            // Create a new bitmap and convert it to a format understood by the ImageView
            scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
            if (scaledBitmap != bitmap) {
                // Same bitmap is returned if sizes are the same
                bitmap.recycle();
            }
            return scaledBitmap;
        } else {
            return bitmap;
        }
    }

    @Override
    public String key() {
        return tag == null ? "" : tag + "transformation_" + "Width:" + Math.min(width, maxWidth) + "Height:" + height;
    }

}
