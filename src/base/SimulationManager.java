package base;
import java.io.*;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

/**
 * Deliver jobs to workers. Run this on the computer that collects the results.
 * @author yechen
 *
 */
public class SimulationManager {
	public static String OUTPUT_FILE = "latest.tsv";
	private String ret = "";
	private boolean wroteHeader = false;
	private ConcurrentLinkedQueue<Params> toRun = new ConcurrentLinkedQueue<Params>();
	private ConcurrentLinkedQueue<SimManagerThread> running = new ConcurrentLinkedQueue<SimManagerThread>();

	private synchronized void appendResult(String s) {
		ret += s;
//		java.lang.System.out.print("(rwh)SimulationManager.appendResult:  " + s);
	}

	/**
	 * @param args
	 * args[0]: config file.
	 * Config file format:
	 * (No extra empty line is acceptable)
	 * Driver Class (full path)
	 * Worker1
	 * Worker2
	 * ...
	 */
	public static void main(String[] args) {
		try {
			try {
				String configFile = args[0];
			} catch(ArrayIndexOutOfBoundsException e) {
				SimLogger.log(Level.SEVERE, "Missing cmd line argument [ConfigFile]");
			}
			BufferedReader reader = new BufferedReader(new FileReader(args[0]));
			String driverClass = reader.readLine().trim();
			OUTPUT_FILE = reader.readLine().trim();

			ArrayList<InetSocketAddress> addrs = new ArrayList<InetSocketAddress>();
			String line = null;
			while((line = reader.readLine()) != null) {
				String[] addr = line.split(":");
				addrs.add(new InetSocketAddress(addr[0], Integer.parseInt(addr[1])));
			}
			reader.close();

			new SimulationManager(driverClass, addrs);
		} catch(FileNotFoundException e) {
			SimLogger.log(Level.SEVERE, "File Not Found: " + args[0]);
			e.printStackTrace();
		} catch(IOException e) {
			SimLogger.log(Level.SEVERE, "I/O Exception while reading config file.");
			e.printStackTrace();
		}
	}

	public SimulationManager(String driverClass, ArrayList<InetSocketAddress> addrs) {
		try {
			Class driverC = Class.forName(driverClass);
			Driver driver = (Driver) driverC.newInstance();
			SimLogger.log(Level.INFO, "Driver loaded successfully.");
			int i = 0;
			int addrIndex = 0;
			while(i < driver.getNumRuns()) {
		        java.lang.System.out.println("dbg> SimulationManager:  Creating simulation parameter for run " + i);
				SimLogger.log(Level.FINE, "Creating simulation parameter for run " + i);
				Params param = driver.generateSimulation();
				param.runIndex = i;

				toRun.add(param);
				if(!wroteHeader) {
					Simulation sim = new Simulation(param);
					sim.build();
					appendResult("-Run-" + sim.sep);
					appendResult(sim.headerStr(false));
					appendResult(sim.sep + "-Time-" + "\n");
					wroteHeader = true;
				}
				i++;
			}
			SimLogger.log(Level.INFO, "All simulations' parameters have been created.");

			while(!toRun.isEmpty() || !running.isEmpty()) {
				if(!toRun.isEmpty()) {
					Params param = toRun.remove();
					try {
						InetSocketAddress addr = addrs.get(addrIndex);
						Socket s = new Socket(addr.getAddress(), addr.getPort());
						SimManagerThread thread = new SimManagerThread(s, param);
						thread.start();
						running.add(thread);

					} catch(Exception e) {
						SimLogger.log(Level.FINE, "Cannot connect to " + addrs.get(addrIndex) + ", possibily doing useful work.");
						toRun.add(param);
						try {
							Thread.sleep(5000);
						} catch(InterruptedException e1) {
							SimLogger.log(Level.WARNING, "Sleep failed");
						}
					}
					addrIndex = (addrIndex + 1) % addrs.size();
				} else {
					try {
						Thread.sleep(5000);
					} catch(InterruptedException e1) {
						SimLogger.log(Level.WARNING, "Sleep failed");
					}
				}
			}
			SimLogger.log(Level.INFO, "Simulations finished. Writing output to " + OUTPUT_FILE);

			File file = new File(OUTPUT_FILE);
			FileWriter writer = new FileWriter(file);
			writer.append(ret);
			writer.close();

			SimLogger.log(Level.INFO, "Output file created. Terminating simulation manager...");
		} catch(ClassNotFoundException e) {
			SimLogger.log(Level.SEVERE, "Cannot start driver: " + e.getMessage());
			e.printStackTrace();
		} catch(InstantiationException e) {
			SimLogger.log(Level.SEVERE, "Cannot start driver: " + e.getMessage());
			e.printStackTrace();
		} catch(IllegalAccessException e) {
			SimLogger.log(Level.SEVERE, "Cannot start driver: " + e.getMessage());
			e.printStackTrace();
		} catch(IOException e) {
			SimLogger.log(Level.SEVERE, "Cannot write to file: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * This thread transmits parameter, and get result.
	 * Should any of these fail, this thread is also responsible for putting
	 * the job back into the job queue.
	 * @author yechen
	 *
	 */
	private class SimManagerThread extends Thread {
		private Params param;
		private Socket s;
		private ObjectOutputStream oos;
		private DataInputStream dais;
		private DataOutputStream daos;

		public SimManagerThread(Socket s, Params param) {
			this.s = s;
			try {
				s.setKeepAlive(true);
			} catch(SocketException e1) {
				e1.printStackTrace();
			}
			this.param = param;
			try {
				oos = new ObjectOutputStream(s.getOutputStream());
				dais = new DataInputStream(s.getInputStream());
				daos = new DataOutputStream(s.getOutputStream());
			} catch(IOException e) {
				toRun.add(param);
				SimLogger.log(Level.WARNING, "Cannot create streams to worker. " + e.getMessage());
			}
		}
		public void run() {
			try {
				oos.writeObject(param);
				daos.writeInt(param.runIndex);
				daos.flush();
				String result = dais.readUTF();
				appendResult(result + "\n");
				s.close();
			} catch(IOException e) {
				toRun.add(param);
				SimLogger.log(Level.WARNING, "Client shutdown prematurely. Work is being rescheduled to other workers. " + e.getMessage());
			}
			running.remove(this);
		}
	}

}
