package base;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The drivers are responsible for:
 * <br>
 * 1. Feeding in the initial data (initial state, actor machine, impl classes, and other nifty stuff.)
 * <br>
 * 2. Generate the plotting script.
 * @author yechen
 *
 */
public abstract class Driver {

	/**
	 * How many runs do we execute in total by the manager.
	 */
	private static final int NUM_RUNS = 200;
	public int getNumRuns() {
		return NUM_RUNS;
	}

	public Driver() {

	}

	/**
	 * Does the heavy lifting of the plotting script generation.
	 * @param impls the set of implementation to be plotted.
	 * @param measures the set of measures to be plotted.
	 * @return the final script.
	 */
	protected static String getPlotter(String[] impls, List<Measure> measures) {
		String ret = "#!/usr/bin/env python\n" +
			"\n" +
			"import os\n" +
			"import errno\n" +
			"import matplotlib as mpl\n" +
			"mpl.use('pdf')\n" +
			"import matplotlib.pyplot as p\n" +
			"import csv\n" +
			"\n" +
			"FIGDIR = 'fig'\n" +
			"DATA = 'latest'\n" +
			"p.rcParams.update({\n" +
			"\t'figure.figsize': (4.8, 3.6),\n" +
			"\t'figure.autolayout': True,\n" +
			"\t'savefig.format': 'pdf',\n" +
			"\t'legend.numpoints': 1,\n" +
			"\t'legend.loc': 'best',\n" +
			"\t'legend.fontsize': 12,\n" +
			"\t'legend.handlelength': 1,\n" +
			"\t'font.family': 'serif',\n" +
			"\t'text.usetex': True\n" +
			"})\n" +
			"\n" +
			"";

		ret += getStyleHeader(impls);

		ret += "def read(data):\n" +
			"\tdata = open(data)\n" +
			"\tcols = None\n" +
			"\tdatas = {}\n" +
			"\treader = csv.reader(data, delimiter='\\t')\n" +
			"\tfor row in reader:\n" +
			"\t\tif cols == None:\n" +
			"\t\t\tcols = []\n" +
			"\t\t\tfor heading in row:\n" +
			"\t\t\t\tcols.append(heading)\n" +
			"\t\t\t\tdatas[heading] = []\n" +
			"\t\telse:\n" +
			"\t\t\ti = 0\n" +
			"\t\t\tfor shite in row:\n" +
			"\t\t\t\ttry:\n" +
			"\t\t\t\t\tdatas[cols[i]].append(int(shite))\n" +
			"\t\t\t\texcept ValueError:\n" +
			"\t\t\t\t\ttry:\n" +
			"\t\t\t\t\t\tdatas[cols[i]].append(float(shite))\n" +
			"\t\t\t\t\texcept ValueError:\n" +
			"\t\t\t\t\t\tdatas[cols[i]].append(0)\n" +
			"\t\t\t\ti+=1\n" +
			"\treturn datas\n" +
			"\n";

		ret += "def plank(x, y, sc):\n" +
			"\treturn p.plot(x, y, ls='None', c='black', marker=MARKER[sc], mec=MEC[sc], fillstyle='none', ms=5)\n" +
			"\n";


		ret += "def mainplot(d):\n";
		for(int i = 0; i < measures.size(); i++) {
			for(int j = 0; j < measures.size(); j++) {
				String xMeasure = measures.get(i).getMeasureName();
				String yMeasure = measures.get(j).getMeasureName();

				ret += "\tp.figure()\n";

				for(int k = 0; k < impls.length; k++) {
					String impl = impls[k];
					String actualX = impl + "-" + xMeasure;
					String actualY = impl + "-" + yMeasure;

					ret += "\tp" + k + ", = " + "plank(d['" +
					       actualX + "'], d['" + actualY + "'], '" + impl + "')" +
					       "\n";

				}
				ret += "\tp.xlabel('" + measures.get(i).getPrintFriendlyName() + "')\n";
				ret += "\tp.ylabel('" + measures.get(j).getPrintFriendlyName() + "')\n";

				ret += "\tp.legend([";
				for(int k = 0; k < impls.length; k++) {
					ret += "p" + k;
					if(k != impls.length - 1) {
						ret += ", ";
					}
				}
				ret += "], [";
				for(int k = 0; k < impls.length; k++) {
					ret += "SCHEMES['" + impls[k] + "']";
					if(k != impls.length - 1) {
						ret += ", ";
					}
				}
				ret += "])\n";

				ret += "\tp.savefig(FIGDIR + " + "'/" + xMeasure + "-vs-" + yMeasure + ".pdf')\n";
				ret += "\tp.close()\n";
				ret += "\n";
			}
		}

		ret += "def main():\n" +
			"\ttry:\n" +
			"\t\tos.makedirs(FIGDIR)\n" +
			"\texcept OSError as exception:\n" +
			"\t\tif exception.errno != errno.EEXIST:\n" +
			"\t\t\traise\n" +
			"\td = read(DATA + '.tsv')\n" +
			"\tmainplot(d)\n" +
			"main()\n";

		return ret;
	}

	/**
	 * The portion in which the style of the plot is decided.
	 * @param impls the set of implementations to be plotted
	 * @return the style header in the script.
	 */
	protected static String getStyleHeader(String[] impls) {
		try {
			ArrayList<String> colors = getColors();
			ArrayList<String> markers = getMarkers();
			String ret = "MARKER = {\n";

			for(int i = 0; i < impls.length; i++) {
				String impl = impls[i];
				ret += "\t'" + impl + "': '" + markers.get(i % markers.size()) + "'";
				if(i != impls.length - 1) {
					ret += ",";
				}
				ret += "\n";
			}
			ret += "}\n";

			ret += "MEC = {\n";
			for(int i = 0; i < impls.length; i++) {
				String impl = impls[i];
				ret += "\t'" + impl + "': '#" + colors.get((i % colors.size()) + (i / (colors.size())))+ "'";
				if(i != impls.length - 1) {
					ret += ",";
				}
				ret += "\n";
			}
			ret += "}\n";

			ret += "SCHEMES = {\n";
			for(int i = 0; i < impls.length; i++) {
				String impl = impls[i];

				try {
					ret += "\t'" + impl + "': '" + (String)Class.forName(impl).getMethod("schemeName").invoke(null) + "'";
				} catch(Exception e) {
				    throw new RuntimeException(e);
				}
				if(i != impls.length - 1) {
					ret += ",";
				}
				ret += "\n";
			}
			ret += "}\n";

			ret += "\n";
			return ret;


		} catch(IndexOutOfBoundsException e) {
			java.lang.System.err.println("Too many implementations to generate plotting script automatically.");
			return null;
		}
	}

	/**
	 * This keeps tracks of available colors in the plotter.
	 * If you don't like the colors, here's the location to change them.
	 * All of these colors are HEX numbers. You can look it up in goggle or HTML writers.
	 * @return all acceptable colors to be used by plotter.
	 */
	protected static ArrayList<String> getColors() {
		ArrayList<String> colors = new ArrayList<String>();
		colors.add("006666");
		colors.add("000066");
		colors.add("660000");
		colors.add("000000");
		colors.add("111111");
		colors.add("222222");
		return colors;
	}

	/**
	 * This keeps track of all acceptable markers.
	 * If you want to get rid of some markers, here's the location to do it.
	 * Note some markers may not be tested throughly and may crash plotting script.
	 * If it actually crashed, here's probably the location of the crash.
	 * @return a list of acceptable markers.
	 */
	protected static ArrayList<String> getMarkers() {
		ArrayList<String> markers = new ArrayList<String>();
		markers.add("+");
		markers.add("o");
		markers.add("^");
		markers.add(".");
		markers.add("_");
		markers.add("8");
		return markers;
	}

	/**
	 * The subclasses need to use getPlotter to generate plots
	 * But to do that, the subclasses need to give getPlotter the correct parameters.
	 * This method must be overwritten, to call getPlotter with the right parameters.
	 * Most likely the right params are in Params class.
	 * @return whatever getPlotter returns most likely.
	 */
	public abstract String generatePlotter();

	/**
	 * This generates parameters to be used by workers to start simulation.
	 * @return initial parameters.
	 */
	public abstract Params generateSimulation();

	static final double CHURN_MAX = 4.0;
	static final double COIF_MAX = 3.0;
	static final double POSTF_MAX = 3.0;

	/**
	 * Computes the time it took for us to run 1 simulation.
	 * @param preTime the time we recorded before simulation.
	 * @return time we took to run simulation.
	 */
	public static double elapsed(long preTime) {
		return (new Date().getTime() - preTime) / 1000.0;
	}


}
