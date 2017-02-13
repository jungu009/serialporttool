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

import static cn.feichao.tools.serialporttool.utils.CommonUtils.asciiStrFrom;
import static cn.feichao.tools.serialporttool.utils.CommonUtils.asciiToBytes;
import static cn.feichao.tools.serialporttool.utils.CommonUtils.asciiToHex;
import static cn.feichao.tools.serialporttool.utils.CommonUtils.hexStrFrom;
import static cn.feichao.tools.serialporttool.utils.CommonUtils.hexToAscii;
import static cn.feichao.tools.serialporttool.utils.CommonUtils.hexToBytes;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

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
            //TODO receive data
            if(msg.what == 123) {
                String receiveData = (String)msg.obj;

                receiveDataEditText.setText(receiveData);
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

    private void initRadioGroup() {
        sendRadioGroup = (RadioGroup)findViewById(R.id.send_radioGroup);
        sendRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int radioId = radioGroup.getCheckedRadioButtonId();
                switch (radioId) {
                    case R.id.send_hex:
                        String str;
                        if(!sendIsHex && (str = sendDataEditText.getText().toString()).length() > 0) {
                            sendDataEditText.setText(asciiToHex(str));
                        }
                        sendIsHex = true;
                        break;
                    case R.id.send_ascii:
                        String str1;
                        if(sendIsHex && (str1 = sendDataEditText.getText().toString()).length() > 0) {
                            sendDataEditText.setText(hexToAscii(str1));
                        }
                        sendIsHex = false;
                        break;
                }
            }
        });
        receiveRadioGroup = (RadioGroup)findViewById(R.id.receive_radioGroup);
        receiveRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int radioId = radioGroup.getCheckedRadioButtonId();
                switch (radioId) {
                    case R.id.receive_hex:
                        String str;
                        if(!receiveIsHex && (str = receiveDataEditText.getText().toString()).length() > 0) {
                            receiveDataEditText.setText(asciiToHex(str));
                        }
                        receiveIsHex = true;
                        break;
                    case R.id.receive_ascii:
                        String str1;
                        if(receiveIsHex && (str1 = receiveDataEditText.getText().toString()).length() > 0) {
                            receiveDataEditText.setText(hexToAscii(str1));
                        }
                        receiveIsHex = false;
                        break;
                }
            }
        });
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
                connect();
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

    private void clear(TextView textView) {
        textView.setText("");
    }

    private void sendData() {
        String sendData = sendDataEditText.getText().toString();

        if(sendData.trim().equals("")) {
            Toast.makeText(this, "请输入发送的数据", Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] bytes = null;
        if(sendIsHex) {
            bytes = hexToBytes(sendData);
        }else {
            bytes = asciiToBytes(sendData);
        }

        try {
            serialPort.send(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void connect() {
        if(!isOpen) {
            isOpen = !isOpen;
            String dev = ((File)serialPortSpinner.getSelectedItem()).getAbsolutePath();
            int baud = Integer.parseInt((String)baudrateSpinner.getSelectedItem());
            serialPort = new SerialPort(dev, baud);
            //TODO 捕捉异常提示错误
            serialPort.open();
            connBtn.setText(getResources().getString(R.string.disconnect_str));
            new ReceiveThread().start();
            Toast.makeText(this, "连接成功", Toast.LENGTH_SHORT).show();
        }else {
            isOpen = !isOpen;
            disconnect();
            connBtn.setText(getResources().getString(R.string.connect_str));
            Toast.makeText(this, "断开成功", Toast.LENGTH_SHORT).show();
        }
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

    class ReceiveThread extends Thread {

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
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

    }


}
