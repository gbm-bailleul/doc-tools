package com.worldline.gmts.td.documentation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.worldline.gmts.td.documentation.procs.ProcedureGenerator;

/**
 * Created by Guillaume Bailleul on 07/01/2018.
 */
public class Runner {

    public static final String PROCEDURE = "proc";


    public static void main (final String [] args ) throws Exception {
        if (args.length == 0) {
            System.err.println("One of these key parameter is expected:");
            System.err.println(" * proc : to generate procedures");
            System.err.println("");
            System.exit(-1);
        }
        ;

        switch (args[0]) {
            case PROCEDURE:
                callProcedure(Arrays.copyOfRange(args,1,args.length));
                break;
            default:
                System.err.println("Invalid key parameter: "+ args[0]);
        }
    }

    private static void callProcedure(String[] args) throws IOException {
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
