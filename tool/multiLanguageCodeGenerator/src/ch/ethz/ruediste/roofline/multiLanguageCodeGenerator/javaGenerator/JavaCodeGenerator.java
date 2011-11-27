package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.javaGenerator;

import java.util.List;

import org.apache.velocity.VelocityContext;

import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.CodeGeneratorBase;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLanguageClass;

/** Generates Java for a list of multi language classes */
public class JavaCodeGenerator extends CodeGeneratorBase {
	public JavaCodeGenerator() {

	}

	@Override
	public void generate(List<MultiLanguageClass> multiLanguageClasses) {

		// generate java code for all classes
		for (MultiLanguageClass multiLangugeClass : multiLanguageClasses) {

			VelocityContext context = new VelocityContext();

			// initialize context
			context.put("class", multiLangugeClass);

			String templateName = "javaTemplate.vm";
			String outputFileName = "generatedJava/ch/ethz/ruediste/roofline/dom/"
					+ multiLangugeClass.getName() + ".java";

			applyTemplate(outputFileName, templateName, context, "javaClass");
		}

		// generate SerializerService

		// initialize context
		VelocityContext context = new VelocityContext();
		context.put("classes", multiLanguageClasses);

		String outputFileName = "generatedJava/ch/ethz/ruediste/roofline/dom/MultiLanguageSerializationService.java";
		String templateName = "javaSerializationServiceTemplate.vm";
		applyTemplate(outputFileName, templateName, context,
				"javaSerializationService");

	}

}
