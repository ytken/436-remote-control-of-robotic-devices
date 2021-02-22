package ru.hse.control_system_v2.dbprotocol;

import android.content.Intent;

import androidx.annotation.Nullable;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ProtocolRepo extends HashMap<String, Byte> {

    private HashMap<String, Integer> lengthOfQuery;
    private HashMap<String, Byte> moveCodes;

    public static final List<String> labels = List.of("class_android","class_computer","class_arduino","type_sphere","type_anthropomorphic",
            "type_cubbi","type_computer","redo_command","new_command","type_move","type_tele","STOP","FORWARD","FORWARD_STOP","BACK","BACK_STOP","LEFT","LEFT_STOP","RIGHT","RIGHT_STOP");

    /*
    public static final String CLASS_ANDROID = ;
    public static final String CLASS_COMPUTER = ;
    public static final String CLASS_ARDUINO = ;

    public static final String TYPE_SPHERE = ;
    public static final String TYPE_ANTROPOMORPHIC = ;
    public static final String TYPE_CUBBIE = ;
    public static final String TYPE_COMPUTER = ;

    public static final String REDO_COMMAND = ;
    public static final String NEW_COMMAND = ;

    public static final String MODE_MOVE = ;
    public static final String MODE_TELE = ;

    public static final String A_STOP = ;
    public static final String A_FORWARD = ;
    public static final String A_FORWARD_STOP = ;
    public static final String A_BACK = ;
    public static final String A_BACK_STOP = ;
    public static final String A_LEFT = ;
    public static final String A_LEFT_STOP = ;
    public static final String A_RIGHT = ;
    public static final String A_RIGHT_STOP = ;
     */

    public ProtocolRepo(String name) {
        String code = ProtocolDBHelper.instance.getCode(name);
        parseCodes(code);
    }

    @Nullable
    @Override
    public Byte get(@Nullable Object key) {
        return moveCodes.get(key);
    }

    public void parseCodes(String xmlString) {
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
        }
    }
}
