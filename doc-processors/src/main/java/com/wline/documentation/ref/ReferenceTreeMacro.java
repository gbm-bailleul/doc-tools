package com.wline.documentation.ref;

import org.asciidoctor.ast.*;
import org.asciidoctor.extension.Treeprocessor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ReferenceTreeMacro
		extends Treeprocessor {

	private Map<String, List<PageLink>> links;

	private boolean initialized = false;


	public ReferenceTreeMacro(Map<String, Object> config) throws RuntimeException {
		super(config);
	}

	private void initialize (DocumentRuby document) throws RuntimeException{
		if (document.getAttributes().get("csv")==null)
			throw new RuntimeException("Missing mandatory property 'csv'");
		File csvFile = new File((String)document.getAttributes().get("csv"));
		File csvDir = new File((String)document.getAttr("references-dir"));
		try {
			this.links = PageLink.loadCsvFile(csvDir,csvFile);
		} catch (IOException e) {
			throw new RuntimeException("Problem when reading csv file : " + e.getMessage());
		}
		initialized = true;
	}


	@Override
	public Document process(Document document) {
		// init
		if (!initialized) {
			initialize(document);
		}
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
				List<PageLink> links = this.links.get(target);
				System.out.println("    links: "+(links!=null?links.size():0));
				if (links != null && links.size()>0) {
					for (PageLink pageLink : links) {
						Map<Object, Object> options = new HashMap<>();
						options.put("type", ":link");
						String tg = pageLink.getTarget()+
								(pageLink.getAnchor()!=null?("#"+pageLink.getAnchor()):"");

						options.put("target", tg);

						String s = createInline(
								section,
								"anchor",
								Arrays.asList(tg),
								section.getAttributes(),
								options
						).convert();
						Block nb = createBlock(
								document,
								"listing",
								Arrays.asList(pageLink.getDescription(),s),
								section.getAttributes(),
								new HashMap<>());
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



