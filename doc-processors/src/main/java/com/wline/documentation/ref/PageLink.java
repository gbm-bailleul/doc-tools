package com.wline.documentation.ref;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
		return this.reference + ";" + this.description + ";" + this.target + ";" + this.anchor  ;
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

	public static Map<String, List<PageLink>> loadCsvFile(File csvFile) throws IOException {
		Map<String, List<PageLink>> result = new HashMap<>();
		List<String> lines = FileUtils.readLines(csvFile);
		for (String line : lines) {
			PageLink pl = PageLink.parseCsvLine(line);
			if (!result.containsKey(pl.getReference())) {
				result.put(pl.getReference(), new LinkedList<>());
			}
			result.get(pl.getReference()).add(pl);
		}
		return result;
	}

}