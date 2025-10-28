package ru.techstandard.client.model;

import java.io.Serializable;

public class Client implements Serializable {
	private static final long serialVersionUID = 861612537040126844L;
	private int ID=0;
	private String name="";
	private String fullName="";
	private String boss="";
	private String address="";
	private String address2="";
	private String phone="";
	private String fax="";
	private String inn="";
	private String kpp="";
	private String email="";
	private boolean actual=false;
	private String bankName="";
	private String rsch="";
	private String ksch="";
	private String okpo="";
	private String okato="";
	private String ogrn="";
	private int deletedBy=0;

	public Client() {
	}
	
	public Client(String name) {
		this.setName(name);
	}
	
	public Client(int ID, String name) {
		this.setId(ID);
		this.setName(name);
	}
	
	public Client(int ID, String name, String boss, String address, String phone, String inn, String email) {
		this.setId(ID);
		this.setName(name);
		this.setBoss(boss);
		this.setAddress(address);
		this.setPhone(phone);
		this.setInn(inn);
		this.setEmail(email);
	}

	public int getId() {
		return ID;
	}

	public void setId(int iD) {
		this.ID = iD;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getBoss() {
		return boss;
	}

	public void setBoss(String boss) {
		this.boss = boss;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getInn() {
		return inn;
	}

	public String getKpp() {
		return kpp;
	}

	public void setKpp(String kpp) {
		this.kpp = kpp;
	}

	public void setInn(String inn) {
		this.inn = inn;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public boolean isActual() {
		return actual;
	}

	public void setActual(boolean actual) {
		this.actual = actual;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getRsch() {
		return rsch;
	}

	public void setRsch(String rsch) {
		this.rsch = rsch;
	}

	public String getKsch() {
		return ksch;
	}

	public void setKsch(String ksch) {
		this.ksch = ksch;
	}

	public String getOkpo() {
		return okpo;
	}

	public void setOkpo(String okpo) {
		this.okpo = okpo;
	}

	public String getOkato() {
		return okato;
	}

	public void setOkato(String okato) {
		this.okato = okato;
	}

	public String getOgrn() {
		return ogrn;
	}

	public void setOgrn(String ogrn) {
		this.ogrn = ogrn;
	}

	public int getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(int deletedBy) {
		this.deletedBy = deletedBy;
	}

	public String toString() {
		return "Client["+ ID +"] = name:"+name+", fullName="+fullName+", boss:"+boss+", address:"+address+", address2:"+address2+
				", phone:"+phone+", fax:"+fax+", inn:"+inn+", kpp:"+kpp+", email:"+email+", actual:"+actual+", bamk:"+bankName+", rsch:"+rsch+
				", ksch:"+ksch+", okpo:"+okpo+", okato:"+okato+", ogrn:"+ogrn;
	}
}
