package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.FieldTypeDescriptor;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLanguageClassBase;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLanguageClass;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLanguageDefine;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLanguageDerivedClass;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLanguageField;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLanguageFieldBase;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM.MultiLanguageList;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.cGenerator.CCodeGenerator;
import ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.javaGenerator.JavaCodeGenerator;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/** loads all class definitions and generates the code */
public class Main {

	public static void main(String args[]) throws FileNotFoundException {

		// get input filenames
		File[] inputFiles = scanClassDefinitionDirectory(new File(
				"./definitions"));

		// create Type Descriptors
		HashMap<String, FieldTypeDescriptor> typeDescriptors = createTypeDescriptorsForPrimitiveTypes();

		// load the class definitions
		List<MultiLanguageClassBase> classes = loadClassDefinitions(inputFiles);

		// create type descriptor for each loaded class
		for (MultiLanguageClassBase multiLanguageClass : classes) {
			typeDescriptors.put(multiLanguageClass.getName(),
					new FieldTypeDescriptor(multiLanguageClass.getName()));
		}

		// set type descriptors of all fields in all classes
		for (MultiLanguageClassBase multiLanguageClass : classes) {
			multiLanguageClass.setTypeDescriptors(typeDescriptors);
			if (multiLanguageClass instanceof MultiLanguageDerivedClass) {
				MultiLanguageDerivedClass derivedClass = (MultiLanguageDerivedClass) multiLanguageClass;
				derivedClass.setBaseClass(findBaseClass(
						derivedClass,
						classes));
			}
		}

		// instantiate generators
		CodeGeneratorBase generators[] = { new JavaCodeGenerator(),
				new CCodeGenerator() };

		// generate code
		for (CodeGeneratorBase generator : generators)
		{
			generator.generate(classes);
		}
	}

	/**
	 * Finds the base class of a bulti language class
	 * 
	 * @param multiLanguageClass
	 *            class to find the base type for
	 * @param classes
	 *            list of all available classes
	 * @return
	 */
	private static MultiLanguageClassBase findBaseClass(
			MultiLanguageDerivedClass multiLanguageClass,
			List<MultiLanguageClassBase> classes) {
		// if there is no base type, there is no base class
		if (multiLanguageClass.getBaseType() == null)
			return null;

		// iterate over all available classes and return the first matching one
		for (MultiLanguageClassBase baseClass : classes) {
			if (baseClass.getName().equals(multiLanguageClass.getBaseType())) {
				return baseClass;
			}
		}

		throw new Error(String.format("base class %s of classs %s not found",
				multiLanguageClass.getBaseType(), multiLanguageClass.getName()));
	}

	/** load all class definitions defined in the given input files */
	private static List<MultiLanguageClassBase> loadClassDefinitions(
			File[] inputFiles) {
		// inistialize XStream
		XStream xStream = createXStream();

		// load all classes
		List<MultiLanguageClassBase> classes = new LinkedList<MultiLanguageClassBase>();
		for (File inputFile : inputFiles) {
			// load the input
			MultiLanguageClassBase multiLanguageClass = (MultiLanguageClassBase) xStream
					.fromXML(inputFile);
			System.out.println(xStream.toXML(multiLanguageClass));

			// add class to the list of classes
			classes.add(multiLanguageClass);
		}
		return classes;
	}

	/**
	 * Scan the given directory for all files containing class definitions (all
	 * .xml files), including sub directories (recursively)
	 */
	private static File[] scanClassDefinitionDirectory(
			File classDefinitionDirectory) {

		// get all sub directories
		File inputDirectoriesFiles[] = classDefinitionDirectory
				.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						// get all directories
						return pathname.isDirectory();
					}
				});

		// get all files in all subdirectories
		ArrayList<File> inputFiles = new ArrayList<File>();

		// scan subdirectories recursively
		for (File dir : inputDirectoriesFiles) {
			Collections.addAll(inputFiles, scanClassDefinitionDirectory(dir));
		}

		// add files in the current directory
		Collections.addAll(
				inputFiles,
				classDefinitionDirectory
						.listFiles(new FileFilter() {
							@Override
							public boolean accept(File pathname) {
								// get all .xml files
								return !pathname.isDirectory()
										&& pathname.getName().endsWith(
												".xml");
							}
						}));

		return inputFiles.toArray(new File[] {});
	}

	/** initialize XStream and process annotaions */
	private static XStream createXStream() {
		XStream xStream = new XStream(new DomDriver());
		xStream.processAnnotations(MultiLanguageClass.class);
		xStream.processAnnotations(MultiLanguageField.class);
		xStream.processAnnotations(MultiLanguageDefine.class);
		xStream.processAnnotations(MultiLanguageList.class);
		xStream.processAnnotations(MultiLanguageFieldBase.class);
		xStream.processAnnotations(MultiLanguageDerivedClass.class);
		return xStream;
	}

	/** create type descirptors for supported primitive types */
	private static HashMap<String, FieldTypeDescriptor> createTypeDescriptorsForPrimitiveTypes() {
		HashMap<String, FieldTypeDescriptor> typeDescriptors = new HashMap<String, FieldTypeDescriptor>();
		typeDescriptors.put("int", new FieldTypeDescriptor("int", "int", "int",
				"%d", "nextInt", "Integer", "0", null));
		typeDescriptors.put("long", new FieldTypeDescriptor("long", "long",
				"long", "%ld", "nextLong", "Long", "0", null));
		typeDescriptors.put("bool", new FieldTypeDescriptor("bool", "bool",
				"boolean", "%d", "nextInt", "Boolean", "0", null));
		typeDescriptors
				.put("double", new FieldTypeDescriptor("double",
						"double", "double", "%lf", "nextDouble", "Double",
						"0.0", null));

		typeDescriptors.put("string", new FieldTypeDescriptor("string",
				"std::string", "String", "%c", "nextLine", "String", "\"\"",
				"\"\""));

		typeDescriptors.put("ulong", new FieldTypeDescriptor("ulong",
				"unsigned long", "BigInteger", "%lu", "nextBigInteger",
				"BigInteger", "0", "BigInteger.ZERO"));

		return typeDescriptors;
	}
}
