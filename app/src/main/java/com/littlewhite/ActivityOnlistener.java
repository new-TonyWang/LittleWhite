package com.littlewhite;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.littlewhite.ReceiveFile.ReceiveActivity;
import com.littlewhite.ReceiveFile.SqllitUtil.SqllitData;

import java.util.ArrayList;

public class ActivityOnlistener extends AppCompatActivity implements View.OnClickListener {
    //private SqllitData sqllitData = new SqllitData(this);
    protected  ArrayList<Button> list = new ArrayList<>();
    @Override
    public void onClick(View v) {
        setButtonsNotEnable();
        switch(v.getId()){
            case R.id.settings:
                Intent settings = new Intent(this,SettingsActivity.class);//跳转到设置界面
                startActivity(settings);
                break;
            case R.id.action:
                Intent action = new Intent(this,ActionActivity.class);
                //action.putExtras();
                startActivity(action);
                break;
            case R.id.history:
                break;
            case R.id.send:
                Intent send = new Intent(this,SendFileActivity.class);
                startActivity(send);
                break;
            case R.id.receive:
                Intent receive = new Intent(this, ReceiveActivity.class);
                startActivity(receive);
                break;
        }
        setButtonsEnable();
    }

    /**
     * 设置监听器(传说中的代码复用??)
     * @param v
     * @param listener
     */
    protected void setOnclickListener(View v,View.OnClickListener listener){
        v.setOnClickListener(listener);
    }
    protected void setButtonsNotEnable(){
        for(int i = 0;i<this.list.size();i++){
            list.get(i).setEnabled(false);
        }
    }
    protected void setButtonsEnable(){
        for(int i = 0;i<this.list.size();i++){
            list.get(i).setEnabled(true);
        }
    }
}
