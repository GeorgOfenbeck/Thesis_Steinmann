package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM;

public class TypeDescriptor {
	private String name;
	private String cName;
	private String javaName;
	private String scanfSpecification;
	private String scannerMethod;
	private String javaBoxedName;
	private boolean isReference;
	
	public TypeDescriptor(String name, String cName, String javaName, String scanfSpecification, String scannerMethod, String javaBoxedName){
		this.name = name;
		this.cName = cName;
		this.javaName = javaName;
		this.scanfSpecification = scanfSpecification;
		this.scannerMethod = scannerMethod;
		this.javaBoxedName = javaBoxedName;
		
	}
	
	public TypeDescriptor(String name) {
		isReference=true;
		this.name = name;
		this.cName=name;
		this.javaName=name;
		this.javaBoxedName = name;
	}

	public String getName() {
		return name;
	}

	public String getcName() {
		return cName;
	}

	public String getJavaName() {
		return javaName;
	}

	public String getScanfSpecification() {
		return scanfSpecification;
	}

	public String getScannerMethod() {
		return scannerMethod;
	}


	public boolean isReference() {
		return isReference;
	}

	public String getJavaBoxedName() {
		return javaBoxedName;
	}


}
