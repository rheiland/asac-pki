package base;
import java.io.*;

import java.net.*;
import java.util.Date;

import base.SimLogger.Log;

/**
 * Objects of this class receives worker from the manager and simulates the execution.
 * Then return the result to the manager.
 * @author yechen
 *
 */
public class SimulationWorker {
	public static final int SERVER_PORT = 20000;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new SimulationWorker();
	}

	public SimulationWorker() {
		ServerSocket ss = null;
		int port = SERVER_PORT;
		while(ss == null && (port < 65535)) {
			try {
				ss = new ServerSocket(port++, 500);
				Log.i("Listening at server port: " + ss.getLocalPort());
				while(true) {
					try {
						Socket s = ss.accept();
						Log.d("New connection accepted: " + s.getRemoteSocketAddress());
						s.setKeepAlive(true);
						ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
						DataInputStream dais = new DataInputStream(s.getInputStream());
						DataOutputStream daos = new DataOutputStream(s.getOutputStream());

						Params param = (Params) ois.readObject();
						Simulation sim = new Simulation(param);
						sim.build();
						int runIndex = dais.readInt();
						long preTime = new Date().getTime();
						Log.i("Running Simulation #" + runIndex);

						String res = "" + runIndex + sim.sep;
						try {
							sim.run();
						} catch(Exception e) {
							e.printStackTrace();
						}
						double seconds = elapsed(preTime);
						res += sim.results(false);
						res += sim.sep + seconds;
						Log.d("Writing result to manager");
						daos.writeUTF(res);
						daos.flush();

						ois.close();
						dais.close();
						daos.close();
						s.close();
						Log.d("Closing socket...");
					} catch(Exception e) {
						Log.e("Remote host disconnected unexpectedly: " + e.getMessage() +
						                             "\nRetry in 10 seconds");
						Thread.sleep(10000);

					}
				}
			} catch(Exception e) {
			}
		}
		Log.e("Cannot start server socket through ports " + SERVER_PORT + "-65535");
	}

	/**
	 * How long it took.
	 * @param preTime the time recorded before execution
	 * @return after execution.
	 */
	public static double elapsed(long preTime) {
		return (new Date().getTime() - preTime) / 1000.0;
	}
}
