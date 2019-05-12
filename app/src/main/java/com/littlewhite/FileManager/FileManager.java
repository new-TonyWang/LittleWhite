package com.littlewhite.FileManager;

import android.app.Activity;

import android.content.Intent;

import android.os.Bundle;

import android.os.Environment;

import android.support.annotation.Nullable;

import android.view.ContextMenu;

import android.view.MenuItem;

import android.view.View;

import android.widget.AdapterView;

import android.widget.ListView;

import android.widget.TextView;

import android.widget.Toast;


import com.littlewhite.R;

import java.io.File;

import java.util.ArrayList;

import java.util.List;
import java.util.Objects;


public class FileManager extends Activity {



    ListView listView;

    TextView title;

    String dir;

    //用存放路劲

    FileAdapter adapter;

    //适配器

    List<File> dateList;
    OpenFileUtil openFileUtil;
    //File 数据

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.file_manager);
        openFileUtil = new OpenFileUtil(this);


        findVById();

        init();

        //初始化

    }

    private void findVById() {

        listView=(ListView)findViewById(R.id.file_listview);

        title=(TextView)findViewById(R.id.file_title);

    }

    //初始化

    private void init() {



        Intent intent=getIntent();

        //获取Intent的，接收activity传来的值，



        dir= intent.getStringExtra("dir");

        //如果为null，dir的值为 ：Environment.getExternalStorageDirectory().getAbsolutePath();


        if (dir!=null)

            ;

        else

            dir= Environment.getExternalStorageDirectory().getAbsolutePath();



        //获取title：让其显示文件路劲：如Android>data>com......

        if(intent.getStringExtra("title")!=null)

            title.setText(intent.getStringExtra("title"));

        else {
            String path = Objects.requireNonNull(getExternalFilesDir("receive")).getAbsolutePath();
            title.setText("历史记录:" +path.substring(path.lastIndexOf("Android"),path.length()));
        }



        //为listView注册上下文菜单，当长按某一个文件出现菜单：

        this.registerForContextMenu(listView);



        dateList=new ArrayList<>();

        adapter=new FileAdapter(this,getDate());

        listView.setAdapter(adapter);



        //listView 点击事件，当点击的文件为目录时，

        // 把dir的值赋值为：dir+点击的目录，再次跳到此页，既可以达到循环，不要再去新建一个activity在现实：

        // intent.putExtra("dir",dir+"/"+dateList.get(i).getName());

        //intent.putExtra("title",title.getText()+">"+dateList.get(i).getName());



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override

            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(dateList.get(i).isDirectory())

                {

                    Intent intent=new Intent(FileManager.this,FileManager.class);

                    intent.putExtra("dir",dir+"/"+dateList.get(i).getName());

                    intent.putExtra("title",title.getText()+">"+dateList.get(i).getName());

                    startActivity(intent);

                }else{

                    Intent  intent = openFileUtil.openFile(dir+"/"+dateList.get(i).getName());
                    startActivity(intent);
                }

            }

        });

    }







    //为上下文菜单添加菜单项

    @Override

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);

        menu.setHeaderTitle("文件操作");

        //menu.setHeaderIcon(R.drawable.ic_brightness_high_black_24dp);

        menu.add(1,1,1,"复制");

        menu.add(1,2,1,"粘贴");

        menu.add(1,3,1,"剪切");

        menu.add(1,4,1,"重命名");



    }



    //选中菜单项点击事件，这里就Toast一下，

    @Override

    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case 1:

                Toast.makeText(FileManager.this,"已复制",Toast.LENGTH_SHORT).show();

                break;

            case 2:

                Toast.makeText(FileManager.this,"已粘贴",Toast.LENGTH_SHORT).show();

                break;

            case 3:

                Toast.makeText(FileManager.this,"剪切",Toast.LENGTH_SHORT).show();

                break;

            case 4:

                Toast.makeText(FileManager.this,"重命名",Toast.LENGTH_SHORT).show();

                break;

        }

        return super.onContextItemSelected(item);

    }



    //获取dir下所有的文件

    public List< File> getDate() {



        File file=new File(dir);

        if(file.exists())

        {

            File[] file1=file.listFiles();

            for (File filename :

                    file1) {

                dateList.add(filename);

            }

        }



        return dateList;

    }

}
