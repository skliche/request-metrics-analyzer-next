package de.ibm.issw.requestmetrics.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FileDetector extends SimpleFileVisitor<Path> {
	private List<File> allFiles = new ArrayList<File>();
	
	public FileDetector(String path) throws Exception {
		Files.walkFileTree(Paths.get(path), this);
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
		allFiles.add(file.toFile());
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	public List<File> getAllFiles() {
		return allFiles;
	}
}
