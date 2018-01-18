package com.wline.documentation.ref;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.extension.Treeprocessor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ReferenceTreeMacro
		extends Treeprocessor {


	private Map<String, List<PageLink>> links;

	public ReferenceTreeMacro(Map<String, Object> config) throws RuntimeException {
		super(config);

		if (config.get("csv")==null)
			throw new RuntimeException("Missing mandatory property 'csv");
		File csvFile = (File)config.get("csv");
		if (!csvFile.exists())
			throw new RuntimeException("Expected csv file doest not exists");

		try {
			this.links = PageLink.loadCsvFile(csvFile);
		} catch (IOException e) {
			System.err.println("Problem when reading csv file : " + e.getMessage());
		}
	}

	@Override
	public Document process(Document document) {
		for (AbstractBlock block: document.getBlocks()) {
			process(document, block);
		}
		return document;
	}

	protected void process (Document document, AbstractBlock block) {
		if (block instanceof Section) {
			Section section = (Section)block;
			System.out.println("In section "+section.getTitle()+" / "+section.getLevel());
			if (section.getAttributes().containsKey("reference")) {
				String target = section.getAttr("reference").toString();
				System.out.println("  Found reference : "+target);
				List<PageLink> anchors = links.get(target);
				if (anchors != null && anchors.size()>0) {
					for (PageLink anchor : anchors) {
						Map<Object, Object> options = new HashMap<>();
						options.put("type", ":link");
						options.put("target", anchor.getTarget());
						String s = createInline(section,"anchor",Arrays.asList(anchor.getTarget()),section.getAttributes(),options).convert();
						Block nb = createBlock(
								document,
								"listing",
								Arrays.asList(anchor.getDescription(),s),
								section.getAttributes(),
								options);
						section.getBlocks().add(0,nb);
					}
				}
			}
			for (AbstractBlock sb: section.getBlocks()) {
				process(document, sb);
			}
		}
	}




}



