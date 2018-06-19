package it.univaq.casatracking.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Images {

    private static Context context;
    private static Images images;
    private static File cacheDirectory;
    private static String cacheDirectoryPath;

    //private static final long MAX_SIZE = 5242880L; // 5MB
    private static final long MAX_SIZE = 524288000L; // 500MB

    public static Images getInstance(Context ctx){
        context = ctx;
        //access cache dir
        cacheDirectory = context.getCacheDir();
        cacheDirectoryPath = cacheDirectory.getAbsolutePath();
        //clear cache if cache size > MAX_SIZE
        clearCache();

        if(images == null)
            images = new Images();

        return images;
    }

    public String getCacheDirectoryPath(){
        //clear cache if cache size > MAX_SIZE
        clearCache();
        return cacheDirectoryPath;
    }

    public boolean exists(String imagename){
        File file = new File(cacheDirectoryPath, imagename);
        return file.exists();
    }

    public Bitmap loadImage(String imagename){
        File file = new File(cacheDirectoryPath, imagename);
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    public Bitmap saveImage(String imagename, InputStream is){
        File file = new File(cacheDirectoryPath, imagename);
        Bitmap bmp = null;
        FileOutputStream out = null;

        try {

            //import from is
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inSampleSize = calculateInSampleSize(option, 200, 200);
            option.inPreferredConfig = Bitmap.Config.RGB_565;

            bmp = BitmapFactory.decodeStream(is, null, option);

            //export to out
            out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 95, out);

            out.flush();

        } catch(IOException e){
            e.printStackTrace();
            bmp = null;

        } finally {

            try {
                if(out != null)
                    out.close();

            } catch(IOException e){
                //
            }

        }

        return bmp;
    }

    public String scalePicture(String imagepath){
        File file = new File(imagepath);
        InputStream in = null;
        FileOutputStream out = null;
        String newImagePath = null;

        try {
            in =  new FileInputStream(file);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize=8;
            Bitmap bit = BitmapFactory.decodeStream(in,null,options);
            Bitmap bitmap = Bitmap.createScaledBitmap(bit, 480, 640, true);
            File newPicture = new File(imagepath.replace(".jpg", "s") + ".jpg");

            //export to out
            out = new FileOutputStream(newPicture);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            out.flush();

            newImagePath = newPicture.getAbsolutePath();

        } catch(IOException e){
            e.printStackTrace();

        } finally {

            try {
                if(out != null)
                    out.close();

            } catch(IOException e){
                //
            }

        }

        return newImagePath;

    }

    /* clear cache func */
    private static void clearCache(){
        long size = getDirSize(cacheDirectory);

        if(size > MAX_SIZE)
            cleanDir(cacheDirectory);
    }


    /* save aux function */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    /* clearcache aux function */
    private static long getDirSize(File dir){

        long size = 0;
        File[] files = dir.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                size += file.length();
            }
        }

        return size;
    }

    /* clearcache aux function */
    private static void cleanDir(File dir){
        File[] files = dir.listFiles();

        for (File file : files) {
            file.delete();
        }
    }

}
