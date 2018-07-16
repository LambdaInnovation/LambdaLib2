package cn.ll2test.unittest;


import cn.lambdalib2.s11n.xml.DOMS11n;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Objects;

public class DOMS11nTest {

    public static class StructForTestA {
        public int valueA;
        public String valueB;
        public StructForTestB structForTestB, yetAnother;
    }

    public static class StructForTestB {
        public int hello;
        public float world;
    }

    public static void main(String[] args) throws Exception {
        DOMS11nTest instance = new DOMS11nTest();
        instance.testSerialization();
    }

    final DOMS11n s11n = DOMS11n.instance;

    private void testSerialization() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder db = factory.newDocumentBuilder();

        Document doc = db.newDocument();

        StructForTestA testStruct = new StructForTestA();
        testStruct.valueA = 123;
        testStruct.valueB = "Nothing is true, everything is permitted";
        testStruct.structForTestB = new StructForTestB();
        testStruct.structForTestB.hello = 233;
        testStruct.structForTestB.world = 3.14159f;

        s11n.registerS11nType(StructForTestA.class);
        s11n.registerS11nType(StructForTestB.class);

        {
            Node node = s11n.serialize(doc, "Test", 123);
            test(node.getTextContent().equals("123"), "s11n int");
            test(s11n.deserialize(int.class, node) == 123, "des11n int");
        }
        {
            Node node = s11n.serialize(doc, "Test", "A quick brown fox");
            test(node.getTextContent().equals("A quick brown fox"), "s11n String");
            test(s11n.deserialize(String.class, node).equals("A quick brown fox"), "des11n String");
        }

        {
            Node node = s11n.serialize(doc, "Test", testStruct);
            NodeList nodeList = node.getChildNodes();

            int propertyCount = 0;
            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node child = nodeList.item(i);
                switch (child.getNodeName()) {
                    case "valueA":
                        test(child.getTextContent().equals("123"), "s11n int struct.valueA");
                        ++propertyCount;
                        break;
                    case "valueB":
                        test(child.getTextContent().equals(testStruct.valueB), "s11n String struct.valueB");
                        ++propertyCount;
                        break;
                    case "structForTestB":
                        ++propertyCount;
                        break;
                    case "yetAnother":
                        ++propertyCount;
                        break;
                }
            }
            test(propertyCount == 4, "s11n struct");

            StructForTestA result = s11n.deserialize(StructForTestA.class, node);
            test(
                Objects.equals(testStruct.valueA, result.valueA) &&
                Objects.equals(testStruct.valueB, result.valueB) &&
                Objects.equals(testStruct.yetAnother, result.yetAnother) &&
                Objects.equals(testStruct.structForTestB.hello, result.structForTestB.hello) &&
                Objects.equals(testStruct.structForTestB.world, result.structForTestB.world),
                "des11n struct"
            );
        }

    }

    private void test(boolean pred, String msg) {
        if (!pred) {
            throw new RuntimeException("FAILED: " + msg);
        } else {
            System.out.println("PASS: " + msg);
        }
    }

}
