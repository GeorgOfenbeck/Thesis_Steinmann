package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.cGenerator;

import java.util.List;

import org.apache.velocity.VelocityContext;

import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.CodeGeneratorBase;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLanguageClass;

/** generates c code from a shared class */
public class CCodeGenerator extends CodeGeneratorBase {

	@Override
	public void generate(List<MultiLanguageClass> multiLanguageClasses) {
		// generate java code for all classes
		for (MultiLanguageClass multiLangugeClass : multiLanguageClasses) {
			// initialize context
			VelocityContext context = new VelocityContext();
			context.put("class", multiLangugeClass);
			String outputFileName = "generatedC/" + multiLangugeClass.getName()
					+ ".h";
			String templateName = "cTemplate.vm";

			// generate output
			applyTemplate(outputFileName, templateName, context, "cClass");
		}

		// generate serializer service and type enum
		
		// initialize context
		VelocityContext context = new VelocityContext();
		context.put("classes", multiLanguageClasses);

		// generate SerializerService
		String outputFileName = "generatedC/MultiLanguageSerializationService.cpp";
		String templateName = "cSerializationServiceTemplate.vm";
		applyTemplate(outputFileName, templateName, context,
				"cSerializationService");

		// generate type enum
		applyTemplate("generatedC/MultiLanguageTypeEnum.h",
				"cMultiLanguageTypeEnumTemplate.vm", context,
				"cMultiLanguageTypeEnum");

	}
}
