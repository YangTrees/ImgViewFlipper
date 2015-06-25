package com.example.yl.imgviewflipper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;


public class SetIP extends Activity {

    String ipname = "127.0.0.1";
    int portname = 6000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* 设置全屏 */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);   //定义一个AlertDialog.Builder对象
        builder.setTitle("登录服务器对话框");                          // 设置对话框的标题

        //装载/res/layout/login.xml界面布局
        RelativeLayout loginForm = (RelativeLayout)getLayoutInflater().inflate( R.layout.activity_set_ip, null);
        final EditText iptext = (EditText)loginForm.findViewById(R.id.ipedittext);
        final EditText porttext = (EditText)loginForm.findViewById(R.id.portedittext);
        builder.setView(loginForm);                              // 设置对话框显示的View对象

        // 为对话框设置一个“登录”按钮
        builder.setPositiveButton("登录"
                // 为按钮设置监听器
                , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //此处可执行登录处理
                ipname = iptext.getText().toString().trim();
                portname = Integer.parseInt(porttext.getText().toString().trim());

                if(ipname==null) ipname = "192.168.253.1";
                if(portname==0) portname = 6000;
                Bundle data = new Bundle();
                data.putString("ipname",ipname);
                data.putInt("portname",portname);
                Intent intent = new Intent(SetIP.this,MainActivity.class);
                intent.putExtras(data);
                startActivity(intent);
            }
        });
        // 为对话框设置一个“取消”按钮
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //取消登录，不做任何事情。
                System.exit(1);
            }
        });

        //创建、并显示对话框
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_i, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
