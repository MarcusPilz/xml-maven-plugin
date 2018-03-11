package de.ruv.soa.xml;

import java.io.Serializable;

public interface Message extends Serializable {
	
	/**
	 * Gets the format portion of the Message
	 * @return 
	 */
	public String getFormat();
	
	/**
	 * Gets the Message formatted as String
	 * @return
	 */
	public String getFormattedMessage();
	
	/**
	 * Gets parameter values, if any.
	 * @return
	 */
	public Object[] getParameter();
			
}
