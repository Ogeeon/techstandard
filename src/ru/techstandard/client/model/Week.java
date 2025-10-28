package ru.techstandard.client.model;

@SuppressWarnings("unused")
public class Week {
	private int id=0;
	private int mon=0;
	private int tue=0;
	private int wed=0;
	private int thu=0;
	private int fri=0;
	private int sat=0;
	private int sun=0;
	private int[] days={0,0,0,0,0,0,0};
	
	public Week() {
	}
	public Week(int id) {
		this.setId(id);
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getMon() {
		return days[0];
	}
	public void setMon(int mon) {
		this.days[0] = mon;
	}

	public int getTue() {
		return days[1];
	}

	public void setTue(int tue) {
		this.days[1] = tue;
	}

	public int getWed() {
		return days[2];
	}

	public void setWed(int wed) {
		this.days[2] = wed;
	}

	public int getThu() {
		return days[3];
	}

	public void setThu(int thur) {
		this.days[3] = thur;
	}

	public int getFri() {
		return days[4];
	}

	public void setFri(int fri) {
		this.days[4] = fri;
	}

	public int getSat() {
		return days[5];
	}

	public void setSat(int sat) {
		this.days[5] = sat;
	}

	public int getSun() {
		return days[6];
	}

	public void setSun(int sun) {
		this.days[6] = sun;
	}

	public int[] getDays() {
		return days;
	}

	public void setDays(int[] days) {
		this.days = days;
	}
	
	public int getDay(int num) {
		return days[num];
	}
	
	public void setDay(int num, int day) {
		days[num] = day;
	}
}
