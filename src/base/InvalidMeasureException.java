package base;

import java.util.logging.Level;

/**
 * Thrown when a measure cannot be measured by a given Measurable class.
 * @author yechen
 *
 */
public class InvalidMeasureException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 2683767095577763089L;


	public InvalidMeasureException(Property p) {
		super("Bad Property Query: " + p);
		SimLogger.log(Level.SEVERE, "Bad measure query: " + p);
	}
}
