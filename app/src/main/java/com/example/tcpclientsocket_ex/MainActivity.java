package com.example.tcpclientsocket_ex;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Trace;
import android.os.storage.StorageManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String TAG = "jhlee";

    public Button btConnect;
    public Button btStart;
    public Button btStop;
    public Button btCheck;
    public Button btDisconnect;
    public EditText etIpText;
    public TextView tvByText;
    public TextView tvDataText;
    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btConnect = findViewById(R.id.bt_connect);
        btStart = findViewById(R.id.bt_start);
        btStop = findViewById(R.id.bt_stop);
        btCheck = findViewById(R.id.bt_check);
        btDisconnect = findViewById(R.id.bt_disconnect);
        etIpText = findViewById(R.id.et_ipText);
        tvByText = findViewById(R.id.tv_byteText);
        tvDataText = findViewById(R.id.tv_recvByte);

        btConnect.setOnClickListener(this);
        btDisconnect.setOnClickListener(this);

        int SDK_INT = Build.VERSION.SDK_INT;
        if(SDK_INT > 8){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    class ConnectThread extends Thread {
        String hostname;
        String TAG = "jhlee";

        public ConnectThread(String addr){
            hostname = addr;
        }

        public void run(){
            try{ // Client Socket 생성
                int port = 35000;
                socket = new Socket(hostname, port);
                Log.d(TAG, "Socket 생성, 연결 시작");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "연결 완료");
                        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (UnknownHostException ue){
                Log.e(TAG, "Error : 호스트의 IP 주소를 식별할 수 없음");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : 호스트의 IP 주소를 식별할 수 없음", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException ie){
                Log.e(TAG, "Error : 네트워크 응답 없음");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : 네트워크 응답 없음", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (SecurityException se){
                Log.e(TAG, "Error : 보안 위반에 대해 보안 관리자에 의해 발생. (프록시 접속 거부, 허용되지 않음 함수 호출)");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : 보안 위반에 대해 보안 관리자에 의해 발생. (프록시 접속 거부, 허용되지 않음 함수 호출)", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IllegalArgumentException ie){
                Log.e(TAG, "Error : 메서드에 잘못된 파라미터가 전달됨. 0~65535 범위 밖의 포트 번호 사용, Null 프록시 전달");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : 메서드에 잘못된 파라미터가 전달됨. 0~65535 범위 밖의 포트 번호 사용, Null 프록시 전달", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    class StartThread extends Thread {

        int bytes;
        String Dtmp;
        int dlen;

        public StartThread(){
        }

        public String byteArrayToHex(byte[] a){
            StringBuilder sb = new StringBuilder();
            for(final byte b : a){
                sb.append(String.format("%02x", b&0xff));
            }
            return sb.toString();
        }

        public void run(){
            // 데이터 송신
            try{
                String outData = "AT+START\n";
                byte[] data = outData.getBytes();
                OutputStream output = socket.getOutputStream();
                output.write(data);
                Log.d(TAG, "AT+START\\n COMMAND 송신");
            } catch(IOException e){
                e.printStackTrace();
                Log.d(TAG, "데이터 송신 오류");
            }

            // 데이터 수신
            try{
                Log.d(TAG, "데이터 수신 준비");

                // TODO : 수신 데이터(프로토콜) 처리
                while(true){
                    byte[] buffer = new byte[1024];
                    InputStream input = socket.getInputStream();

                    bytes = input.read(buffer);
                    Log.d(TAG, "byte = " + bytes);
                    
                    // 바이트 헥사(String)로 바꿔서 Dtmp string에 저장
                    Dtmp = byteArrayToHex(buffer);
                    Dtmp = Dtmp.substring(0, bytes * 3);
                    Log.d(TAG, Dtmp);

                    // 프로토콜 나누기
                    String[] Dsplit = Dtmp.split("a5 5a"); // sync(2byte) 0xA5, 0x5A
                    Dtmp = "";
                    for(int i=1; i<Dsplit.length-1; i++){ // 제일 처음과 끝은 잘림. 데이터 버린다.
                        Dtmp = Dtmp + Dsplit[i] + "\n";
                    }
                    dlen = Dsplit.length - 2;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvDataText.setText(Dtmp);
                            tvByText.setText("데이터 " + dlen + "개");
                        }
                    });
                }
            } catch (IOException e){
                e.printStackTrace();
                Log.e(TAG, "데이터 수신 에러");
            }
        }
    }

    class StopThread extends Thread {
        public StopThread(){
        }

        public void run(){
            // 데이터 송신
            try{
                String OutData = "AT+STOP\n";
                byte[] data = OutData.getBytes();
                OutputStream output = socket.getOutputStream();
                output.write(data);
                Log.d(TAG, "AT+STOP\\n COMMAND 송신");
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bt_connect:
                // 포트 연결
                connect();
                break;
            case R.id.bt_start:
                // 데이터 송수신
                start();
                break;
            case R.id.bt_stop:
                // 데이터 송수신 중지
                stop();
                break;
            case R.id.bt_disconnect:
                disconnect();
                // 연결 해제
                break;
            case R.id.bt_check:
                // 연결상태 확인
                check();
                break;
        }
    }

    public void connect() {
        try{
            Socket socket = new Socket("192.168.0.8", 35005);
        } catch (UnknownHostException e) {
            Log.e(TAG, "Error : 호스트의 IP 주소를 식별할 수 없음(잘못된 주소값 또는 호스트이름 사용");
        } catch (IOException e) {
            Log.e(TAG, "Error : 네트워크 응답 없음");
        } catch (SecurityException se){
            Log.e(TAG, "Error : 보안 위반에 대해 보안 관리자에 의해 발생. (프록시 접속 거부, 허용되지 않은 함수 호출)");
        } catch (IllegalArgumentException ie){
            Log.e(TAG, "Error : 메서드에 잘못된 파라미터가 전달됨. 0~65535 범위 밖의 포트 번호 사용, Null 프록시 전달");
        }

        Toast.makeText(getApplicationContext(), "Connect 시도", Toast.LENGTH_SHORT).show();
        String addr = etIpText.getText().toString().trim();

        ConnectThread thread = new ConnectThread(addr);
        thread.start();
    }

    public void start(){
        StartThread sthread = new StartThread();
        btStart.setEnabled(false);
        btStop.setEnabled(true);

        sthread.start();
    }

    public void stop(){
        StopThread spthread = new StopThread();
        btStart.setEnabled(true);
        btStop.setEnabled(false);
        spthread.start();
    }

    public void disconnect(){
        try{
            socket.close();
            Toast.makeText(getApplicationContext(), "Disconnect", Toast.LENGTH_SHORT).show();
            btDisconnect.setEnabled(false);
            btConnect.setEnabled(true);
            btStart.setEnabled(false);
            btStop.setEnabled(false);
        } catch (IOException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Disconnect 실패", Toast.LENGTH_SHORT).show();
        }
    }

    public void check(){
        boolean isConnect = socket.isClosed();
        InetAddress addr = socket.getInetAddress();
        String tmp = addr.getHostAddress();

        if(!isConnect){
            Toast.makeText(getApplicationContext(), tmp + "연결중", Toast.LENGTH_SHORT).show();
        } else{
            Toast.makeText(getApplicationContext(), "연결 안됨", Toast.LENGTH_SHORT).show();
        }
    }
}