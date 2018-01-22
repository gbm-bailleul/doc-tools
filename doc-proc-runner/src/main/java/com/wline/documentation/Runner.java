package com.wline.documentation;

import java.io.PrintStream;
import java.util.Arrays;

/**
 * Created by Guillaume Bailleul on 07/01/2018.
 */
public class Runner {


    public static void main (final String [] args ) throws Exception {
        PrintStream output = System.err;
        if (args.length == 0) {
            output.println("One of these key parameter is expected:");
            output.println(" * proc : to generate procedures");
            output.println(" * canvas : to generate reference matrix from specific excel file");
            output.println("");
            System.exit(-1);
        }

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
                System.exit(-1);
        }
    }


}
