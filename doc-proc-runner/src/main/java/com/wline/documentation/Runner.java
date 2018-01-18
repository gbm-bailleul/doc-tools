package com.wline.documentation;

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

import com.wline.documentation.procs.ProcedureGenerator;

/**
 * Created by Guillaume Bailleul on 07/01/2018.
 */
public class Runner {


    public static void main (final String [] args ) throws Exception {
        if (args.length == 0) {
            System.err.println("One of these key parameter is expected:");
            System.err.println(" * proc : to generate procedures");
            System.err.println(" * canvas : to generate reference matrix from specific excel file");
            System.err.println("");
            System.exit(-1);
        }
        ;

        switch (args[0]) {
            case CallProcedure.PROCEDURE:
                CallProcedure.callProcedure(Arrays.copyOfRange(args,1,args.length));
                break;
            case CallCanvas.KEY:
                CallCanvas.call(Arrays.copyOfRange(args,1,args.length));
                break;
            case CallAsciidoctor.KEY:
                CallAsciidoctor.call(Arrays.copyOfRange(args,1,args.length));
                break;
            default:
                System.err.println("Invalid key parameter: "+ args[0]);
        }
    }


}
