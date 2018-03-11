package de.ruv.soa.xml.plugins.validator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.transform.stream.StreamSource;

import org.apache.maven.execution.MavenSession;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.oclc.purl.dsdl.svrl.SuccessfulReport;

import com.helger.schematron.ISchematronResource;
import com.helger.schematron.pure.SchematronResourcePure;
import com.helger.schematron.svrl.SVRLMarshaller;

import de.ruv.soa.xml.FindFiles;
import de.ruv.soa.xml.Messages;
import de.ruv.soa.xml.ValidatorMessage;
import net.sf.saxon.trans.XPathException;

/**
 * 
 */

@Mojo( name = "validate", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, threadSafe = true, requiresDependencyResolution = ResolutionScope.TEST )
public class XMLValidationMojo   extends AbstractMojo
{
	
	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession session;
	
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;
	
	/**
	 * The Schematron File. 
	 */
	@Parameter(property = "schematronFile", required = true)
	private File schematronFile;
	
	/**
	 * The directory where the xml,xsd,,wsdl files reside taht are expected to match
	 * the schematron rules
	 */
	@Parameter(property="xmlDirectory")
	private File xmlDirectory;
	
	/**
	 * The SVRL path to write to. The filenames are based on 
	 * the source filenames
	 */
	@Parameter(defaultValue = "${project.build.directory}", property="svrlDirectory", required = true)
	private File svrlDirectory;	
	
	/**
	 * The SVRL path to write to. The filenames are based on 
	 * the source filenames
	 */
	@Parameter(defaultValue = "${project.build.directory}/xml-validation-report", property="reportDirectory", required = true)
	private File reportDirectory;	
	
	/**
	 * Define the phase to be used for schematron validation
	 * Actual not supported.
	 */
	@Parameter ( property ="phase" )
	private String phase;
	
	@Parameter
	private String languageCode;
	
    /**
     * Target Directory
     */
    @Parameter( defaultValue = "${project.build.directory}", property = "outputDirectory", required = true )
    private File outputDirectory;
    
    @Parameter( property = "skip")
    private boolean skip; 
    
    @Parameter 
    private Set<String> xmlIncludes = new HashSet<String>();
    
    @Parameter
    private Set<String> xmlExcludes = new HashSet<String>();
    
    
    /**
     * 
     * @return
     */
    public File getSchematronFile() {
		return schematronFile;
	}
    
    /**
     * 
     * @param schematronFile
     */
	public void setSchematronFile(final File aFile) {
		this.schematronFile = aFile;
		
		if (!schematronFile.isAbsolute()) {
			schematronFile = new File(project.getBasedir(), aFile.getPath());
		}
		if (getLog().isDebugEnabled()) {
			getLog().debug("Using Scheamtron file '" + schematronFile + "'");
		}
	}

	

	public File getXmlDirectory() {
		return xmlDirectory;
	}

	public void setXmlDirectory(final File aXmlDirectory) {
		this.xmlDirectory = aXmlDirectory;
		if (!xmlDirectory.isAbsolute()) {
			xmlDirectory = new File(project.getBasedir(), aXmlDirectory.getPath());
		}
		if (getLog().isDebugEnabled()) {
			getLog().debug("Searching files in the directory '" + xmlDirectory + "'");
		}
	}
	
	public void setSvrlDirectory(File aDirectory) {
		this.svrlDirectory = aDirectory;
		if (!svrlDirectory.isAbsolute()) {
			svrlDirectory = new File(project.getBasedir(), aDirectory.getPath());
		}
		if (getLog().isDebugEnabled()) {
			getLog().debug("Writing svrl files to directory '" + svrlDirectory + "'");
		}
	}

	protected Collection<Path> getSources(  ) throws IOException {
		Path path = Paths.get(xmlDirectory.toURI());
		FindFiles ff = new FindFiles("");		
		if (xmlIncludes.isEmpty() && xmlExcludes.isEmpty()) {			
			ff = new FindFiles("*.xsd");	
		}
		Files.walkFileTree(path,ff);		
		return ff.getFiles();	
	}
	
	

	/**
     * 
     */
    public void execute() throws MojoExecutionException {
    	//skipped nothing to do
    	if (skip) {
    		getLog().info( "Not validate xml sources" );
    		return;
    	}
    	if (this.schematronFile == null) {
    		throw new MojoExecutionException("No schematron file specified!");
    	}
    	if (this.schematronFile.exists() && !schematronFile.isFile()) {
    		throw new MojoExecutionException("The specified schematron file '" + schematronFile + "' is not a file!");
    	}
    	
    	if (xmlDirectory == null) {
    		throw new MojoExecutionException("No XML Directory is specified!");
    	}
    	
    	if (xmlDirectory != null) {
    		if (xmlDirectory.exists() && !xmlDirectory.isDirectory()) {
    			throw new MojoExecutionException("The specified XML Directory '" + xmlDirectory + "' is not a directory!");
    		}
    	}
    	
    	Collection<Path> filetoCheck;
		try {
			filetoCheck = getSources();
		} catch (IOException e) {
			throw new MojoExecutionException(e.toString());
		}
    	getLog().info( "validate " + filetoCheck.size() + " xsd sources" );
    	Messages results = new Messages();
    	for (Path p : filetoCheck) {	
    		try {
    			validate(this.schematronFile, p.toFile(), results);
    		} catch (Exception e) {
    			throw new MojoExecutionException(e.toString());
    		}
		}
    	if (results.size() > 0) {
    		if (!reportDirectory.exists() && !reportDirectory.mkdir()) {
    			throw new MojoExecutionException("Failed to create '" + reportDirectory + "'. Report could not generated!");
    		}
    		File summary = null;
    		try {
    			summary = new File(reportDirectory, "validation-report.xml");
        		Element report = new Element("validation-report");
        		report.addContent(results.getElement());
        		Document document = new Document(report);  
        		XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());	
    			FileWriter xmlOut = new FileWriter(summary);
				xmlOutput.output(document, xmlOut);
				xmlOut.close();
				getLog().info("Report " + summary + " successful generated!");
			} catch (IOException e) {				
				throw new MojoExecutionException("Report '" + summary.getPath() +"' could not generated!",e);
			}
    	}
    }
    
    /**
	 * runChecker - run the Namespace Checker
	 * 
	 * @param aSchematronFile
	 * @param aFile
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws XPathException 
	 */
	public void validate(File aSchematronFile,final File aFile, final Messages aHandler)
			throws IllegalArgumentException {
		
		if (!aFile.exists() || !aFile.canRead()) {
			throw new IllegalArgumentException(aFile.getAbsolutePath() + " does not exist or is readable!");
		}
		
		final ISchematronResource aResPure = SchematronResourcePure					
					.fromFile(aSchematronFile);
		if (!aResPure.isValidSchematron())
			throw new IllegalArgumentException("Invalid Schematron!");
		
		/* this block is for each file */
		try {
			SchematronOutputType ob = null;
			ob = aResPure.applySchematronValidationToSVRL(new StreamSource(aFile));
			
			if (svrlDirectory != null) {
				
				final File aSVRLFile = new File(svrlDirectory, xmlDirectory.getName().toString() + aFile.getAbsolutePath().replace(xmlDirectory.getAbsolutePath(),"") + ".svrl");
				if (!aSVRLFile.getParentFile().exists() && !aSVRLFile.getParentFile().mkdirs()) {
					getLog().error("Failed to create parent directory of '" + aSVRLFile.getAbsolutePath() + "'");
				}
				if ( new SVRLMarshaller().write(ob,aSVRLFile).isSuccess() ) {
					getLog().info("Successfully saved svrl file '" + aSVRLFile.getPath() + "'");
				} else {
					getLog().error("Error saving svrl file '" + aSVRLFile.getPath() + "'");
				}
			}

			getLog().debug("ob" + ob.toString());

			// check if there are nay fired rules or asserts
			if (ob.hasActivePatternAndFiredRuleAndFailedAssertEntries()) {
				List<Object> results = ob.getActivePatternAndFiredRuleAndFailedAssert();
				for (Object obj : results) {
					if (obj instanceof FailedAssert) {
						FailedAssert fa = (FailedAssert) obj;
						ValidatorMessage vm = new ValidatorMessage();
						vm.setId(fa.getId());
						vm.setSeverity(fa.getRole());
						vm.setFile(aFile);				
						vm.setFailedAssert(fa);
						aHandler.addMessage(vm);
						//getLog().info("FailedAssert " + fa.toString());
					} else if (obj instanceof SuccessfulReport) {
						SuccessfulReport sr = (SuccessfulReport) obj;
						ValidatorMessage vm = new ValidatorMessage();
						vm.setFile(aFile);	
						vm.setSuccessfullReport(sr);
						aHandler.addMessage(vm);
						//getLog().info("SuccessfulReport " + sr.toString());
					}
				}				
			}

		} catch (Exception e) {
			getLog().error("Unexpected Error occurs ",e);			
		}
	}
}
