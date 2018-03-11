package de.ruv.soa.xml;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

import org.jdom2.Element;

public class Messages implements Serializable {
	
	private static final long serialVersionUID = 6957923301392440240L;
	private LinkedList<ValidatorMessage> results = null; 
	
	public Messages() {
		super();	
		setResults(new LinkedList<ValidatorMessage>());
	}

	/**
	 * @return the results
	 */
	public LinkedList<ValidatorMessage> getResults() {
		return results;
	}

	/**
	 * @param results the results to set
	 */
	public void setResults(LinkedList<ValidatorMessage> results) {
		this.results = results;
	}
	
	/**
	 * Appends a @see ValidatorMessage to the end of this list.
	 * @param vm the ValidatorMessage to add
	 * @return true (as specified by Collection.add(E))
	 */	
	public boolean addMessage(ValidatorMessage vm) {
		return results.add(vm);
	}
	
	/**
	 * Returns the number of ValidatorMessages in this list.
	 * @return - the number of elements in this list
	 */
	public int size() {
		return results.size();
	}

	/**
	 * 
	 * @return
	 */
	public Iterator iterator() {		
		return results.iterator();
	}

	public Element getElement() {
		Element e = new Element("messages");
		for (Iterator<ValidatorMessage> iterator = results.iterator(); iterator.hasNext(); ) {
        	e.addContent(iterator.next().getElement());
        }
		return e;
	}
	
}
