package asciidoctor;

import java.io.File;

import com.wline.documentation.procs.ProcedureGenerator;

public class TestRunner {


	public static void main (String [] args) throws Exception {
		ProcedureGenerator generator = new ProcedureGenerator();
		generator.setBackend("pdf");

		String workingDir = "target/working-docs";
		String outputDir = "target/generated-docs";
		String source = "src/test/resources/hello2.adoc";
		String attributes = "src/test/resources/hello2.properties";

		generator.generate(
				new File (source),
				new File (attributes),
				new File (outputDir),
				new File (workingDir),
				"generated"
		);

	}

}
