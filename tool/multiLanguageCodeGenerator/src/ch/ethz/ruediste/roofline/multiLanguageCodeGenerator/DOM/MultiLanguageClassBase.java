package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM;

import java.util.*;

import com.thoughtworks.xstream.annotations.*;

public abstract class MultiLanguageClassBase {

	@XStreamImplicit
	private LinkedList<MultiLanguageFieldBase> fields = new LinkedList<MultiLanguageFieldBase>();
	@XStreamAsAttribute
	protected String name;
	@XStreamAsAttribute
	protected String javaSuffix = "";
	@XStreamAsAttribute
	protected String cSuffix = "";
	@XStreamAsAttribute()
	private String comment;
	@XStreamAsAttribute
	private boolean isAbstract = false;

	public abstract String getcBaseType();

	public abstract boolean hascBaseType();

	public abstract String getJavaBaseType();

	public abstract boolean hasJavaBaseType();

	public MultiLanguageClassBase() {
		super();
	}

	/**
	 * Fields declared directly in this class
	 */
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

	/**
	 * all fields of this class, including fields of eventual base classes,
	 * transitively.
	 */
	public List<MultiLanguageFieldBase> getAllFields() {
		return getFields();
	}

	/**
	 * the name of the class for the java source code. Contains the suffix
	 */
	public String getJavaName() {
		if (javaSuffix != null && !javaSuffix.equals("")) {
			return name + javaSuffix;
		}
		return name;
	}

	/**
	 * the name of the class for the C source code. Contains the suffix
	 */
	public String getCName() {
		if (cSuffix != null && !cSuffix.equals("")) {
			return name + cSuffix;
		}
		return name;
	}

	public String getcSuffix() {
		return cSuffix;
	}

	public void setcSuffix(String cSuffix) {
		this.cSuffix = cSuffix;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}
}