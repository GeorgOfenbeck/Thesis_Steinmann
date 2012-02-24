package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator;

import java.io.File;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.*;

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

		Template headerTemplate = getTemplate("cTemplate.vm");
		Template cppTemplate = getTemplate("cppTemplate.vm");

		// generate java code for all classes
		for (MultiLanguageClassBase multiLanguageClass : multiLanguageClasses) {
			// initialize context
			VelocityContext context = new VelocityContext();
			context.put("class", multiLanguageClass);
			String outputPathName = "generatedC/";
			if (!multiLanguageClass.getPath().isEmpty())
				outputPathName += StringUtils.join(
						multiLanguageClass.getPath(), "/") + "/";

			// collect all referenced classes
			HashSet<String> referencedClasses = new HashSet<String>();
			if (multiLanguageClass instanceof MultiLanguageDerivedClass) {
				MultiLanguageDerivedClass derivedClass = (MultiLanguageDerivedClass) multiLanguageClass;

				MultiLanguageClassBase baseClass = derivedClass.getBaseClass();

				String baseClassInclude = "sharedEntities/";
				if (!baseClass.getPath().isEmpty()) {
					baseClassInclude += StringUtils.join(baseClass.getPath(),
							"/") + "/";
				}
				baseClassInclude += baseClass.getName();

				context.put("baseClassInclude", baseClassInclude);
			}

			if (multiLanguageClass.getFields() != null) {
				for (MultiLanguageFieldBase field : multiLanguageClass
						.getFields()) {
					if (!field.getTypeDescriptor().isReference()) {
						continue;
					}

					referencedClasses.add(field.getTypeDescriptor().getName());
				}
			}
			context.put("references", referencedClasses.toArray());

			// generate output
			applyTemplate(headerTemplate, context, outputPathName
					+ multiLanguageClass.getCName() + ".h");

			// gemerate c[[ fo;e
			context = new VelocityContext();
			context.put("class", multiLanguageClass);
			String includeFile = "sharedEntities/";
			if (!multiLanguageClass.getPath().isEmpty()) {
				includeFile += StringUtils.join(multiLanguageClass.getPath(),
						"/") + "/";
			}
			includeFile += multiLanguageClass.getName();
			context.put("includeFile", includeFile);
			applyTemplate(cppTemplate, context, outputPathName
					+ multiLanguageClass.getName() + ".cpp");

		}
	}
}
