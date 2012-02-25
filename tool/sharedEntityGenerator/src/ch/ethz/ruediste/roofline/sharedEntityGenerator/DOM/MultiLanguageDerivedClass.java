package ch.ethz.ruediste.roofline.sharedEntityGenerator.DOM;

import java.util.*;

import com.thoughtworks.xstream.annotations.*;

@XStreamAlias("derivedClass")
public class MultiLanguageDerivedClass extends MultiLanguageClassBase {
	@XStreamAsAttribute
	private String baseType;

	@XStreamOmitField
	protected MultiLanguageClassBase baseClass;

	public String getBaseType() {
		return baseType;
	}

	public void setBaseType(String baseType) {
		this.baseType = baseType;
	}

	@Override
	public String getcBaseType() {
		return baseType;
	}

	@Override
	public boolean hascBaseType() {
		return true;
	}

	@Override
	public String getJavaBaseType() {
		return baseClass.getJavaNameQualified();
	}

	@Override
	public boolean hasJavaBaseType() {
		return true;
	}

	@Override
	public List<MultiLanguageFieldBase> getAllFields() {
		// check if the base class has been set
		if (baseClass == null) {
			throw new Error(String.format("Base class of %s is not set",
					getName()));
		}

		// combine the fields of this class with all fields of the base class,
		// transitively
		LinkedList<MultiLanguageFieldBase> result = new LinkedList<MultiLanguageFieldBase>();
		result.addAll(getFields());
		result.addAll(baseClass.getAllFields());
		return result;
	}

	public MultiLanguageClassBase getBaseClass() {
		return baseClass;
	}

	public void setBaseClass(MultiLanguageClassBase baseClass) {
		this.baseClass = baseClass;
	}

	public boolean isGeneratedJavaClassAbstract() {
		return isAbstract()
				|| (baseClass.isAbstract() && javaSuffix != null && !javaSuffix
						.isEmpty());
	}
}
