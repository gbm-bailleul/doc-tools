package com.wline.documentation.ref;

import org.apache.commons.io.FileUtils;
import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.extension.BlockMacroProcessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class RequirementMacro
		extends BlockMacroProcessor {

	private File csvFile ;

	public RequirementMacro(String macroName, Map<String, Object> config) {
		super(macroName, config);
		if (config.get("csv")==null)
			throw new RuntimeException("Missing mandatory property 'csv'");
		csvFile = (File)config.get("csv");
		FileUtils.deleteQuietly(this.csvFile);

	}

	@Override
	protected Object process(AbstractBlock parent, String target, Map<String, Object> attributes) {
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
			FileUtils.writeStringToFile(this.csvFile,pl.toString() + "\n",true);
		} catch (IOException e) {
			throw new RuntimeException("Problem when writing into csv file : " +  e.getMessage(),e);
		}

		return null;
	}



}

