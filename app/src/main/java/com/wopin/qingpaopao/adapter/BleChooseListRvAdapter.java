package com.wopin.qingpaopao.adapter;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wopin.qingpaopao.R;

import java.util.ArrayList;

public class BleChooseListRvAdapter extends RecyclerView.Adapter<BleChooseListRvAdapter.WifiChooseListViewHolder> {

    private ArrayList<BluetoothDevice> mBluetoothDevices;
    private BleChooseListAdapterCallback mBleChooseListAdapterCallback;

    public BleChooseListRvAdapter(BleChooseListAdapterCallback bleChooseListAdapterCallback) {
        this.mBluetoothDevices = new ArrayList<>();
        this.mBleChooseListAdapterCallback = bleChooseListAdapterCallback;
    }

    public void putBlueToothDevice(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice != null) {
            mBluetoothDevices.add(bluetoothDevice);
        } else {
            mBluetoothDevices.clear();
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WifiChooseListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WifiChooseListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_mine_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull WifiChooseListViewHolder holder, final int position) {
        final BluetoothDevice bluetoothDevice = mBluetoothDevices.get(position);
        String name = bluetoothDevice.getName();
        String address = bluetoothDevice.getAddress();
        holder.mTitleTv.setText(TextUtils.isEmpty(name) ? address : name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBleChooseListAdapterCallback.onBleDeviceChoose(bluetoothDevice, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBluetoothDevices.size();
    }

    class WifiChooseListViewHolder extends RecyclerView.ViewHolder {

        TextView mTitleTv;

        public WifiChooseListViewHolder(View itemView) {
            super(itemView);
            itemView.findViewById(R.id.iv_item_rv_mine_list).setVisibility(View.GONE);
            itemView.findViewById(R.id.next_item_rv_mine_list).setVisibility(View.GONE);
            mTitleTv = itemView.findViewById(R.id.tv_item_rv_mine_list);
        }
    }


    public interface BleChooseListAdapterCallback {
        void onBleDeviceChoose(BluetoothDevice bluetoothDevice, int position);
    }
}
