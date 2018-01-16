package com.wline.documentation;

import com.wline.documentation.canvas.BuildCanvas;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;

/**
 * Created by Guillaume Bailleul on 16/01/2018.
 */
public class CallCanvas {

    public static final String KEY = "canvas";

    protected static void call(String[] args) throws IOException {
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
                Option.builder("t")
                        .longOpt("target")
                        .required()
                        .hasArg()
                        .desc("Output file name")
                        .build()
        );

        CommandLineParser parser = new DefaultParser();

        CommandLine command = null;
        try {
            command = parser.parse(options,args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "Runner "+KEY, options );
            System.exit(-2);
        }
        if (command==null)
            System.exit(-3);



        BuildCanvas bc = new BuildCanvas();
        bc.parse(
                new File(command.getOptionValue("s")),
                new File(command.getOptionValue("t"))
        );

    }
}
