package ru.hse.control_system_v2.list_devices;

import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.hse.control_system_v2.MainActivity;
import ru.hse.control_system_v2.R;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class ListDevicesHolder extends RecyclerView.ViewHolder {
    TextView mName, mAddress;
    Resources resources;

    public ListDevicesHolder(@NonNull View itemView, IListener listener) {
        super(itemView);
        resources = itemView.getResources();
        mName = itemView.findViewById(R.id.item_name);
        //mAddress = itemView.findViewById(R.id.item_address);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeviceClicked(getAdapterPosition());
                //TODO
                //if(MainActivity.devicesList.size() != 0){
                        //if(v.getBackground() != v.getResources().getDrawable(R.drawable.background_selected)){
                            //v.setBackground(v.getResources().getDrawable(R.drawable.background_selected));
                        //} else{
                            //v.setBackground(v.getResources().getDrawable(R.drawable.background_not_selected));
                    //}
                //}
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onDeviceLongClicked(getAdapterPosition());
                //TODO
                //if(MainActivity.devicesList.size() == 0){
                    //v.setBackground(v.getResources().getDrawable(R.drawable.background_selected));
                //}
                return true;
            }
        });
    }

    interface IListener {
        void onDeviceClicked(int id);
        void onDeviceLongClicked(int id);
    }

    void bind(DeviceItem item) {
        mName.setText(item.name);
        //mAddress.setText(item.MAC);

    }

}
