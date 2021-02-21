package ru.hse.control_system_v2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.UUID;

public class DataThread extends Thread{ // класс поток для приема и передачи данных

    public void setSelectedDevice(String selectedDevice) {
        this.MAC = selectedDevice;
    }
    public void setProtocol(String classDevice) {
        this.classDevice = classDevice;
    }
    public void setSocket(BluetoothSocket clientSocket){
        this.clientSocket = clientSocket;
    }
    public void startManualMode(Context c){
        Intent intent = new Intent(c.getApplicationContext(),Manual_mode.class);
        intent.putExtra("MAC", MAC);
        intent.putExtra("protocol", classDevice);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        c.startActivity(intent);
    }
    String MAC;
    String classDevice;
    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothSocket clientSocket;
    private byte inputPacket[];
    private static final String TAG = "Thread";
    OutputStream OutStrem;
    InputStream InStrem;
    private int[] my_data;
    // SPP UUID сервиса
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean ready_to_request;         // флаг готовности принятия данных: true - высылай новый пакет   false - не высылай пакет
    @Override
    public void run()
    {
        OutputStream tmpOut = null;
        InputStream tmpIn = null;
        try
        {
            tmpOut = clientSocket.getOutputStream();
            tmpIn = clientSocket.getInputStream();
        } catch (IOException e)
        {
        }

        OutStrem = tmpOut;
        InStrem = tmpIn;
        inputPacket = new byte[12];
        byte[] buffer = new byte[12]; // 12 должно быть: 2 - префикс 7 - данные 3 - контр сумма
        int bufNum;
        int pacNum = 0;

        while (true) // ну а дальше тоже 12
        {
            try
            {
                bufNum = InStrem.read(buffer);
                if (pacNum + bufNum < 13) // нормально считываем данные
                {
                    System.arraycopy(buffer, 0, inputPacket, pacNum, bufNum);
                    pacNum = pacNum + bufNum;
                } else // если неправильые данные, то переслать
                {
                    ready_to_request = false;
                    Log.d(TAG, "Как то много всего пришло");
                    pacNum = 0;

                    byte[] message = new byte[]{0x30, 0x15, 0x0f, 0x37, 0x37, 0x37};
                    sendData(message);
                }

                if (pacNum == 12) // все нормально
                {
                    pacNum = 0;//здесь проверяем пакет и прочее

                    Log.d(TAG, "***Получаем данные: " +
                            buffer[0] + " " +
                            buffer[1] + " " +
                            buffer[2] + " " +
                            buffer[3] + " " +
                            buffer[4] + " " +
                            buffer[5] + " " +
                            buffer[6] + " " +
                            buffer[7] + " " +
                            buffer[8] + " " +
                            buffer[9] + " " +
                            buffer[10] + " " +
                            buffer[11] + " ");

                    ready_to_request = true;
                }
            } catch (IOException e)
            {
                break;
            }
        }
    }
    public void sendData(String message)
    {
        byte[] msgBuffer = message.getBytes();
        Log.d(TAG, "***Отправляем данные: " + message + "***");

        try
        {
            OutStrem.write(msgBuffer);
        } catch (IOException e)
        {
        }
    }

    public void sendData(byte[] message)
    {
        String logMessage = "***Отправляем данные: ";
        for (int i=0; i < 32; i++)
            logMessage += message[i] + " ";
        Log.d(TAG, logMessage + "***");
        try
        {
            OutStrem.write(message);
        } catch (IOException e)
        {
        }
    }

    public void cancel()
    {
        try
        {
            clientSocket.close();
        } catch (IOException e)
        {
        }
    }

    public Object status_OutStrem()
    {
        if (OutStrem == null)
        {
            return null;
        }
        return OutStrem;
    }


    //возвращает true, если bluetooth включён
    public boolean btIsEnabledFlagVoid() {
        return btAdapter.isEnabled();
    }



    public void Send_Data(String message) { sendData(message);}

    public void Send_Data(byte message[]) { sendData(message);}
    public void Disconnect(Timer bt_timer) // для работы через определенные промежутки времени
    {
        Log.d(TAG, "...In onPause()...");

        if (status_OutStrem() != null)
        {
            cancel();
            bt_timer.cancel();
        }

        try
        {
            clientSocket.close();

        } catch (IOException e2)
        {
            //MyError("Fatal Error", "В onPause() Не могу закрыть сокет" + e2.getMessage() + ".", "Не могу закрыть сокет.");
        }

    }
    public void Disconnect() // при ручном управлении передачей пакетов
    {
        Log.d(TAG, "...In onPause()...");

        if (status_OutStrem() != null)
        {
            cancel();
        }

        try
        {
            clientSocket.close();

        } catch (IOException e2)
        {
            //MyError("Fatal Error", "В onPause() Не могу закрыть сокет" + e2.getMessage() + ".", "Не могу закрыть сокет.");
        }
    }

    public boolean isReady_to_request()
    {
        return ready_to_request;
    }

    public void setReady_to_request(boolean ready_to_request)
    {
        this.ready_to_request = ready_to_request;
    }

    public int[] getMy_data()
    {
        for (int i = 0; i < 5; i++) // 12 должно быть: 2 - префикс 7 - данные 3 - контр сумма.  ... ну или 7 - только данные
        {
            my_data[i] = (int) inputPacket[i +2];
            my_data[i] = my_data[i]*5;
        }
        return my_data;
    }

}
