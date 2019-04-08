package com.littlewhite.History;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

import com.littlewhite.R;
import com.littlewhite.ReceiveFile.SqllitUtil.FileInfo;
import com.littlewhite.ReceiveFile.SqllitUtil.SqllitData;

public class HistoryActivity extends Activity{
    private ListView listview;
    private HistoryAdapter historyAdapter;
    private HistoryManager historyManager;
    //private SqllitData sqllitData;
    /**
    * 列表的数据源
    */
    private List<FileInfo> listData;
    /**
    * 记录选中item的下标
    */
    private List<Integer> checkedIndexList;
    /**
    * 保存每个item中的checkbox
    */
    private List<CheckBox> checkBoxList;
    private List<TextView> percentList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

       initListData();
        initView();
    }
    /**
    * 初始化列表的数据源
    */
    public void initListData() {
       //静态赋值
        historyManager = new HistoryManager(this);
      this.listData =  historyManager.SearchHistory();
      /* listData = new ArrayList<String>();
        for (int i = 0; i < 80; i++) {
            listData.add("item" + i);
        }
       //listData.add("none");
       */
    }
    /**
    * 初始化控件
    */
    public void initView() {
        listview = (ListView) findViewById(R.id.listview);

        listview.setAdapter(historyAdapter);
        //监听listview的长按事件
        listview.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int arg2, long arg3) {
                //将checkbox设置为可见
                for (int i = 0; i < checkBoxList.size(); i++) {
                    percentList.get(i).setVisibility(View.INVISIBLE);
                    checkBoxList.get(i).setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

       checkedIndexList = new ArrayList<Integer>();
        checkBoxList = new ArrayList<CheckBox>();
        historyAdapter = new HistoryAdapter(getApplicationContext(), listData,checkBoxList,percentList,checkedIndexList);
    }
    /**
    * 编辑按钮的点击事件
    */
    public void click_editButton(View v) {
        //将checkbox设置为可见
        for (int i = 0; i < checkBoxList.size(); i++) {
            percentList.get(i).setVisibility(View.INVISIBLE);
            checkBoxList.get(i).setVisibility(View.VISIBLE);
        }
    }
    /**
    * 删除按钮的点击事件
    */
    public void click_deleteButton(View v) {
        //先将checkedIndexList中的元素从大到小排列,否则可能会出现错位删除或下标溢出的错误
        checkedIndexList = sortCheckedIndexList(checkedIndexList);
        LinkedList<Integer> IDS = new LinkedList<>();
        for (int i = 0; i < checkedIndexList.size(); i++) {
            //需要强转为int,才会删除对应下标的数据,否则默认删除与括号中对象相同的数据
            IDS.add(listData.get(checkedIndexList.get(i)).getID());
           listData.remove((int) checkedIndexList.get(i));
            checkBoxList.remove(checkedIndexList.get(i));
            percentList.remove(percentList.get(i));
        }
        this.historyManager.DeleteFileLog(IDS);//删除数据库内容
        for (int i = 0; i < checkBoxList.size(); i++) {
            //将已选的设置成未选状态
            checkBoxList.get(i).setChecked(false);
            //将checkbox设置为不可见
            checkBoxList.get(i).setVisibility(View.INVISIBLE);
            percentList.get(i).setVisibility(View.VISIBLE);
        }
        //更新数据源
        historyAdapter.notifyDataSetChanged();
        //清空checkedIndexList,避免影响下一次删除
        checkedIndexList.clear();
    }
    /**
    * 取消按钮的点击事件
    */
    public void click_cancelButton(View v) {
        for (int i = 0; i < checkBoxList.size(); i++) {
            //将已选的设置成未选状态
            checkBoxList.get(i).setChecked(false);
            //将checkbox设置为不可见
            checkBoxList.get(i).setVisibility(View.INVISIBLE);
            percentList.get(i).setVisibility(View.VISIBLE);
        }
    }
    /**
    * 对checkedIndexList中的数据进行从大到小排序
    */
    public List<Integer> sortCheckedIndexList(List<Integer> list) {
        int[] ass = new int[list.size()];//辅助数组
        for (int i = 0; i < list.size(); i++) {
            ass[i] = list.get(i);
        }
        Arrays.sort(ass);
        list.clear();
        for (int i = ass.length - 1; i >= 0; i--) {
            list.add(ass[i]);
        }
        return list;
    }
     class CheckBoxListener  implements  OnCheckedChangeListener{
        private int position;
        public CheckBoxListener(int position){
        this.position = position;
        }
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {

                checkedIndexList.add(position);

            } else {

                checkedIndexList.remove((Integer) position);

            }
        }
    }
    /**
    * checkbox的监听器
    */

}
