package ch.ethz.ruediste.roofline.sharing;
import java.io.Writer;

import ch.ethz.ruediste.roofline.sharing.DOM.SharedClass;


public interface ICodeGenerator {
	void generate(SharedClass sharedClass, Writer writer);
	String getExtension();
}
