package au.com.isell.rlm.module.jbe.common;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * User: kierend
 * Date: 1/08/2005
 * Time: 09:59:08
 */
public class MessageWriter {
    PrintStream out;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public MessageWriter(PrintStream out) {
        this.out = out;
    }

    public void writeMessage(Message message) {
        writeElement("", message);
    }

    public static DataElement createHeader(String request) {
        DataElement header = new DataElement("header");
        header.addChild("name", request);
        header.addChild("username", "iSell");
        header.addChild("password", "iSell");
        return header;
    }

    public void writeElement(String indent, DataElement parent) {
        //todo: conversion to string (escape xml)
        out.print(indent + "<" + parent.getName());
        if (parent.isRepeatable()) {
            out.print(" repeatable=\"true\"");
        }
        out.print(">");
        Object value = parent.getValue();
        if (value != null) {
            String valueString = value.toString();
            if (value instanceof Date) {
                valueString = DATE_FORMAT.format(value);
            }
            if (valueString.contains("\n")) {
                out.print("<![CDATA[");
                out.print(valueString);
                out.print("]]>");
            } else {
                out.print(encode(valueString));
            }

        }
        Vector<DataElement> children = parent.getChildren();
        if (children != null) {
            out.println();
            for (DataElement child : children) {
                writeElement(indent + "  ", child);
            }
            out.print(indent);
        }
        out.println("</" + parent.getName() + ">");
    }

    private String encode(String valueString) {

        valueString = valueString.replaceAll("&", "&amp;");
        valueString = valueString.replaceAll("<", "&lt;");
        valueString = valueString.replaceAll(">", "&gt;");
        valueString = valueString.replaceAll("'", "&apos;");
        valueString = valueString.replaceAll("\"", "&quot;");
         byte[] strbytes = valueString.getBytes();
        for (byte b : strbytes) {
            if ((b >= 1) && (b <= 9)) {
                valueString.replaceAll(byteToString(b), "&#'&ChrValue&';");
            }
            if ((b >= 10) && (b <= 31)) {
                valueString.replaceAll(byteToString(b), "&#10;");
            }
            if ((b >= 127) && (b < 251)) {
                valueString.replaceAll(byteToString(b), "&#'&ChrValue&';");
            }
            if ((b >= 252) && (b < 255)) {
                valueString.replaceAll(byteToString(b), "&#10;");
            }
        }

        return valueString;
    }

    public static String byteToString(byte b) {
        byte high, low;
        byte maskHigh = (byte) 0xf0;
        byte maskLow = 0x0f;

        high = (byte) ((b & maskHigh) >> 4);
        low = (byte) (b & maskLow);

        StringBuffer buf = new StringBuffer();
        buf.append(findHex(high));
        buf.append(findHex(low));

        return buf.toString();
    }

    private static char findHex(byte b) {
        int t = new Byte(b).intValue();
        t = t < 0 ? t + 16 : t;

        if ((0 <= t) && (t <= 9)) {
            return (char) (t + '0');
        }

        return (char) (t - 10 + 'A');
    }

}
