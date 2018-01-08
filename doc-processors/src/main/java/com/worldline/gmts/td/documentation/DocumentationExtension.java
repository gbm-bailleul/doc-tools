package com.worldline.gmts.td.documentation;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.spi.ExtensionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentationExtension implements ExtensionRegistry {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());


	public void register(Asciidoctor asciidoctor) {
		logger.debug("Register specific processors");
		JavaExtensionRegistry jer = asciidoctor.javaExtensionRegistry();

		// load yell
		//		Map<String, Object> config = new HashMap<String, Object>();
		//		config.put("contexts", Arrays.asList(":paragraph"));
		//		config.put("content_model", ":simple");
		//		YellBlock yellBlock = new YellBlock("yell", config);
		//		jer.block(yellBlock);

//		jer.treeprocessor(MyTreeProcessor.class);
//		jer.treeprocessor(ProcedureProcessor.class);

	}
}
