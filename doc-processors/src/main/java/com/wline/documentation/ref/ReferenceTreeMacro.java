package com.wline.documentation.ref;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.Treeprocessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferenceTreeMacro
		extends Treeprocessor {

	private Map<String, List<PageLink>> links;

	private boolean initialized = false;

	private Logger logger = LoggerFactory.getLogger(this.getClass());


	public ReferenceTreeMacro(Map<String, Object> config) {
		super(config);
	}

	private void initialize (Document document) {
		File csvDir = new File((String)document.getAttr("references-dir"));
		try {
			this.links = PageLink.loadCsvFile(csvDir);
		} catch (IOException e) {
			throw new IllegalArgumentException("Problem when reading csv file : " + e.getMessage());
		}
		initialized = true;
	}


	@Override
	public Document process(Document document) {
		// init
		if (!initialized) {
			initialize(document);
		}
		for (StructuralNode block: document.getBlocks()) {
			process(document, block);
		}
		return document;
	}

	protected void process (Document document, StructuralNode block) {
		if (block instanceof Section) {
			processSection(document,(Section)block);
		}
	}


	protected void processSection (Document document, Section section) {
			logger.debug("In section "+section.getTitle()+" / "+section.getLevel());
			if (section.getAttributes().containsKey("reference")) {
				String target = section.getAttr("reference").toString();
				logger.debug("  Found reference : {}",target);
				List<PageLink> pageLinks = this.links.get(target);
				logger.debug("    links: {} ",pageLinks!=null?pageLinks.size():0);
				if (pageLinks != null && !pageLinks.isEmpty()) {
					for (PageLink pageLink : pageLinks) {
						Map<Object, Object> options = new HashMap<>();
						options.put("type", ":link");
						String tg = pageLink.getTarget()+
								(pageLink.getAnchor()!=null?("#"+pageLink.getAnchor()):"");

						options.put("target", tg);

						String s = createPhraseNode(
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
						section.getBlocks().add(1,nb);
					}
				}
			}
			for (StructuralNode sb: section.getBlocks()) {
				process(document, sb);
			}
	}




}



