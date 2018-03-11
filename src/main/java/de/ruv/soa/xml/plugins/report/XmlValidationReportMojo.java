package de.ruv.soa.xml.plugins.report;

import java.io.File;
import java.util.Locale;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

@Mojo(name = "report", inheritByDefault = false)
@Execute(lifecycle = "xml", phase = LifecyclePhase.TEST)
public class XmlValidationReportMojo extends AbstractMavenReport {
	/**
	 * The filename to use for the report.
	 */
	@Parameter(defaultValue = "xml-validation-report", property = "outputName", required = true)
	private String outputName;

	@Override
	public String getDescription(Locale locale) {
		return "XML Maven Report Description";
	}

	@Override
	public String getName(Locale locale) {

		return "XML Maven Report";
	}

	@Override
	public String getOutputName() {
		return "xml-validation-report";
	}

	/**
	 * If set to true the xml validation report generation will be skipped.
	 * 
	 */
	@Parameter(defaultValue = "false", property = "skipXMLValidationReport")
	private boolean skipXMLValidationReport;

	//@Override
	protected File getXMLReportsDirectory(MavenProject subProject) {
		String buildDir = subProject.getBuild().getDirectory();
		return new File(buildDir + "/xml-validation-reports");
	}

	@Override
	protected void executeReport(Locale locale) throws MavenReportException {
		getLog().info("Generating XML Validation Report");
		// direct report generation using Doxia: compare with
		// CustomReportRenderer to see the benefits of using
		// ReportRenderer
		getSink().head();
		getSink().title();
		getSink().text("XML Report Title");
		getSink().title_();
		getSink().head_();

		getSink().body();

		getSink().section1();
		getSink().sectionTitle1();
		getSink().text("section");
		getSink().sectionTitle1_();

		getSink().text("XML Maven Report content.");
		getSink().section1_();

		getSink().body_();

	}

}
