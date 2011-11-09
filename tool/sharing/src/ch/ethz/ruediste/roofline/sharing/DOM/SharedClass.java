package ch.ethz.ruediste.roofline.sharing.DOM;
import java.util.LinkedList;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("class")
public class SharedClass {
	@XStreamAsAttribute()
	private String comment;
	
	@XStreamImplicit
	private LinkedList<SharedFieldBase> fields
		=new LinkedList<SharedFieldBase>();
	
	@XStreamAsAttribute
	private String name;
	@XStreamAsAttribute
	private String cBaseType;
	
	@XStreamAsAttribute
	private String javaBaseType;


	
	public LinkedList<SharedFieldBase> getFields() {
		return fields;
	}

	public void addField(SharedFieldBase sharedField) {
		fields.add(sharedField);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getcBaseType() {
		return cBaseType;
	}

	public void setcBaseType(String cBaseType) {
		this.cBaseType = cBaseType;
	}

	public String getJavaBaseType() {
		return javaBaseType;
	}

	public void setJavaBaseType(String javaBaseType) {
		this.javaBaseType = javaBaseType;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment=comment;
	}


}
