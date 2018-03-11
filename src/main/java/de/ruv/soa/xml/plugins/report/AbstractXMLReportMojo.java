/**
 * 
 */
package de.ruv.soa.xml.plugins.report;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.maven.model.ReportPlugin;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.shared.utils.PathTool;
import org.apache.maven.shared.utils.StringUtils;
import org.apache.maven.shared.utils.io.DirectoryScanner;

import static java.util.Collections.addAll;
import static org.apache.maven.shared.utils.StringUtils.isEmpty;
import static org.apache.maven.shared.utils.StringUtils.isNotEmpty;

/**
 * Abstract base class for reporting results using xml
 * 
 * @author xv881zo
 *
 */
public abstract class AbstractXMLReportMojo extends AbstractMavenReport {

	private static final String INCLUDES = "*.xml";

	private static final String EXCLUDES = "*.txt, testng-failed.xml, testng-failures.xml, testng-results.xml, failsafe-summary*.xml";

	/**
	 * If set to false, only failures are shown.
	 */
	@Parameter(defaultValue = "true", required = true, property = "showSuccess")
	private boolean showSuccess;

	/**
	 * Directories containing the XML Report files that will be parsed and
	 * rendered to HTML format.
	 */
	@Parameter
	private File[] reportsDirectories;

	/**
	 * Location of the Xrefs to link.
	 */
	@Parameter(defaultValue = "${project.reporting.outputDirectory}/xref-test")
	private File xrefLocation;

	/**
	 * Whether to link the XRef if found.
	 */
	@Parameter(defaultValue = "true", property = "linkXRef")
	private boolean linkXRef;
	/**
	 * The projects in the reactor for aggregation report.
	 */
	@Parameter(defaultValue = "${reactorProjects}", readonly = true)
	private List<MavenProject> reactorProjects;
	/**
	 * Whether to build an aggregated report at the root, or build individual
	 * reports.
	 */
	@Parameter(defaultValue = "false", property = "aggregate")
	private boolean aggregate;

	private List<File> resolvedReportsDirectories;

	/**
	 * Whether the report should be generated or not.
	 *
	 * @return {@code true} if and only if the report should be generated.
	 * 
	 */
	protected boolean isSkipped() {
		return false;
	}

	/**
	 * Whether the report should be generated when there are no test results.
	 *
	 * @return {@code true} if and only if the report should be generated when
	 *         there are no result files at all.
	 * 
	 */
	protected boolean isGeneratedWhenNoResults() {
		return false;
	}

	public void executeReport(Locale locale) throws MavenReportException {
		if (!hasReportDirectories()) {
			return;
		}

		//new XMLReportGenerator(getReportsDirectories(), locale, showSuccess, determineXrefLocation(), getLog(),
		//		isNotEmpty(getTitle()) ? getTitle() : null).doGenerateReport(getBundle(locale), getSink());
	}

	private boolean hasReportDirectories() {
		if (isSkipped()) {
			return false;
		}

		final List<File> reportsDirectories = getReportsDirectories();

		if (reportsDirectories == null) {
			return false;
		}

		if (!isGeneratedWhenNoResults()) {
			boolean atLeastOneDirectoryExists = false;
			for (Iterator<File> i = reportsDirectories.iterator(); i.hasNext() && !atLeastOneDirectoryExists;) {
				atLeastOneDirectoryExists = hasReportFiles(i.next());
			}
			if (!atLeastOneDirectoryExists) {
				return false;
			}
		}
		return true;
	}

	private List<File> getReportsDirectories() {
		if (resolvedReportsDirectories != null) {
			return resolvedReportsDirectories;
		}

		resolvedReportsDirectories = new ArrayList<File>();

		if (this.reportsDirectories != null) {
			addAll(resolvedReportsDirectories, this.reportsDirectories);
		}

		if (aggregate) {
			if (!project.isExecutionRoot()) {
				return null;
			}
			if (this.reportsDirectories == null) {
				for (MavenProject mavenProject : getProjectsWithoutRoot()) {
					resolvedReportsDirectories.add(getXMLReportsDirectory(mavenProject));
				}
			} else {
				// Multiple report directories are configured.
				// Let's see if those directories exist in each sub-module to
				// fix SUREFIRE-570
				String parentBaseDir = getProject().getBasedir().getAbsolutePath();
				for (MavenProject subProject : getProjectsWithoutRoot()) {
					String moduleBaseDir = subProject.getBasedir().getAbsolutePath();
					for (File reportsDirectory1 : this.reportsDirectories) {
						String reportDir = reportsDirectory1.getPath();
						if (reportDir.startsWith(parentBaseDir)) {
							reportDir = reportDir.substring(parentBaseDir.length());
						}
						File reportsDirectory = new File(moduleBaseDir, reportDir);
						if (reportsDirectory.exists() && reportsDirectory.isDirectory()) {
							getLog().debug("Adding report dir : " + moduleBaseDir + reportDir);
							resolvedReportsDirectories.add(reportsDirectory);
						}
					}
				}
			}
		} else {
			if (resolvedReportsDirectories.isEmpty()) {

				resolvedReportsDirectories.add(getXMLReportsDirectory(project));
			}
		}
		return resolvedReportsDirectories;
	}

	/**
	 * Gets the default surefire reports directory for the specified project.
	 *
	 * @param subProject
	 *            the project to query.
	 * @return the default xml reports directory for the specified project.
	 */
	protected abstract File getXMLReportsDirectory(MavenProject subProject);

	private List<MavenProject> getProjectsWithoutRoot() {
		List<MavenProject> result = new ArrayList<MavenProject>();
		for (MavenProject subProject : reactorProjects) {
			if (!project.equals(subProject)) {
				result.add(subProject);
			}
		}
		return result;

	}

	private String determineXrefLocation() {
		String location = null;

		if (linkXRef) {
			String relativePath = PathTool.getRelativePath(getOutputDirectory(), xrefLocation.getAbsolutePath());
			if (isEmpty(relativePath)) {
				relativePath = ".";
			}
			relativePath = relativePath + "/" + xrefLocation.getName();
			if (xrefLocation.exists()) {
				// XRef was already generated by manual execution of a lifecycle
				// binding
				location = relativePath;
			} else {
				// Not yet generated - check if the report is on its way
				for (Object o : project.getReportPlugins()) {
					ReportPlugin report = (ReportPlugin) o;

					String artifactId = report.getArtifactId();
					if ("maven-jxr-plugin".equals(artifactId) || "jxr-maven-plugin".equals(artifactId)) {
						location = relativePath;
					}
				}
			}

			if (location == null) {
				getLog().warn("Unable to locate Test Source XRef to link to - DISABLED");
			}
		}
		return location;
	}

	/**
	 * Returns {@code true} if the specified directory contains at least one
	 * report file.
	 *
	 * @param directory
	 *            the directory
	 * @return {@code true} if the specified directory contains at least one
	 *         report file.
	 */
	public static boolean hasReportFiles(File directory) {
		return directory != null && directory.isDirectory()
				&& getIncludedFiles(directory, INCLUDES, EXCLUDES).length != 0;
	}

	private static String[] getIncludedFiles(File directory, String includes, String excludes) {
		DirectoryScanner scanner = new DirectoryScanner();

		scanner.setBasedir(directory);

		scanner.setIncludes(StringUtils.split(includes, ","));

		scanner.setExcludes(StringUtils.split(excludes, ","));

		scanner.scan();

		return scanner.getIncludedFiles();
	}

	public abstract void setTitle(String title);

	public abstract String getTitle();

	public abstract void setDescription(String description);

	public abstract String getDescription();
}
