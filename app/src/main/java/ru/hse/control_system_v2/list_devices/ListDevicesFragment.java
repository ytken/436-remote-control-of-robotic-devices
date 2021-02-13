package ru.hse.control_system_v2.list_devices;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Set;

import ru.hse.control_system_v2.DialogDevice;
import ru.hse.control_system_v2.Manual_mode;
import ru.hse.control_system_v2.R;

public class ListDevicesFragment extends Fragment {
    ListDevicesAdapter adapter;
    protected static final String EXTRAS = "NUMBER";
    int MY_REQUEST_CODE = 5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_devices, container, false);
    }

    class MyListener extends Fragment implements ListDevicesAdapter.DeviceClickedListener {
        @Override
        public void deviceClicked(DeviceItem item) {
            DialogDevice dialog = new DialogDevice();
            Bundle args = new Bundle();
            args.putInt("id", item.id);
            args.putString("name", item.name);
            args.putString("MAC", item.getMAC());
            args.putString("protocol", item.getType());
            dialog.setArguments(args);
            //dialog.setTargetFragment(this, MY_REQUEST_CODE);
            dialog.show(ListDevicesFragment.this.getParentFragmentManager(), "dialog");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final RecyclerView recycler = view.findViewById(R.id.recycler_devices);
        adapter = new ListDevicesAdapter(DeviceRepository.getInstance(getContext()).list(), new MyListener());
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null) {return;}
        String message = data.getStringExtra("message");
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }
}
