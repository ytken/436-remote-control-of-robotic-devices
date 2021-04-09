package ru.hse.control_system_v2;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ru.hse.control_system_v2.list_devices.DeviceItem;

public class BluetoothConnectionService extends Service {
    public static BluetoothDevice device;
    public String selectedDevice;
    private static final String TAG = "SendDataActivity";
    // SPP UUID сервиса
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public BluetoothAdapter btAdapter;
    public boolean stateOfConnection = false;
    BluetoothSocket clientSocket;
    String classDevice;
    String deviceName;
    int numberOfDevices;
    ExecutorService executorService;
    ArrayList<DeviceItem> devicesList;
    static ArrayList<MyRun> treadList;
    static ArrayList<Boolean> resultOfConnection;
    static ArrayList<BluetoothSocket> socketList;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent serviceStarted;
        serviceStarted = new Intent("serviceStarted");
        Log.d(TAG, "...Соединение начато...");
        sendBroadcast(serviceStarted);

        resultOfConnection = new ArrayList<>();
        treadList = new ArrayList<>();
        socketList = new ArrayList<>();
        devicesList = new ArrayList<>();

        Bundle arguments = intent.getExtras();
        classDevice = arguments.get("protocol").toString();
        devicesList.addAll(MainActivity.devicesList);
        executorService = Executors.newFixedThreadPool(8);
        Log.d(TAG, "...Соединение начато...");
        for(int i = 0; i < devicesList.size(); i++){
            Log.d(TAG, "...Создаю массивы данных...");
            MyRun mr = new MyRun(devicesList.get(i).getMAC(), i);
            resultOfConnection.add(i, false);
            treadList.add(i, mr);
            socketList.add(i, null);
        }
        for(int i = 0; i < devicesList.size(); i++){
            Log.d(TAG, "...Создаю потоки...");
            executorService.execute(treadList.get(i));
        }
        return Service.START_NOT_STICKY;
    }

    class MyRun implements Runnable {
        String deviceMAC;
        int i;

        public MyRun(String deviceMAC, int i) {
            this.deviceMAC = deviceMAC;
            this.i = i;
        }

        public void run() {
            btAdapter = BluetoothAdapter.getDefaultAdapter();
            if (btIsEnabledFlagVoid()) {
                device = btAdapter.getRemoteDevice(deviceMAC);
                // Попытка подключиться к устройству
                try {
                    socketList.set(i, (BluetoothSocket) device.getClass().getMethod("createRfcommSocketToServiceRecord", UUID.class).invoke(device, MY_UUID));
                    Log.d(TAG, "...Создаю сокет...");
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    Log.d("BLUETOOTH", e.getMessage());
                    Log.d(TAG, "...Создание сокета неуспешно...");
                    resultOfConnection.set(i, false);
                    e.printStackTrace();
                }
                try {
                    socketList.get(i).connect();
                    // Отключаем поиск устройств для сохранения заряда батареи
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    resultOfConnection.set(i, true);
                    Log.d(TAG, "...Подключаюсь к сокету...");
                } catch (IOException e) {
                    resultOfConnection.set(i, false);
                    Log.d(TAG, "...Соединение через сокет неуспешно...");
                    try {
                        // В случае ошибки пытаемся закрыть соединение
                        socketList.get(i).close();
                    } catch (IOException closeException) {
                        Log.d("BLUETOOTH", e.getMessage());
                        e.printStackTrace();
                    }
                    Log.d("BLUETOOTH", e.getMessage());
                    e.printStackTrace();
                }
            } else {
                resultOfConnection.set(i, false);
            }
            resultOfConnection();
            Log.d(TAG, "...Попытка подключения для текущего устройства завершено...");
        }
    }

    //возвращает true, если bluetooth включён
    public boolean btIsEnabledFlagVoid() {
        return btAdapter.isEnabled();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "...Сервис остановлен...");
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Передаём данные о статусе соединения в Main Activity
    public void resultOfConnection() {
        Intent resultOfConnectionIntent;
        boolean isSuccess = false;
        for(int i = 0; i < devicesList.size(); i++){
            if(resultOfConnection.get(i).equals(true)){
                isSuccess = true;
            }
        }
        if(isSuccess){
            resultOfConnectionIntent = new Intent("success");
            resultOfConnectionIntent.putExtra("protocol", classDevice);
            SocketHandler.setSocketList(socketList);
            SocketHandler.setDevicesList(devicesList);
            SocketHandler.setResultOfConnection(resultOfConnection);
            Log.d(TAG, "...Соединение успешно, передаю результат в Main Activity...");
        } else{
            resultOfConnectionIntent = new Intent("not_success");
            Log.d(TAG, "...Соединение неуспешно, передаю результат в Main Activity...");
        }

        sendBroadcast(resultOfConnectionIntent);
        //onDestroy();
    }
}