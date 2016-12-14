package com.cdelg4do.waiterdroid.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.cdelg4do.waiterdroid.adapters.AllergenListAdapter;
import com.cdelg4do.waiterdroid.backgroundtaskhandler.BackgroundTaskHandler;
import com.cdelg4do.waiterdroid.backgroundtaskhandler.BackgroundTaskListener;
import com.cdelg4do.waiterdroid.backgroundtasks.DownLoadImageTask;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;


// This class provides useful common auxiliar functions.
// This class is abstract, and all its public methods are static.
// ----------------------------------------------------------------------------

public abstract class Utils {

    public static enum MessageType {

        DIALOG,
        TOAST,
        NONE
    }


    // Shows the user a message of the given type (toast, dialog or none)
    // If the type is dialog, a title must be provided.
    public static void showMessage(Context ctx, String msg, MessageType type, String title) {

        if ( type==MessageType.NONE ) {
            return;
        }

        else if ( type==MessageType.TOAST ) {
            Toast.makeText(ctx,msg, Toast.LENGTH_LONG).show();
        }

        else if ( type==MessageType.DIALOG ) {

            AlertDialog dialog = new AlertDialog.Builder(ctx).create();
            dialog.setTitle(title);
            dialog.setMessage(msg);

            // OK buton
            dialog.setButton(
                ctx.getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                    }
                }
            );

            dialog.show();
        }
    }


    // Returns a random int, between min and max
    public static int randomInt(int min, int max) {

        if (min==max)
            return min;

        if (min > max) {
            int newMin = max;
            max = min;
            min = newMin;
        }

        Random rnd = new Random();
        int num = rnd.nextInt((max-min)+1) + min;
        return num;
    }


    // This method is useful when using a ListView inside an ScrollView
    // (https://kennethflynn.wordpress.com/2012/09/12/putting-android-listviews-in-scrollviews/)
    public static void setListViewHeightBasedOnChildren(ListView listView) {

        AllergenListAdapter listAdapter = (AllergenListAdapter) listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 75;

        for (int i = 0; i < listAdapter.getCount(); i++) {

            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }


    // Gets the money string from a BigDecimal
    public static String getMoneyString(BigDecimal num, String currency)
    {
        DecimalFormat formatter;
        String res;

        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(new Locale("es", "ES"));
        formatter = new DecimalFormat("#,###.##",symbols);

        res = formatter.format(num);

        return twoDecimalsNumber( res , "," ) + " " + currency;
    }


    // Returns a string representing a number with 2 decimals
    private static String twoDecimalsNumber(String num, String separator)
    {
        String res = num;
        String[] temp = num.split(separator);

        if ( temp.length == 2 && temp[1].length() == 1 )
                res += "0";

        return res;
    }


    // Launches a new task in background to download an image
    public static void downloadImageInBackground(URL imageUrl, ImageView imageView, BackgroundTaskListener listener) {

        DownLoadImageTask downloadImage = new DownLoadImageTask(imageUrl, imageView, ImageCache.getCache());
        new BackgroundTaskHandler(downloadImage,listener).execute();
    }


    // Attempts to load into a view an image previously download in background
    public static boolean showDownloadedImage(BackgroundTaskHandler taskHandler, int defaultImageResource) {

        if ( taskHandler == null || taskHandler.getTaskId() != DownLoadImageTask.taskId )
            return false;

        HashMap<String,Object> taskProduct = (HashMap<String,Object>) taskHandler.getTaskProduct();

        WeakReference<ImageView> imageViewRef = (WeakReference<ImageView>) taskProduct.get(DownLoadImageTask.IMAGEVIEW_WEAKREF_KEY);
        Bitmap bitmap = (Bitmap) taskProduct.get(DownLoadImageTask.BITMAP_KEY);

        try {
            ImageView imageView = imageViewRef.get();

            if (bitmap == null || taskHandler.hasFailed() ) {

                Log.d("Utils.showDownloadedImg","WARNING: The image download has failed!");
                imageView.setImageResource(defaultImageResource);
                return false;
            }

            imageView.setImageBitmap(bitmap);
        }
        catch(NullPointerException e) {

            Log.d("Utils.showDownloadedImg","WARNING: NullPointerException cached!");
            return false;
        }

        return true;
    }

}
