package com.standard.wechat.wechatassist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btnOpenService;
    private Button btnCloseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void initView(){
        btnOpenService = (Button) findViewById(R.id.btn_openService);
        btnCloseService = (Button) findViewById(R.id.btn_closeService);
    }

    private void initEvents(){
        btnOpenService.setOnClickListener(this);
        btnCloseService.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_openService:
                break;
            case R.id.btn_closeService:
                break;
            default:
                break;
        }
    }
}
