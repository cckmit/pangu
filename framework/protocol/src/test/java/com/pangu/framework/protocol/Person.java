package com.pangu.framework.protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.pangu.framework.protocol.annotation.Ignore;
import com.pangu.framework.protocol.annotation.Transable;

@Transable
public class Person {
	private long id;
	private String name;
	private String ok = "EMPTY";
	private int a;
	private int bbbbbbbbbbbbbbbbb;
	private int cc;
	private Status status = Status.NEW;
	private Status status1 = Status.NEW;
	private Status status2 = Status.NEW;
	private Status status3 = Status.NEW;
	private Status status4 = Status.NEW;
	private Status status5 = Status.NEW;
	private Status status6 = Status.NEW;
	private List<Person> list = new ArrayList<Person>();

	private Person() {
	}

	public static Person valueOf(long id, String name) {
		Person e = new Person();
		e.id = id;
		e.name = name;
		return e;
	}

	public long getId() {
		return id;
	}

	void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	@Ignore
	public String getOk() {
		return ok;
	}

	public void setOk(String ok) {
		this.ok = ok;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((ok == null) ? 0 : ok.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Person other = (Person) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (ok == null) {
			if (other.ok != null)
				return false;
		} else if (!ok.equals(other.ok))
			return false;
		return true;
	}

	public int getA() {
		return a;
	}

	public void setA(int a) {
		this.a = a;
	}

	public int getBbbbbbbbbbbbbbbbb() {
		return bbbbbbbbbbbbbbbbb;
	}

	public void setBbbbbbbbbbbbbbbbb(int bbbbbbbbbbbbbbbbb) {
		this.bbbbbbbbbbbbbbbbb = bbbbbbbbbbbbbbbbb;
	}

	public int getCc() {
		return cc;
	}

	public void setCc(int cc) {
		this.cc = cc;
	}

	public Status getStatus1() {
		return status1;
	}

	public void setStatus1(Status status1) {
		this.status1 = status1;
	}

	public Status getStatus2() {
		return status2;
	}

	public void setStatus2(Status status2) {
		this.status2 = status2;
	}

	public Status getStatus3() {
		return status3;
	}

	public void setStatus3(Status status3) {
		this.status3 = status3;
	}

	public Status getStatus4() {
		return status4;
	}

	public void setStatus4(Status status4) {
		this.status4 = status4;
	}

	public Status getStatus5() {
		return status5;
	}

	public void setStatus5(Status status5) {
		this.status5 = status5;
	}

	public Status getStatus6() {
		return status6;
	}

	public void setStatus6(Status status6) {
		this.status6 = status6;
	}

	public List<Person> getList() {
		return list;
	}

	public void setList(List<Person> list) {
		this.list = list;
	}

	public Collection<String> getStrings() {
		return Arrays.asList("A", "B");
	}

	public void setStrings(Collection<String> v) {
		// System.out.println(v);
	}

}
