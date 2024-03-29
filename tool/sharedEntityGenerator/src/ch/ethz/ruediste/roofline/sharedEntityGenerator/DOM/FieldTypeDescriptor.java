package ch.ethz.ruediste.roofline.sharedEntityGenerator.DOM;

import java.util.*;

/**
 * Describes the type of a field. Various information is needed for a field type
 * in order to generate the source code in both C and Java, and for
 * serialization.
 */
public class FieldTypeDescriptor {
	private String name;
	private String cName;
	private String javaName;
	private String scanfSpecification;
	private String scannerMethod;
	private String javaBoxedName;
	private boolean isReference;
	private String cDefault;
	private String javaDefault;
	private List<String> path = new ArrayList<String>();

	/**
	 * Initialize an instance
	 * 
	 * @param name
	 *            type name
	 * @param cName
	 *            type name in C
	 * @param javaName
	 *            type name in java
	 * @param scanfSpecification
	 *            scanf specification for parsing in C
	 * @param scannerMethod
	 *            scanner method name to be used in Java
	 * @param javaBoxedName
	 *            boxed name of the type for java lists
	 */
	public FieldTypeDescriptor(String name, String cName, String javaName,
			String scanfSpecification, String scannerMethod,
			String javaBoxedName, String cDefault, String javaDefault) {
		this.name = name;
		this.cName = cName;
		this.javaName = javaName;
		this.scanfSpecification = scanfSpecification;
		this.scannerMethod = scannerMethod;
		this.javaBoxedName = javaBoxedName;
		this.cDefault = cDefault;
		this.javaDefault = javaDefault;

	}

	/**
	 * Initialize an instance, describing a MultiLanguageClass.
	 * 
	 * The resulting type is a reference type and therefore needs no
	 * scanfSpecification and scanner method. All names are set to the name of
	 * the class (cName, javaName, javaBoxedName)
	 * 
	 * @param name
	 *            Name of the MultiLanugageClass.
	 */
	public FieldTypeDescriptor(MultiLanguageClassBase clazz) {
		isReference = true;
		this.name = clazz.getName();
		this.path = clazz.getPath();
		this.cName = name;
		this.javaName = clazz.getJavaNameQualified();
		this.javaBoxedName = javaName;
		this.cDefault = "NULL";
	}

	/**
	 * name of the type in definitions
	 */
	public String getName() {
		return name;
	}

	/**
	 * name of the type in C
	 */
	public String getcName() {
		return cName;
	}

	/**
	 * name of the type in Java
	 */
	public String getJavaName() {
		return javaName;
	}

	/**
	 * scanf specification (%d,%f, etc) used to scan a primitive type
	 */
	public String getScanfSpecification() {
		return scanfSpecification;
	}

	/**
	 * name of the method in the Scanner class used to read a primitive type
	 */
	public String getScannerMethod() {
		return scannerMethod;
	}

	/**
	 * true if the type is a reference and not a primitive type
	 * 
	 * @return
	 */
	public boolean isReference() {
		return isReference;
	}

	/**
	 * the type as boxed java type. Boxed type for primitive type, normal type
	 * otherwise. Used as type parameter for collection classes in Java.
	 */
	public String getJavaBoxedName() {
		return javaBoxedName;
	}

	public String getCDefault() {
		return cDefault;
	}

	public String getJavaDefault() {
		return javaDefault;
	}

	public boolean hasJavaDefault() {
		return javaDefault != null && !javaDefault.isEmpty();
	}

	public List<String> getPath() {
		return path;
	}

	public void setPath(List<String> path) {
		this.path = path;
	}
}
