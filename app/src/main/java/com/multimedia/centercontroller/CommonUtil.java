
package com.multimedia.centercontroller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class CommonUtil {
    public static final String SEATNO = "seatno";
    public static final String ROW_COLUMN = "row_column";
    public static final String CONFID_IP = "config_ip";
    public static final String IP_PRIFEX = "prefix";
    private static SharedPreferences mSharedPreferences;
    private static final CommonUtil INSTANCE = new CommonUtil();
    private static Context sContext;
    private static final String INTERNAL_CONFIG_FILE_NAME = "config.cg";
    public static final String CHARSET_UTF_8 = "UTF-8";
    public static final String CHARSET_GBK = "GBK";
    public static final String[] sSeatPrefix = {
            "A", "B", "c", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
            "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
    };
    public static final String[] sSeatsuffix = {
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "15", "16", "17",
            "18", "19", "20"
    };

    public static final String[] sSeatRow = {
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"
    };
    public static final String[] sSeatColumn = {
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"
    };

    private CommonUtil() {

    }

    public static String getIpPrefix() {
        return mSharedPreferences.getString(IP_PRIFEX, "192.168.1");
    }
    public static void setIpPrefix(String prefix) {
        Editor editor = mSharedPreferences.edit();
        editor.putString(IP_PRIFEX, prefix);
        editor.commit();
    }

    public static boolean isConfigIP() {
        return mSharedPreferences.getBoolean(CONFID_IP, false);
    }

    public static void configIP(boolean flag) {
        Editor editor = mSharedPreferences.edit();
        editor.putBoolean(CONFID_IP, flag);
        editor.commit();
    }

    public static String getSeatNo() {
        return mSharedPreferences.getString(SEATNO, "teacher");
    }

    public static String getRowColumn() {
        return mSharedPreferences.getString(ROW_COLUMN, "8,8");
    }

    public static synchronized CommonUtil getInstance(Context context) {
        if (sContext == null) {
            sContext = context;
            mSharedPreferences = sContext.getSharedPreferences(
                    INTERNAL_CONFIG_FILE_NAME, Context.MODE_PRIVATE);
        }
        return INSTANCE;
    }

    public static String getValueFromSharePreferences(String key) {
        return mSharedPreferences.getString(key, "");
    }

    public static void reStoreValueIntoSharePreferences(String key, String value) {
        Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static boolean checkSdcard() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public static void showDialog(Context context, int resId, int msgId) {
        new AlertDialog.Builder(context).setTitle(resId).setMessage(msgId)
                .setPositiveButton(R.string.ok, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                }).create().show();
    }

    public static void showCustomDialog(Context context, View view,
            String title, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context).setView(view).setTitle(title)
                .setPositiveButton(R.string.ok, listener).create().show();
    }

    public static void showWarnDialog(Context context, String title,
            String msg, OnClickListener listener) {
        new AlertDialog.Builder(context).setTitle(title).setMessage(msg)
                .setPositiveButton(R.string.ok, listener)
                .setNegativeButton(R.string.cancel, null).create().show();
    }

    public static void showSelectedDialog(Context context,
            OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setItems(new String[] {
                        "查看更多", "返回"
                }, listener).create()
                .show();
    }

    public static String convertStreamToString(InputStream is, String charset) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is, charset),
                    512 * 1024);
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            Log.e("DataProvier convertStreamToString", e.getLocalizedMessage(),
                    e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static String getData(String url, String charSet) throws
            IOException {
        String result = "";
        HttpGet httpGet = new HttpGet(url);
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            String charset = EntityUtils.getContentCharSet(httpEntity);
            Log.d("Charset", "charset= " + charset);
            if (httpEntity != null) {
                InputStream inputStream = httpEntity.getContent();
                result = convertStreamToString(inputStream, charSet);

            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw e;
        } finally {
            httpClient.getConnectionManager().shutdown();
            httpResponse = null;
        }
        return result;

    }

    public static String convertTime(long mis) {
        int time = (int) (mis / 1000);
        StringBuilder sb = new StringBuilder();
        int leftseconds = time % 60;
        int minutes = time / 60;
        int leftMinutes = minutes % 60;
        int hour = minutes / 60;

        if (hour < 10) {
            sb.append("0").append(hour).append(":");
        } else {
            sb.append("0").append(hour).append(":");
        }
        if (minutes < 10) {
            sb.append("0").append(leftMinutes).append(":");
        } else {
            sb.append(leftMinutes).append(":");
        }
        if (leftseconds < 10) {
            sb.append("0").append(leftseconds);
        } else {
            sb.append(leftseconds);
        }

        return sb.toString();
    }

    public static String getFileData(String url)
            throws IOException {
        String result = "";
        HttpGet httpGet = new HttpGet(url);
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                InputStream inputStream = httpEntity.getContent();
                result = convertStreamToString(inputStream, CHARSET_GBK);

            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw e;
        } finally {
            httpClient.getConnectionManager().shutdown();
            httpResponse = null;
        }
        return result;

    }

    public static MediaMessage parseMessage(String content) {
        MediaMessage message = null;
        try {
            JSONObject jsonStr = new JSONObject(content);
            String tpye = jsonStr.getString("type");
            String receiver = jsonStr.getString("receiver");
            String mode = jsonStr.getString("mode");
            String group = jsonStr.getString("group");
            String command = jsonStr.getString("command");
            String param = jsonStr.getString("param");

            message = new MediaMessage(tpye, receiver, mode, command, group, param);
            Log.d("message", "message" + message.toString());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return message;
    }
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("WifiPreference IpAddress", ex.toString());
        }
        return null;
    }
}
