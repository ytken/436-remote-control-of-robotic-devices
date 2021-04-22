package ru.hse.control_system_v2.dbprotocol;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import ru.hse.control_system_v2.R;

public class ProtocolRepo extends HashMap<String, Byte> {

    private static final String LOG_TAG = "XMLParcer";
    private ArrayList<String> queryTags;
    private HashMap<String, Byte> moveCodes;
    private Context context;
    ProtocolDBHelper dbHelper;

    //TODO ругается, что Call requires API level R (current min is 23): java.util.List#of
    public static final List<String> labels = List.of("class_android","class_computer","class_arduino","type_sphere","type_anthropomorphic",
            "type_cubbi","type_computer","no_class","no_type","redo_command","new_command","type_move","type_tele",
            "STOP","FORWARD","FORWARD_STOP","BACK", "BACK_STOP","LEFT","LEFT_STOP","RIGHT","RIGHT_STOP");

    public ProtocolRepo(Context context, String name) {
        this.context = context;
        dbHelper = ProtocolDBHelper.getInstance(context);
        Log.d("mLog", "name = "+name);
        queryTags = new ArrayList<>();
        moveCodes = new HashMap<>();
        if (!name.isEmpty())
            parseCodes(name);
    }

    public boolean getTag(String tag) {
        return queryTags.contains(tag);
    }

    public Byte get(String key) {
        return moveCodes.get(key);
    }

    XmlPullParser prepareXpp(String name) throws IOException, XmlPullParserException {
        XmlPullParser xpp;
        if (name.equals(context.getResources().getString(R.string.TAG_default_protocol)+".xml") || name.equals(context.getResources().getString(R.string.TAG_default_protocol))){
            xpp = context.getResources().getXml(R.xml.arduino_default);
            return xpp;
        }
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new
                File(context.getFilesDir() + File.separator + dbHelper.getFileName(name))));
        String read;
        StringBuilder builder = new StringBuilder();

        while((read = bufferedReader.readLine()) != null){
            Log.d("mLog", read);
            if (read.contains("<?xml"))
                continue;
            read = read.replaceAll(" ","");
            builder.append(read);
            Log.d("mLog", read);
        }
        String codeText = builder.toString();
        bufferedReader.close();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        xpp = factory.newPullParser();
        xpp.setInput(new StringReader(codeText));

        return xpp;
    }
    public void parseCodes(String name) {
        String curName = "";
        try {
            XmlPullParser xpp = prepareXpp(name);
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    // начало документа
                    case XmlPullParser.START_TAG:
                        curName = xpp.getName();
                        queryTags.add(curName);
                        Log.d(LOG_TAG, "At " + curName);
                        break;
                    // содержимое тэга
                    case XmlPullParser.TEXT:
                        if (labels.contains(curName)) {
                            Log.d(LOG_TAG, xpp.getText());
                            String codeEl = xpp.getText();
                            if (codeEl.charAt(1) == 'x')
                                codeEl = codeEl.substring(2);
                            Byte xppCode = (byte) ((Character.digit(codeEl.charAt(0), 16) << 4)
                                    + Character.digit(codeEl.charAt(1), 16));
                            Log.d(LOG_TAG, "CODE " + curName + " " + xppCode);
                            /*byte intXpp = 0;
                            for (int i = 1; i >= 0; i--) {
                                char d = codeEl.charAt(i);
                                switch (d) {
                                    case '1': intXpp += 1*16^i; break;
                                    case '2': intXpp += 2*16^i; break;
                                    case '3': intXpp += 3*16^i; break;
                                    case '4': intXpp += 4; break;
                                    case '5': intXpp += 5; break;
                                    case '6': intXpp += 6; break;
                                    case '7': intXpp += 7; break;
                                    case '8': intXpp += 8; break;
                                    case '9': intXpp += 9; break;
                                    case 'A': intXpp += 10; break;
                                    case 'B': intXpp += 11; break;
                                    case 'C': intXpp += 12; break;
                                    case 'D': intXpp += 13; break;
                                    case 'E': intXpp += 14; break;
                                    case 'F': intXpp += 15; break;
                                }
                            }*/
                            //Byte xppCode = intXpp.byteValue();
                                    /*(byte) ((Character.digit(codeEl.charAt(0), 16) << 4)
                                    + Character.digit(codeEl.charAt(1), 16));*/
                            moveCodes.put(curName, xppCode);
                            }
                        break;
                    default:
                        break;
                }
                // следующий элемент
                xpp.next();
            }
            Log.d(LOG_TAG, "END_DOCUMENT");

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

        public int stringXMLparser(String code) {
        try {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        code = code.replaceAll(" ","");
        code = code.replaceAll("\n","");
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader(code));

        String curName = "";
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.START_TAG:
                        curName = xpp.getName();
                        break;
                    case XmlPullParser.TEXT:
                        if (curName.equals("length")) {
                            int len = Integer.parseInt(xpp.getText());
                        } else {
                            if (labels.contains(curName)) {
                                String codeEl = xpp.getText();
                                if (codeEl.charAt(1) == 'x')
                                    codeEl = codeEl.substring(2);
                                Byte xppCode = (byte) ((Character.digit(codeEl.charAt(0), 16) << 4)
                                        + Character.digit(codeEl.charAt(1), 16));
                            }
                        }
                        break;

                    default:
                        break;
                }
                // следующий элемент
                xpp.next();
            }
            Log.d(LOG_TAG, "END_DOCUMENT");
        } catch (IOException e) {
            return 1;
        } catch (XmlPullParserException e) {
            return 2;
        }
        return 0;
    }
}
