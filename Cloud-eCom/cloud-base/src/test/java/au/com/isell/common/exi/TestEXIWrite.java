package au.com.isell.common.exi;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import au.com.isell.common.exi.EXIDriver;
import au.com.isell.common.exi.Person;

import com.thoughtworks.xstream.XStream;

public class TestEXIWrite {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		XStream xs = new XStream(new EXIDriver());
		xs.alias("person", Person.class);
		Person p1 = new Person();
		p1.setName("John Smith");
		p1.setAge(25);
		p1.setGender(true);
		p1.setId(UUID.randomUUID());
		Person father = new Person();
		father.setName("Al Smith");
		father.setAge(60);
		father.setGender(true);
		father.setId(UUID.randomUUID());
		Person mother = new Person();
		mother.setName("Jane Smith");
		mother.setAge(56);
		mother.setGender(false);
		mother.setId(UUID.randomUUID());
		List<Person> parents = new ArrayList<Person>();
		parents.add(father);
		parents.add(mother);
		p1.setParents(parents);
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream("c:/work/Efficient XML/samples/person.exi");
			xs.toXML(p1, fout);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		Map<String,Person> parentMaps = new HashMap<String,Person>();
		parentMaps.put(father.getName(), father);
		parentMaps.put(mother.getName(), mother);
		
		FileOutputStream fout1 = null;
		try {
			fout1 = new FileOutputStream("c:/work/Efficient XML/samples/map_person.exi");
			xs.toXML(parentMaps, fout1);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		Map<String,Person> parentMaps1 = (Map<String,Person>) xs.fromXML(new File("c:/work/Efficient XML/samples/map_person.exi"));
		XStream xs1 = new XStream();
		xs1.alias("person", Person.class);
		try {
			fout1 = new FileOutputStream("c:/work/Efficient XML/samples/map_person.xml");
			xs1.toXML(parentMaps1, fout1);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

}
