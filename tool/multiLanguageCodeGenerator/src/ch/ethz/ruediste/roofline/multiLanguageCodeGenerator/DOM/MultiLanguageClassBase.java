package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

public abstract class MultiLanguageClassBase {

	@XStreamImplicit
	private LinkedList<MultiLanguageFieldBase> fields = new LinkedList<MultiLanguageFieldBase>();
	@XStreamAsAttribute
	protected String name;
	@XStreamAsAttribute
	protected String javaSuffix = "";
	@XStreamAsAttribute()
	private String comment;

	public abstract String getcBaseType();

	public abstract boolean hascBaseType();

	public abstract String getJavaBaseType();

	public abstract boolean hasJavaBaseType();

	public MultiLanguageClassBase() {
		super();
	}

	public LinkedList<MultiLanguageFieldBase> getFields() {
		if (fields == null)
			return new LinkedList<MultiLanguageFieldBase>();
		return fields;
	}

	public void addField(MultiLanguageFieldBase sharedField) {
		fields.add(sharedField);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setTypeDescriptors(
			HashMap<String, FieldTypeDescriptor> typeDescriptors) {
		// can happen due to deserialization
		if (fields == null) {
			return;
		}

		// set the type descriptor of all fields
		for (MultiLanguageFieldBase field : fields) {
			// check if the type of the field is known
			if (!typeDescriptors.containsKey(field.getType())) {
				throw new Error(String.format(
						"Type %s of field %s in class %s not found",
						field.getType(), field.getName(), getName()));
			}

			// set the type descriptor
			field.setTypeDescriptor(typeDescriptors.get(field.getType()));
		}
	}

	public String getNameUpperCamel() {
		return getName().substring(0, 1).toUpperCase() + getName().substring(1);
	}

	public String getJavaSuffix() {
		return javaSuffix;
	}

	public void setJavaSuffix(String javaSuffix) {
		this.javaSuffix = javaSuffix;
	}

	public List<MultiLanguageFieldBase> getAllFields() {
		return getFields();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.IMultiLanguageClass
	 * #getJavaName()
	 */
	public String getJavaName() {
		if (javaSuffix != null && !javaSuffix.equals("")) {
			return name + javaSuffix;
		}
		return name;
	}
}