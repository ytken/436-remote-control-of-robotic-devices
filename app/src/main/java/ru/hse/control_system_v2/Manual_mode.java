package ru.hse.control_system_v2;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

import androidx.appcompat.widget.SwitchCompat;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import ru.hse.control_system_v2.dbprotocol.ProtocolDBHelper;
import ru.hse.control_system_v2.dbprotocol.ProtocolRepo;

public class Manual_mode extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
{
    private boolean is_hold_command;

    private DataThread dataThreadForArduino;                  // устройство, с которого буду получаю получать данные
    //TODO:переделать в массив
    private Timer arduino_timer;            // таймер для arduino

    private String[] pre_str_sens_data;             // форматирование вывода данных с сенсоров
    private int[] sens_data;                        // непосредственно данные с сенсоров
    private byte[] message;      // комманда посылаемая на arduino
    private byte prevCommand = 0;
    String MAC;
    String classDevice;
    private TextView text_sens_data;
    private byte inputPacket[];
    OutputStream OutStrem;
    InputStream InStrem;
    private int[] my_data;
    private boolean ready_to_request;         // флаг готовности принятия данных: true - высылай новый пакет   false - не высылай пакет
    BluetoothSocket clientSocket;
    private int countCommands, lenOfPart;
    Resources res;

    public void setSocket(BluetoothSocket clientSocket) {
        this.clientSocket = clientSocket;
    }

    ProtocolRepo getDevicesID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manual_mode);
        showToast("Started Manual mode!");
        findViewById(R.id.button_stop).setEnabled(false);

        clientSocket = SocketHandler.getSocket();

        //много устройств, но сейчас одно
        Bundle b = getIntent().getExtras();
        MAC = b.get("MAC").toString();
        classDevice = b.get("protocol").toString();
        dataThreadForArduino = new DataThread();
        dataThreadForArduino.setSelectedDevice(MAC);
        dataThreadForArduino.setSocket(clientSocket);
        dataThreadForArduino.setProtocol(classDevice);
        dataThreadForArduino.start();

        pre_str_sens_data = new String[5];
        pre_str_sens_data[0] = "     0º \t\t-\t\t ";
        pre_str_sens_data[1] = " -45º \t\t-\t\t ";
        pre_str_sens_data[2] = "  45º \t\t-\t\t ";
        pre_str_sens_data[3] = " -90º \t\t-\t\t ";
        pre_str_sens_data[4] = "  90º \t\t-\t\t ";

        sens_data = new int[5];
        my_data = new int[12];

        res = getResources();
        is_hold_command = false;
        boolean is_sens_data = false;
        boolean is_fixed_angel = false;
        getDevicesID = new ProtocolRepo(getApplicationContext(), b.getString("protocol"));
        MAC = b.getString("MAC");
        message = new byte[getDevicesID.getLength(res.getString(R.string.TAG_FULL_MES))];
        countCommands = 0; lenOfPart = 0;


        if (!BluetoothAdapter.checkBluetoothAddress(MAC)) {
            showToast("Wrong MAC address");
            Manual_mode.this.finish();
        }

        TextView deviceInfo = findViewById(R.id.textViewNameManual);
        deviceInfo.setText("Устройство: " + b.getString("name") + "\n MAC: " + MAC);

        arduino_timer = new Timer();
        // функция выполняющаяся при тике таймера для arduino
        TimerTask arduino_timer_task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Data_request();
                    }
                });
            }
        };

        /*Button button_left_45 = findViewById(R.id.button_left_45);
        button_left_45.setVisibility(View.INVISIBLE);
        Button button_right_45 = findViewById(R.id.button_right_45);
        button_right_45.setVisibility(View.INVISIBLE);
        Button button_left_90 = findViewById(R.id.button_left_90);
        button_left_90.setVisibility(View.INVISIBLE);
        Button button_right_90 = findViewById(R.id.button_right_90);
        button_right_90.setVisibility(View.INVISIBLE);


        findViewById(R.id.button_stop).setOnClickListener(this);
        findViewById(R.id.button_left_45).setOnClickListener(this);
        findViewById(R.id.button_right_45).setOnClickListener(this);
        findViewById(R.id.button_left_90).setOnClickListener(this);
        findViewById(R.id.button_right_90).setOnClickListener(this);*/

        findViewById(R.id.button_up).setOnTouchListener(touchListener);
        findViewById(R.id.button_down).setOnTouchListener(touchListener);
        findViewById(R.id.button_left).setOnTouchListener(touchListener);
        findViewById(R.id.button_right).setOnTouchListener(touchListener);
        findViewById(R.id.button_stop).setOnClickListener(this);

        Switch hold_command = findViewById(R.id.switch_hold_command_mm);
        hold_command.setOnCheckedChangeListener(this);

        Arrays.fill(message, (byte) 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data==null) return;
        String message = data.getStringExtra("message");
        Intent intent = new Intent();
        intent.putExtra("message", message);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        //arduino.BluetoothConnectionServiceVoid();     // соединяемся с bluetooth
        //TODO - вызывает вылет приложения
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        completeDevicesInfo();

        lenOfPart = getDevicesID.getLength(res.getString(R.string.TAG_TURN_COM));
        if (lenOfPart != 0)
            message[countCommands++] = getDevicesID.get("new_command");
        lenOfPart = getDevicesID.getLength(res.getString(R.string.TAG_TYPE_COM));
        if (lenOfPart != 0)
            message[countCommands++] = getDevicesID.get("type_move");
        message[countCommands++] = getDevicesID.get("STOP");
        dataThreadForArduino.Send_Data(message);

        if(arduino_timer != null)
        {
            arduino_timer.cancel();
            arduino_timer = null;
        }

        try
        {
            dataThreadForArduino.Disconnect();                 // отсоединяемся от bluetooth
            //arduino.Shut_down_bt();               // и выключаем  bluetooth на cubietruck
        }
        catch (Exception e)
        {}
    }

    @Override
    public void onClick(View v)
    {
        completeDevicesInfo();
        switch (v.getId())
        {
            case R.id.button_stop:
                //Toast.makeText(getApplicationContext(), "Стоп всех комманд", Toast.LENGTH_SHORT).show();
                completeMessage("STOP");
                countCommands = 0;
                break;
        }
    }

    View.OnTouchListener touchListener = new View.OnTouchListener()
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            completeDevicesInfo();
            if(event.getAction() == MotionEvent.ACTION_DOWN)                        // если нажали на кнопку и не важно есть удержание команд или нет
            {
                switch (v.getId())
                {
                    case R.id.button_up:
                        //Toast.makeText(getApplicationContext(), "Вперед поехали", Toast.LENGTH_SHORT).show();
                        Log.d("Вперед поехали", "********************************************");
                        completeMessage("FORWARD");
                        countCommands = 0;
                        break;
                    case R.id.button_down:
                        Log.d("Назад поехали", "********************************************");
                        //Toast.makeText(getApplicationContext(), "Назад поехали", Toast.LENGTH_SHORT).show();
                        completeMessage("BACK");
                        countCommands = 0;
                        break;
                    case R.id.button_left:
                        //Toast.makeText(getApplicationContext(), "Влево поехали", Toast.LENGTH_SHORT).show();
                        Log.d("Влево поехали", "********************************************");
                        completeMessage("LEFT");
                        countCommands = 0;
                        break;
                    case R.id.button_right:
                        //Toast.makeText(getApplicationContext(), "Вправо поехали", Toast.LENGTH_SHORT).show();
                        Log.d("Вправо поехали", "********************************************");
                        completeMessage("RIGHT");
                        countCommands = 0;
                        break;
                }
            }
            else if(event.getAction() == MotionEvent.ACTION_UP)             // если отпустили кнопку
            {
                if(!is_hold_command)    // и нет удержания команд то все кнопки отправляют команду стоп
                {
                    switch (v.getId())
                    {
                        case R.id.button_up:
                            completeMessage("FORWARD_STOP");
                            countCommands = 0;
                            break;
                        case R.id.button_down:
                            completeMessage("BACK_STOP");
                            countCommands = 0;
                            break;
                        case R.id.button_left:
                            completeMessage("LEFT_STOP");
                            countCommands = 0;
                            break;
                        case R.id.button_right:
                            completeMessage("RIGHT_STOP");
                            countCommands = 0;
                            break;
                    }
                    Log.d("mLog", String.valueOf(countCommands));
                }
            }
            return false;
        }
    };

    public void completeDevicesInfo() {
        countCommands = 0;
        lenOfPart = getDevicesID.getLength(res.getString(R.string.TAG_CLASS_FROM));
        if (lenOfPart != 0)
            message[countCommands++] = getDevicesID.get("class_android");
        lenOfPart = getDevicesID.getLength(res.getString(R.string.TAG_TYPE_FROM));
        if (lenOfPart != 0)
            message[countCommands++] = getDevicesID.get("type_computer"); // класс и тип устройства отправки
        lenOfPart = getDevicesID.getLength(res.getString(R.string.TAG_CLASS_TO));
        if (lenOfPart != 0)
            message[countCommands++] = getDevicesID.get("class_arduino");
        lenOfPart = getDevicesID.getLength(res.getString(R.string.TAG_TYPE_TO));
        if (lenOfPart != 0)
            message[countCommands++] = getDevicesID.get("no_type");// класс и тип устройства приема
    }

    public void completeMessage (String command) {
        lenOfPart = getDevicesID.getLength(res.getString(R.string.TAG_TURN_COM));
        if (lenOfPart != 0) {
            message[countCommands++] = (prevCommand == getDevicesID.get(command))? getDevicesID.get("redo_command"): getDevicesID.get("new_command");
            prevCommand = getDevicesID.get(command);
        }
        lenOfPart = getDevicesID.getLength(res.getString(R.string.TAG_TYPE_COM));
        if (lenOfPart != 0)
            message[countCommands++] = getDevicesID.get("type_move");
        message[countCommands++] = getDevicesID.get(command);
        dataThreadForArduino.Send_Data(message);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        switch (buttonView.getId())
        {
            case R.id.switch_hold_command_mm:
                is_hold_command = isChecked;
                if(is_hold_command)
                {
                    Toast.makeText(this, "Удерживание комманды включено: ", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.button_stop).setEnabled(true);
                }
                else
                {
                    Toast.makeText(this, "Удерживание комманды отключено: ", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.button_stop).setEnabled(false);
                }
                break;
        }
    }

    private void Data_request()
    {
        if (dataThreadForArduino.isReady_to_request()) // если готовы принимать данные, таймер действует
        {
            sens_data = dataThreadForArduino.getMy_data();
            text_sens_data.setText( pre_str_sens_data[0] + sens_data[0] + "\n" +
                    pre_str_sens_data[1] + sens_data[1] + "\n" +
                    pre_str_sens_data[2] + sens_data[2] + "\n" +
                    pre_str_sens_data[3] + sens_data[3] + "\n" +
                    pre_str_sens_data[4] + sens_data[4]);

            dataThreadForArduino.Send_Data(message);
            dataThreadForArduino.setReady_to_request(false); // как только отправили запрос, то так сказать приостанавливаем таймер
        } else // если не готовы получать данные то просто ничего не делаем
        {
            Log.d("qwerty", "******************************************** ошибка");
        }
    }

    // Метод для вывода всплывающих данных на экран
    public void showToast(String outputInfoString) {
        Toast outputInfoToast = Toast.makeText(this, outputInfoString, Toast.LENGTH_SHORT);
        outputInfoToast.show();
    }
}
