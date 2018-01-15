package com.wline.documentation.plugins;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.wline.documentation.procs.ProcedureGenerator;

@Mojo(name = "procedure")
public class ProcedureMojo extends AbstractMojo {

	@Parameter( property = "procedure.output", defaultValue = "target/generated-docs" )
	private File target;

	@Parameter( property = "procedure.working", defaultValue = "target/generated-working" )
	private File working;

	@Parameter( property = "procedure.sources", required =  true)
	private File source;

	@Parameter( property = "procedure.descriptor", required =  true)
	private File descriptor;


	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		ProcedureGenerator generator = new ProcedureGenerator();
		generator.setBackend("pdf");
		try {
			generator.generateFromDescription(
					descriptor,
					source,
					target,
					working);
		} catch (IOException e) {
			throw new MojoFailureException("Failed to generate procedure", e);
		}

	}
}
