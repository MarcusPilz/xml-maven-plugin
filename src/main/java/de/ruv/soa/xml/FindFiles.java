package de.ruv.soa.xml;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;

public class FindFiles extends SimpleFileVisitor<Path> {
	private PathMatcher matcher = null;
	private Collection<Path> files = new ArrayList<Path>();
	private int numMatches = 0;

	public FindFiles(String pattern) {
		System.err.println("search.. " + pattern + " files.");
		matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
	}
	
	/**
	 * 
	 * @return
	 */
	public Collection<Path> getFiles() {
		System.err.println("return " + numMatches + " files.");
		return files;
	}

	/**
	 * 
	 * @return
	 */
	public int getNumMatches() {
		return numMatches;
	}

	private void find(Path file) {		
		Path name = file.getFileName();
		if (name != null && matcher.matches(name)) {
			numMatches++;			
			files.add(file);
		}
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		find(file);
		return java.nio.file.FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		find(dir);
		return java.nio.file.FileVisitResult.CONTINUE;
	}
	
	@Override
    public FileVisitResult visitFileFailed(Path file,
            IOException exc) throws IOException {
        System.err.println(exc);
        return super.visitFileFailed(file, exc);
    }


}
