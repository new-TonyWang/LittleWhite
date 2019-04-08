package com.littlewhite.History;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.littlewhite.R;
import com.littlewhite.ReceiveFile.SqllitUtil.FileInfo;

import java.util.List;
/**
 * 自定义listview的适配器
 */
public class HistoryAdapter extends BaseAdapter {
    private List<FileInfo> listData;
    private LayoutInflater inflater;
    private List<CheckBox> checkBoxList;
    private List<TextView> percentList;
    private List<Integer> checkedIndexList;
    public HistoryAdapter(Context context, List<FileInfo> listData,List<CheckBox> checkBoxList,List<TextView> percentList, List checkedIndexList) {
        this.listData = listData;
        this.checkBoxList = checkBoxList;
        this.percentList = percentList;
        this.checkedIndexList = checkedIndexList;
        inflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return listData.size();
    }
    @Override
    public Object getItem(int arg0) {
        return listData.get(arg0);
    }
    @Override
    public long getItemId(int arg0) {
        return arg0;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_item, null);
            viewHolder.name = (TextView) convertView.findViewById(R.id.textview);
            viewHolder.progress = (TextView) convertView.findViewById(R.id.progress);
            viewHolder.checkbox = (CheckBox) convertView.findViewById(R.id.checkbox);
            //将item中的checkbox放到checkBoxList中
            checkBoxList.add(viewHolder.checkbox);
            percentList.add(viewHolder.progress);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.name.setText(listData.get(position).getFileName());
        StringBuilder percentage = new StringBuilder();
        percentage.append(listData.get(position).getReceivedNum());
        percentage.append("/");
        percentage.append(listData.get(position).getTotalSymbolNum());
        viewHolder.progress.setText(percentage.toString());
        viewHolder.checkbox.setOnCheckedChangeListener(new CheckBoxListener(position,this.checkedIndexList));
        return convertView;
    }
    class ViewHolder {
        TextView name;
        TextView progress;
        CheckBox checkbox;
    }
}
