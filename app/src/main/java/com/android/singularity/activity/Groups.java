package com.android.singularity.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.singularity.R;

import java.util.List;

public class Groups extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
    }

    class CustomAdapter extends BaseAdapter {

        Context mContext;
        List list;

//        public CustomAdapter(List<TallyCard> list, Context context) {
//            this.list = list;
//            this.mContext = context;
//        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View grid;
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //ListCard lc = list.get(position);
            if (convertView == null) {
                grid = layoutInflater.inflate(R.layout.grid_item, null);
            } else {
                grid = convertView;
            }
            return grid;
        }
    }
}