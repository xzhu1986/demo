package au.com.isell.common.exi;

import java.io.File;

import com.thoughtworks.xstream.XStream;

public class TestEXIRead {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		XStream xs = new XStream(new EXIDriver());
		xs.alias("person", Person.class);
		Person p1 = (Person) xs.fromXML(new File("/Users/yezhou/Documents/tech/Efficient XML/samples/person.exi"));
		System.out.println(p1);
	}

}
