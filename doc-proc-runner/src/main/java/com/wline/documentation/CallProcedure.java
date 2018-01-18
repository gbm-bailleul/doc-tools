package com.wline.documentation;

import com.wline.documentation.procs.ProcedureGenerator;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;

/**
 * Created by Guillaume Bailleul on 16/01/2018.
 */
public class CallProcedure {

    public static final String PROCEDURE = "proc";

    protected static void callProcedure(String[] args) throws IOException {
        Options options = new Options();
        options.addOption(
                Option.builder("s")
                        .longOpt("source")
                        .required()
                        .hasArg()
                        .desc("Source of procedure description")
                        .build()
        );
        options.addOption(
                Option.builder("a")
                        .longOpt("attr")
                        .required(false)
                        .hasArg()
                        .desc("Attribute property file")
                        .build()
        );
        options.addOption(
                Option.builder("w")
                        .longOpt("working")
                        .required(false)
                        .hasArg()
                        .desc("Working directory")
                        .build()
        );
        options.addOption(
                Option.builder("t")
                        .longOpt("target")
                        .required()
                        .hasArg()
                        .desc("Target directory")
                        .build()
        );


        CommandLineParser parser = new DefaultParser();

        CommandLine command = null;
        try {
            command = parser.parse(options,args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "Runner "+PROCEDURE, options );
            System.exit(-2);
        }
        if (command==null)
            System.exit(-3);


        File workingDir =  command.hasOption("w")?
                new File(command.getOptionValue("w")):
                new File(System.getProperty("java.tmp.dir"));
        if (!workingDir.exists()) {
            workingDir.mkdirs();
            workingDir.deleteOnExit();
        }

        File source = new File(command.getOptionValue("s"));

        File target = new File(command.getOptionValue("t"));

        File attrs = command.hasOption("a")?
                new File (command.getOptionValue("a")):
                null;


        ProcedureGenerator generator = new ProcedureGenerator();
        generator.setBackend("pdf");

        generator.generate(
                source,
                attrs,
                target,
                workingDir
        );
    }

}