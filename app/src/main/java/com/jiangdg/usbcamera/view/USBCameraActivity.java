package com.jiangdg.usbcamera.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;
import com.jiangdg.usbcamera.R;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jiangdg.usbcamera.UVCCameraHelper;
import com.jiangdg.usbcamera.application.MyApplication;
import com.jiangdg.usbcamera.utils.FileUtils;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.encoder.RecordParams;
import com.serenegiant.usb.widget.CameraViewInterface;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * UVCCamera use demo
 * <p>
 * Created by jiangdongguo on 2017/9/30.
 */

public class USBCameraActivity extends AppCompatActivity implements CameraDialog.CameraDialogParent, CameraViewInterface.Callback {

    private static final String TAG = "Debug";
    public static final String IP1 = "10.236.13.59";
    public static final String IP2 = "192.168.43.47";
    public static final String IP_computer = "192.168.43.29";

    @BindView(R.id.camera_view)
    public View mTextureView;
    @BindView(R.id.toolbar)
    public Toolbar mToolbar;
    @BindView(R.id.seekbar_brightness)
    public SeekBar mSeekBrightness;
    @BindView(R.id.seekbar_contrast)
    public SeekBar mSeekContrast;
    @BindView(R.id.switch_rec_voice)
    public Switch mSwitchVoice;

    private UVCCameraHelper mCameraHelper;
    private CameraViewInterface mUVCCameraView;
    private AlertDialog mDialog;

    private boolean isRequest;
    private boolean isPreview;

    private UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {

        @Override
        public void onAttachDev(UsbDevice device) {
            // request open permission
            if (!isRequest) {
                isRequest = true;
                if (mCameraHelper != null) {
                    mCameraHelper.requestPermission(1);//开启第几个设备
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            // close camera
            if (isRequest) {
                isRequest = false;
                mCameraHelper.closeCamera();
                showShortMsg(device.getDeviceName() + " is out");
            }
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            if (!isConnected) {
                showShortMsg("fail to connect,please check resolution params");
                isPreview = false;
            } else {
                isPreview = true;
                showShortMsg("connecting");
                // initialize seekbar
                // need to wait UVCCamera initialize over
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Looper.prepare();
                        if (mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                            mSeekBrightness.setProgress(mCameraHelper.getModelValue(UVCCameraHelper.MODE_BRIGHTNESS));
                            mSeekContrast.setProgress(mCameraHelper.getModelValue(UVCCameraHelper.MODE_CONTRAST));
                        }
                        Looper.loop();
                    }
                }).start();
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {
            showShortMsg("disconnecting");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usbcamera);
        ButterKnife.bind(this);
        initView();

        // step.1 initialize UVCCameraHelper
        mUVCCameraView = (CameraViewInterface) mTextureView;
        mUVCCameraView.setCallback(this);
        mCameraHelper = UVCCameraHelper.getInstance();
        mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_MJPEG);
        mCameraHelper.initUSBMonitor(this, mUVCCameraView, listener);

        mCameraHelper.setOnPreviewFrameListener(new AbstractUVCCameraHandler.OnPreViewResultListener() {
            @Override
            public void onPreviewResult(byte[] nv21Yuv) {
                Log.d(TAG, "onPreviewResult: " + nv21Yuv.length);
            }
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

    }

    private void initView() {
        setSupportActionBar(mToolbar);

        mSeekBrightness.setMax(100);
        mSeekBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    mCameraHelper.setModelValue(UVCCameraHelper.MODE_BRIGHTNESS, progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSeekContrast.setMax(100);
        mSeekContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    mCameraHelper.setModelValue(UVCCameraHelper.MODE_CONTRAST, progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // step.2 register USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.registerUSB();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // step.3 unregister USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.unregisterUSB();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toobar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_takepic:
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("sorry,camera open failed");
                    return super.onOptionsItemSelected(item);
                }
                String picPath = UVCCameraHelper.ROOT_PATH + MyApplication.DIRECTORY_NAME + "/images/"
                    + System.currentTimeMillis() + UVCCameraHelper.SUFFIX_JPEG;

                mCameraHelper.capturePicture(picPath, new AbstractUVCCameraHandler.OnCaptureListener() {
                    @Override
                    public void onCaptureResult(String path) {
                        if (TextUtils.isEmpty(path)) {
                            return;
                        }
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(USBCameraActivity.this, "save path:" + path, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                break;
            case R.id.sendIMG:          //向手机发送---------------------------------^-^
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("sorry,camera open failed");
                    return super.onOptionsItemSelected(item);
                }
                //绑手机IP
                UDPSend_noThread udpSend = new UDPSend_noThread(new byte[0], IP1, 6666);
                UDPSend_noThread udpSend1 = new UDPSend_noThread(new byte[0], IP2, 6667);

//                new Thread(udpSend).start();
//                new Thread(udpSend1).start();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            String picPath1 = UVCCameraHelper.ROOT_PATH + MyApplication.DIRECTORY_NAME + "/images/"
                                + "1" + UVCCameraHelper.SUFFIX_JPEG;
                            String picPath2 = UVCCameraHelper.ROOT_PATH + MyApplication.DIRECTORY_NAME + "/images/"
                                + "2.jpg";
                            mCameraHelper.capturePicture(picPath1, new AbstractUVCCameraHandler.OnCaptureListener() {
                                @Override
                                public void onCaptureResult(String path) {
                                    if (TextUtils.isEmpty(path)) {
                                        return;
                                    }
                                }
                            });
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                            }
                            Bitmap image = ScaleImage.compressBitmapFromPath(picPath1, 400, 300);
                            if (image!=null){
                                ScaleImage.saveBitmap(image, picPath2);
                                //绑手机IP
//                            UDPSend udpSend = new UDPSend(imageToByte(picPath2), IP1, 6666);
//                            UDPSend udpSend1 = new UDPSend(imageToByte(picPath2), IP2, 6667);
                                udpSend.setData(imageToByte(picPath2));
                                udpSend.sendData();
                                udpSend1.setData(imageToByte(picPath2));
                                udpSend1.sendData();
                            }
//                            new Thread(udpSend).start();
//                            new Thread(udpSend1).start();
//                            mCameraHelper.capturePicture(picPath1, new AbstractUVCCameraHandler.OnCaptureListener() {
//                                @OverrideString picPath1 = UVCCameraHelper.ROOT_PATH + MyApplication.DIRECTORY_NAME + "/images/"
//                                    + "1"+ UVCCameraHelper.SUFFIX_JPEG;
//                            String picPath2 = UVCCameraHelper.ROOT_PATH + MyApplication.DIRECTORY_NAME + "/images/"
//                                    + "2.jpg";
//                            mCameraHelper.capturePicture(picPath1, new AbstractUVCCameraHandler.OnCaptureListener() {
//                                @Override
//                                public void onCaptureResult(String path) {
//                                    if (TextUtils.isEmpty(path)) {
//                                        return;
//                                    }
//                                }
//                            });
//                            try{Thread.sleep(500);}catch (InterruptedException e){}
//                            Bitmap image = ScaleImage.compressBitmapFromPath(picPath1,400,300);
//                            ScaleImage.saveBitmap(image,picPath2);
//                            //绑手机IP
//                            UDPSend udpSend=new UDPSend(imageToByte(picPath2),"100.99.48.73",6666);
//                                public void onCaptureResult(String path) {
//                                    if (TextUtils.isEmpty(path)) {
//                                        return;
//                                    }
//                                    new Handler(getMainLooper()).post(new Runnable() {
//                                        @Override
//                                        public void run() {
////                                            Toast.makeText(USBCameraActivity.this, "save path:" + path, Toast.LENGTH_SHORT).show();
//                                            byte[] data = imageToByte(picPath1);
////                                            //TODO　sxz TCP传输
////                                            CameraClient cameraClient = new CameraClient(data, USBCameraActivity.this);
////                                            cameraClient.beginListen();
//                                            //TODO  sxz    udp传输
//                                            UDPSend udpSend=new UDPSend(data);
//                                        }
//                                    });
//                                }
//                            });
                        }
                    }
                }).start();
                break;
            case R.id.menu_recording:
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("sorry,camera open failed");
                    return super.onOptionsItemSelected(item);
                }
                if (!mCameraHelper.isPushing()) {
                    String videoPath = UVCCameraHelper.ROOT_PATH + MyApplication.DIRECTORY_NAME + "/videos/" + System.currentTimeMillis()
                        + UVCCameraHelper.SUFFIX_MP4;

//                    FileUtils.createfile(FileUtils.ROOT_PATH + "test666.h264");
                    // if you want to record,please create RecordParams like this
                    RecordParams params = new RecordParams();
                    params.setRecordPath(videoPath);
                    params.setRecordDuration(0);                        // auto divide saved,default 0 means not divided
                    params.setVoiceClose(mSwitchVoice.isChecked());    // is close voice

                    params.setSupportOverlay(true); // overlay only support armeabi-v7a & arm64-v8a
                    mCameraHelper.startPusher(params, new AbstractUVCCameraHandler.OnEncodeResultListener() {
                        @Override
                        public void onEncodeResult(byte[] data, int offset, int length, long timestamp, int type) {
                            // type = 1,h264 video stream
                            if (type == 1) {
                                FileUtils.putFileStream(data, offset, length);
                            }
                            // type = 0,aac audio stream
                            if (type == 0) {

                            }
                        }

                        @Override
                        public void onRecordResult(String videoPath) {
                            if (TextUtils.isEmpty(videoPath)) {
                                return;
                            }
                            new Handler(getMainLooper()).post(() -> Toast.makeText(USBCameraActivity.this, "save videoPath:" + videoPath, Toast.LENGTH_SHORT).show());
                        }
                    });
                    // if you only want to push stream,please call like this
                    // mCameraHelper.startPusher(listener);
                    showShortMsg("start record...");
                    mSwitchVoice.setEnabled(false);
                } else {
                    FileUtils.releaseFile();
                    mCameraHelper.stopPusher();
                    showShortMsg("stop record...");
                    mSwitchVoice.setEnabled(true);
                }
                break;
            case R.id.menu_resolution:                  //向电脑发送---------------------------------^-^
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("sorry,camera open failed");
                    return super.onOptionsItemSelected(item);
                }
                UDPSend_noThread udpSend_computer = new UDPSend_noThread(new byte[0], IP_computer, 7777);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            String picPath1 = UVCCameraHelper.ROOT_PATH + MyApplication.DIRECTORY_NAME + "/images/"
                                + "1" + UVCCameraHelper.SUFFIX_JPEG;
                            String picPath2 = UVCCameraHelper.ROOT_PATH + MyApplication.DIRECTORY_NAME + "/images/"
                                + "2.jpg";
                            mCameraHelper.capturePicture(picPath1, new AbstractUVCCameraHandler.OnCaptureListener() {
                                @Override
                                public void onCaptureResult(String path) {
                                    if (TextUtils.isEmpty(path)) {
                                        return;
                                    }
                                }
                            });
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                Log.d("","",e);
                            }
                            Bitmap image = ScaleImage.compressBitmapFromPath(picPath1, 400, 300);
                            if (image!=null){
                                ScaleImage.saveBitmap(image, picPath2);
                                //绑电脑IP
                                udpSend_computer.setData(imageToByte(picPath2));
                                udpSend_computer.sendData();
                            }
                        }
                    }
                }).start();

                break;
            case R.id.menu_focus:
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("sorry,camera open failed");
                    return super.onOptionsItemSelected(item);
                }
                mCameraHelper.startCameraFoucs();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * @param path 图片路径
     * @return
     * @将图片文件转化为字节数组
     */

    public static byte[] imageToByte(String path) {
        // 读取图片字节数组
        try {
            InputStream in = new FileInputStream(path);
            byte[] buffer = new byte[in.available()];
            int len = -1;
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            while ((len = in.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            byte[] data = outStream.toByteArray();
            outStream.close();
            in.close();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    private class CameraClient {

        private Executor executor = Executors.newCachedThreadPool();//线程池;
        private byte[] imageInByte;
        private Socket clientSocket;
        private SendImageRunnable sendImageRunnable;
        private Activity superActivity;

        public CameraClient(byte[] imageInByte, Activity superActivity) {
            this.imageInByte = imageInByte;
            this.superActivity = superActivity;
            try {
                //建立socket
                clientSocket = new Socket("172.20.0.185 ", 8888);//ip地址换成运行Camerasever.java程序的电脑的ip
//                Toast.makeText(superActivity, "连接成功", Toast.LENGTH_SHORT).show();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setImageInByte(byte[] imageInByte) {
            this.imageInByte = imageInByte;
        }

        public void beginListen() {
            sendImageRunnable = new SendImageRunnable(clientSocket, imageInByte);
            executor.execute(sendImageRunnable);

        }

        private class SendImageRunnable implements Runnable {

            Socket writeImageSocket;
            byte[] imageInByte;

            public SendImageRunnable(Socket socket, byte[] imageInByte) {
                this.writeImageSocket = socket;
                this.imageInByte = imageInByte;
//                Toast.makeText(USBCameraActivity.this, "图片转换成功" + imageInByte.length, Toast.LENGTH_SHORT).show();
            }


            public void run() {
                DataOutputStream dos = null;
                try {
                    dos = new DataOutputStream(writeImageSocket.getOutputStream());
                    while (!writeImageSocket.isClosed()) {
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
                        if (dos != null) {
                            dos.close();
                        }
//                        writeImageSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private void showResolutionListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(USBCameraActivity.this);
        View rootView = LayoutInflater.from(USBCameraActivity.this).inflate(R.layout.layout_dialog_list, null);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_dialog);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(USBCameraActivity.this, android.R.layout.simple_list_item_1, getResolutionList());
        if (adapter != null) {
            listView.setAdapter(adapter);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    return;
                }
                final String resolution = (String) adapterView.getItemAtPosition(position);
                String[] tmp = resolution.split("x");
                if (tmp != null && tmp.length >= 2) {
                    int widht = Integer.valueOf(tmp[0]);
                    int height = Integer.valueOf(tmp[1]);
                    mCameraHelper.updateResolution(widht, height);
                }
                mDialog.dismiss();
            }
        });

        builder.setView(rootView);
        mDialog = builder.create();
        mDialog.show();
    }

    // example: {640x480,320x240,etc}
    private List<String> getResolutionList() {
        List<Size> list = mCameraHelper.getSupportedPreviewSizes();
        List<String> resolutions = null;
        if (list != null && list.size() != 0) {
            resolutions = new ArrayList<>();
            for (Size size : list) {
                if (size != null) {
                    resolutions.add(size.width + "x" + size.height);
                }
            }
        }
        return resolutions;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileUtils.releaseFile();
        // step.4 release uvc camera resources
        if (mCameraHelper != null) {
            mCameraHelper.release();
        }
    }

    private void showShortMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public USBMonitor getUSBMonitor() {
        return mCameraHelper.getUSBMonitor();
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            showShortMsg("取消操作");
        }
    }

    public boolean isCameraOpened() {
        return mCameraHelper.isCameraOpened();
    }

    @Override
    public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
        if (!isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.startPreview(mUVCCameraView);
            isPreview = true;
        }
    }

    @Override
    public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {

    }

    @Override
    public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
        if (isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.stopPreview();
            isPreview = false;
        }
    }
}
