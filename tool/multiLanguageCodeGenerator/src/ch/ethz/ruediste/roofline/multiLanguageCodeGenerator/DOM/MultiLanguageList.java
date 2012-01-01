package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * represents a field containing a list
 * 
 * The java default is always set to a new list instance
 */
@XStreamAlias("list")
public class MultiLanguageList extends MultiLanguageFieldBase {

	public String getcType() {
		return String.format("std::vector<%s>", super.getcType());
	}

	/**
	 * The type of an item of the list, if the field contains a list. The normal
	 * field type otherwise
	 * 
	 * @return
	 */
	public String getcItemType() {
		return super.getcType();
	}

	public String getJavaType() {
		return String.format("List<%s>", getJavaItemType());
	}

	public String getJavaItemType() {
		return getTypeDescriptor().getJavaBoxedName();
	}

	@Override
	public String getJavaDefault() {
		return String.format("new LinkedList<%s>()", getJavaItemType());
	}

	@Override
	public boolean hasJavaDefault() {
		return true;
	}
}
