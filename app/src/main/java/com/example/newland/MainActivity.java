package com.example.newland;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cn.com.newland.nle_sdk.requestEntity.SignIn;
import cn.com.newland.nle_sdk.responseEntity.User;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.NetWorkBusiness;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private EditText username;   //账户
    private EditText password;  //密码
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String _username = "";
    private String _password = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences("nlecloud",MODE_PRIVATE);
        editor = sp.edit();
        //登录
        Button login = (Button) findViewById(R.id.login);
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        if (sp.getString("username",_username)!=null && sp.getString("password",_password)!=null){
            if (!sp.getString("username",_username).equals("") && !sp.getString("password",_password).equals("")){
                username.setText(sp.getString("username","1"));// "17674738454";
                password.setText(sp.getString("password","2"));
            }
        }
        //判断SharedPreferences文件中，用户名、密码是否存在
        //第二个参数是该值如果获取不到的默认值

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }
    private void signIn(){
        String platformAddress = "http://api.nlecloud.com:80/";
        _username = username.getText().toString();// "17674738454";
        _password = password.getText().toString();//"123456";
        if (_username.equals("") || _password.equals("")){
            Toast.makeText(this,"用户名或密码不为空",Toast.LENGTH_SHORT).show();
            return ;
        }
        final NetWorkBusiness netWorkBusiness = new NetWorkBusiness("",platformAddress);
        netWorkBusiness.signIn(new SignIn(_username, _password), new Callback<BaseResponseEntity<User>>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponseEntity<User>> call, @NonNull Response<BaseResponseEntity<User>> response) {
                BaseResponseEntity<User> baseResponseEntity = response.body();
                if (baseResponseEntity != null) {
                    if (baseResponseEntity.getStatus() == 0) {
                        //需要传输秘钥
                        //String accessToken = baseResponseEntity.getResultObj().getAccessToken();        //json数据返回
                        //成功.
                        editor.putString("username",_username);
                        editor.putString("password",_password);
                        editor.apply(); //提交信息.
                        String accessToken = baseResponseEntity.getResultObj().getAccessToken();
                        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("accessToken", accessToken);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, baseResponseEntity.getMsg(), Toast.LENGTH_SHORT).show();  //返回为空...
                    }
                }
            }
            @Override
            public void onFailure(Call<BaseResponseEntity<User>> call, Throwable t) {
                Toast.makeText(MainActivity.this,"登录失败 " + t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
