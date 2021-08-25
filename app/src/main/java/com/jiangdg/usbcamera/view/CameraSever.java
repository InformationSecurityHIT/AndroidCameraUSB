package com.jiangdg.usbcamera.view;

import android.app.Activity;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraSever {
    private Executor executor = Executors.newCachedThreadPool();//线程池;
    private String host = "172.20.0.185";
    private byte[] imageInByte;
    private Socket clientSocket;
    private SendImageRunnable sendImageRunnable;
    private Activity superActivity;

    public CameraSever(byte[] imageInByte, Activity superActivity) {
        this.imageInByte=imageInByte;
        this.superActivity=superActivity;
    }

    public void beginListen(){
        try {
            //建立socket
            clientSocket = new Socket("172.20.72.49", 8888);//ip地址换成运行Camerasever.java程序的电脑的ip
            Toast.makeText(superActivity, "连接成功" , Toast.LENGTH_SHORT).show();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendImageRunnable=new SendImageRunnable(clientSocket, imageInByte);
        executor.execute(sendImageRunnable);
    }

    private class SendImageRunnable implements Runnable {
        Socket writeImageSocket;
        byte[] imageInByte;

        public SendImageRunnable(Socket socket, byte[] imageInByte) {
            this.writeImageSocket = socket;
            this.imageInByte = imageInByte;
        }


        public void run() {
            DataOutputStream dos = null;
            try {
                dos = new DataOutputStream(writeImageSocket.getOutputStream());
                while (writeImageSocket.isConnected()) {
                    dos.writeInt(imageInByte.length);
                    dos.write(imageInByte, 0, imageInByte.length);
                    dos.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (dos != null) dos.close();
                    writeImageSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
