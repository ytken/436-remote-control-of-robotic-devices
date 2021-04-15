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
    boolean isSelected = false;

    public ListDevicesHolder(@NonNull View itemView, IListener listener) {
        super(itemView);
        resources = itemView.getResources();
        mName = itemView.findViewById(R.id.item_name);
        //mAddress = itemView.findViewById(R.id.item_address);

        itemView.setOnClickListener(v -> {
            listener.onDeviceClicked(getAdapterPosition(), itemView);
        });
        itemView.setOnLongClickListener(v -> {
            listener.onDeviceLongClicked(getAdapterPosition(), itemView);
            return true;
        });
    }

    interface IListener {
        void onDeviceClicked(int id, View itemView);
        void onDeviceLongClicked(int id, View itemView);
    }

    void bind(DeviceItem item) {
        mName.setText(item.name);
        //mAddress.setText(item.MAC);

    }

}
