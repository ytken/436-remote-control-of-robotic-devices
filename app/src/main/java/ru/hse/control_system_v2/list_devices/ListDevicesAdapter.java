package ru.hse.control_system_v2.list_devices;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ru.hse.control_system_v2.DialogDevice;
import ru.hse.control_system_v2.MainActivity;
import ru.hse.control_system_v2.R;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static androidx.core.content.ContextCompat.getDrawable;

public class ListDevicesAdapter extends RecyclerView.Adapter<ListDevicesHolder> implements ListDevicesHolder.IListener {

    List<DeviceItem> mData = new ArrayList<>();
    DeviceClickedListener listener;
    Context context;
    MainActivity ma;

    @Override
    public void onDeviceClicked(int id, View itemView) {
        listener.deviceClicked(mData.get(id), itemView);
    }

    @Override
    public void onDeviceLongClicked(int id, View itemView) {
        listener.deviceLongClicked(mData.get(id), itemView);
    }

    public interface DeviceClickedListener {
        void deviceClicked(DeviceItem item, View itemView);
        void deviceLongClicked(DeviceItem item, View itemView);
    }

    public ListDevicesAdapter(Set<DeviceItem> data, @NonNull Context context){
        super();
        mData.addAll(data);
        this.context = context;
        if (context instanceof Activity){
            ma = (MainActivity) context;
        }
        listener = new MyListener();
    }

    public class MyListener implements ListDevicesAdapter.DeviceClickedListener{
        private static final String TAG = "MA_ItemsAdapter";
        @Override
        public void deviceClicked(DeviceItem item, View itemView) {
            View deviceImage = itemView.findViewById(R.id.icon_image_view);
            View deviceName = itemView.findViewById(R.id.item_name);
            View checkMark = itemView.findViewById(R.id.check_mark);
            //проверяю происходит ли выбор списка устройств
            if(MainActivity.devicesList.size() != 0) {
                Log.d(TAG, "...Список не пуст, нажато устройство...");
                //список не пуст
                if (!MainActivity.devicesList.get(0).getType().equals(item.getType())) {
                    //если протокол нажатого устройства отличается от уже выбранных
                    //значит это попытка добавить новое устройство
                    ma.showToast("Пожалуйста выберите устройства с одинаковыми протоколами");
                } else {
                    //если протокол совпал
                    //необходимо проверить на присутствие в списке
                    boolean wasAlreadySelected = false;
                    for (int i = 0; i < MainActivity.devicesList.size(); i++) {
                        if (MainActivity.devicesList.get(i).getMAC().equals(item.getMAC())) {
                            MainActivity.devicesList.remove(i);
                            wasAlreadySelected = true;
                            Log.d(TAG, "...В списке нашлось это устройство, удаляю...");
                            deviceImage.setVisibility(VISIBLE);
                            deviceName.setAlpha(1f);
                            checkMark.setVisibility(INVISIBLE);
                        }
                    }
                    if (!wasAlreadySelected) {
                        Log.d(TAG, "...В списке не нашлось это устройство, добавляю...");
                        MainActivity.devicesList.add(item);
                        MainActivity.fabToAddDevice.setVisibility(INVISIBLE);
                        MainActivity.fabToStartConnecting.setVisibility(VISIBLE);
                        deviceImage.setVisibility(INVISIBLE);
                        deviceName.setAlpha(0.6f);
                        checkMark.setVisibility(VISIBLE);
                    } else {
                        if(MainActivity.devicesList.size() == 0) {
                            Log.d(TAG, "...Список очищен...");
                            MainActivity.fabToStartConnecting.setVisibility(INVISIBLE);
                            MainActivity.fabToAddDevice.setVisibility(VISIBLE);
                        }
                    }
                }
            } else {
                Log.d(TAG, "...Список пуст, открываю диалог...");
                //список пуст, открываем диалог для одного устройства
                DialogDevice dialog = new DialogDevice();
                Bundle args = new Bundle();
                dialog.setArguments(args);
                MainActivity.currentDevice = item;
                dialog.show(ma.getSupportFragmentManager(), "dialog");
            }
        }

        @Override
        public void deviceLongClicked(DeviceItem item, View itemView) {
            View deviceImage = itemView.findViewById(R.id.icon_image_view);
            View deviceName = itemView.findViewById(R.id.item_name);
            View checkMark = itemView.findViewById(R.id.check_mark);
            if(MainActivity.devicesList.size() == 0){
                Log.d(TAG, "...Список пуст, добавляю устройство...");
                MainActivity.devicesList.add(item);
                MainActivity.fabToAddDevice.setVisibility(INVISIBLE);
                MainActivity.fabToStartConnecting.setVisibility(VISIBLE);
                deviceImage.setVisibility(INVISIBLE);
                //deviceImage.setAlpha(0.6f);
                deviceName.setAlpha(0.6f);
                checkMark.setVisibility(VISIBLE);
                //deviceImage.setForeground(getDrawable(ma, R.drawable.ic_baseline_check_24));
            } else {
                if(!MainActivity.devicesList.get(0).getType().equals(item.getType())){
                    ma.showToast("Пожалуйста выберите устройства с одинаковыми протоколами");
                    ma.showToast(MainActivity.devicesList.get(0).type);
                    ma.showToast(item.getType());
                } else {
                    boolean wasAlreadySelected = false;
                    for(int i = 0; i < MainActivity.devicesList.size(); i++){
                        if(MainActivity.devicesList.get(i).getMAC().equals(item.getMAC())){
                            wasAlreadySelected = true;
                        }
                    }
                    if(!wasAlreadySelected){
                        MainActivity.devicesList.add(item);
                        deviceImage.setVisibility(INVISIBLE);
                        deviceName.setAlpha(0.6f);
                        checkMark.setVisibility(VISIBLE);
                    }
                }

            }
        }
    }

    @NonNull
    @Override
    public ListDevicesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View layout = inflater.inflate(R.layout.item_list_devices, parent, false);
        return new ListDevicesHolder(layout, this);
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

}
