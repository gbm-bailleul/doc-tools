package com.wline.documentation;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.extension.JavaExtensionRegistry;

import com.wline.documentation.ref.RequirementMacro;

/**
 * Created by Guillaume Bailleul on 18/01/2018.
 */
public class CallAsciidoctor {

    public static final String KEY = "doctor";

    private CallAsciidoctor () {}

    protected static void call(String[] args) {
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
        options.addOption(
                Option.builder("r")
                        .longOpt("references")
                        .required()
                        .hasArg()
                        .desc("References file name")
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

        Map<String,Object> blockMacroOptions = new HashMap<>();
        blockMacroOptions.put("csv",new File(command.getOptionValue("r")));

        Asciidoctor doctor = Asciidoctor.Factory.create();
        JavaExtensionRegistry jer = doctor.javaExtensionRegistry();
        jer.blockMacro(new RequirementMacro("req",blockMacroOptions));

        doctor.convertFile(
                new File(command.getOptionValue("s")),
                OptionsBuilder.options()
                .mkDirs(true)
                .attributes(new HashMap<>())
                .backend("html5")
                        .backend("pdf")
                .toDir(new File(command.getOptionValue("t"))));


    }
}
