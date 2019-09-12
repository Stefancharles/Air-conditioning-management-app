package com.example.newland;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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

public class Main3Activity extends AppCompatActivity {
    private NetWorkBusiness netWorkBusiness;
    private Button GetTemp,SaveTemp,DeleteTemp;
    private ListView listView;
    private DBOpenHelper dbOpenHelper;
    private int lastrecord = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        Bundle bundle = getIntent().getExtras();
        dbOpenHelper = new DBOpenHelper(Main3Activity.this,"record.db",null,1);
        String accessToken = bundle.getString("accessToken");   //获得传输秘钥
        netWorkBusiness = new NetWorkBusiness(accessToken,"http://api.nlecloud.com:80/");
        //...
        GetTemp = (Button)findViewById(R.id.getTemp);
        SaveTemp = (Button)findViewById(R.id.saveTemp); //初始化
        DeleteTemp = (Button)findViewById(R.id.deleteTemp);
        SaveTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //保存记录..
                saveRecord();
            }
        });

        listView = (ListView)findViewById(R.id.listView1);
        GetTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取温度记录时间.....
                getTempdata();
            }
        });
        DeleteTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbOpenHelper.getReadableDatabase().delete("record",null,null); //删除记录
            }
        });
    }
    private void getTempdata(){
        netWorkBusiness.getSensorData("40717", "temperature", "6", "30", "2019-07-01 00:00:00",
                "2019-07-20 00:00:00", "DESC", "20", "0", new NCallBack<BaseResponseEntity<SensorDataPageDTO>>() {
                    @Override
                    protected void onResponse(BaseResponseEntity<SensorDataPageDTO> response) {

                    }
                    public void onResponse(final Call<BaseResponseEntity<SensorDataPageDTO>> call, final Response<BaseResponseEntity<SensorDataPageDTO>> response) {
                        BaseResponseEntity baseResponseEntity = response.body();
                        if (baseResponseEntity != null) {
                            //有返回的数据
                            final Gson gson = new Gson();
                            try {
                                JSONObject jsonObject = null;
                                String msg = gson.toJson(baseResponseEntity);
                                jsonObject = new JSONObject(msg);   //解析数据.
                                JSONObject resultobj = jsonObject.getJSONObject("ResultObj");
                                int count = Integer.parseInt(resultobj.get("Count").toString());    //获取记录数20
                                JSONArray jsonArray = resultobj.getJSONArray("DataPoints");
                                JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                                JSONArray jsonArray1 = jsonObject1.getJSONArray("PointDTO");
                                List<HashMap<String,Object>> data = new ArrayList<>(100);
                                Toast.makeText(Main3Activity.this, "++", Toast.LENGTH_SHORT).show();
                                //泛型.
                                //C++模板

                                for (int i=0;i<count;i++){
                                    JSONObject resultObj1 = jsonArray1.getJSONObject(i);
                                    //
                                    HashMap<String,Object> item = new HashMap<>();
                                    item.put("Id",i+1);//Id","Temp","Time"
                                    item.put("Temp",resultObj1.get("Value"));
                                    item.put("Time",resultObj1.get("RecordTime"));
                                    data.add(item); //往数组添加元素

                                    ContentValues values = new ContentValues(); //插入到数据库里面
                                    values.put("_id",lastrecord+i+1);
                                    values.put("temperature",resultObj1.get("Value").toString());
                                    values.put("time",resultObj1.get("RecordTime").toString());
                                    //把数据库的数据存到data1中,然后显示在listview里面...
                                    dbOpenHelper.getReadableDatabase().insert("record",null,values);
                                }
                                lastrecord = lastrecord + count;    //20,40
                                SimpleAdapter adapter = new SimpleAdapter( Main3Activity.this,data,R.layout.item,new String[]{"Id","Temp","Time"},new int[]{R.id.showid,R.id.showTemp,R.id.showTime});
                                listView.setAdapter(adapter);   //添加适配器.
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
        });
    }
    private void saveRecord(){
        //保存记录...
        //先读取数据库，有没有记录
        Cursor cursor = dbOpenHelper.getReadableDatabase().query("record",null,null,null,null,null,null);
        List<HashMap<String,Object>> data1 = new ArrayList<>(100);
        while (cursor.moveToNext()){
            //查询数据.
            HashMap<String,Object> item = new HashMap<>();
            item.put("Id",cursor.getString(0));
            item.put("Temp",cursor.getString(1));
            item.put("Time",cursor.getString(2));
            data1.add(item);    //查询...
        }
        SimpleAdapter adapter = new SimpleAdapter( Main3Activity.this,data1,R.layout.item,new String[]{"Id","Temp","Time"},new int[]{R.id.showid,R.id.showTemp,R.id.showTime});
        listView.setAdapter(adapter);   //添加适配器.

    }
}
