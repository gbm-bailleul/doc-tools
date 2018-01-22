package com.wline.documentation.ref;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.extension.BlockMacroProcessor;

public class RequirementMacro
		extends BlockMacroProcessor {

	private File csvFile ;

	private boolean initialized = false;

	public RequirementMacro(String macroName, Map<String, Object> config) {
		super(macroName, config);
	}

	private void initialize (DocumentRuby document) {
		if (document.getAttributes().get("csv")==null)
			throw new IllegalArgumentException("Missing mandatory property 'csv'");
		csvFile = new File((String)document.getAttributes().get("csv"));
		FileUtils.deleteQuietly(csvFile);
		initialized = true;
	}

	@Override
	public Object process(AbstractBlock parent, String target, Map<String, Object> attributes) {
		if (!initialized) {
			initialize(parent.getDocument());
		}

		// TODO maybe ugly; list of book format ? epub...
		boolean isBook =  "pdf".equals(parent.getDocument().getAttr("backend"));

		String dir = (String)parent.getDocument().getAttr("docdir") ;

		Path parentPath = Paths.get(dir);
		String parsedDocument = (String)parent.getDocument().getAttr("docfile") ;
		Path relativePath = parentPath.relativize(Paths.get(parsedDocument));

		String description = parent.getTitle();
		if (description==null) description = parent.getDocument().doctitle().toString();

		try {
			PageLink pl = new PageLink(target, description, relativePath.toString(),isBook?null:parent.id());
			FileUtils.write(this.csvFile,pl.toString() + "\n", "ISO-8859-1", true);
		} catch (IOException e) {
			throw new RuntimeException("Problem when writing into csv file : " +  e.getMessage(),e);
		}

		return null;
	}



}

