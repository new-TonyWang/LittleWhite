package com.littlewhite.FileManager;

import android.content.Context;

import android.view.View;

import android.view.ViewGroup;

import android.widget.BaseAdapter;

import android.widget.ImageView;

import android.widget.TextView;

import com.littlewhite.R;

import java.io.File;

import java.util.List;



/**

 * Created by Administrator on 2018/2/5/005.

 */



public class FileAdapter extends BaseAdapter {

//传递File数据和上下文；

    List<File> list;

    Context context;



    public FileAdapter(Context context,List<File> list) {

        this.list = list;

        this.context=context;

    }



    @Override

    public int getCount() {

        return list.size();

    }



    @Override

    public Object getItem(int i) {

        return list.get(i);

    }



    @Override

    public long getItemId(int i) {

        return i;

    }



    @Override

    public View getView(int i, View view, ViewGroup viewGroup) {

        view= View.inflate(context,R.layout.item_file,null);



        File file=list.get(i);

        ImageView image_photo= (ImageView) view.findViewById(R.id.file_image);

        TextView tv_name= (TextView) view.findViewById(R.id.filename);

        TextView tv_age= (TextView) view.findViewById(R.id.isDictionary);



        //如果某个文件是目录：就在后面显示》；否则显示空



        if (!file.isDirectory()) {

            tv_age.setText(" ");

            image_photo.setImageResource(R.drawable.notdictionary);

        } else{

            tv_age.setText("▶");
            image_photo.setImageResource(R.drawable.isdictionary);

        }


        if(i==0){
            tv_name.setText("../");
            return view;
        }
        tv_name.setText(file.getName());



        return view;



    }

}
