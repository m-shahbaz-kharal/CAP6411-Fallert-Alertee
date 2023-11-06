package com.cap6411.fallert_alertee.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
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
            int length = string.getBytes().length;
            byte[] lenBytes = ByteBuffer.allocate(4).putInt(length).array();
            socket.getOutputStream().write(lenBytes);
            socket.getOutputStream().write(string.getBytes());
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
            byte[] lenBytes = new byte[4];
            socket.getInputStream().read(lenBytes);
            byte[] buffer = new byte[ByteBuffer.wrap(lenBytes).getInt()];
            int read = socket.getInputStream().read(buffer);
            return new String(buffer);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (Exception ignore) {}
            return null;
        }
    }

    public static String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public static Bitmap StringToBitMap(String base64EncodedString){
        try {
            byte [] encodeByte=Base64.decode(base64EncodedString,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
