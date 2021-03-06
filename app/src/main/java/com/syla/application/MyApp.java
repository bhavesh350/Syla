package com.syla.application;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.syla.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by User on 1/2/2017.
 */
public class MyApp extends Application {

    private static MyApp instance = null;
    private static ProgressDialog dialog;
    public static String SHARED_PREF_NAME = "RS_PREF";
    private static final String KEYSERVERID = "keyserverid";
    private static Context ctx;
    private static MyApp myApplication = null;


    @Override
    public void onLowMemory() {
        Runtime.getRuntime().gc();
        super.onLowMemory();
    }

    public static MyApp getApplication() {
        return myApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ctx = getApplicationContext();
        myApplication = this;
        FacebookSdk.setApplicationId("2540966832596163");
        FacebookSdk.sdkInitialize(getApplicationContext());

//        final Fabric fabric = new Fabric.Builder(this)
//                .kits(new Crashlytics())
//                .debuggable(true)           // Enables Crashlytics debugger
//                .build();
//        Fabric.with(fabric);
    }

    static Handler mHandler = new Handler();

    @Override
    public void onTerminate() {
        super.onTerminate();
    }


    public static synchronized MyApp getInstance() {
        if (instance != null) {
            return instance;
        } else {
            instance = new MyApp();
            return instance;
        }

    }

    public static void spinnerStart(Context context, String text) {
        String pleaseWait = text;
        try {
            dialog = ProgressDialog.show(context, pleaseWait, "", true);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
        } catch (Exception e) {
        }

    }

    public static void spinnerStartData(Context context, String title, String text) {
        String pleaseWait = text;
        try {
            dialog = ProgressDialog.show(context, pleaseWait, "", true);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
        } catch (Exception e) {
        }

    }

    public static float getDisplayMatrix(Context c) {
//        WindowManager wm = (WindowManager) ctx
//                .getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        int width = size.x;
        return c.getResources().getDisplayMetrics().density;
    }

    public static void spinnerStop() {
        if (dialog != null) {
            if (dialog.isShowing()) {
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                }

            }
        }

    }

    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }

    public static boolean isImage(String inFile) {
        if (inFile.toLowerCase().endsWith(".jpeg")
                || inFile.toLowerCase().endsWith(".jpg")
                || inFile.toLowerCase().endsWith(".png")
                || inFile.toLowerCase().endsWith(".tiff")
                || inFile.toLowerCase().endsWith(".bmp")
                && !inFile.toLowerCase().contains("com.")) {
            return true;
        } else {
            return false;
        }
    }


    public static void popMessage(String titleMsg, String errorMsg,
                                  Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleMsg).setMessage(Html.fromHtml(errorMsg)).setIcon(R.mipmap.ic_launcher)
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = builder.create();
        try {
            alert.show();
        } catch (Exception e) {
        }

    }

    public static void popFinishableMessage(String titleMsg, String errorMsg,
                                            final Activity context) {
        // pop error message
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleMsg).setMessage(errorMsg)
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        context.finish();
                    }
                });

        AlertDialog alert = builder.create();
        try {
            alert.show();
        } catch (Exception e) {
        }

    }

    public static void showMassage(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    public static final String DISPLAY_MESSAGE_ACTION = "pushnotifications.DISPLAY_MESSAGE";
    public static final String EXTRA_MESSAGE = "message";

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }

    public static long getSharedPrefLong(String preffConstant) {
        long longValue = 0;
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        longValue = sp.getLong(preffConstant, 0);
        return longValue;
    }

    public static void setSharedPrefLong(String preffConstant, long longValue) {
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(preffConstant, longValue);
        editor.commit();
    }

    public static String getSharedPrefString(String preffConstant) {
        String stringValue = "";
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        stringValue = sp.getString(preffConstant, "");
        return stringValue;
    }

    public static void setSharedPrefString(String preffConstant,
                                           String stringValue) {
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(preffConstant, stringValue);
        editor.commit();
    }

    public static int getSharedPrefInteger(String preffConstant) {
        int intValue = 0;
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        intValue = sp.getInt(preffConstant, 0);
        return intValue;
    }

    public static void setSharedPrefInteger(String preffConstant, int value) {
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(preffConstant, value);
        editor.commit();
    }

    public static float getSharedPrefFloat(String preffConstant) {
        float floatValue = 0;
        SharedPreferences sp = myApplication.getSharedPreferences(
                preffConstant, 0);
        floatValue = sp.getFloat(preffConstant, 0);
        return floatValue;
    }

    public static void setSharedPrefFloat(String preffConstant, float floatValue) {
        SharedPreferences sp = myApplication.getSharedPreferences(
                preffConstant, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(preffConstant, floatValue);
        editor.commit();
    }

    public static void setSharedPrefArray(String preffConstant, float floatValue) {
        SharedPreferences sp = myApplication.getSharedPreferences(
                preffConstant, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(preffConstant, floatValue);
        editor.commit();
    }

    public static boolean getStatus(String name) {
        boolean status;
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        status = sp.getBoolean(name, false);
        return status;
    }

    public static boolean getStatusTrue(String name) {
        boolean status;
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        status = sp.getBoolean(name, true);
        return status;
    }

    public static void setStatus(String name, boolean istrue) {
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(name, istrue);
        editor.commit();
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()
                .getWindowToken(), 0);
    }

    public static Bitmap getimagebitmap(String imagepath) {
        Bitmap bitmap = decodeFile(new File(imagepath));

        // rotate bitmap
        Matrix matrix = new Matrix();
        // matrix.postRotate(MyApplication.getExifOrientation(imagepath));
        // create new rotated bitmap
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);

        return bitmap;
    }

    private static Bitmap decodeFile(File F) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(F), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE = 204;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE
                    && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(F), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    public static double roundDouble(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

//    public static String getDeviceId() {
//
//        String android_id = "";
//        final TelephonyManager tm = (TelephonyManager) ctx
//                .getSystemService(Context.TELEPHONY_SERVICE);
//
//        final String tmDevice, tmSerial, androidId;
//        tmDevice = "" + tm.getDeviceId();
//        tmSerial = "" + tm.getSimSerialNumber();
//        androidId = ""
//                + Settings.Secure.getString(
//                ctx.getContentResolver(),
//                Settings.Secure.ANDROID_ID);
//
//        UUID deviceUuid = new UUID(androidId.hashCode(),
//                ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
//        android_id = deviceUuid.toString();
//        return android_id;
//
//    }

    public static String getProgressDisplayLine(long currentBytes, long totalBytes) {
        return getBytesToMBString(currentBytes) + "/" + getBytesToMBString(totalBytes);
    }

    private static String getBytesToMBString(long bytes) {
        return String.format(Locale.ENGLISH, "%.2fMb", bytes / (1024.00 * 1024.00));
    }

    public static int getDisplayWidth() {
        WindowManager wm = (WindowManager) ctx
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return width;
    }

    public static int getDisplayHeight() {
        WindowManager wm = (WindowManager) ctx
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        return height;
    }

    public static boolean isEmailValid(String email) {
        String regExpn = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches())
            return true;
        else
            return false;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static String base64Decode(String token) {
        try {
            byte[] data = Base64.decode(token, Base64.DEFAULT);
            String text = new String(data, "UTF-8");
            return text;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return "";
    }

    public static String millsToDate(long mills) {

        Date d = new Date(mills);
        String format = "MM/dd/yyyy";
        return new SimpleDateFormat(format).format(d);
    }

    public static String millsToDate2(long mills) {

        Date d = new Date(mills);
        String format = "dd-MMM-yyyy";
        return new SimpleDateFormat(format).format(d);
    }

    public static String millsToTime(long mills) {

        Date d = new Date(mills);
        String format = "hh:mm aa";
        return new SimpleDateFormat(format).format(d);
    }

    public static double millsToDayTime(long mills) {

        Date d = new Date(mills);
        String format = "kk:mm";
        String dateString = new SimpleDateFormat(format).format(d);
        double hr = Double.parseDouble(dateString.split(":")[0]);
        double min = Double.parseDouble(dateString.split(":")[1]);
        return hr + (min / 100d);
    }


    public static String parseDateFullMonth(String time) {
        String inputPattern = "yyyy-MM-dd";
        String outputPattern = "dd MMMM yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String parseDateSortDate(String time) {
        String inputPattern = "yyyy-MM-dd HH:mm:ss";//2018-06-25 13:40:38
        String outputPattern = "dd MMM hh:mm a";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
//        http://www.espnfc.com//feed
        String domain = uri.getHost().replace("http://", "").replace("https://", "");
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }


    public static String getDateOrTimeFromMillis(String x) {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yy - hh:mm a");

        long milliSeconds;
        try {
            milliSeconds = Long.parseLong(x);
        } catch (Exception e) {
            milliSeconds = Long.parseLong(x.replace(".", ""));
        }
        System.out.println(milliSeconds);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(System.currentTimeMillis());

        String s = formatter.format(calendar.getTime()).split("-")[0];
        String s1 = formatter.format(calendar2.getTime()).split("-")[0];

        if (s.equals(s1)) {
            return formatter.format(calendar.getTime()).split("-")[1];
        } else {
            return formatter.format(calendar.getTime()).split("-")[0];
        }
    }


    public static String getDateOrTimeFromMillis(Long x) {
        DateFormat formatter = new SimpleDateFormat("dd MMM hh:mm a");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(x);

        return formatter.format(calendar.getTime());

    }

    public static String getTodayDate(Long x) {
        DateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(x);

        return formatter.format(calendar.getTime());

    }


    public static void openFile(Context context, File url) throws IOException {
        // Create URI
        File file = url;
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        // Check what kind of file you are trying to open, by comparing the url with extensions.
        // When the if condition is matched, plugin sets the correct intent (mime) type,
        // so Android knew what application to use to open the file
        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if (url.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (url.toString().contains(".zip") || url.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/x-wav");
        } else if (url.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (url.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if (url.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") || url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

//    public List<Doctor.Data> readDoctors() {
//        String path = "/data/data/" + ctx.getPackageName()
//                + "/doctorList.ser";
//        File f = new File(path);
//        List<Doctor.Data> device = new ArrayList<>();
//        if (f.exists()) {
//            try {
//                System.gc();
//                FileInputStream fileIn = new FileInputStream(path);
//                ObjectInputStream in = new ObjectInputStream(fileIn);
//                device = (List<Doctor.Data>) in.readObject();
//                in.close();
//                fileIn.close();
//            } catch (StreamCorruptedException e) {
//                e.printStackTrace();
//            } catch (OptionalDataException e) {
//                e.printStackTrace();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return device;
//    }

//    public void writeDoctors(List<Doctor.Data> device) {
//        try {
//            String path = "/data/data/" + ctx.getPackageName()
//                    + "/doctorList.ser";
//            File f = new File(path);
//            if (f.exists()) {
//                f.delete();
//            }
//            FileOutputStream fileOut = new FileOutputStream(path);
//            ObjectOutputStream out = new ObjectOutputStream(fileOut);
//            out.writeObject(device);
//            out.close();
//            fileOut.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    public User readUser() {
//        String path = "/data/data/" + ctx.getPackageName()
//                + "/user.ser";
//        File f = new File(path);
//        User device = new User();
//        if (f.exists()) {
//            try {
//                System.gc();
//                FileInputStream fileIn = new FileInputStream(path);
//                ObjectInputStream in = new ObjectInputStream(fileIn);
//                device = (User) in.readObject();
//                in.close();
//                fileIn.close();
//            } catch (StreamCorruptedException e) {
//                e.printStackTrace();
//            } catch (OptionalDataException e) {
//                e.printStackTrace();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return device;
//    }

//    public void writeUser(User device) {
//        try {
//            String path = "/data/data/" + ctx.getPackageName()
//                    + "/user.ser";
//            File f = new File(path);
//            if (f.exists()) {
//                f.delete();
//            }
//            FileOutputStream fileOut = new FileOutputStream(path);
//            ObjectOutputStream out = new ObjectOutputStream(fileOut);
//            out.writeObject(device);
//            out.close();
//            fileOut.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static void shareText(Context d, String subject, String text) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, text);
        d.startActivity(Intent.createChooser(sharingIntent, "Share with"));
    }

    public static byte[] encode(String txt, int bit) {
        int length = txt.length();
        float tmpRet1 = 0, tmpRet2 = 0;
        if (bit == 6) {
            tmpRet1 = 3.0f;
            tmpRet2 = 4.0f;
        } else if (bit == 5) {
            tmpRet1 = 5.0f;
            tmpRet2 = 8.0f;
        }
        byte encoded[] = new byte[(int) (tmpRet1 * Math.ceil(length / tmpRet2))];
        char str[] = new char[length];
        txt.getChars(0, length, str, 0);
        int chaVal = 0;
        String temp;
        String strBinary = new String("");
        for (int i = 0; i < length; i++) {
            temp = Integer.toBinaryString(toValue(str[i]));
            while (temp.length() % bit != 0) {
                temp = "0" + temp;
            }
            strBinary = strBinary + temp;
        }
        while (strBinary.length() % 8 != 0) {
            strBinary = strBinary + "0";
        }
        Integer tempInt = new Integer(0);
        for (int i = 0; i < strBinary.length(); i = i + 8) {
            tempInt = tempInt.valueOf(strBinary.substring(i, i + 8), 2);
            encoded[i / 8] = tempInt.byteValue();
        }
        return encoded;
    }

    public static String decode(byte[] encoded, int bit) {
        String strTemp = new String("");
        String strBinary = new String("");
        String strText = new String("");
        Integer tempInt = new Integer(0);
        int intTemp = 0;
        for (int i = 0; i < encoded.length; i++) {
            if (encoded[i] < 0) {
                intTemp = (int) encoded[i] + 256;
            } else
                intTemp = (int) encoded[i];
            strTemp = Integer.toBinaryString(intTemp);
            while (strTemp.length() % 8 != 0) {
                strTemp = "0" + strTemp;
            }
            strBinary = strBinary + strTemp;
        }
        for (int i = 0; i < strBinary.length(); i = i + bit) {
            tempInt = tempInt.valueOf(strBinary.substring(i, i + bit), 2);
            strText = strText + toChar(tempInt.intValue());
        }
        return strText;
    }

    public static int toValue(char ch) {
        int chaVal = 0;
        switch (ch) {
            case ' ':
                chaVal = 0;
                break;
            case 'a':
                chaVal = 1;
                break;
            case 'b':
                chaVal = 2;
                break;
            case 'c':
                chaVal = 3;
                break;
            case 'd':
                chaVal = 4;
                break;
            case 'e':
                chaVal = 5;
                break;
            case 'f':
                chaVal = 6;
                break;
            case 'g':
                chaVal = 7;
                break;
            case 'h':
                chaVal = 8;
                break;
            case 'i':
                chaVal = 9;
                break;
            case 'j':
                chaVal = 10;
                break;
            case 'k':
                chaVal = 11;
                break;
            case 'l':
                chaVal = 12;
                break;
            case 'm':
                chaVal = 13;
                break;
            case 'n':
                chaVal = 14;
                break;
            case 'o':
                chaVal = 15;
                break;
            case 'p':
                chaVal = 16;
                break;
            case 'q':
                chaVal = 17;
                break;
            case 'r':
                chaVal = 18;
                break;
            case 's':
                chaVal = 19;
                break;
            case 't':
                chaVal = 20;
                break;
            case 'u':
                chaVal = 21;
                break;
            case 'v':
                chaVal = 22;
                break;
            case 'w':
                chaVal = 23;
                break;
            case 'x':
                chaVal = 24;
                break;
            case 'y':
                chaVal = 25;
                break;
            case 'z':
                chaVal = 26;
                break;
            case '.':
                chaVal = 27;
                break;
            case '*':
                chaVal = 28;
                break;
            case ',':
                chaVal = 29;
                break;
            case '\\':
                chaVal = 30;
                break;
            case '2':
                chaVal = 31;
                break;
            case 'A':
                chaVal = 32;
                break;
            case 'B':
                chaVal = 33;
                break;
            case 'C':
                chaVal = 34;
                break;
            case 'D':
                chaVal = 35;
                break;
            case 'E':
                chaVal = 36;
                break;
            case 'F':
                chaVal = 37;
                break;
            case 'G':
                chaVal = 38;
                break;
            case 'H':
                chaVal = 39;
                break;
            case 'I':
                chaVal = 40;
                break;
            case 'J':
                chaVal = 41;
                break;
            case 'K':
                chaVal = 42;
                break;
            case 'L':
                chaVal = 43;
                break;
            case 'M':
                chaVal = 44;
                break;
            case 'N':
                chaVal = 45;
                break;
            case 'O':
                chaVal = 46;
                break;
            case 'P':
                chaVal = 47;
                break;
            case 'Q':
                chaVal = 48;
                break;
            case 'R':
                chaVal = 49;
                break;
            case 'S':
                chaVal = 50;
                break;
            case 'T':
                chaVal = 51;
                break;
            case 'U':
                chaVal = 52;
                break;
            case 'V':
                chaVal = 53;
                break;
            case 'W':
                chaVal = 54;
                break;
            case '0':
                chaVal = 55;
                break;
            case '1':
                chaVal = 56;
                break;
            case '3':
                chaVal = 57;
                break;
            case '4':
                chaVal = 58;
                break;
            case '5':
                chaVal = 59;
                break;
            case '6':
                chaVal = 60;
                break;
            case '7':
                chaVal = 61;
                break;
            case '8':
                chaVal = 62;
                break;
            case '9':
                chaVal = 63;
                break;
            default:
                chaVal = 0;
        }
        return chaVal;
    }

    static char toChar(int val) {
        char ch = ' ';
        switch (val) {
            case 0:
                ch = ' ';
                break;
            case 1:
                ch = 'a';
                break;
            case 2:
                ch = 'b';
                break;
            case 3:
                ch = 'c';
                break;
            case 4:
                ch = 'd';
                break;
            case 5:
                ch = 'e';
                break;
            case 6:
                ch = 'f';
                break;
            case 7:
                ch = 'g';
                break;
            case 8:
                ch = 'h';
                break;
            case 9:
                ch = 'i';
                break;
            case 10:
                ch = 'j';
                break;
            case 11:
                ch = 'k';
                break;
            case 12:
                ch = 'l';
                break;
            case 13:
                ch = 'm';
                break;
            case 14:
                ch = 'n';
                break;
            case 15:
                ch = 'o';
                break;
            case 16:
                ch = 'p';
                break;
            case 17:
                ch = 'q';
                break;
            case 18:
                ch = 'r';
                break;
            case 19:
                ch = 's';
                break;
            case 20:
                ch = 't';
                break;
            case 21:
                ch = 'u';
                break;
            case 22:
                ch = 'v';
                break;
            case 23:
                ch = 'w';
                break;
            case 24:
                ch = 'x';
                break;
            case 25:
                ch = 'y';
                break;
            case 26:
                ch = 'z';
                break;
            case 27:
                ch = '.';
                break;
            case 28:
                ch = '*';
                break;
            case 29:
                ch = ',';
                break;
            case 30:
                ch = '\\';
                break;
            case 31:
                ch = '2';
                break;
            case 32:
                ch = 'A';
                break;
            case 33:
                ch = 'B';
                break;
            case 34:
                ch = 'C';
                break;
            case 35:
                ch = 'D';
                break;
            case 36:
                ch = 'E';
                break;
            case 37:
                ch = 'F';
                break;
            case 38:
                ch = 'G';
                break;
            case 39:
                ch = 'H';
                break;
            case 40:
                ch = 'I';
                break;
            case 41:
                ch = 'J';
                break;
            case 42:
                ch = 'K';
                break;
            case 43:
                ch = 'L';
                break;
            case 44:
                ch = 'M';
                break;
            case 45:
                ch = 'N';
                break;
            case 46:
                ch = 'O';
                break;
            case 47:
                ch = 'P';
                break;
            case 48:
                ch = 'Q';
                break;
            case 49:
                ch = 'R';
                break;
            case 50:
                ch = 'S';
                break;
            case 51:
                ch = 'T';
                break;
            case 52:
                ch = 'U';
                break;
            case 53:
                ch = 'V';
                break;
            case 54:
                ch = 'W';
                break;
            case 55:
                ch = '0';
                break;
            case 56:
                ch = '1';
                break;
            case 57:
                ch = '3';
                break;
            case 58:
                ch = '4';
                break;
            case 59:
                ch = '5';
                break;
            case 60:
                ch = '6';
                break;
            case 61:
                ch = '7';
                break;
            case 62:
                ch = '8';
                break;
            case 63:
                ch = '9';
                break;
            default:
                ch = ' ';
        }
        return ch;
    }
}
