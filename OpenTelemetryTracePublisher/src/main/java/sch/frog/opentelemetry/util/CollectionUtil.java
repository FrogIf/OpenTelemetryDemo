package sch.frog.opentelemetry.util;

import java.util.Collection;

public class CollectionUtil {

    public static <T> void addIfNotNull(Collection<T> collection, T val){
        if(val != null){
            collection.add(val);
        }
    }

    public static <T> boolean isEmpty(Collection<T> collection){
        return collection == null || collection.isEmpty();
    }

}
