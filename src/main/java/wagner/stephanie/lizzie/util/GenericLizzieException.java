package wagner.stephanie.lizzie.util;


import com.google.common.base.MoreObjects;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class GenericLizzieException extends RuntimeException implements Map<String, Object> {
    private Map<String, Object> exceptionAttributes;

    public GenericLizzieException() {
        super();
        this.exceptionAttributes = Collections.emptyMap();
    }

    public GenericLizzieException(String message) {
        super(message);
        this.exceptionAttributes = Collections.emptyMap();
    }

    public GenericLizzieException(String message, Throwable cause) {
        super(message, cause);
        this.exceptionAttributes = Collections.emptyMap();
    }

    public GenericLizzieException(Throwable cause) {
        super(cause);
        this.exceptionAttributes = Collections.emptyMap();
    }

    protected GenericLizzieException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.exceptionAttributes = Collections.emptyMap();
    }

    public GenericLizzieException(Map<String, Object> exceptionAttributes) {
        super();
        this.exceptionAttributes = exceptionAttributes;
    }

    public GenericLizzieException(Map<String, Object> exceptionAttributes, String message) {
        super(message);
        this.exceptionAttributes = exceptionAttributes;
    }

    public GenericLizzieException(Map<String, Object> exceptionAttributes, String message, Throwable cause) {
        super(message, cause);
        this.exceptionAttributes = exceptionAttributes;
    }

    public GenericLizzieException(Map<String, Object> exceptionAttributes, Throwable cause) {
        super(cause);
        this.exceptionAttributes = exceptionAttributes;
    }

    public Map<String, ?> getExceptionAttributes() {
        return exceptionAttributes;
    }

    public void setExceptionAttributes(Map<String, Object> exceptionAttributes) {
        this.exceptionAttributes = exceptionAttributes;
    }

    @Override
    public String getMessage() {
        return toString();
    }

    @Override
    public String getLocalizedMessage() {
        return super.getLocalizedMessage() + ". " + toString();
    }

    @Override
    public int size() {
        return exceptionAttributes.size();
    }

    @Override
    public boolean isEmpty() {
        return exceptionAttributes.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return exceptionAttributes.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return exceptionAttributes.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return exceptionAttributes.get(key);
    }

    public Object put(String key, Object value) {
        return exceptionAttributes.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return exceptionAttributes.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        exceptionAttributes.putAll(m);
    }

    @Override
    public void clear() {
        exceptionAttributes.clear();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return exceptionAttributes.keySet();
    }

    @NotNull
    @Override
    public Collection<Object> values() {
        return exceptionAttributes.values();
    }

    @NotNull
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return exceptionAttributes.entrySet();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("message", super.getMessage())
                .add("cause", getCause())
                .add("exceptionAttributes", exceptionAttributes)
                .toString();
    }
}
