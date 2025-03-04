package factory;

import java.util.HashMap;
import java.util.function.Function;

public class Factory<K,T,D> {
    private final HashMap<K,Function<D, ? extends T>> methodCreator = new HashMap<>();

    public T create(K key, D data) throws IllegalArgumentException {
        if (!methodCreator.containsKey(key)) {
            throw new IllegalArgumentException("Key " + key + " not found");
        }

        return methodCreator.get(key).apply(data);
    }

    public void add(K key, Function<D, ? extends T> function) throws IllegalArgumentException {
        methodCreator.put(key, function);
    }
}