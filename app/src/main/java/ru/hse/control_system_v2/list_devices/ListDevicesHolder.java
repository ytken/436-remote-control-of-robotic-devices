package ru.hse.control_system_v2.list_devices;

import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import ru.hse.control_system_v2.R;

public class ListDevicesHolder extends RecyclerView.ViewHolder {
    TextView mName, mAddress;
    Resources resources;

    public ListDevicesHolder(@NonNull View itemView, IListener listener) {
        super(itemView);
        resources = itemView.getResources();
        mName = itemView.findViewById(R.id.item_name);
        mAddress = itemView.findViewById(R.id.item_address);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeviceClicked(getAdapterPosition());
            }
        });
    }

    interface IListener {
        void onDeviceClicked(int id);
    }

    void bind(DeviceItem item) {
        mName.setText(item.name);
        mAddress.setText(item.MAC);
    }

}
