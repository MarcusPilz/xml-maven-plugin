package de.ruv.soa.xml;

import java.io.File;
import java.nio.file.Path;
import java.util.Stack;

import org.jdom2.Element;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SuccessfulReport;


public class ValidatorMessage {
	private String id = null;

	private File file;

	private String location = null;

	private String severity = null;

	private String text;
	/*
	 * private int severity; private String shortText; private String text;
	 * private Path file; private String soaTypeName; private String
	 * soaTypeLabel; private Integer lineNumber = null; private Integer
	 * charNumber = null;
	 * 
	 * 
	 * private Stack<String> elementHierarchy; private String elementLocation =
	 * null;
	 * 
	 */

	/**
	 * Default Constructor
	 */
	public ValidatorMessage() {

	}

	/**
	 * Default Constructor
	 */
	public ValidatorMessage(String text) {
		this.text = text;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return the severity
	 */
	public String getSeverity() {
		return severity;
	}

	/**
	 * @param severity
	 *            the severity to set
	 */
	public void setSeverity(String severity) {
		this.severity = severity;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * 
	 * @param fa
	 */
	public void setFailedAssert(FailedAssert fa) {
		this.setId(fa.getId());
		this.setSeverity(fa.getRole());
		this.setLocation(fa.getLocation());
		this.setText(fa.getText());
	}

	/**
	 * 
	 * @param sr
	 */
	public void setSuccessfullReport(SuccessfulReport sr) {
		this.setId(sr.getFlag());
		this.setSeverity(sr.getRole());
		this.setLocation(sr.getLocation());
		this.setText(sr.getText());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getId());
		sb.append(",");
		sb.append(this.getFile().getAbsolutePath());
		sb.append(",");
		sb.append(this.getLocation());
		sb.append(",");
		sb.append(this.getText());
		return sb.toString();
	}

	/**
	 * 
	 * @return the XML representation of the message
	 */
	public Element getElement() {
		Element e = new Element("message");
		e.addContent(new Element("id").setText(this.getId()));
		e.addContent(new Element("severity").setText(this.getSeverity()));
		e.addContent(new Element("file").setText(this.getFile().getAbsolutePath()));
		e.addContent(new Element("text").setText(this.getText()));
		e.addContent(new Element("location").setText(this.getLocation()));
		return e;
	}
}
