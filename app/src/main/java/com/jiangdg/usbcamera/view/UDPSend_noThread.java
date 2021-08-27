package com.jiangdg.usbcamera.view;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @description: TODO
 * @author: 91657
 * @modified By: 91657
 * @date: Created in 2021/7/17 15:59
 * @version:v1.0
 */
public class UDPSend_noThread{

    private byte[] data;
    private String ip = null;
    private int port = 0;

    public UDPSend_noThread(byte[] data, String ip, int port){
        this.data=data;
        this.ip = ip;
        this.port = port;
    }

    public void setData(byte[] data){
        this.data=data;
    }
    //            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("100.66.120.160"), 6666);//手机IP
    //DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("192.168.43.150"), 6666);//电脑IP

    public void sendData() {
        DatagramSocket socket = null; //随机端口号
        try {
            socket = new DatagramSocket();
            //DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("172.20.72.49"), 6666);

//            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("100.66.120.160"), 6666);//手机IP
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(ip), port);//电脑IP
            socket.send(packet);
            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}