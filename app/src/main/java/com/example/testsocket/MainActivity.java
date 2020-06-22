package com.example.testsocket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    private EditText ip, port, sendtext;
    private TextView msg11;
    private Button button, send;
    Socket socket = null;
    private SharedPreferences sp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.start);
        send = findViewById(R.id.send);
        msg11 = findViewById(R.id.msg);
        sendtext = findViewById(R.id.sendtext);
        ip = findViewById(R.id.ip);
        port = findViewById(R.id.port);

        sp = this.getSharedPreferences("userinfo", Context.MODE_PRIVATE);

        String saved_ip = sp.getString("IP", ""); //获取sp里面存储的数据
        String saved_port = sp.getString("PORT","");
        ip.setText(saved_ip);//将sp中存储的ip写入EditeText
        port.setText(saved_port);//将sp中存储的port写入EditeText

        //接收数据按钮的监听事件
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getMsg();
                    }
                }).start();
            }
        });

        //发送数据按钮的监听事件
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendMsg();
                    }
                }).start();

            }
        });

    }

    //用于处理多线程无法获取主键问题
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 100){
                String res = (String) msg.obj;
                msg11.setText(res);
//                Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
            }
    }};



    //获取IP地址
    private String GetHostIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> ipAddr = intf.getInetAddresses(); ipAddr
                        .hasMoreElements();) {
                    InetAddress inetAddress = ipAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
        } catch (Exception e) {
        }
        return null;
    }


    private void sendMsg(){

        try {
            String msg = sendtext.getText().toString();
            ip = findViewById(R.id.ip);
            port = findViewById(R.id.port);
            String sip = ip.getText().toString();
            int sport = Integer.parseInt(port.getText().toString());
            socket = new Socket(sip, sport);
            OutputStream outputStream = socket.getOutputStream();
            String info = msg;
//            outputStream.write((GetHostIp()+ ":" +info).getBytes());
            outputStream.write(info.getBytes());
//            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void getMsg() {
        try {
            ip = findViewById(R.id.ip);
            port = findViewById(R.id.port);
            String sip = ip.getText().toString();
            int sport = Integer.parseInt(port.getText().toString());

            SharedPreferences.Editor editor = sp.edit();
            editor.putString("IP", sip);
            editor.putString("PORT", port.getText().toString());
            editor.commit();
            // 创建socket对象，指定服务器端地址和端口号
            socket = new Socket(sip, sport);

            OutputStream outputStream = socket.getOutputStream();
            String info = "hello again111";
            outputStream.write(info.getBytes());

            InputStream inputStream = socket.getInputStream();
            byte[] bt = new byte[100];
            inputStream.read(bt);
            Message msg = new Message();
            msg.what = 100;  //消息发送的标志
            msg.obj = "接收到发来的消息为：" + new String(bt);
            handler.sendMessage(msg);

        } catch (NumberFormatException e){
            Message msg = new Message();
            msg.what = 100;  //消息发送的标志
            msg.obj = "ip或端口输入不符合规则！";
            handler.sendMessage(msg);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(socket==null){
                    Message msg = new Message();
                    msg.what = 100;  //消息发送的标志
                    msg.obj = "连接失败";
                    handler.sendMessage(msg);
                }else {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
