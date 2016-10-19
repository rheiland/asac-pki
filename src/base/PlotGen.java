package base;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

/**
 * Start this up if you want to use the automated plot generator.
 * The data must already exist before you run the script here.
 * The data should be called latest.tsv and must be in the working directory.
 * @author yechen
 *
 */
public class PlotGen {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Scanner keyboard = new Scanner(java.lang.System.in);
		java.lang.System.out.println("Input Driver Class: ");
		String className = keyboard.nextLine().trim();
		try {
			Class driverC = Class.forName(className);
			Driver driver = (Driver) driverC.newInstance();
			driver.generateSimulation();
			String res = driver.generatePlotter();
			java.lang.System.out.println(res);

			//java.lang.System.out.println("Do you want to save the script to a file and execute it immediately? (Y/N)?");
			java.lang.System.out.println("Do you want to save the script to a file? (Y/N)?");
			String ans = keyboard.nextLine().trim();
			if(ans.toLowerCase().startsWith("y")) {
				java.lang.System.out.println("Filename: ");
				String filename = keyboard.nextLine().trim();
				if(filename.length() == 0) {
					filename = "chartify.py";
				}
				File file = new File(filename);
				FileWriter writer = new FileWriter(file);
				writer.append(res);
				writer.close();
				//java.lang.System.out.println("Executing Plotting Script...");
				//Process process = new ProcessBuilder("py", filename).start();
				//java.lang.System.out.println("Waiting for script to terminate...");
				//int code = process.waitFor();
				//java.lang.System.out.println("Script terminated with exit code" + code);
			}

		} catch(Exception e) {
			e.printStackTrace();
		}

	}

}
