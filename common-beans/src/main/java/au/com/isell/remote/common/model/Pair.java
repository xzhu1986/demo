package au.com.isell.remote.common.model;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Created by IntelliJ IDEA.
 * User: brucez
 * Date: 9/06/2005
 * Time: 15:57:31
 * To change this template use File | Settings | File Templates.
 */
@XStreamAlias("pair")
public class Pair<K,V> implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -7450539197853112132L;
    private K key;
    private V value;

    public Pair() {
    }

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;

        final Pair pair = (Pair) o;

        if (key != null ? !key.equals(pair.key) : pair.key != null) return false;

        return true;
    }

    @Override
	public int hashCode() {
        return (key != null ? key.hashCode() : 0);
    }
    
    @Override
	public String toString() {
        return "[" + key + ',' + value + "]";
    }
}
