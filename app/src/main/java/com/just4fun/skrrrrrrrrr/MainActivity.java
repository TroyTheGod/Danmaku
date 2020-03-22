package com.just4fun.skrrrrrrrrr;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import kotlin.UInt;
import kotlin.UIntArray;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity {
    private EditText edtRoomId;
    private Button btnConn;
    private TextView tvViewer;
    private ScrollView scrollView;
    private LinearLayout insideScroll;
    private String roomId = "0";
    private WebSocket ws;
    private SocketListener socketListener;
    private Button btnMenu;
    private Timer timer;
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private boolean wsChecker = false;
    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        edtRoomId = findViewById(R.id.editText);
        btnMenu = findViewById(R.id.btnMenu);
        btnConn = findViewById(R.id.btnconn);
        scrollView = findViewById(R.id.mainScrollView);
        insideScroll = findViewById(R.id.insideScroll);
        tvViewer = findViewById(R.id.tvViewer);
        btnConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnConn.setEnabled(false);
                edtRoomId.setEnabled(false);
                roomId = edtRoomId.getText().toString();
                startConnection();

            }
        });
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,Menu.class));
            }
        });


    }

    @Override
    protected void onStop() {
        Log.v("Onstop","strop");
        if(wsChecker == true) {
            ws.close(NORMAL_CLOSURE_STATUS, null);
            timer.cancel();;
        }
        super.onStop();
    }

    private void startConnection(){
        OkHttpClient client;
        client = new OkHttpClient();
        Request request = new Request.Builder().url("wss://broadcastlv.chat.bilibili.com:2245/sub").build();
        socketListener = new SocketListener();
        ws = client.newWebSocket(request, socketListener);
    }

    private String encode(String str,int op){
        String hex = "";
        DecimalFormat g1=new DecimalFormat("00");
        int[] header = new int[]{0,0,0,0,0,16,0,1,0,0,0,op,0,0,0,1};
        //Log.v("encode +",""+str.length());
        //Log.v("oldheader : ", Arrays.toString(header));
        int packetLength = 16+ str.length();
        header = writeInt(header,0,4,packetLength);
        //Log.v("newheader : ",Arrays.toString(header));
        for(int i = 0;i < header.length;i++){
            String temp = Integer.toHexString(header[i]);
            hex = hex + (g1.format(Integer.valueOf(temp)));
        }
        //Log.v("hex = ",""+hex);
        ByteBuffer buff = ByteBuffer.allocate(hex.length()/2);
        for (int i = 0; i < hex.length(); i+=2) {
            buff.put((byte)Integer.parseInt(hex.substring(i, i+2), 16));
        }
        buff.rewind();
        Charset cs = StandardCharsets.UTF_8;
        CharBuffer cb = cs.decode(buff);
        String finalString = cb.toString()+str;
        //Log.v("finalString",""+finalString);
        return finalString;

    }
    private void decode(ByteString bytes){
        String hex = bytes.hex();
        String op = String.valueOf(hex.charAt(23));
        Log.v("decode-op:",op);

        //int packetLen = readInt(bytes,0,4);
        switch (op){
            case "8":
                addText("連接成功");
                break;
            case "5":
                getCmd(bytes);
                break;
            case "3":
                getViewer(bytes);
                break;
            default:break;
        }

}
    private void getCmd(ByteString bytes){
        ArrayList<String> msg = new ArrayList<String>();
        String wCount = bytes.hex();
        Log.v("wCount1",wCount);
        for(int i = 0;0 < wCount.length();i++){
            Log.v("getCmd 1I",""+i);
            int pkLength = getPkLength(wCount);
            int realLength = pkLength * 2;
            Log.v("realLength",""+realLength);
            msg.add(wCount.substring(0,realLength));
            Log.v("msg",msg.get(i));
            wCount = wCount.replaceAll(msg.get(i),"");
            Log.v("getcmd i count",""+i);
            Log.v("wCount",wCount);
            if(i > 10){
                break;
            }
        }
        ArrayList<String> json = removeHeader(msg);
        for (int i = 0; i < json.size(); i++){
            String danmu = getValue(byteTranslate(json.get(i)));
        }
    }
    private String getValue(String json){
        String danmu = "Error";
        try {
            JSONObject jsonObject = new JSONObject(json);
            jsonObject.get("cmd");
            Log.v("jsonobj",""+jsonObject.get("cmd"));
            if(jsonObject.get("cmd").equals("DANMU_MSG")){
                JSONArray jsonArray = jsonObject.getJSONArray("info");
                String danmuMsg = jsonArray.getString(1);
                Log.v("Things", jsonArray.getString(1));
                String name = jsonArray.getJSONArray(2).getString(1);
                Log.v("Name", jsonArray.getJSONArray(2).getString(1));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addText(name + ": "+danmuMsg);
                    }
                });
            }else if (jsonObject.get("cmd").equals("SEND_GIFT")){
                JSONObject object = jsonObject.getJSONObject("data");
                String gift = object.getString("giftName");
                Log.v("gift", gift);
                String name = object.getString("uname");
                Log.v("name", name);
                String num = object.getString("num");
                Log.v("num", num);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addText(name + ": "+num+"x"+gift+"(Gift)");
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return danmu;
    }
    private ArrayList<String> removeHeader(ArrayList<String> msg){
        ArrayList<String> json = new ArrayList<String>();
        for(int i = 0; i < msg.size() ; i++){
            //Log.v("msg.get(i)",""+msg.get(i));
            //Log.v("msg.get(i).length()",""+(msg.get(i).length()-32));
           json.add(msg.get(i).substring(32));
           //Log.v("json",json.get(i));
        }
        return json;
    }
    private int getPkLength(String string){
        String temp = string.substring(0,8);
        //Log.v("Pktemp :",temp);
        int length =Integer.parseInt(temp,16);
        //Log.v("Pklength",""+length);
        return length;
    }
    private void getViewer(ByteString bytes){
        String temp = bytes.hex().substring(bytes.hex().length() -8);
        //Log.v("temp :",temp);
        int count =Integer.parseInt(temp,16);
        //Log.v("count",""+count);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvViewer.setText("Viewr: "+count);
            }
        });
    }

    private String byteTranslate(String bytes){
        ByteBuffer buff = ByteBuffer.allocate(bytes.length()/2);
        for (int i = 0; i < bytes.length(); i+=2) {
            buff.put((byte)Integer.parseInt(bytes.substring(i, i+2), 16));
        }
        buff.rewind();
        Charset cs = StandardCharsets.UTF_8;
        CharBuffer cb = cs.decode(buff);
        String decodedString = cb.toString();
        Log.v("decodedString",""+decodedString);
        return decodedString;
    }

    private int[] writeInt(int[] header,int start,int len,int value){
        int i = 0;
        while(i<len){
            header[start+i] = (int) (value/Math.pow(256,len - i - 1));
            i++;
        }
        return header;
    }


    private void addText(String temp){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.v("addtext",temp);
                LinearLayout.LayoutParams Text = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                LinearLayout danMu = new LinearLayout(getApplicationContext());
                danMu.setLayoutParams(Text);
                danMu.setGravity(Gravity.LEFT);

                TextView text = new TextView(getApplicationContext());
                text.setText(temp);
                text.setTextSize(25);
                text.setTextColor(getResources().getColor(R.color.white, null));
                danMu.addView(text);
                insideScroll.addView(danMu);
                danMu.setVisibility(View.INVISIBLE);
                danMu.setVisibility(View.VISIBLE);
                scrollView.fullScroll(View.FOCUS_DOWN);

            }
        });

    }

    public class SocketListener extends WebSocketListener {

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
            super.onFailure(webSocket, t, response);
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            super.onMessage(webSocket, text);
            Toast.makeText(getApplicationContext(),"Onmassage"+text,Toast.LENGTH_LONG).show();

        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
            super.onMessage(webSocket, bytes);
            //Log.v("Onmassage(Byte) :",""+bytes.hex());
            decode(bytes);
        }

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            super.onOpen(webSocket, response);
            wsChecker = true;
            webSocket.send(encode("{\"roomid\":"+roomId+"}",7));
            //Log.v("Sent:",""+encode("{\"roomid\":"+roomId+"}",7));
            timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    webSocket.send(encode("",2));
                    Log.v("HeartBeat","Sent");
                }
            };
            timer.schedule(task,0,30000);

        }
    }
}
