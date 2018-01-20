package com.wline.documentation.ref;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PageLink {

	private String reference;

	private String description;

	private String target;

	private String anchor;

	public PageLink(String reference, String description, String target, String anchor) {
		this.reference = reference;
		this.description = description;
		this.target = target;
		this.anchor = anchor;
	}

	public String getDescription() {
		return description;
	}

	public String getAnchor() {
		return anchor;
	}

	public String getTarget() {
		return target;
	}

	public String getReference() {
		return reference;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(reference).append(';');
		sb.append(description).append(';');
		sb.append(target);
		if (anchor!=null)
			sb.append(';').append(anchor);
		return sb.toString();
	}

	public static PageLink parseCsvLine(String line) {
		String[] elements = line.split(";");
		if (elements.length < 3) {
			throw new IllegalArgumentException("Csv has not the correct format");
		} else if (elements.length == 3)
			return new PageLink(elements[0], elements[1], elements[2],null);
		else
			return new PageLink(elements[0], elements[1], elements[2],elements[3]);
	}

	public static Map<String, List<PageLink>> loadCsvFile(File csvDir,File csvFile) throws IOException {
		Map<String, List<PageLink>> result = new HashMap<>();
		Collection<File> files  = csvDir!=null?FileUtils.listFiles(csvDir, new String []{"csv"},false):new ArrayList<>();
		files.add(csvFile);
		for (File file: files) {
			System.out.println(">> loading references from: "+file.getAbsolutePath());
			List<String> lines = FileUtils.readLines(file);
			for (String line : lines) {
				System.out.println("   > "+line);
				PageLink pl = PageLink.parseCsvLine(line);
				if (!result.containsKey(pl.getReference())) {
					result.put(pl.getReference(), new LinkedList<>());
				}
				result.get(pl.getReference()).add(pl);
			}
		}
		return result;
	}





	public static Map<String, List<PageLink>> loadCsvFile(File csvFile) throws IOException {
		return loadCsvFile(null,csvFile);
	}

}