package ru.hse.control_system_v2.dbprotocol;

import android.content.Context;
import android.util.Log;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import ru.hse.control_system_v2.R;

public class ProtocolRepo extends HashMap<String, Byte> {

    private static final String LOG_TAG = "XMLParcer";
    private HashMap<String, Integer> lengthOfQuery;
    private HashMap<String, Byte> moveCodes;
    private Context context;

    public static final List<String> labels = List.of("class_android","class_computer","class_arduino","type_sphere","type_anthropomorphic",
            "type_cubbi","type_computer","redo_command","new_command","type_move","type_tele","STOP","FORWARD","FORWARD_STOP","BACK","BACK_STOP","LEFT","LEFT_STOP","RIGHT","RIGHT_STOP");

    public ProtocolRepo(Context context, String name) {
        this.context = context;
        Log.d("mLog", "name = "+name);
        lengthOfQuery = new HashMap<>();
        moveCodes = new HashMap<>();
        parseCodes(name);
    }

    public Byte get(String key) {
        Log.d("mLog", "key = " + key);
        Log.d("mLog", "value = " + moveCodes.get(key));
        return moveCodes.get(key);
    }

    XmlPullParser prepareXpp(String name) throws IOException, XmlPullParserException {
        XmlPullParser xpp;
        if (name.equals(context.getResources().getString(R.string.TAG_default_protocol)+".xml")){
            xpp = context.getResources().getXml(R.xml.arduino_default);
            return xpp;
        }

        BufferedReader bufferedReader = new BufferedReader(new FileReader(new
                File(context.getFilesDir() + File.separator + name)));
        String read;
        StringBuilder builder = new StringBuilder("");

        while((read = bufferedReader.readLine()) != null){
            Log.d("mLog", read);
            read = read.replaceAll(" ","");
            /*while (read.contains("\n"))
                read.replace("\n","");*/
            builder.append(read);
            Log.d("mLog", read);
        }
        String codeText = builder.toString();
        bufferedReader.close();

        /*Document docCode = null;
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = builderFactory.newDocumentBuilder();
            // парсим переданную на вход строку с XML разметкой
            docCode = docBuilder.parse(new InputSource(new StringReader(codeText)));
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        xpp = factory.newPullParser();
        xpp.setInput(new StringReader(codeText));

        return xpp;
    }

    public void parseCodes(String name) {
        String parentName = "", curName = "";
        try {
            XmlPullParser xpp = prepareXpp(name);
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    // начало документа
                    case XmlPullParser.START_TAG:
                        //Log.d(LOG_TAG, "START_TAG: name = " + xpp.getName() + ", depth = " + xpp.getDepth() + ", attrCount = " + xpp.getAttributeCount());
                        parentName = curName;
                        curName = xpp.getName();
                        break;
                    // содержимое тэга
                    case XmlPullParser.TEXT:
                        if(curName.equals("length")){
                            int len = Integer.parseInt(xpp.getText());
                            lengthOfQuery.put(parentName, len);
                            Log.d(LOG_TAG, "length of " + parentName + " = " + len);
                        }
                        else {
                            if (labels.contains(curName)) {
                                Log.d(LOG_TAG, xpp.getText());
                                String codeEl = xpp.getText();
                                if (codeEl.charAt(1) == 'x')
                                    codeEl = codeEl.substring(2);
                                Byte xppCode = (byte) ((Character.digit(codeEl.charAt(0), 16) << 4)
                                        + Character.digit(codeEl.charAt(1), 16));
                                Log.d(LOG_TAG, "CODE " + curName + " " + xppCode);
                                moveCodes.put(curName, xppCode);
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

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



        /*
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc;
        //API to obtain DOM Document instance
        DocumentBuilder builder = null;
        try
        {
            //Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();

            //Parse the content to Document object
            doc = builder.parse(new InputSource(new StringReader(xmlString)));
            Node root = doc.getDocumentElement();
            NodeList codes = root.getChildNodes();
            for (int i = 0; i < codes.getLength(); i++) {
                Node elem = codes.item(i);
                NodeList elCode = elem.getChildNodes();
                for (int j = 0; j < elCode.getLength(); j++) {
                    Node propNode = elCode.item(j);
                    if (labels.contains(propNode.getNodeName()))
                        moveCodes.put(propNode.getNodeName(), Byte.parseByte(propNode.getNodeValue()));
                    else if (propNode.getNodeName().equals("Length"))
                        lengthOfQuery.put(elem.getNodeName(), Integer.parseInt(propNode.getNodeValue()));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }*/
    }
}
