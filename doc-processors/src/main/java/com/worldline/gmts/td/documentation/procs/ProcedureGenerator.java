package com.worldline.gmts.td.documentation.procs;

import org.apache.commons.io.FileUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;

import java.io.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Created by Guillaume Bailleul on 06/01/2018.
 */
public class ProcedureGenerator {

    private String backend = "pdf";

    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }


    protected Map<String,Object> loadAttributes (File attributes) throws IOException {
        Map<String,Object> result = new HashMap<>();
        if (attributes!=null) {
            Properties props = new Properties();
            props.load(new FileInputStream(attributes));
            for (Map.Entry<Object,Object> entry: props.entrySet()) {
                result.put(entry.getKey().toString(),entry.getValue());
            }
        }
        return result;
    }

    public void generate (File source, File attributes,File outputDir, File workingDir) throws IOException {
        Map<String,Object> externalAttributes = loadAttributes(attributes);

        File outputFile = new File(workingDir,source.getName());

        FileUtils.forceMkdir(workingDir);

        FileUtils.copyDirectory(source.getParentFile(),workingDir);

        ProcedurePreProc ppp = new ProcedurePreProc();
        FileInputStream input = new FileInputStream(source);
        FileOutputStream output = new FileOutputStream(outputFile);

        try {
            ppp.process(input, output);
        } catch (ParseException e) {
            // TODO ugly
            throw new IOException("Failed to parse source: "+e.getMessage());
        }

        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
//		JavaExtensionRegistry extensionRegistry = asciidoctor.javaExtensionRegistry();
//		extensionRegistry.treeprocessor(new MyTreeProcessor(new HashMap<String, Object>()));

        asciidoctor.convertFile(outputFile, OptionsBuilder.options()
                .mkDirs(true)
                .attributes(externalAttributes)
                .backend(backend)
                .toDir(outputDir));


    }

    /**
     * Created by Guillaume Bailleul on 06/01/2018.
     */
    public static class ProcedurePreProc {

        public static final String TAG_START = "%%procedure";

        public static final String TAG = "%%";

        public static final String TAG_END = "%%end";

        public static final String TAG_DETAIL = "%%detail";

        protected int stepCount = 0;

        protected int lineCount = 0;

        protected ThreadLocal<BufferedReader> reader = new ThreadLocal<BufferedReader>();

        public void process (InputStream input, OutputStream output) throws IOException,ParseException {
            reader.set(new BufferedReader(new InputStreamReader(input)));
            Writer writer = new OutputStreamWriter(output);
            String buffer = nextLine();
            while (buffer!=null) {
                if (buffer.startsWith(TAG_START)) {
                    // table first lines
                    writer.write("[width=\"99%\",cols=\"1,1,1,10a,1\",options=header]\n");
                    writer.write("|=======\n");
                    writer.write("|step|actor|type|description|paraph\n");
                    // copy all until next TAG
                    while (!buffer.startsWith(TAG_END)) {
                        buffer = procedureStep(writer,buffer);
                    }
                    // table end
                    writer.write("|=======\n");
                    buffer = nextLine();
                } else {
                    writer.write(buffer);
                    writer.write("\n");
                    buffer = nextLine();
                }
            }
            writer.flush();
            reader.get().close();

        }

        protected String procedureStep (Writer writer, String buffer) throws IOException,ParseException {
            // check if we are in detail view
            if (buffer.startsWith(TAG_DETAIL))
                return procedureStepDetail(writer,buffer);

            StepInfo si = parseStepInfo(buffer);
            buffer = nextLine();

            stepCount++;
            writer.write("|"+stepCount);
            writer.write("|"+si.actor);
            writer.write("|"+si.action);
            writer.write("\n");
            writer.write("|");

            writer.write(buffer);
            buffer = nextLine();
            while (!buffer.startsWith(TAG)) {
                writer.write(buffer);
                writer.write("\n");
                buffer = nextLine();
            }
            writer.write("|{nbsp}");
            writer.write("\n");
            // new table line
            writer.write("\n");
//            writer.write("\n");
            return buffer;
        }

        private String procedureStepDetail(Writer writer, String buffer) throws IOException {
            buffer = nextLine();
            writer.write("5+a|");
    //        writer.write("\n");
            writer.write(buffer);
            writer.write("\n");
            buffer = nextLine();
            while (!buffer.startsWith(TAG)) {
                writer.write(buffer);
                writer.write("\n");
                buffer = nextLine();
            }
    //        writer.write("|{nbsp}");
    //        writer.write("\n");
            // new table line
            writer.write("\n");
            writer.write("\n");
            return buffer;
        }

        protected String nextLine () throws IOException {
            lineCount++;
            return reader.get().readLine();
        }



        public StepInfo parseStepInfo (String buffer) throws ParseException {
            int sta = buffer.indexOf("[");
            int end = buffer.indexOf("]");
            if (sta<0 || end < 0 || end <sta)
                throw new ParseException("Step Description is mandatory at line "+lineCount,0);
            String values = buffer.substring(sta+1, end);
            StringTokenizer tokens = new StringTokenizer(values," ");
            StepInfo si = new StepInfo();
            // First token is the actor
            if (!tokens.hasMoreTokens()) {
                // no token at all, this is an error
                // TODO exception
                si.action = "Act";
                si.actor = "CC"; // TODO
            } else {
                si.actor = tokens.nextToken();
            }
            // Second token is action if exists
            if (tokens.hasMoreTokens()) {
                si.action = tokens.nextToken();
            } else {
                // default action is Do
                si.action = "Do";
            }
            return si;
        }

        private static class StepInfo {

            public String actor;

            public String action;

        }
    }
}
