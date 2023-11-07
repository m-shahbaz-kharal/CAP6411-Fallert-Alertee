package com.cap6411.fallert_alertee.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

public class StringNetwork {
    public static Socket establishConnection(String ipAddress, int port) {
        try {
            return new Socket(ipAddress, port);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Socket bindConnection(String ipAddress, int port) {
        try {
            Socket socket = new Socket();
            socket.bind(new InetSocketAddress(ipAddress, port));
            return socket;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void closeConnection(Socket socket) {
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean sendString(Socket socket, String string) {
        try {
            int string_len = string.length();
            byte[] intBytes = ByteBuffer.allocate(4).putInt(string_len).array();
            socket.getOutputStream().write(intBytes);
            socket.getOutputStream().write(string.getBytes());
            socket.getOutputStream().flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try {socket.close();}
            catch (Exception ignore) {}
            return false;
        }
    }
    public static String receiveString(Socket socket) {
        try {
            InputStream inputStream = socket.getInputStream();
            byte[] intBytes = new byte[4];
            inputStream.read(intBytes);
            int string_len = ByteBuffer.wrap(intBytes).getInt();
            byte[] stringBytes = new byte[string_len];
            int read_bytes = 0;
            while (read_bytes < string_len) {
                read_bytes += inputStream.read(stringBytes, read_bytes, string_len - read_bytes);
            }
            return new String(stringBytes);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (Exception ignore) {}
            return null;
        }
    }

    public static String bitmapToSTring(Bitmap bitmap){
        ByteArrayOutputStream baos = new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public static Bitmap stringToBitmap(String base64EncodedString){
        try {
            byte [] encodeByte=Base64.decode(base64EncodedString,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
