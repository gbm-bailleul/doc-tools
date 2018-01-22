package com.wline.documentation.ref;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageLink {

	private String reference;

	private String description;

	private String target;

	private String anchor;

	private static Logger logger = LoggerFactory.getLogger(PageLink.class);

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

	public static Map<String, List<PageLink>> loadCsvFile(File csvDir) throws IOException {
		Map<String, List<PageLink>> result = new HashMap<>();
		Collection<File> files  = csvDir!=null?FileUtils.listFiles(csvDir, new String []{"csv"},false):new ArrayList<>();
		for (File file: files) {
			logger.info("Loading references from: "+file.getAbsolutePath());
			List<String> lines = FileUtils.readLines(file, "ISO-8859-1");
			for (String line : lines) {
				PageLink pl = PageLink.parseCsvLine(line);
				if (!result.containsKey(pl.getReference())) {
					result.put(pl.getReference(), new LinkedList<>());
				}
				result.get(pl.getReference()).add(pl);
			}
		}
		return result;
	}

}