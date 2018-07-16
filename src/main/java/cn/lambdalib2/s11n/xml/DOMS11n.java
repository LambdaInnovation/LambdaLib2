package cn.lambdalib2.s11n.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.function.Predicate;

public enum DOMS11n {
    instance;

    @FunctionalInterface
    public interface Serializer<T> {
        void serialize(T obj, Node node);
    }

    @FunctionalInterface
    public interface Deserializer<T> {
        T deserialize(Class<T> type, Node node);
    }

    public void addSerializer(Predicate<?> pred, Serializer serializer) {

    }

    public <T> void addDeserializer(Class<T> type, Deserializer<T> deserializer) {

    }

    public Node serialize(Document doc, String name, Object obj) {
        // TODO
        return null;
    }

    public <T> T deserialize(Class<T> type, Node node) {
        // TODO
        return null;
    }

}
