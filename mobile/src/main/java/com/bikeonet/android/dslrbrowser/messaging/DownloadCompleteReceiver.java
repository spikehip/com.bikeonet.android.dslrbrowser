package com.bikeonet.android.dslrbrowser.messaging;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bikeonet.android.dslrbrowser.content.PhotoItem;
import com.bikeonet.android.dslrbrowser.content.PhotoList;
import com.bikeonet.android.dslrbrowser.util.LocationStore;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.exifinterface.media.ExifInterface;

public class DownloadCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            long idDownload=intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Cursor result = manager.query(new DownloadManager.Query().setFilterById(idDownload));
            result.moveToFirst();
            int status = result.getInt(result.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_SUCCESSFUL:
                    boolean insertGPS = prefs.getBoolean("insert_gps", false);
                    Uri contentUri = Uri.parse(result.getString(result.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
                    String fileName = result.getString(result.getColumnIndex(DownloadManager.COLUMN_TITLE));
                    String mimeType = result.getString(result.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
                    try {
                        String absoluteFilePath = processDownload(context, contentUri, fileName);
                        if (insertGPS) {
                            insertGPSCoordinatesToExifDataOf(context, absoluteFilePath);
                        }
                        MediaScannerConnection.scanFile(context, new String[]{absoluteFilePath},
                                new String[]{mimeType},
                                (path, uri) -> Log.d(this.getClass().getCanonicalName(), "Media scanned: "+path + uri.toString()));
                        context.getContentResolver().delete(contentUri, null, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                case DownloadManager.STATUS_FAILED:
                case DownloadManager.STATUS_PAUSED:
                case DownloadManager.STATUS_PENDING:
                case DownloadManager.STATUS_RUNNING:
                default:
                    break;
            }
        }
    }

    private String processDownload(Context context, Uri contentUri, String fileName) throws IOException {
        if ( checkSDCardAvailable() ) {
            File pdir = context.getExternalMediaDirs()[0];
            File dcim = new File(pdir.getAbsolutePath() + "/"+getDownloadDirectory(context));
            if ( createDir(dcim)) {
                File outputFile = new File(dcim.getAbsolutePath() + "/" + fileName);
                InputStream is = context.getContentResolver().openInputStream(contentUri);
                BufferedInputStream bis = new BufferedInputStream(is);
                int current = 0;
                FileOutputStream fos = new FileOutputStream(outputFile);
                byte[] buffer = new byte[1000];
                while((current = bis.read(buffer))!=-1) {
                    fos.write(buffer,0,current);
                }
                fos.close();
                Log.d(this.getClass().getName(), "Finishing download of "+contentUri.toString()+" into "+outputFile.getAbsolutePath());
                return outputFile.getAbsolutePath();
            }
        }

        return "";
    }

    @SuppressLint("SimpleDateFormat")
    private String getDownloadDirectory(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean downloadToAlbum = prefs.getBoolean("download_to_album", false);

        if (downloadToAlbum) {
            Date today = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            return format.format(today);
        }
        else {
            return "";
        }
    }

    private static boolean createDir(File dir) {
        if ( dir.exists()) {
            return true;
        }

        return dir.mkdirs();
    }

    private boolean checkSDCardAvailable() {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        return mExternalStorageAvailable && mExternalStorageWriteable;
    }

    private void insertGPSCoordinatesToExifDataOf(Context context, String fileName) throws IOException {
            ExifInterface exifInterface = new ExifInterface(fileName);
            float[] latlong = new float[2];
            if (!exifInterface.getLatLong(latlong)) {
                // TODO: insert gps stuff here
                if (LocationStore.getInstance() != null) {
                    Location location = LocationStore
                            .getInstance().getLastLocation();

                    if (location != null) {
                        String lat = decimalToDMS(location
                                .getLatitude());
                        String lon = decimalToDMS(location
                                .getLongitude());
                        String latRef = location.getLatitude() > 0 ? "N"
                                : "S";
                        String lonRef = location.getLongitude() > 0 ? "E"
                                : "W";

                        exifInterface.setAttribute(
                                ExifInterface.TAG_GPS_LATITUDE, lat);
                        exifInterface.setAttribute(
                                ExifInterface.TAG_GPS_LATITUDE_REF,
                                latRef);
                        exifInterface.setAttribute(
                                ExifInterface.TAG_GPS_LONGITUDE, lon);
                        exifInterface.setAttribute(
                                ExifInterface.TAG_GPS_LONGITUDE_REF,
                                lonRef);

                    }
                }

            }

            exifInterface.saveAttributes();
    }

    String decimalToDMS(double coord) {
        String output, degrees, minutes, seconds;

        // gets the modulus the coordinate devided by one (MOD1).
        // in other words gets all the numbers after the decimal point.
        // e.g mod = 87.728056 % 1 == 0.728056
        //
        // next get the integer part of the coord. On other words the whole
        // number part.
        // e.g intPart = 87

        double mod = coord % 1;
        int intPart = (int) coord;

        // set degrees to the value of intPart
        // e.g degrees = "87"

        degrees = String.valueOf(intPart);

        // next times the MOD1 of degrees by 60 so we can find the integer part
        // for minutes.
        // get the MOD1 of the new coord to find the numbers after the decimal
        // point.
        // e.g coord = 0.728056 * 60 == 43.68336
        // mod = 43.68336 % 1 == 0.68336
        //
        // next get the value of the integer part of the coord.
        // e.g intPart = 43

        coord = mod * 60;
        mod = coord % 1;
        intPart = (int) coord;

        // set minutes to the value of intPart.
        // e.g minutes = "43"
        minutes = String.valueOf(intPart);

        // do the same again for minutes
        // e.g coord = 0.68336 * 60 == 40.0016
        // e.g intPart = 40
        coord = mod * 60;
        intPart = (int) coord;

        // set seconds to the value of intPart.
        // e.g seconds = "40"
        seconds = String.valueOf(intPart);

        // I used this format for android but you can change it
        // to return in whatever format you like
        // e.g output = "87/1,43/1,40/1"
        output = degrees + "/1," + minutes + "/1," + seconds + "/1";

        // Standard output of D°M′S″
        // output = degrees + "°" + minutes + "'" + seconds + "\"";

        return output;
    }

}