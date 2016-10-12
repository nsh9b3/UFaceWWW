package com.stuff.nsh9b3.uface;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

import java.nio.IntBuffer;

/**
 * Created by nsh9b3 on 10/7/2016.
 */

public class ImageTransform
{
    public static int bitmapWidth;
    public static int bitmapHeight;

    public static int sectionWidth;
    public static int sectionHeight;

    public static int[][] setGridPixelMap(Bitmap bitmap)
    {
        bitmapWidth = bitmap.getWidth();
        bitmapHeight = bitmap.getHeight();
        int gridSize = 16; //TODO: Call this from somewhere
        int gridWidth = 4;
        int gridHeight = 4;
        sectionWidth = bitmapWidth/gridWidth;
        sectionHeight = bitmapHeight/gridHeight;
        int[][] pixelMap = new int[gridSize][sectionWidth*sectionHeight];

        for(int i = 0; i < gridSize; i++)
        {
            int[] secPixels = new int[sectionWidth*sectionHeight];
            bitmap.getPixels(secPixels, 0, sectionWidth, (i % 4) * sectionWidth, (i / 4) * sectionHeight, sectionWidth, sectionHeight);
            pixelMap[i] = secPixels;
        }

        return pixelMap;
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }
}
