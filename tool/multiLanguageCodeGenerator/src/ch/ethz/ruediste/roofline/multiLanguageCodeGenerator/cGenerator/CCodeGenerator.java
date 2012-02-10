package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.cGenerator;

import java.io.File;
import java.util.*;

import org.apache.velocity.VelocityContext;

import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.*;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.*;

/** generates c code from a shared class */
public class CCodeGenerator extends CodeGeneratorBase {

	@Override
	public void generate(List<MultiLanguageClassBase> multiLanguageClasses) {
		// clear output directory
		{
			File outputDirectory = new File("generatedC");
			System.out.println("output directory is "
					+ outputDirectory.getAbsolutePath());
			if (outputDirectory.exists()) {
				Utility.deleteDirectory(outputDirectory);
			}
		}

		// generate java code for all classes
		for (MultiLanguageClassBase multiLanguageClass : multiLanguageClasses) {
			// initialize context
			VelocityContext context = new VelocityContext();
			context.put("class", multiLanguageClass);
			String outputFileName = "generatedC/"
					+ multiLanguageClass.getCName() + ".h";
			String templateName = "cTemplate.vm";

			// collect all referenced classes
			HashSet<String> referencedClasses = new HashSet<String>();
			if (multiLanguageClass.hascBaseType()) {
				String baseType = multiLanguageClass.getcBaseType();
				referencedClasses.add(//"sharedDOM/" + 
						baseType);
			}

			if (multiLanguageClass.getFields() != null) {
				for (MultiLanguageFieldBase field : multiLanguageClass
						.getFields()) {
					if (!field.getTypeDescriptor().isReference()) {
						continue;
					}
					referencedClasses.add(//"sharedDOM/"+
							field.getTypeDescriptor().getName());
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
	}
}
