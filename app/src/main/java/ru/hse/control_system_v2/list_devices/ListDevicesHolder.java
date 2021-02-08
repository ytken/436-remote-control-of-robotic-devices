package ru.hse.control_system_v2.list_devices;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.hse.control_system_v2.R;

public class ListDevicesHolder extends RecyclerView.ViewHolder {
    TextView mName, mAddress, mClass;

    public ListDevicesHolder(@NonNull View itemView, IListener listener) {
        super(itemView);
        mName = itemView.findViewById(R.id.item_name);
        mAddress = itemView.findViewById(R.id.item_address);
        mClass = itemView.findViewById(R.id.item_class);

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
        if (item.type.equals("main_protocol"))
            mClass.setText("Arduino основной");
        else if (item.type.equals("wheel_platform"))
            mClass.setText("Arduino колесная");
        else
            mClass.setText("Не определено");

    }

}
