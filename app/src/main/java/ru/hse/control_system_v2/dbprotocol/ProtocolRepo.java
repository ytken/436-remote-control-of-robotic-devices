package ru.hse.control_system_v2.dbprotocol;

import android.content.Context;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

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
        //String code = ProtocolDBHelper.instance.getCode(name);
        lengthOfQuery = new HashMap<>();
        moveCodes = new HashMap<>();
        parseCodes();
    }

    public Byte get(String key) {
        Log.d("mLog", "key = " + key);
        Log.d("mLog", "value = " + moveCodes.get(key));
        return moveCodes.get(key);
    }

    XmlPullParser prepareXpp() {
        return context.getResources().getXml(R.xml.xmlcode);
    }

    public void parseCodes() {
        String parentName = "", curName = "";

        try {
            XmlPullParser xpp = prepareXpp();
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    // начало документа
                    case XmlPullParser.START_TAG:
                        Log.d(LOG_TAG, "START_TAG: name = " + xpp.getName()
                                + ", depth = " + xpp.getDepth() + ", attrCount = "
                                + xpp.getAttributeCount());
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
                                Byte xppCode = (byte) (Integer.parseInt(xpp.getText(),16) & 0xff);
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
