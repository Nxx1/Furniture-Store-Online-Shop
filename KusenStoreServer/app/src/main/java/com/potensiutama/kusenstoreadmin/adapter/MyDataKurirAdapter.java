package com.potensiutama.kusenstoreadmin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.potensiutama.kusenstoreadmin.R;
import com.potensiutama.kusenstoreadmin.model.RekeningBankModel;
import com.potensiutama.kusenstoreadmin.model.ServerUserModel;

import java.util.ArrayList;

public class MyDataKurirAdapter extends BaseAdapter {
    Context context;
    private ArrayList<ServerUserModel> serverUserModelArrayList = new ArrayList<>();

    public void setKurirList(ArrayList<ServerUserModel> serverUserModelArrayList) {
        this.serverUserModelArrayList = serverUserModelArrayList;
    }

    public MyDataKurirAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return serverUserModelArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return serverUserModelArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View itemView = view;

        if (itemView == null) {
            itemView = LayoutInflater.from(context)
                    .inflate(R.layout.layout_data_kurir_item, viewGroup, false);
        }

        ViewHolder viewHolder = new ViewHolder(itemView);

        ServerUserModel serverUserModel = (ServerUserModel) getItem(i);
        viewHolder.bind(serverUserModel);
        return itemView;
    }

    private class ViewHolder {
        private TextView tNamaKurir, tNomorHP;

        ViewHolder(View view) {
            tNamaKurir = view.findViewById(R.id.txt_kurir_nama);
            tNomorHP = view.findViewById(R.id.txt_kurir_no_hp);
        }

        void bind(ServerUserModel serverUserModel) {

            tNamaKurir.setText(serverUserModel.getName());
            tNomorHP.setText(serverUserModel.getPhone());
        }
    }
}