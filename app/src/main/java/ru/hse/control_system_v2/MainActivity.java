package ru.hse.control_system_v2;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;


import java.util.ArrayList;

import ru.hse.control_system_v2.dbdevices.AddDeviceDBActivity;
import ru.hse.control_system_v2.dbdevices.DeviceDBHelper;
import ru.hse.control_system_v2.dbprotocol.AddProtocolDBActivity;
import ru.hse.control_system_v2.dbprotocol.ProtocolDBHelper;
import ru.hse.control_system_v2.list_devices.DeviceItem;
import ru.hse.control_system_v2.list_devices.DeviceRepository;
import ru.hse.control_system_v2.list_devices.ListDevicesAdapter;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity  implements SwipeRefreshLayout.OnRefreshListener
{
    DeviceDBHelper dbdevice;
    ProtocolDBHelper dbprotocol;
    int bdUpdated = 0;
    //инициализация swipe refresh
    SwipeRefreshLayout swipeToRefreshLayout;
    BluetoothAdapter btAdapter;
    boolean stateOfFabToEnBt;
    public static ExtendedFloatingActionButton fabToEnBt;

    public static ExtendedFloatingActionButton fabToStartConnecting;
    public static RecyclerView recycler;
    ListDevicesAdapter adapter = null;
    TextView headerText;
    boolean isItemSelected;
    GridLayoutManager gridLayoutManager;
    private Drawer.Result drawerResult = null;
    public static ArrayList<DeviceItem> devicesList;
    public static DeviceItem currentDevice;
    Button buttonToAddDevice;
    Button buttonToAddDeviceViaMAC;
    public static BottomSheetBehavior<View> bottomSheetBehavior;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        dbdevice = DeviceDBHelper.getInstance(getApplicationContext());
        dbprotocol = ProtocolDBHelper.getInstance(getApplicationContext());

        devicesList = new ArrayList<>();

        progressOfConnectionDialog =new ProgressDialog(this);

        gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3, LinearLayoutManager.VERTICAL, false);

        registerReceiver(mMessageReceiverNotSuccess, new IntentFilter("not_success"));
        registerReceiver(mMessageReceiverSuccess, new IntentFilter("success"));
        registerReceiver(mMessageReceiverServiceStarted, new IntentFilter("serviceStarted"));


        headerText = findViewById(R.id.paired_devices_title_add_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        swipeToRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeToRefreshLayout.setOnRefreshListener(this);
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        buttonToAddDeviceViaMAC = findViewById(R.id.button_manual_mac);
        buttonToAddDevice = findViewById(R.id.button_add_device);

        fabToEnBt = findViewById(R.id.floating_action_button_En_Bt);
        fabToStartConnecting = findViewById(R.id.floating_action_button_start_sending_data);
        fabToStartConnecting.setOnClickListener(view -> {
            fabToStartConnecting.setEnabled(false);
            Intent startBluetoothConnectionService = new Intent(this, BluetoothConnectionService.class);
            startBluetoothConnectionService.putExtra("protocol", devicesList.get(0).getType());
            startService(startBluetoothConnectionService);
        });
        buttonToAddDevice.setOnClickListener(view -> {
            Intent intent = new Intent();
            startActivityForResult(intent.setClass(MainActivity.this, AddDeviceDBActivity.class), 10);
        });
        buttonToAddDeviceViaMAC.setOnClickListener(view -> {
            DialogSaveDeviceWithMAC dialog = new DialogSaveDeviceWithMAC();
            dialog.show(this.getSupportFragmentManager(), "dialog");
        });
        fabToEnBt.setOnClickListener(view -> {
            Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            int REQUEST_ENABLE_BT = 1;
            startActivityForResult(intentBtEnabled, REQUEST_ENABLE_BT);
        });

        // настройка поведения нижнего экрана
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        TextView textView = findViewById(R.id.bottom_sheet_text_view);
        textView.setOnClickListener(view -> {
            if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        fabToEnBt.setVisibility(INVISIBLE);
        fabToStartConnecting.setVisibility(INVISIBLE);



        stateOfFabToEnBt = false;
        dbdevice = new DeviceDBHelper(this);

        recycler = findViewById(R.id.recycler_main);
        recycler.setLayoutManager(gridLayoutManager);
        recycler.setHasFixedSize(true);
        adapter = new ListDevicesAdapter(DeviceRepository.getInstance(getApplicationContext()).list(), this);
        recycler.setAdapter(adapter);

        // Инициализируем Navigation Drawer
        // Обработка клика
        // Обработка длинного клика
        drawerResult = new Drawer()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withHeader(R.layout.drawer_header)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_add_protocol).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_code_24, null)).withIdentifier(0),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_data_base).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_delete_outline_24, null)).withIdentifier(1),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_code).withIcon(FontAwesome.Icon.faw_github).withIdentifier(2)
                )
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Скрываем клавиатуру при открытии Navigation Drawer
                        //InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                        //inputMethodManager.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), 0);
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {

                    }
                })
                .withOnDrawerItemClickListener((parent, view, position, id, drawerItem) -> {
                    drawerResult.closeDrawer();
                    if (id == 0){
                        startActivity(new Intent().setClass(MainActivity.this, AddProtocolDBActivity.class));

                    }
                    if (id == 1){
                        Log.d("button", "button delete");
                        AlertDialog dialog =new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Подтверждение")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setMessage("Вы действительно хотите удалить все имеющиеся устройства?")
                                .setPositiveButton("OK", (dialog1, whichButton) -> {
                                    DeviceDBHelper helper = new DeviceDBHelper(MainActivity.this);
                                    dbdevice.onUpgrade(helper.getReadableDatabase(), dbdevice.DATABASE_VERSION, dbdevice.DATABASE_VERSION + 1);
                                    dbdevice = helper;
                                    bdUpdated = 1;
                                    onRefresh();
                                })
                                .setNegativeButton("Отмена", null)
                                .create();
                        dialog.show();

                    }
                    if (id == 3){
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ytken/436-remote-control-of-robotic-devices"));
                        startActivity(browserIntent);

                    }
                })
                .withOnDrawerItemLongClickListener((parent, view, position, id, drawerItem) -> {
                    if (id>=0) {
                        Toast.makeText(MainActivity.this, MainActivity.this.getString(((Nameable) drawerItem).getNameRes()), Toast.LENGTH_SHORT).show();
                    }
                    return false;
                })
                .build();

    }


    ProgressDialog progressOfConnectionDialog;
    //Результат работы Service
    private final BroadcastReceiver mMessageReceiverServiceStarted = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            showToast("Соединение начато");
            devicesList.clear();
            progressOfConnectionDialog.setMessage("Соединение...");
            progressOfConnectionDialog.setCancelable(false);
            progressOfConnectionDialog.setInverseBackgroundForced(false);
            progressOfConnectionDialog.show();
            isItemSelected = true;

        }
    };
    //Результат работы Service
    private final BroadcastReceiver mMessageReceiverNotSuccess = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            showToast("Подключение не успешно");
            onRefresh();
            progressOfConnectionDialog.hide();
        }
    };

    private final BroadcastReceiver mMessageReceiverSuccess = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Устройство подключено, Service выполнился успешно
            Bundle arguments = intent.getExtras();
            String classDevice = arguments.get("protocol").toString();
            Intent startSendingData = new Intent(MainActivity.this, Manual_mode.class);
            startSendingData.putExtra("protocol", classDevice);
            startSendingData.putExtra("length", dbprotocol.getLength(classDevice));
            startActivity(startSendingData);
            fabToStartConnecting.setEnabled(true);
            progressOfConnectionDialog.hide();
        }
    };



    //Обновляем внешний вид приложения, скрываем и добавляем нужные элементы интерфейса
    @Override
    public void onRefresh() {
        devicesList.clear();
        fabToStartConnecting.setEnabled(true);
        fabToStartConnecting.setVisibility(INVISIBLE);
        if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        if (btIsEnabledFlagVoid()) {
            headerText.setText(R.string.favorites_devices);
            // Bluetooth включён. Предложим пользователю добавить устройства и начать передачу данных.
            if (stateOfFabToEnBt) {
                // Bluetooth включён, надо скрыть кнопку включения Bluetooth
                fabToEnBt.setVisibility(INVISIBLE);
                stateOfFabToEnBt = false;
            }
            invalidateOptionsMenu();
            bottomSheetBehavior.setHideable(false);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            // Bluetooth включён, надо показать кнопку добавления устройств и другую информацию
            adapter = new ListDevicesAdapter(DeviceRepository.getInstance(getApplicationContext()).list(), this);
            recycler.setAdapter(adapter);
        } else {
            headerText.setText(R.string.suggestionEnableBluetooth);
            recycler.setAdapter(null);
            invalidateOptionsMenu(); // now onCreateOptionsMenu(...) is called again
            bottomSheetBehavior.setHideable(true);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            if (!stateOfFabToEnBt) {
                // Bluetooth выключён, надо показать кнопку включения Bluetooth
                fabToEnBt.setVisibility(VISIBLE);
                stateOfFabToEnBt = true;
            }
        }
        // Приложение обновлено, завершаем анимацию обновления
        swipeToRefreshLayout.setRefreshing(false);
    }

    // проверка на наличие Bluetooth адаптера; дальнейшее продолжение работы в случае наличия
    public void checkForBtAdapter() {
        if (btAdapter != null) {
            onRefresh();
        } else {
            System.out.println("There is no bluetooth adapter on device!");
            // объект Builder для создания диалогового окна
            //suggestionNoBtAdapter
            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this).create();
            dialog.setTitle(getString(R.string.error));
            dialog.setMessage(getString(R.string.suggestionNoBtAdapter));
            dialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL, "OK",
                    (dialog1, which) -> {
                        // Closes the dialog and terminates the activity.
                        dialog1.dismiss();
                        MainActivity.this.finish();
                    });
        }
    }

    private static long back_pressed = 0;

    @Override
    public void onBackPressed() {

        if(drawerResult.isDrawerOpen()){
            drawerResult.closeDrawer();
        } else {
            if (back_pressed + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
            } else {
                showToast("Нажмите ещё раз для выхода");
            }
            back_pressed = System.currentTimeMillis();
        }
    }

    // Метод для вывода всплывающих данных на экран
    public void showToast(String outputInfoString) {
        System.out.println("show toast: " + outputInfoString);
        Toast outputInfoToast = Toast.makeText(getApplicationContext(), outputInfoString, Toast.LENGTH_SHORT);
        outputInfoToast.show();
    }

    //True, если Bluetooth включён
    public boolean btIsEnabledFlagVoid(){
        return btAdapter.isEnabled();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkForBtAdapter();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkForBtAdapter();
    }
}
