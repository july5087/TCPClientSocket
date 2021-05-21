package com.example.tcpclientsocket_ex;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Trace;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String TAG = "jhlee";

    public Button btConnect = findViewById(R.id.bt_connect);
    public Button btStart = findViewById(R.id.bt_start);
    public Button btStop = findViewById(R.id.bt_stop);
    public Button btCheck = findViewById(R.id.bt_check);
    public Button btDisconnect = findViewById(R.id.bt_disconnect);
    public EditText etIpText = (EditText) findViewById(R.id.et_ipText);
    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                
            } catch (IOException e){
                e.printStackTrace();
                Log.e(TAG, "데이터 수신 에러");
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
                break;
            case R.id.bt_disconnect:
                // 연결 해제
                break;
            case R.id.bt_check:
                // 연결상태 확인
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
        sthread.start();
    }
}