package com.stuff.nsh9b3.uface;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Created by nshilbe on 10/10/16.
 */

public class LBP
{
    // Keys for histogram containing uniform numbers only
    private static HashMap<Integer, Integer> histogramKeys;

    public static byte[][] generateFeatureVector(int[][] pixels)
    {
        int[][] featureVector = new int[16][59];

        // Keys used to properly list uniform values
        histogramKeys = new HashMap<>();
        generateKeyMappings(histogramKeys);

        for(int i = 0; i < 16; i++)
        {
            featureVector[i] = generateRegionHistogram(pixels, i);
        }

        return splitEncryptions(convertMatToArray(featureVector, 59, 16));
    }

    private static int[] generateRegionHistogram(int[][] pixels, int region)
    {
        int[] histogram = new int[59];
        int startRow = (region >= 4 ? 0 : 1);
        int endRow = (region < 12 ? ImageTransform.sectionHeight : ImageTransform.sectionHeight - 1);
        int startCol = (region % 4 != 0 ? 0 : 1);
        int endCol = (region % 4 != 3 ? ImageTransform.sectionWidth : ImageTransform.sectionHeight - 1);

        for(int i = startRow; i < endRow; i++)
        {
            for(int k = startCol; k < endCol; k++)
            {
                int label = getLabel(pixels, region, i, k,
                        ((region % 4 > 0) && (k == startCol)), ((region % 4 < 3) && (k + 1 == endCol)),
                        ((region / 4 > 0) && (i == startRow)), ((region / 4 < 3) && (i + 1 == endRow)));

                // Place the value in the correct spot in the array based off the keys
                // If the value is not uniform value, throw it into the last bin
                if (histogramKeys.get((label & 0xFF)) != null)
                    histogram[histogramKeys.get((label & 0xFF))]++;
                else
                    histogram[histogram.length - 1]++;
            }
        }

        return histogram;
    }

    private static int getLabel(int[][] pixels, int region, int row, int col,
                                boolean secLeft, boolean secRight,
                                boolean secUp, boolean secDown)
    {
        int label = 0;

        int pValue = pixels[region][row * ImageTransform.sectionWidth + col] & 0xFF;

        // Top Left Pixel
        int tL;
        if(secUp && secLeft)
            tL = pixels[region - 5][ImageTransform.sectionHeight * ImageTransform.sectionWidth - 1] & 0xFF;
        else if(secUp && !secLeft)
            tL = pixels[region - 4][(ImageTransform.sectionHeight - 1) * ImageTransform.sectionWidth + (col - 1)] & 0xFF;
        else if(!secUp && secLeft)
            tL = pixels[region - 1][(row - 1) * ImageTransform.sectionWidth + (ImageTransform.sectionWidth - 1)] & 0xFF;
        else
            tL = pixels[region][(row - 1) * ImageTransform.sectionWidth + (col - 1)] & 0xFF;

        // Top Pixel
        int t;
        if(secUp)
            t = pixels[region - 4][(ImageTransform.sectionHeight - 1) * ImageTransform.sectionWidth + (col)] & 0xFF;
        else
            t = pixels[region][(row - 1) * ImageTransform.sectionWidth + (col)] & 0xFF;

        // Top Right Pixel
        int tR;
        if(secUp && secRight)
            tR = pixels[region - 3][(ImageTransform.sectionHeight - 1) * ImageTransform.sectionWidth] & 0xFF;
        else if(secUp && !secRight)
            tR = pixels[region - 4][(ImageTransform.sectionHeight - 1) * ImageTransform.sectionWidth + (col + 1)] & 0xFF;
        else if(!secUp && secRight)
            tR = pixels[region + 1][(row - 1) * ImageTransform.sectionWidth] & 0xFF;
        else
            tR = pixels[region][(row - 1) * ImageTransform.sectionWidth + (col + 1)] & 0xFF;

        // Right Pixel
        int r;
        if(secRight)
            r = pixels[region + 1][row * ImageTransform.sectionWidth] & 0xFF;
        else
            r = pixels[region][row * ImageTransform.sectionWidth + (col + 1)] & 0xFF;

        // Down Right Pixel
        int dR;
        if(secDown && secRight)
            dR = pixels[region + 5][0] & 0xFF;
        else if(secDown && !secRight)
            dR = pixels[region + 4][(col + 1)] & 0xFF;
        else if(!secDown && secRight)
            dR = pixels[region + 1][(row + 1) * ImageTransform.sectionWidth] & 0xFF;
        else
            dR = pixels[region][(row + 1) * ImageTransform.sectionWidth + (col + 1)] & 0xFF;


        // Down Pixel
        int d;
        if(secDown)
            d = pixels[region + 4][col] & 0xFF;
        else
            d = pixels[region][(row + 1) * ImageTransform.sectionWidth + col] & 0xFF;

        // Down Left Pixel
        int dL;
        if(secDown && secLeft)
            dL = pixels[region + 3][(ImageTransform.sectionWidth - 1)] & 0xFF;
        else if(secDown && !secLeft)
            dL = pixels[region + 4][(col - 1)] & 0xFF;
        else if(!secDown && secLeft)
            dL = pixels[region - 1][(row + 1) * ImageTransform.sectionWidth + (ImageTransform.sectionWidth - 1)] & 0xFF;
        else
            dL = pixels[region][(row + 1) * ImageTransform.sectionWidth + (col - 1)] & 0xFF;

        // Left Pixel
        int l;
        if(secLeft)
            l = pixels[region - 1][row * ImageTransform.sectionWidth + (ImageTransform.sectionWidth - 1)] & 0xFF;
        else
            l = pixels[region][row * ImageTransform.sectionWidth + (col - 1)] & 0xFF;

        if(tL > pValue)
            label = label | 0x80;
        if(t > pValue)
            label = label | 0x40;
        if(tR > pValue)
            label = label | 0x20;
        if(r > pValue)
            label = label | 0x10;
        if(dR > pValue)
            label = label | 0x08;
        if(d > pValue)
            label = label | 0x04;
        if(dL > pValue)
            label = label | 0x02;
        if(l > pValue)
            label = label | 0x01;

        return label;
    }

    private static int[] convertMatToArray(int[][] matrix, int length, int width)
    {
        int[] array = new int[length*width];
        int counter = 0;
        for(int i = 0; i < width; i++)
        {
            for(int k = 0; k < length; k++)
                array[counter++] = matrix[i][k];
        }

        return array;
    }

    private static byte[][] splitEncryptions(int[] featureVector)
    {
        int bitsPerInt = (int)(Math.ceil(Math.log(ImageTransform.sectionWidth*ImageTransform.sectionHeight) / Math.log(2))) + 1;
        int bitsAllowedPerInt = 20;
        int zeroBitsPerInt = bitsAllowedPerInt - bitsPerInt;
        int bytesPerBigInt = 128; //1024 / 8
        int intsPerBigInt = 1024 / bitsAllowedPerInt;
        int zeroBitsPerBigInt = 1024 % bitsAllowedPerInt;
        int bigIntsNeeded = (int)Math.ceil(featureVector.length / (double)intsPerBigInt);
        byte[][] byteFV = new byte[bigIntsNeeded][bytesPerBigInt];
        byte nextByte = 0x00;
        int emptyBits = zeroBitsPerBigInt;
        int bigIntIndex = 0;
        int byteIndex = 0;
        int bitsUsedPerInt = 0;
        int bitsNeededPerInt = bitsPerInt;
        int bitsUsedPerByte = 0;

        for(int bin : featureVector)
        {
            emptyBits += zeroBitsPerInt;
            while(emptyBits > 0)
            {
                if((bitsUsedPerByte + emptyBits) >= 8)
                {
                    byteFV[bigIntIndex][byteIndex++] = nextByte;
                    emptyBits -= (8 - bitsUsedPerByte);
                    bitsUsedPerByte = 0;
                    if(byteIndex == bytesPerBigInt)
                    {
                        Log.d("TAG", "ERROR");
                    }
                    nextByte = 0x00;
                }
                else
                {
                    bitsUsedPerInt = 8 - bitsUsedPerByte - emptyBits;
                    nextByte = (byte)(((bin >>> (bitsPerInt - bitsUsedPerInt)) & 0xFF) | nextByte);
                    emptyBits = 0;
                    bitsNeededPerInt -= bitsUsedPerInt;
                    bitsUsedPerByte = 0;
                    byteFV[bigIntIndex][byteIndex++] = nextByte;
                    if(byteIndex == bytesPerBigInt)
                    {
                        Log.d("TAG", "ERROR");
                    }
                    nextByte = 0x00;
                }
            }
            while(bitsNeededPerInt >= 8)
            {
                nextByte = (byte)((bin >>> (bitsNeededPerInt - 8)) & 0xFF);
                bitsNeededPerInt -= 8;
                byteFV[bigIntIndex][byteIndex++] = nextByte;
                if(byteIndex == bytesPerBigInt)
                {
                    bigIntIndex++;
                    emptyBits += zeroBitsPerBigInt;
                    byteIndex = 0;
                }
            }
            if(bitsNeededPerInt != 0)
            {
                bitsUsedPerByte = bitsNeededPerInt;
                nextByte = (byte)((bin << bitsNeededPerInt) & 0xFF);
            }
            else
            {
                nextByte = 0x00;
            }
            bitsNeededPerInt = bitsPerInt;
        }

        return byteFV;
    }

    /**
     * Generates bins for the numbers below only. These are uniform values (less than 3 bitwise changes
     * in each value). All other values get dumped into a separate bin (non-uniform bin).
     * Only contains 1, 2, 3, 4, 6, 7, 8, 12, 14, 15, 16, 24, 28, 30, 31, 32, 48, 56, 60, 62, 63, 64,
     * 96, 112, 120, 124, 126, 127, 128, 129, 131, 135, 143, 159, 191, 192, 193, 195, 199, 207, 223,
     * 224, 225, 227, 231, 239, 240, 241, 243, 247, 248, 249, 251, 252, 253, 254, 255
     *
     * @param keys hashmap for these uniform values into the correct location in an array
     */
    private static void generateKeyMappings(HashMap<Integer, Integer> keys)
    {
        int count = 0;
        for (int i = 0; i < 256; i++)
        {
            byte value = (byte) i;
            int transitions = 0;
            int last = value & 1;
            for (int k = 1; k < 8; k++)
            {
                if (((value >> k) & 1) != last)
                {
                    last = ((value >> k) & 1);
                    transitions++;
                    if (transitions > 2)
                    {
                        break;
                    }
                }
            }
            if (transitions <= 2)
            {
                keys.put(i, count++);
            }
        }
    }
}
