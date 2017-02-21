package cn.feichao.tools.serialporttool;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static cn.feichao.tools.serialporttool.utils.CommonUtils.asciiStrFrom;
import static cn.feichao.tools.serialporttool.utils.CommonUtils.asciiToBytes;
import static cn.feichao.tools.serialporttool.utils.CommonUtils.asciiToHex;
import static cn.feichao.tools.serialporttool.utils.CommonUtils.hexStrFrom;
import static cn.feichao.tools.serialporttool.utils.CommonUtils.hexToAscii;
import static cn.feichao.tools.serialporttool.utils.CommonUtils.hexToBytes;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener, RadioGroup.OnCheckedChangeListener{

    private String[] bauds = {"9600", "115200", "230400", "500000"};
    private boolean isOpen = false;
    private SerialPort serialPort;
    private static boolean receiveIsHex = true, sendIsHex = true;


    private Spinner serialPortSpinner, baudrateSpinner;
    private Button connBtn, sendBtn, receiveClearBtn, sendClearBtn;
    private static EditText receiveDataEditText, sendDataEditText;
    private RadioGroup receiveRadioGroup, sendRadioGroup;


    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        if(msg.what == 123) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("[HH:mm:ss]");
            String receiveData = timeFormat.format(new Date()) + "\n" + (String)msg.obj + "\n";
            receiveDataEditText.append(receiveData);
        }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        initSpinner();
        initButton();
        initEditText();
        initRadioGroup();
    }

    // TODO 优化
    private void initRadioGroup() {
        sendRadioGroup = (RadioGroup)findViewById(R.id.send_radioGroup);
        sendRadioGroup.setOnCheckedChangeListener(this);
        receiveRadioGroup = (RadioGroup)findViewById(R.id.receive_radioGroup);
        receiveRadioGroup.setOnCheckedChangeListener(this);
    }

    private void initEditText() {
        receiveDataEditText = (EditText)findViewById(R.id.receive_data);
        sendDataEditText = (EditText)findViewById(R.id.send_data);
    }

    private void initButton() {
        connBtn = (Button)findViewById(R.id.connect);
        connBtn.setOnClickListener(this);
        sendBtn = (Button)findViewById(R.id.send);
        sendBtn.setOnClickListener(this);
        receiveClearBtn = (Button)findViewById(R.id.receive_clear);
        receiveClearBtn.setOnClickListener(this);
        sendClearBtn = (Button)findViewById(R.id.clear_send);
        sendClearBtn.setOnClickListener(this);
    }

    private void initSpinner() {
        serialPortSpinner = (Spinner)findViewById(R.id.serialport);
        baudrateSpinner = (Spinner)findViewById(R.id.baudrate);

        File boot = new File("/dev");
        File[] files = boot.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
            boolean bool = false;
            String name = pathname.getName();
            if(name.contains("ttyMT")
                    || name.contains("ttyUSB")
                    || name.contains("ttyGS")) {
                bool = true;
            }
            return bool;
            }
        });
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, files);
        serialPortSpinner.setAdapter(adapter);

        baudrateSpinner.setAdapter(new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, bauds));
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.connect:
                openConnect();
                break;
            case R.id.send:
                sendData();
                break;
            case R.id.receive_clear:
                clear(receiveDataEditText);
                break;
            case R.id.clear_send:
                clear(sendDataEditText);
                break;
            default:
                break;
        }
    }

    /**
     * 实现hex和ascii之间的转换
     * @param isHex 目前选择的格式是否是 hex
     * @param data  数据显示所在的Editext
     * @return
     */
    private boolean convert(boolean isHex, EditText data) {
        String str;
        if((str = data.getText().toString()).length() > 0) {
            String dataStr = isHex?hexToAscii(str):asciiToHex(str);
            if(data == null)
                toast("数据格式有误");
            else
                data.setText(dataStr);
        }
        return !isHex;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        int radioId = radioGroup.getCheckedRadioButtonId();
        switch (radioId) {
            case R.id.receive_hex:
//                receiveIsHex = convert(receiveIsHex, receiveDataEditText);
                receiveIsHex = true;
                break;
            case R.id.receive_ascii:
//                receiveIsHex = convert(receiveIsHex, receiveDataEditText);
                receiveIsHex = false;
                break;
            case R.id.send_hex:
                sendIsHex = convert(sendIsHex, sendDataEditText);
                break;
            case R.id.send_ascii:
                sendIsHex = convert(sendIsHex, sendDataEditText);
                break;
            default:
                break;
        }
    }


    private void clear(TextView textView) {
        textView.setText("");
    }

    private void sendData() {
        String sendData = sendDataEditText.getText().toString();

        if(sendData.trim().equals("")) {
            toast("请输入发送的数据");
            return;
        }

        byte[] bytes = null;
        if(sendIsHex) {
            bytes = hexToBytes(sendData);
        }else {
            bytes = asciiToBytes(sendData);
        }

        if(bytes == null) {
            toast("数据格式有误");
            return;
        }

        if(serialPort == null || !serialPort.isOpen()) {
            toast("串口未打开");
            return;
        }
        try {
            serialPort.send(bytes);
        } catch (IOException e) {
            toast("发送失败");
        }

    }


    private void openConnect() {
        if(!isOpen) {
            boolean bool = connect();
            if(bool) {
                isOpen = true;
                new ReceiveThread().start();
                connBtn.setText(getResources().getString(R.string.disconnect_str));
                toast("连接成功");
            } else {
                toast("连接失败");
            }
        }else {
            isOpen = false;
            disconnect();
            connBtn.setText(getResources().getString(R.string.connect_str));
            toast("断开成功");
        }
    }


    private boolean connect() {
        String dev = ((File)serialPortSpinner.getSelectedItem()).getAbsolutePath();
        int baud = Integer.parseInt((String)baudrateSpinner.getSelectedItem());
        serialPort = new SerialPort(dev, baud);
        return serialPort.open(this);
    }

    private void disconnect() {
        if(serialPort != null) {
            try {
                serialPort.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    private class ReceiveThread extends Thread {

        @Override
        public void run() {

            byte[] bytes = new byte[512];
            int len;
            StringBuilder builder = new StringBuilder("");

            try {

                while((len = serialPort.getIn().read(bytes)) != -1) {
//                                            builder.append(new String(bytes, 0, len));
                    if(len > 0) {
                        Message msg = new Message();
                        msg.what = 123;
                        if(receiveIsHex) {
                            msg.obj = hexStrFrom(bytes, len);
                        }else {
                            msg.obj = asciiStrFrom(bytes, len);
                        }
                        handler.sendMessage(msg);
                    }

                    Thread.sleep(100L);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private void toast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
}
