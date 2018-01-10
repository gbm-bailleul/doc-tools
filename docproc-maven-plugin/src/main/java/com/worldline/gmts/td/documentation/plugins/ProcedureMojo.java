package com.worldline.gmts.td.documentation.plugins;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.worldline.gmts.td.documentation.procs.ProcedureGenerator;

@Mojo(name = "procedure")
public class ProcedureMojo extends AbstractMojo {

	@Parameter( property = "procedure.output", defaultValue = "target/generated-docs" )
	private File target;

	@Parameter( property = "procedure.working", defaultValue = "target/generated-working" )
	private File working;

	@Parameter( property = "procedure.sources", required =  true)
	private File source;

	@Parameter( property = "procedure.properties", required =  true)
	private File attributes;


	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		ProcedureGenerator generator = new ProcedureGenerator();
		generator.setBackend("pdf");
	 	try {
			generator.generateDirectory(
					source,
					attributes,
					target,
					working
			);
		} catch (IOException e) {
			throw new MojoFailureException("Failed to generate procedure", e);
		}

	}
}
