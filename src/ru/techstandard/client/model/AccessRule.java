package ru.techstandard.client.model;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public class AccessRule {
	public interface AccessRuleProps extends PropertyAccess<AccessRule> {
		ModelKeyProvider<AccessRule> id();
		
		@Path("name")
		LabelProvider<AccessRule> nameLabel();
		
		ValueProvider<AccessRule, String> name();
		ValueProvider<AccessRule, Boolean> r();
		ValueProvider<AccessRule, Boolean> u();
		ValueProvider<AccessRule, Boolean> i();
		ValueProvider<AccessRule, Boolean> d();
		ValueProvider<AccessRule, Boolean> p();
	}
	
	private String id="";
	private String name="";
	private boolean r=false;
	private boolean u=false;
	private boolean i=false;
	private boolean d=false;
	private boolean p=false;
	
	public AccessRule() {
	}
	
	public AccessRule(String id, String name) {
		this.setId(id);
		this.setName(name);
	}
	
	public AccessRule(String id, String name, boolean r, boolean u, boolean i, boolean d, boolean p) {
		this.setId(id);
		this.setName(name);
		this.setR(r);
		this.setU(u);
		this.setI(i);
		this.setD(d);
		this.setP(p);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isR() {
		return r;
	}

	public void setR(boolean r) {
		this.r = r;
	}

	public boolean isU() {
		return u;
	}

	public void setU(boolean u) {
		this.u = u;
	}

	public boolean isI() {
		return i;
	}

	public void setI(boolean i) {
		this.i = i;
	}

	public boolean isD() {
		return d;
	}

	public void setD(boolean d) {
		this.d = d;
	}
	
	public boolean isP() {
		return p;
	}

	public void setP(boolean p) {
		this.p = p;
	}

	public String toString() {
		return "["+id+"] "+name+" |"+r+" |"+u+" |"+i+" |"+d+"|"+p+"|";
	}
}
