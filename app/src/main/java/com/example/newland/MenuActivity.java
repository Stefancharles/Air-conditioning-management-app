package com.example.newland;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.com.newland.nle_sdk.responseEntity.DeviceState;
import cn.com.newland.nle_sdk.responseEntity.SensorInfo;
import cn.com.newland.nle_sdk.responseEntity.User;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.NCallBack;
import cn.com.newland.nle_sdk.util.NetWorkBusiness;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuActivity extends AppCompatActivity {
    private EditText EquimentID;    //设备id
    private TextView Isonline;  //在线状态
    private Button GetStatus;   //获取状态.
    private EditText MinTemp,MaxTemp;       //最小,最大温度
    private TextView CurrentTemp;   //当前温度
    private Button OpenLight,CloseLight;    //开关灯
    private Button OpenFan,CloseFan;    //开关风扇
    private Button GetTemp; //获取温度
    private Button Auto,Noauto;     //是否自动控制...
    private Button ConfirmDevice;
    private Button RecordTemperature;
    private SeekBar SeekBarX,SeekBarY;  //拖动条
    private TextView CurrentSeekBarX,CurrentSeekBarY; //当前拖动条x,y
    private boolean isAuto = false; //标志位.
    private double tem;
    private NetWorkBusiness netWorkBusiness;
    private String accessToken;
    private String deviceID = "40717";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        init();
        //在线状态获取...
        GetStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取状态.
                Getdeviceisonline();
            }
        });
        OpenLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                control(deviceID,"ctrl",1);  //灯.
            }
        });
        CloseLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                control(deviceID,"ctrl",0);  //灯.
            }
        });
        OpenFan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                control(deviceID,"defense",1);   //fan
            }
        });
        CloseFan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                control(deviceID,"defense",0);
            }
        });
        GetTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTemperature();
            }
        });
        Auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //自动控制
                isAuto = true;  //运行线程...
                //线程.
                Thread1 th = new Thread1();
                new Thread(th).start();
            }
        });
        Noauto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAuto = false;
            }
        });
        RecordTemperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转.
                Intent intent = new Intent(MenuActivity.this, Main3Activity.class);
                Bundle bundle = new Bundle();
                bundle.putString("accessToken", accessToken);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });
        SeekBarX.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //获取值..
                String a = Integer.toString(i);
                CurrentSeekBarX.setText(a);
                control(deviceID,"steeringengine1",i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        SeekBarY.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String a = Integer.toString(i);
                CurrentSeekBarY.setText(a);
                control(deviceID,"steeringengine0",i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        ConfirmDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //确定设备id...
                deviceID = EquimentID.getText().toString();
            }
        });
    }
    void init(){
        EquimentID = findViewById(R.id.equimentID);   //设备id
        Isonline = findViewById(R.id.isonline);   //状态
        GetStatus = findViewById(R.id.getStatus);   //获取状态..
        CurrentTemp = findViewById(R.id.currentTemp);    //当前温度
        OpenLight = findViewById(R.id.openLight);  //开灯
        CloseLight = findViewById(R.id.closeLight);    //关灯
        OpenFan = findViewById(R.id.openFan);  //开风扇
        CloseFan = findViewById(R.id.closeFan);    //关风扇
        GetTemp = findViewById(R.id.getTemp);   //获取温度
        Auto = findViewById(R.id.auto);     //自动控制..
        Noauto = findViewById(R.id.noauto); //关闭自动控制.
        RecordTemperature = findViewById(R.id.recordTemperature);   //记录温度
        MinTemp = findViewById(R.id.minTemp); //最小温度...
        MaxTemp = findViewById(R.id.maxTemp); //最大温度...
        SeekBarX = findViewById(R.id.seekBarX);    //水平拖动条
        SeekBarY = findViewById(R.id.seekBarY);
        CurrentSeekBarX = findViewById(R.id.currentX);
        CurrentSeekBarY = findViewById(R.id.currentY);
        ConfirmDevice = findViewById(R.id.confirmDevice);
        Bundle bundle = getIntent().getExtras();
        accessToken = bundle.getString("accessToken");   //获得传输秘钥
        netWorkBusiness = new NetWorkBusiness(accessToken,"http://api.nlecloud.com:80/");   //进行登录连接
    }
    public void control(String id,String apiTag,Object value){
        //设备id,标识符,值.
        netWorkBusiness.control(id, apiTag, value, new Callback<BaseResponseEntity>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponseEntity> call,@NonNull Response<BaseResponseEntity> response) {
                BaseResponseEntity<User> baseResponseEntity = response.body();  //获得返回体
                if (baseResponseEntity==null){
                    Toast.makeText(MenuActivity.this,"请求内容为空",Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<BaseResponseEntity> call, Throwable t) {
                Toast.makeText(MenuActivity.this,"请求出错 " + t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void getTemperature(){
        //获取温度...
        //id和设备标识符写死
        netWorkBusiness.getSensor(deviceID, "temperature", new NCallBack<BaseResponseEntity<SensorInfo>>() {
            @Override
            public void onResponse(final Call<BaseResponseEntity<SensorInfo>> call, final Response<BaseResponseEntity<SensorInfo>> response) {
                BaseResponseEntity baseResponseEntity = response.body();
                if (baseResponseEntity!=null){
                    //获取到了内容,使用json解析.
                    //JSON 是一种文本形式的数据交换格式，它比XML更轻量、比二进制容易阅读和编写，调式也更加方便;解析和生成的方式很多，Java中最常用的类库有：JSON-Java、Gson、Jackson、FastJson等
                    final Gson gson=new Gson();
                    JSONObject jsonObject=null;
                    String msg=gson.toJson(baseResponseEntity);
                    try {
                        jsonObject = new JSONObject(msg);   //解析数据.

                        JSONObject resultObj = (JSONObject) jsonObject.get("ResultObj");
                        String aaa=resultObj.getString("Value");
                        tem=Double.valueOf(aaa).intValue();
                        CurrentTemp.setText(tem+"℃");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onResponse(BaseResponseEntity<SensorInfo> response) {

            }

            public void onFailure(final Call<BaseResponseEntity<SensorInfo>> call, final Throwable t) {
                Toast.makeText(MenuActivity.this,"温度获取失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
    class Thread1 implements Runnable{
        @Override
        public void run() {
            while(true){
                //获取温度...
                getTemperature();   //并显示出来
                int currentTemp = (int)tem;
                int min  = Double.valueOf(MinTemp.getText().toString()).intValue();
                int max  = Double.valueOf(MaxTemp.getText().toString()).intValue();
                if (currentTemp>max && isAuto){
                    //开风扇.
                    control(deviceID,"defense",1);
                    control(deviceID,"ctrl",0);  //灯.
                }else if (currentTemp<min && isAuto){
                    //小于报警开灯.,关风扇.
                    control(deviceID,"ctrl",1);  //灯.
                    control(deviceID,"defense",0);
                }else if (isAuto){
                    control(deviceID,"ctrl",0);  //灯.
                    control(deviceID,"defense",0);
                }
                try{
                    Thread.sleep(1000); //希望不用使用Handler处理,使用handler加快处理速度吗？
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void Getdeviceisonline(){
        netWorkBusiness.getBatchOnLine(deviceID, new NCallBack<BaseResponseEntity<List<DeviceState>>>() {
            @Override
            protected void onResponse(BaseResponseEntity<List<DeviceState>> response) {

            }
            @Override
            public void onResponse(final Call<BaseResponseEntity<List<DeviceState>>> call, final Response<BaseResponseEntity<List<DeviceState>>> response) {
                BaseResponseEntity baseResponseEntity = response.body();
                if (baseResponseEntity!=null){
                    //获取到了内容,使用json解析.
                    //JSON 是一种文本形式的数据交换格式，它比XML更轻量、比二进制容易阅读和编写，调式也更加方便;解析和生成的方式很多，Java中最常用的类库有：JSON-Java、Gson、Jackson、FastJson等
                    boolean value = false;
                    final Gson gson=new Gson();
                    try {
                        JSONObject jsonObject=null;
                        String msg=gson.toJson(baseResponseEntity);
                        jsonObject = new JSONObject(msg);   //解析数据.
                        JSONArray resultObj = (JSONArray) jsonObject.get("ResultObj");
                        value = resultObj.getJSONObject(0).getBoolean("IsOnline");
                        if (value){
                            Isonline.setText("在线");
                        }else{
                            Isonline.setText("离线");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
