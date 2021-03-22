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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.UUID;

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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle arguments = intent.getExtras();
        selectedDevice = arguments.get("MAC").toString();
        classDevice = arguments.get("protocol").toString();
        deviceName = arguments.get("name").toString();
        Intent serviceStarted;
        serviceStarted = new Intent("serviceStarted");
        Log.d(TAG, "...Соединение начато...");
        sendBroadcast(serviceStarted);
        BluetoothConnectionServiceVoid();
        return Service.START_NOT_STICKY;
    }


    public void BluetoothConnectionServiceVoid() {
        //Фикс зависания Main Activity
        new Thread(() -> {
            btAdapter = BluetoothAdapter.getDefaultAdapter();
            if (btIsEnabledFlagVoid()) {
                device = btAdapter.getRemoteDevice(selectedDevice);
                // Попытка подключиться к устройству
                try {
                    clientSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocketToServiceRecord", UUID.class).invoke(device, MY_UUID);
                    Log.d(TAG, "...Создаю сокет...");
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    Log.d("BLUETOOTH", e.getMessage());
                    Log.d(TAG, "...Создание сокета неуспешно...");
                    stateOfConnection = false;
                    e.printStackTrace();
                }
                try {
                    clientSocket.connect();
                    // Отключаем поиск устройств для сохранения заряда батареи
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    stateOfConnection = true;
                    Log.d(TAG, "...Подключаюсь к сокету...");
                } catch (IOException e) {
                    stateOfConnection = false;
                    Log.d(TAG, "...Соединение через сокет неуспешно...");
                    try {
                        // В случае ошибки пытаемся закрыть соединение
                        clientSocket.close();
                    } catch (IOException closeException) {
                        Log.d("BLUETOOTH", e.getMessage());
                        e.printStackTrace();
                    }
                    Log.d("BLUETOOTH", e.getMessage());
                    e.printStackTrace();
                }

                if (stateOfConnection) {
                    try {
                        // Решение ошибки, зависящей от версии Android - даём время на установку полного подключения
                        Thread.sleep(2000);
                        Log.d(TAG, "...Даю время на корректное соединение...");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    resultOfConnection();
                }
            } else {
                stateOfConnection = false;
            }
            if (!stateOfConnection) {
                resultOfConnection();
            }
        }).start();

    }



    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //возвращает true, если bluetooth включён
    public boolean btIsEnabledFlagVoid() {
        return btAdapter.isEnabled();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "...Сервис остановлен...");
    }

    // Передаём данные о статусе соединения в Main Activity
    public void resultOfConnection() {
        Intent resultOfConnectionIntent;
        if (!stateOfConnection) {
            resultOfConnectionIntent = new Intent("not_success");
            Log.d(TAG, "...Соединение неуспешно, передаю результат в Main Activity...");
        } else {
            resultOfConnectionIntent = new Intent("success");
            resultOfConnectionIntent.putExtra("MAC", selectedDevice);
            resultOfConnectionIntent.putExtra("protocol", classDevice);
            resultOfConnectionIntent.putExtra("name", deviceName);
            SocketHandler.setSocket(clientSocket);
            Log.d(TAG, "...Соединение успешно, передаю результат в Main Activity...");
        }
        sendBroadcast(resultOfConnectionIntent);
        onDestroy();
    }
}