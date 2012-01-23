package ch.ethz.ruediste.roofline.measurementDriver;

import java.util.*;

import com.google.inject.Singleton;

@Singleton
public class RuntimeMonitor {
	public static class Category {
		public Category(String name) {
			this.name = name;
		}

		public Category(String name, Category parent) {
			this.name = name;
			this.parent = parent;
			parent.children.add(this);
		}

		public List<Category> children = new LinkedList<Category>();
		public Category parent;
		public String name;

		private int count = 0;
		private long start;
		private long categoryTime;
		private int startCount;

		public void enter() {
			if (count == 0) {
				startCount++;
				start = System.currentTimeMillis();
			}
			count++;
		}

		public void leave() {
			count--;
			if (count == 0) {
				categoryTime += System.currentTimeMillis() - start;
			}
		}

		public long getCategoryTime() {
			return categoryTime;
		}

		public long getTotalTime() {
			long result = categoryTime;
			for (Category c : children) {
				result += c.getTotalTime();
			}
			return result;
		}
	}

	public Category rootCategory = new Category("root");
	public Category hashingCategory = new Category("hashing", rootCategory);
	public Category buildCategory = new Category("build", rootCategory);
	public Category buildPreparationCategory = new Category("buildPreparation",
			buildCategory);
	public Category compilationCategory = new Category("compilation",
			buildCategory);
	public Category runMeasurementCategory = new Category("run measurement",
			rootCategory);
	public Category loadMeasurementResultsCategory = new Category(
			"load measurements", rootCategory);
	public Category startupCategory = new Category("startup", rootCategory);

	public void print() {
		System.out.println("Execution Time Spent");
		print(rootCategory, 0);
		System.out.println();
	}

	private void print(Category category, int indent) {
		for (int i = 0; i < indent; i++)
			System.out.print("  ");
		System.out.printf("%s: #%d %.1f", category.name, category.startCount,
				category.getCategoryTime() / 1000.0);

		if (category.children.size() >= 0) {

			System.out.printf(" %.1f\n",
					category.getTotalTime() / 1000.0);

			for (Category c : category.children) {
				print(c, indent + 1);
			}
		}
		else {
			System.out.println();
		}
	}
}
