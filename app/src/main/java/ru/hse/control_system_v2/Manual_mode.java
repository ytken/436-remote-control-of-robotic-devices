package ru.hse.control_system_v2;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import ru.hse.control_system_v2.dbprotocol.ProtocolRepo;
import ru.hse.control_system_v2.list_devices.DeviceRepository;

public class Manual_mode extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
{
    private boolean is_hold_command;
    private boolean is_sens_data;
    private boolean is_fixed_angel;

    private Bt_connection arduino;                  // устройство, с которого буду получаю получать данные
    private Timer arduino_timer;            // таймер для arduino
    private TimerTask arduino_timer_task;   // функция выполняющаяся при тике таймера для arduino

    private String[] pre_str_sens_data;             // форматирование вывода данных с сенсоров
    private int[] sens_data;                        // непосредственно данные с сенсоров
    private byte[] message= new byte[32];      // комманда посылаемая на arduino
    private byte prevCommand = 0;

    private TextView deviceInfo, text_sens_data;

    HashMap<String, Byte> getDevicesID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manual_mode);

        findViewById(R.id.button_stop).setEnabled(false);

        pre_str_sens_data = new String[5];
        pre_str_sens_data[0] = "     0º \t\t-\t\t ";
        pre_str_sens_data[1] = " -45º \t\t-\t\t ";
        pre_str_sens_data[2] = "  45º \t\t-\t\t ";
        pre_str_sens_data[3] = " -90º \t\t-\t\t ";
        pre_str_sens_data[4] = "  90º \t\t-\t\t ";

        sens_data = new int[5];

        is_hold_command = false;
        is_sens_data = false;
        is_fixed_angel = false;

        Bundle b = getIntent().getExtras();
        getDevicesID = new ProtocolRepo(b.getString("protocol"));
        String MAC = DeviceRepository.getInstance(getApplicationContext()).item(b.getInt("id")).getMAC();

        if (!BluetoothAdapter.checkBluetoothAddress(MAC)) {
            Toast.makeText(getApplicationContext(), "Wrong MAC adress", Toast.LENGTH_LONG);
            Manual_mode.this.finish();
        }
        else
        {
            arduino = new Bt_connection(getApplicationContext(), MAC);
        }


        deviceInfo = findViewById(R.id.textViewNameManual);
        deviceInfo.setText("Устройство: " + b.getString("name") + "\n MAC: " + MAC);

        arduino_timer = new Timer();
        arduino_timer_task = new TimerTask()
        {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Data_request();
                    }
                });
            }
        };

        Button button_left_45 = (Button) findViewById(R.id.button_left_45);
        button_left_45.setVisibility(View.INVISIBLE);
        Button button_right_45 = (Button) findViewById(R.id.button_right_45);
        button_right_45.setVisibility(View.INVISIBLE);
        Button button_left_90 = (Button) findViewById(R.id.button_left_90);
        button_left_90.setVisibility(View.INVISIBLE);
        Button button_right_90 = (Button) findViewById(R.id.button_right_90);
        button_right_90.setVisibility(View.INVISIBLE);


        findViewById(R.id.button_stop).setOnClickListener(this);
        findViewById(R.id.button_left_45).setOnClickListener(this);
        findViewById(R.id.button_right_45).setOnClickListener(this);
        findViewById(R.id.button_left_90).setOnClickListener(this);
        findViewById(R.id.button_right_90).setOnClickListener(this);

        findViewById(R.id.button_up).setOnTouchListener(touchListener);
        findViewById(R.id.button_down).setOnTouchListener(touchListener);
        findViewById(R.id.button_left).setOnTouchListener(touchListener);
        findViewById(R.id.button_right).setOnTouchListener(touchListener);

        Switch hold_command = (Switch) findViewById(R.id.switch_hold_command_mm);
        hold_command.setOnCheckedChangeListener(this);

        //Switch fixed_angel = (Switch) findViewById(R.id.switch_fxed_angel_mm);
        //fixed_angel.setOnCheckedChangeListener(this);

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

        arduino.Connect();     // соединяемся с bluetooth
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        message[4] = getDevicesID.get("new_command");
        message[5] = getDevicesID.get("type_move");
        message[6] = getDevicesID.get("STOP");
        arduino.Send_Data(message);

        if(arduino_timer != null)
        {
            arduino_timer.cancel();
            arduino_timer = null;
        }

        try
        {
            arduino.Disconnect();                 // отсоединяемся от bluetooth
            //arduino.Shut_down_bt();               // и выключаем  bluetooth на cubietruck
        }
        catch (Exception e)
        {}
    }

    @Override
    public void onClick(View v)
    {
        message[0] = getDevicesID.get("class_android"); message[1] = getDevicesID.get("type_computer"); // класс и тип устройства отправки
        message[2] = getDevicesID.get("class_arduino"); // класс и тип устройства приема
        switch (v.getId())
        {
            case R.id.button_stop:
                //Toast.makeText(getApplicationContext(), "Стоп всех комманд", Toast.LENGTH_SHORT).show();
                message[4] = (prevCommand == getDevicesID.get("STOP"))? getDevicesID.get("redo_command"): getDevicesID.get("new_command");
                message[5] = getDevicesID.get("type_move");
                message[6] = prevCommand = getDevicesID.get("STOP");
                arduino.Send_Data(message);
                break;
                /*
            case R.id.button_left_45:
                message = new byte[] {0x30, 0x0a, 0x5c, 0x37, 0x37, 0x37};
                arduino.Send_Data(message);
                break;
            case R.id.button_right_45:
                message = new byte[] {0x30, 0x0a, 0x54, 0x37, 0x37, 0x37};
                arduino.Send_Data(message);
                break;
            case R.id.button_left_90:
                message = new byte[] {0x30, 0x0a, 0x5a, 0x37, 0x37, 0x37};
                arduino.Send_Data(message);
                break;
            case R.id.button_right_90:
                message = new byte[] {0x30, 0x0a, 0x56, 0x37, 0x37, 0x37};
                arduino.Send_Data(message);
                break;*/
        }
    }

    View.OnTouchListener touchListener = new View.OnTouchListener()
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            message[0] = ProtocolRepo.getDeviceCode("class_android"); message[1] = ProtocolRepo.getDeviceCode("type_computer"); // класс и тип устройства отправки
            message[2] = getDevicesID.get("class_arduino"); // класс и тип устройства приема
            message[5] = getDevicesID.get("type_move");
            if(event.getAction() == MotionEvent.ACTION_DOWN)                        // если нажали на кнопку и не важно есть удержание команд или нет
            {
                switch (v.getId())
                {
                    case R.id.button_up:
                        //Toast.makeText(getApplicationContext(), "Вперед поехали", Toast.LENGTH_SHORT).show();
                        message[4] = (prevCommand == getDevicesID.get("FORWARD"))? getDevicesID.get("redo_command"): getDevicesID.get("new_command");
                        message[6] = prevCommand = getDevicesID.get("FORWARD");
                        arduino.Send_Data(message);
                        break;
                    case R.id.button_down:
                        //Toast.makeText(getApplicationContext(), "Назад поехали", Toast.LENGTH_SHORT).show();
                        message[4] = (prevCommand == getDevicesID.get("BACK"))? getDevicesID.get("redo_command"): getDevicesID.get("new_command");
                        message[6] = prevCommand = getDevicesID.get("BACK");
                        arduino.Send_Data(message);
                        break;
                    case R.id.button_left:
                        //Toast.makeText(getApplicationContext(), "Влево поехали", Toast.LENGTH_SHORT).show();
                        message[4] = (prevCommand == getDevicesID.get("LEFT"))? getDevicesID.get("redo_command"): getDevicesID.get("new_command");
                        message[6] = prevCommand = getDevicesID.get("LEFT");
                        arduino.Send_Data(message);
                        break;
                    case R.id.button_right:
                        //Toast.makeText(getApplicationContext(), "Вправо поехали", Toast.LENGTH_SHORT).show();
                        message[4] = (prevCommand == getDevicesID.get("RIGHT"))? getDevicesID.get("redo_command"): getDevicesID.get("new_command");
                        message[6] = prevCommand = getDevicesID.get("RIGHT");
                        arduino.Send_Data(message);
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
                            //Toast.makeText(getApplicationContext(), "Стоп комманды вперед", Toast.LENGTH_SHORT).show();
                            message[4] = (prevCommand == getDevicesID.get("FORWARD_STOP"))? getDevicesID.get("redo_command"): getDevicesID.get("new_command");
                            message[6] = prevCommand = getDevicesID.get("FORWARD_STOP");
                            arduino.Send_Data(message);
                            break;
                        case R.id.button_down:
                            //Toast.makeText(getApplicationContext(), "Стоп комманды назад", Toast.LENGTH_SHORT).show();
                            message[4] = (prevCommand == getDevicesID.get("BACK_STOP"))? getDevicesID.get("redo_command"): getDevicesID.get("new_command");
                            message[6] = prevCommand = getDevicesID.get("BACK_STOP");
                            arduino.Send_Data(message);
                            break;
                        case R.id.button_left:
                            //Toast.makeText(getApplicationContext(), "Стоп комманды влево", Toast.LENGTH_SHORT).show();
                            message[4] = (prevCommand == getDevicesID.get("LEFT_STOP"))? getDevicesID.get("redo_command"): getDevicesID.get("new_command");
                            message[6] = prevCommand = getDevicesID.get("LEFT_STOP");
                            arduino.Send_Data(message);
                            break;
                        case R.id.button_right:
                            //Toast.makeText(getApplicationContext(), "Стоп комманды вправо", Toast.LENGTH_SHORT).show();
                            message[4] = (prevCommand == getDevicesID.get("RIGHT_STOP"))? getDevicesID.get("redo_command"): getDevicesID.get("new_command");
                            message[6] = prevCommand = getDevicesID.get("RIGHT_STOP");
                            arduino.Send_Data(message);
                            break;
                    }
                }
            }
            return false;
        }
    };

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        switch (buttonView.getId())
        {
            case R.id.switch_hold_command_mm:
                is_hold_command = isChecked;
                if(is_hold_command)
                {
                    //Toast.makeText(this, "Удерживание комманды включено: ", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.button_stop).setEnabled(true);
                }
                else
                {
                    //Toast.makeText(this, "Удерживание комманды отключено: ", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.button_stop).setEnabled(false);
                }
                break;
        }
    }

    private void Data_request()
    {
        if (arduino.isReady_to_request()) // если готовы принимать данные, таймер действует
        {
            sens_data = arduino.getMy_data();

            // добавить проверку пакета на корректность по контрольной сумме


            // добавить проверку пакета на корректность по контрольной сумме

            text_sens_data.setText( pre_str_sens_data[0] + sens_data[0] + "\n" +
                    pre_str_sens_data[1] + sens_data[1] + "\n" +
                    pre_str_sens_data[2] + sens_data[2] + "\n" +
                    pre_str_sens_data[3] + sens_data[3] + "\n" +
                    pre_str_sens_data[4] + sens_data[4]);

            arduino.Send_Data(message);
            arduino.setReady_to_request(false); // как только отправили запрос, то так сказать приостанавливаем таймер
        } else // если не готовы получать данные то просто ничего не делаем
        {
            Log.d("qwerty", "******************************************** ошибка");
        }
    }

}

