package ru.hse.control_system_v2.list_devices;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ru.hse.control_system_v2.R;

public class ListDevicesAdapter extends RecyclerView.Adapter<ListDevicesHolder> implements ListDevicesHolder.IListener {

    List<DeviceItem> mData = new ArrayList<>();
    DeviceClickedListener listener;

    public interface DeviceClickedListener {
        void deviceClicked(DeviceItem item);
    }

    public ListDevicesAdapter(Set<DeviceItem> data, DeviceClickedListener nlistener){
        super();
        mData.addAll(data);
        listener = nlistener;
    }


    @NonNull
    @Override
    public ListDevicesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View layout = inflater.inflate(R.layout.item_list_devices, parent, false);
        return new ListDevicesHolder(
                layout,
                this
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ListDevicesHolder holder, int position) {
        DeviceItem item = mData.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onDeviceClicked(int id) {
        listener.deviceClicked(mData.get(id));
    }
}
