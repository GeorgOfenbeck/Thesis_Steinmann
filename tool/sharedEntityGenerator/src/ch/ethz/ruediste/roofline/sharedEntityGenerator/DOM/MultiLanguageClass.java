package ch.ethz.ruediste.roofline.sharedEntityGenerator.DOM;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * represents a multi language class
 * 
 * @author ruedi
 * 
 */
@XStreamAlias("class")
public class MultiLanguageClass extends MultiLanguageClassBase {

	@XStreamAsAttribute
	private String cBaseType;

	@XStreamAsAttribute
	private String javaBaseType;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.ruediste.roofline.sharedEntityGenerator.DOM.IMultiLanguageClass
	 * #getcBaseType()
	 */
	@Override
	public String getcBaseType() {
		return cBaseType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.ruediste.roofline.sharedEntityGenerator.DOM.IMultiLanguageClass
	 * #hascBaseType()
	 */
	@Override
	public boolean hascBaseType() {
		return cBaseType != null && !cBaseType.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.ruediste.roofline.sharedEntityGenerator.DOM.IMultiLanguageClass
	 * #setcBaseType(java.lang.String)
	 */
	public void setcBaseType(String cBaseType) {
		this.cBaseType = cBaseType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.ruediste.roofline.sharedEntityGenerator.DOM.IMultiLanguageClass
	 * #getJavaBaseType()
	 */
	@Override
	public String getJavaBaseType() {
		return javaBaseType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.ruediste.roofline.sharedEntityGenerator.DOM.IMultiLanguageClass
	 * #hasJavaBaseType()
	 */
	@Override
	public boolean hasJavaBaseType() {
		return javaBaseType != null && !javaBaseType.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.ruediste.roofline.sharedEntityGenerator.DOM.IMultiLanguageClass
	 * #setJavaBaseType(java.lang.String)
	 */
	public void setJavaBaseType(String javaBaseType) {
		this.javaBaseType = javaBaseType;
	}

}
