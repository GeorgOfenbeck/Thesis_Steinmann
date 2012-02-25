package ch.ethz.ruediste.roofline.sharedEntityGenerator;

import java.io.File;

public class Utility {

	/**
	 * deletes a directory recursively
	 */
	public static void deleteDirectory(File directory) {
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				deleteDirectory(file);
			}
			else {
				//System.out.println("deleting file " + file.getAbsolutePath());
				file.delete();
			}
		}
		//System.out.println("deleting directory " + directory.getAbsolutePath());
		directory.delete();
	}
}
