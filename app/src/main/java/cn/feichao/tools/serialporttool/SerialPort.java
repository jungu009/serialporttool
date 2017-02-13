package cn.feichao.tools.serialporttool;


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


    /**
     * 打开串口
     */
    public void open() {
        this.fd = open(path, baud);
        out = new FileOutputStream(fd);
        in = new FileInputStream(fd);
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
        }else {
            throw new RuntimeException("未打开设备");
        }
    }

}
