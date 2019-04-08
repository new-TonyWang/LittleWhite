package com.littlewhite.History;


import android.widget.CompoundButton;

import java.util.List;

public class CheckBoxListener implements CompoundButton.OnCheckedChangeListener {

    /**
     * 列表item的下标位置
     */
    int position;
    List checkedIndexList;
    public CheckBoxListener(int position, List checkedIndexList) {
        this.position = position;
        this.checkedIndexList = checkedIndexList;
    }
    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
        if (isChecked) {
            checkedIndexList.add(position);
        } else {
            checkedIndexList.remove((Integer) position);
        }
    }
}
