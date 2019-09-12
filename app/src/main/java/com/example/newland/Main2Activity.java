package com.example.newland;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import cn.com.newland.nle_sdk.responseEntity.SensorDataPageDTO;

import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.NCallBack;
import cn.com.newland.nle_sdk.util.NetWorkBusiness;
import retrofit2.Call;
import retrofit2.Response;

public class Main2Activity extends AppCompatActivity {
    private DBOpenHelper dbOpenHelper;
    private NetWorkBusiness netWorkBusiness;
    private ListView listView;
    final int FLAG_MSG = 0x001; //定义发送的消息代码
    final int FLAG_MSG1 = 0x002; //定义发送的消息代码
    private boolean flag = false;
    private int RecordCount = 0;
    private int lastCount = 0;
    List<HashMap<String, Object>> data ;
    List<HashMap<String, Object>> data1 ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        //创建DBOpenHelper对象,指定名称、版本号并保存在databases目录下
        dbOpenHelper = new DBOpenHelper(Main2Activity.this,"record.db",null,1);
        listView = (ListView) findViewById(R.id.listView1);
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        String accessToken = bundle.getString("accessToken");   //获得传输秘钥
        netWorkBusiness = new NetWorkBusiness(accessToken,"http://api.nlecloud.com:80/");   //进行登录连接
        //加载listview....
        final Button GetTemp = (Button) findViewById(R.id.getTemp);
        final Button PastTemp = (Button) findViewById(R.id.pastTemp);
        final Button DeleteTemp = (Button)findViewById(R.id.deleteTemp);
        final Button SaveTemp = (Button)findViewById(R.id.saveTemp);
        //获取记录
        GetTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                data = new ArrayList<HashMap<String,Object>>(500);  //重新初始化,但一开始也要判断.
                data1 = new ArrayList<HashMap<String,Object>>(500);
                getTempData();
            }
        });
        //历史记录
        PastTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!flag){
                    Toast.makeText(Main2Activity.this,"当前没有历史记录可查看!!!",Toast.LENGTH_SHORT).show();
                }else{
                    Message message;    //声明消息对象
                    message = Message.obtain();
                    message.what = FLAG_MSG1;
                    handler.sendMessage(message);
                }
            }
        });
        //保存记录
        SaveTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //保存...
                if (!flag){
                    Toast.makeText(Main2Activity.this,"当前没有记录可保存!!!",Toast.LENGTH_SHORT).show();
                }else{
                    //保存记录...
                    // 系统总是根据 Hash 算法来计算 key-value 的存储位置，这样可以保证能快速存、取 Map 的 key-value 对。
                    Message message;    //声明消息对象
                    message = Message.obtain();
                    message.what = FLAG_MSG;
                    handler.sendMessage(message);

                }
            }
        });
        //删除记录
        DeleteTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //删除所有记录...
                if (!flag){
                    Toast.makeText(Main2Activity.this,"当前没有记录可删除!!!",Toast.LENGTH_SHORT).show();
                }else{
                    flag = false;
                    data.clear();
                    data1.clear();
                    lastCount = 0;
                    dbOpenHelper.getReadableDatabase().delete("record",null,null);
                    Toast.makeText(Main2Activity.this,"当前记录已删除!!!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void getTempData(){
        netWorkBusiness.getSensorData("40717", "temperature", "6", "30", "2019-07-01 01:01:01", "2019-07-20 01:01:01", "DESC", "20", "0", new NCallBack<BaseResponseEntity<SensorDataPageDTO>>() {
            @Override
            protected void onResponse(BaseResponseEntity<SensorDataPageDTO> response) {
                Toast.makeText(Main2Activity.this,"默认获取成功", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onResponse(final Call<BaseResponseEntity<SensorDataPageDTO>> call, final Response<BaseResponseEntity<SensorDataPageDTO>> response) {
                //需要解析数据了...
                BaseResponseEntity baseResponseEntity = response.body();
                if (baseResponseEntity!=null){
                    final Gson gson=new Gson();
                    try {
                        JSONObject jsonObject=null;
                        String msg=gson.toJson(baseResponseEntity);
                        jsonObject = new JSONObject(msg);   //解析数据.
                        JSONObject resultObj = (JSONObject) jsonObject.get("ResultObj");    //最外层的json对象.
                        JSONArray jsonArray = resultObj.getJSONArray("DataPoints"); //然后是该层的jsonArray
                        JSONObject jsonObject1 = jsonArray.getJSONObject(0);    //对象转换
                        RecordCount = Integer.parseInt(resultObj.getString("Count")); //总条数...
                        JSONArray jsonArray1 = jsonObject1.getJSONArray("PointDTO"); //然后是该层的jsonArray
                        //显示出来看看.
                        //这里，我读的数据就是降序，所以id越小,时间越新...
                        for (int i=0;i<RecordCount;i++) {
                            //显示10条...
                            JSONObject resultObj1 = jsonArray1.getJSONObject(i);   //最外层的json对象.
                            HashMap<String, Object> item = new HashMap<String, Object>();//对于 HashMap 而言，系统 key-value 当成一个整体进行处理，
                            item.put("Id", String.valueOf(i + 1));   //myUser.getText()时数据会重复添加，toString()后数据不重复添加.????
                            item.put("Temp", resultObj1.get("Value")); //返回对象的字符串表示.
                            item.put("Time", resultObj1.get("RecordTime"));
                            data.add(item);
                        }
                        flag = true;
                        Toast.makeText(Main2Activity.this,"获取成功", Toast.LENGTH_SHORT).show();
                        SimpleAdapter adapter = new SimpleAdapter( Main2Activity.this,data,R.layout.item,new String[]{"Id","Temp","Time"},new int[]{R.id.showid,R.id.showTemp,R.id.showTime});
                        listView.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            public void onFailure(final Call<BaseResponseEntity<SensorDataPageDTO>> call, final Throwable t) {
                Toast.makeText(Main2Activity.this,"获取失败"+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==FLAG_MSG){	//接收到的是消息标记，能用于处理不同类型的消息.
                //Log.v(LogDemo.ACTIVITY_TAG, "This is Verbose.");
                //当前活动,数据源,item布局文件,显示对应.
                for (int i=0;i<RecordCount;i++){
                    HashMap<String, Object> item = new HashMap<String, Object>();//对于 HashMap 而言，系统 key-value 当成一个整体进行处理，
                    //List<HashMap<String, Object>> data = new ArrayList<HashMap<String,Object>>(100);
                    item= data.get(i);
                    String temp = item.get("Temp").toString();
                    String time = item.get("Time").toString();
                    insertData(dbOpenHelper.getReadableDatabase(),lastCount+i,temp,time); //数据插入到数据库中
                }
                lastCount = lastCount + RecordCount;
                Toast.makeText(Main2Activity.this,"保存成功",Toast.LENGTH_SHORT).show();
            }
            if (msg.what==FLAG_MSG1){
                //android.database.sqlite.SQLiteException: no such column: DESC (code 1 SQLITE_ERROR): , while compiling: SELECT * FROM record ORDER BY DESC
                //Cursor cursor  = dbOpenHelper.getReadableDatabase().query("record",null,null,null,null,null,"DESC");
                Cursor cursor  = dbOpenHelper.getReadableDatabase().query("record",null,null,null,null,null,null);
                while (cursor.moveToNext()){
                    HashMap<String, Object> item1 = new HashMap<String, Object>();//对于 HashMap 而言，系统 key-value 当成一个整体进行处理，
                    item1.put("Id",cursor.getString(0));
                    item1.put("Temp",cursor.getString(1));
                    item1.put("Time",cursor.getString(2));//从数据库取结果...
                    data1.add(item1);   //所有记录保存到数据库中...
                }
                SimpleAdapter adapter1 = new SimpleAdapter( Main2Activity.this,data1,R.layout.item,new String[]{"Id","Temp","Time"},new int[]{R.id.showid,R.id.showTemp,R.id.showTime});
                listView.setAdapter(adapter1);
            }
        }
    };
    private void insertData(SQLiteDatabase readableDatabase,int i,String temp,String time){
        ContentValues values = new ContentValues();
        values.put("_id",i);
        values.put("temperature",temp);
        values.put("time",time);
        readableDatabase.insert("record",null,values);
    }
    protected void onDestroy(){
        super.onDestroy();
        if (dbOpenHelper!=null){
            dbOpenHelper.close();
        }
    }
}
