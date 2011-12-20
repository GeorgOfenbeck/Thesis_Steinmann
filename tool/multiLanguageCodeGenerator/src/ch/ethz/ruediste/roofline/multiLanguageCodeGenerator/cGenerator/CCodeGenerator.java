package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.cGenerator;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import org.apache.velocity.VelocityContext;

import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.CodeGeneratorBase;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLanguageClassBase;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLanguageFieldBase;

/** generates c code from a shared class */
public class CCodeGenerator extends CodeGeneratorBase {

	@Override
	public void generate(List<MultiLanguageClassBase> multiLanguageClasses) {
		// clear output directory
		{
			File outputDirectory = new File("generatedC");
			outputDirectory.delete();
		}

		// generate java code for all classes
		for (MultiLanguageClassBase multiLangugeClass : multiLanguageClasses) {
			// initialize context
			VelocityContext context = new VelocityContext();
			context.put("class", multiLangugeClass);
			String outputFileName = "generatedC/" + multiLangugeClass.getName()
					+ ".h";
			String templateName = "cTemplate.vm";

			// collect all referenced classes
			HashSet<String> referencedClasses = new HashSet<String>();
			if (multiLangugeClass.hascBaseType()) {
				referencedClasses.add(multiLangugeClass.getcBaseType());
			}

			if (multiLangugeClass.getFields() != null) {
				for (MultiLanguageFieldBase field : multiLangugeClass
						.getFields()) {
					if (!field.getTypeDescriptor().isReference()) {
						continue;
					}
					referencedClasses.add(field.getTypeDescriptor().getcName());
				}
			}
			context.put("references", referencedClasses.toArray());

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
