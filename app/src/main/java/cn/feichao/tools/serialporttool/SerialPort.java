package cn.feichao.tools.serialporttool;


import android.content.Context;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by feichao on 2/7/17.
 */

public class SerialPort {

    private FileDescriptor fd;
    private String path;
    private int baud;
    private FileInputStream in;
    private FileOutputStream out;
    private boolean isOpen = false;

    static {
        System.loadLibrary("serialport");
    }

    private static native FileDescriptor open(String path, int baud);

    private static native int close(FileDescriptor fd);

    /**
     * 三种构造方法
     */
    public SerialPort() {}

    public SerialPort(String path, int baud) {
        this.path = path;
        this.baud = baud;
    }

    public SerialPort(File dev, int baud) {
        this.path = dev.getAbsolutePath();
        this.baud = baud;
    }


    public boolean isOpen() {
        return isOpen;
    }

    /**
     * 打开串口
     */
    public boolean open(Context context) {
        boolean bool = true;
        fd = open(path, baud);
        if(fd == null) {
            return false;
        }
        out = new FileOutputStream(fd);
        in = new FileInputStream(fd);
        isOpen = true;
        return bool;
    }

    public FileInputStream getIn() {
        return in;
    }

    public FileOutputStream getOut() {
        return out;
    }


    public void send(byte[] bytes) throws IOException {
        out.write(bytes);
    }


    /**
     * 关闭串口
     * @throws IOException
     */
    public void close() throws IOException {
        if(in != null) {
            in.close();
        }
        if(out != null) {
            out.close();
        }
        if(fd != null) {
            close(fd);
            isOpen = false;
        }else {
            throw new RuntimeException("未打开设备");
        }
    }

}
