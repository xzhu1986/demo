package au.com.isell.rlm.module.jbe.common;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Vector;

/**
 * User: kierend
 * Date: 24/02/2005
 * Time: 09:42:40
 */
public class DataElement {
    private String name;
    private Object value;

    private DataElement parent;
    private Vector<DataElement> children;
    
    private boolean repeatable = false;

    public DataElement(String name) {
        this.name = name;
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public boolean hasChild(String name) {
        if (hasChildren()) {
            for (DataElement element : children) {
                if (element.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Collection<DataElement> getChildren(String name) throws XMLException {
        if (children == null) return null;
        Vector<DataElement> v = new Vector<DataElement>();
        for (DataElement element : children) {
            if (element.getName().equals(name)) {
                v.add(element);
            }
        }
        return v;
    }

    public Object getValue(String name) throws XMLException {
        DataElement element = getChild(name);
        if (element != null) {
            return element.getValue();
        } else {
            return null;
        }
    }

    public DataElement getChild(String name) throws XMLException {
        if (children == null) return null;
        Collection<DataElement> col = getChildren(name);
        int count = col.size();
        if (count == 1) {
            return col.iterator().next();
        }
        if (count > 1) {
            throw new XMLException("Found multiple children matching '" + name + "'");
        } else {
            return null;
            // throw new XMLException("Child '" + name + "' not found");
        }
    }

    public void removeFromParent() {
        parent.removeChild(this);
        setParent(null);
    }

    public void removeChild(DataElement element) {
        children.remove(element);
    }

    public DataElement addChild(String name, Object bodyValue) {
        DataElement element = addChild(name);
//        element.setValue(convertToString(bodyValue));
        element.setValue(bodyValue);
        return element;
    }

    public String getValueAsString(String name)  {
        Object bodyValue = null;
        try {
			bodyValue=getValue(name);
		} catch (XMLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
        if (bodyValue == null) return null;
        if (bodyValue instanceof String) {
            return (String) bodyValue;
        }
        return bodyValue.toString();
    }

    public boolean getValueAsBoolean(String name, boolean defaultValue) throws XMLException {
        Object value = getValue(name);
        if (value == null) return defaultValue;
        String s = value.toString().trim();
        if (s.length() <= 0) return defaultValue;
        return Boolean.parseBoolean(s);
    }

    public int getValueAsInt(String name, int defaultValue) throws XMLException {
        Object bodyValue = getValue(name);
        if (bodyValue == null) return defaultValue;

        String s = bodyValue.toString().trim();
        if (s.length() <= 0) return defaultValue;
        if (bodyValue instanceof Integer) {
            return (Integer) bodyValue;
        }
        try {
            return new BigDecimal(bodyValue.toString()).intValue();
        } catch (NumberFormatException e) {
            throw new XMLFormatException("The value for '" + name + "' is not a number (" + bodyValue + ")");
        }
    }
    
    public long getValueAsLong(String name, long defaultValue) throws XMLException {
        Object bodyValue = getValue(name);
        if (bodyValue == null) return defaultValue;

        String s = bodyValue.toString().trim();
        if (s.length() <= 0) return defaultValue;
        if (bodyValue instanceof Long) {
            return (Long) bodyValue;
        }
        try {
            return new BigDecimal(bodyValue.toString()).longValue();
        } catch (NumberFormatException e) {
            throw new XMLFormatException("The value for '" + name + "' is not a number (" + bodyValue + ")");
        }
    }

    public double getValueAsDouble(String name, double defaultValue) throws XMLException {
        Object bodyValue = getValue(name);
        if (bodyValue == null) return defaultValue;
        String s = bodyValue.toString().trim();
        if (s.length() <= 0) return defaultValue;
        if (bodyValue instanceof Double) {
            return (Double) bodyValue;
        }
        try {
            return new BigDecimal(bodyValue.toString()).doubleValue();
        } catch (NumberFormatException e) {
            throw new XMLFormatException("The value for '" + name + "' is not a number (" + bodyValue + ")");
        }
    }
    
    public BigDecimal getValueAsDecimal(String name, BigDecimal defaultValue) throws XMLException {
        Object bodyValue = getValue(name);
        if (bodyValue == null) return defaultValue;
        String s = bodyValue.toString().trim();
        if (s.length() <= 0) return defaultValue;
        if (bodyValue instanceof BigDecimal) {
            return (BigDecimal) bodyValue;
        }
        try {
            return new BigDecimal(bodyValue.toString());
        } catch (NumberFormatException e) {
            throw new XMLFormatException("The value for '" + name + "' is not a number (" + bodyValue + ")");
        }
    }


    public DataElement addChild(String name) {
        DataElement element = new DataElement(name);
        element.setParent(this);
        if (children == null) {
            children = new Vector<DataElement>();
        }
        children.add(element);
        return element;
    }

    public DataElement addChild(DataElement element) {
        element.setParent(this);
        if (children == null) {
            children = new Vector<DataElement>();
        }
        children.add(element);
        return element;
    }

    //////////////////////////////
    // GETTERS/SETTERS
    //////////////////////////////

    public DataElement getParent() {
        return parent;
    }

    public void setParent(DataElement parent) {
        this.parent = parent;
    }

    public Vector<DataElement> getChildren() {
        return children;
    }

    public void setChildren(Vector<DataElement> children) {
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    @Override
	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name: ").append(name);
        sb.append(", value: ").append(value).append('\n');
        if (children != null && children.size() > 0) {
            for (DataElement elem : children) {
                sb.append("    ").append(elem.toString().replaceAll("\n    name", "\n        name").replaceAll("\nname", "\n    name"));
            }
        }
        return sb.toString();
    }

}