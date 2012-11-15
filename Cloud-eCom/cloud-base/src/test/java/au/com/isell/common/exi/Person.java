package au.com.isell.common.exi;

import java.util.List;
import java.util.UUID;

public class Person {
	private String name;
	private int age;
	private boolean gender;
	private UUID id;
	private List<Person> parents;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public boolean isGender() {
		return gender;
	}
	public void setGender(boolean gender) {
		this.gender = gender;
	}
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public List<Person> getParents() {
		return parents;
	}
	public void setParents(List<Person> parents) {
		this.parents = parents;
	}
	@Override
	public String toString() {
		return "Person [name=" + name + ", age=" + age + ", gender=" + gender
				+ ", id=" + id + ", parents=" + parents + "]";
	}
}
