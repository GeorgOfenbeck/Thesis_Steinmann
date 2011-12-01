package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM;

import java.util.LinkedList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

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
		return baseType;
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
}
