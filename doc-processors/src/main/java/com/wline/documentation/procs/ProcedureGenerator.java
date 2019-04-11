package com.wline.documentation.procs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;

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


    protected Map<String,Object> loadAttributes (File attributes, File mainAttributes) throws IOException {
        Map<String,Object> result = new HashMap<>();
        for (File f : new File [] {mainAttributes,attributes}) {
            if (f!=null && f.exists()) {
                Properties props = new Properties();
                props.load(new FileInputStream(f));
                for (Map.Entry<Object,Object> entry: props.entrySet()) {
                    result.put(entry.getKey().toString(),entry.getValue());
                }
            }
        }
        return result;
    }

    public void generate (File source, File attributes, File outputDir, File workingDir, String outputName) throws IOException {
        generate(source,attributes,null,outputDir,workingDir, outputName);
    }

    public void generateFromDescription(File description, File dataDir, File outputDir, File workingDir) throws IOException {
        if (!description.exists() || !description.isFile())
            throw new IOException("Invalid description file path: "+description);
        ProceduresDescriptor descriptor = ProceduresDescriptor.load(description);

        if (descriptor.isMapOfDocuments()) {
            for (String docid : descriptor.getDocumentsKey()) {
                File template = new File (dataDir, descriptor.getTemplate(docid));
                Map<String,Object> attributes = descriptor.getAttributes(docid,true);
                generate(template,attributes,outputDir,workingDir, descriptor.getOutput(docid));
            }
        } else if (descriptor.isListOfDocuments()) {
            for (Object o: descriptor.getDocumentsAsList()) {
                Map<String,Object> documentDescription = (Map<String,Object>)o;
                File template = new File (dataDir,documentDescription.get("template").toString());
                Map<String,Object> attributes = descriptor.getAttributes(documentDescription,true);
                generate(template,attributes,outputDir,workingDir, descriptor.getOutput(documentDescription));
            }
        }

    }


    public void generate (File source, File attributes, File mainAttributes, File outputDir, File workingDir, String outputName) throws IOException {
        Map<String,Object> externalAttributes = loadAttributes(attributes,mainAttributes);
        generate(source,externalAttributes,outputDir,workingDir, outputName);

    }

    public void generate (File source, Map<String,Object> attributes, File outputDir, File workingDir, String outputName) throws IOException {

        if (outputName==null)
            outputName = source.getName();
        else
            outputName += ".adoc";

        File workingFile = new File(workingDir,outputName);

        FileUtils.forceMkdir(workingDir);

        FileUtils.copyDirectory(source.getParentFile(),workingDir);

        ProcedurePreProc ppp = new ProcedurePreProc(attributes);
        FileInputStream input = new FileInputStream(source);
        FileOutputStream output = new FileOutputStream(workingFile);

        try {
            ppp.process(input, output);
        } catch (ParseException e) {
            throw new IOException("Failed to parse source: "+e.getMessage());
        }

        Asciidoctor asciidoctor = Asciidoctor.Factory.create();

        Map<String,Object> modifiedAttributes = ppp.getAttributes();

        modifiedAttributes.put("pdf-stylesdir",workingDir.getAbsolutePath());
        if (!modifiedAttributes.containsKey("pdf-style")) {
            modifiedAttributes.put("pdf-style","custom");
        }

        asciidoctor.convertFile(workingFile, OptionsBuilder.options()
                .mkDirs(true)
                .attributes(modifiedAttributes)
                .backend(backend)
                .toDir(outputDir));
    }

    /**
     * Created by Guillaume Bailleul on 06/01/2018.
     */
    public static class ProcedurePreProc {

        public static final String TAG_START = "%%procedure";

        public static final String TAG_SECRET_START = "%%secret";

        public static final String TAG_CARTRIDGE = "%%cartridge";

        public static final String TAG = "%%";

        public static final String TAG_END = "%%end";

        public static final String TAG_DETAIL = "%%detail";

        public static final String TAG_LOOP = "%%loop";

        public static final String TAG_END_LOOP = "%%eloop";

        private final Map<String, Object> attributes;

        protected int stepCount = 0;

        protected int lineCount = 0;

        protected ThreadLocal<BufferedReader> reader = new ThreadLocal<>();

        protected ThreadLocal<List<String>> bufferedLines = new ThreadLocal<>();

        protected List<String> loopKeys = new ArrayList<>();

        public ProcedurePreProc (Map<String,Object> attributes ) {
            this.attributes = attributes;
        }

        public Map<String, Object> getAttributes() {
            return attributes;
        }

        public void process (InputStream input, OutputStream output) throws IOException,ParseException {
            reader.set(new BufferedReader(new InputStreamReader(input)));
            bufferedLines.set(new ArrayList<>());
            Writer writer = new OutputStreamWriter(output);
            String buffer = nextLine();
            while (buffer!=null) {
                if (buffer.startsWith(TAG_START)) {
                    buffer = procedure(writer, buffer);
                } else if (buffer.startsWith(TAG_SECRET_START)) {
                    buffer = secret(writer, buffer);
                } else if (buffer.startsWith(TAG_CARTRIDGE)) {
                    buffer = cartridge(writer);
                } else if (buffer.startsWith(TAG_LOOP)) {
                    buffer = loop(writer,buffer);
                } else {
                    writer.write(buffer);
                    writer.write("\n");
                    buffer = nextLine();
                }
            }
            writer.flush();
            reader.get().close();

        }

        private String loop(Writer writer, String buffer) throws IOException,ParseException {
            StepInfo si = parseStepInfo(buffer);
            // add loop key if not already in list
            if (!loopKeys.contains(si.actor)) loopKeys.add(si.actor);
            buffer = nextLine();
            List<String> loopContent = new ArrayList<>();
            while (!buffer.startsWith(TAG_END_LOOP)) {
                loopContent.add(buffer);
                buffer = nextLine();
            }
            List<Map<String,Object>> elements = (List<Map<String,Object>>) attributes.get(si.actor);
            if (elements==null) {
                throw new ParseException("Expected list key does not exists: "+si.actor,lineCount);
            }
            int count = 0;
            for (Map<String,Object> element : elements) {
                for (String line: loopContent) {
                    bufferLine(renameAttributes(line,count,element));
                }
                count++;
            }
            // first line after eloop
            return nextLine();
        }

        private String renameAttributes (String line, int count,Map<String,Object> element) {
            StringBuilder sb = new StringBuilder(line.length()+10);
            StringBuilder name = new StringBuilder();
            for (char c : line.toCharArray()) {
                if ('{' == c) {
                    name = new StringBuilder();
                } else if ('}' == c) {
                    if (element.containsKey(name.toString())) {
                        // add suffix to key name
                        sb.append('_').append(count);
                        // create attribute with suffix
                        String attrName = name.toString()+"_"+count;
                        attributes.put(attrName,element.get(name.toString()));
                    } // else not a key
                } else {
                    name.append(c);
                }
                sb.append(c);
            }
            return sb.toString();
        }

        protected String cartridge (Writer writer) throws IOException {
            writer.write("[width=\"80%\",cols=\"1a,1,1\",options=header]\n");
            writer.write("|=======\n");
            writer.write("|Maître de Cérémonie|Auditeur Interne|Opérateur d’enregistrement\n");
            writer.write("| {nbsp}\n{nbsp}\n{nbsp}\n{nbsp}\n{nbsp}\n{nbsp}\n{nbsp}\n{nbsp}\n{nbsp}\n{nbsp}\n");
            writer.write("| \n");
            writer.write("| \n");
            writer.write("|=======\n");
            writer.write("\n");
            writer.write("<<<");
            return nextLine();
        }


        protected String procedure (Writer writer, String buffer) throws IOException, ParseException {
            // table first lines
            writer.write("[width=\"99%\",cols=\"1,1,1,10a,2\",options=header]\n");
            writer.write("|=======\n");
            writer.write("|step|actor|type|description|paraph\n");
            // copy all until next TAG
            while (!buffer.startsWith(TAG_END)) {
                buffer = procedureStep(writer,buffer);
            }
            // table end
            writer.write("|=======\n");
            return nextLine();
        }

        protected String secret (Writer writer, String buffer) throws IOException, ParseException {
            // table first lines
            writer.write("[width=\"99%\",cols=\"1,8a,2a\",options=header]\n");
            writer.write("|=======\n");
            writer.write("|Porteur|Description|Valeur\n");
            // copy all until next TAG
            while (!buffer.startsWith(TAG_END)) {
                buffer = secretStep(writer,buffer);
            }
            // table end
            writer.write("|=======\n");
            return nextLine();
        }

        protected String secretStep (Writer writer, String buffer) throws IOException,ParseException {
            StepInfo si = parseStepInfo(buffer);
            buffer = nextLine();

            writer.write("|"+si.actor);
            writer.write("|");

            writer.write(buffer);
            buffer = nextLine();
            while (!buffer.startsWith(TAG)) {
                writer.write(buffer);
                writer.write("\n");
                buffer = nextLine();
            }
            writer.write("|"+si.action);
            // new table line
            writer.write("\n");
            return buffer;
        }


        protected String procedureStep (Writer writer, String buffer) throws IOException,ParseException {
            // check if we are in detail view
            if (buffer.startsWith(TAG_DETAIL))
                return procedureStepDetail(writer);

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
            while (buffer.startsWith(TAG_LOOP)) {
                // loop in the procedure step
                buffer = loop(writer,buffer);
                // up to next tag
                while (!buffer.startsWith(TAG)) {
                    writer.write(buffer);
                    writer.write("\n");
                    buffer = nextLine();
                }
            }

            writer.write("|{nbsp}");
            writer.write("\n");
            // new table line
            writer.write("\n");
            return buffer;
        }

        private String procedureStepDetail(Writer writer) throws IOException {
            String buffer = nextLine();
            writer.write("5+a|");
            writer.write(buffer);
            writer.write("\n");
            buffer = nextLine();
            while (!buffer.startsWith(TAG)) {
                writer.write(buffer);
                writer.write("\n");
                buffer = nextLine();
            }
            // new table line
            writer.write("\n");
            writer.write("\n");
            return buffer;
        }

        protected String nextLine () throws IOException {
            if (bufferedLines.get().size()>0) {
                // some line has been buffered
                return bufferedLines.get().remove(0);
            } else {
                lineCount++;
                return reader.get().readLine();
            }
        }

        protected void bufferLines (List<String> lines) {
            bufferedLines.get().addAll(lines);
        }

        protected void bufferLine (String line) {
            bufferedLines.get().add(line);
        }

        public StepInfo parseStepInfo (String buffer) throws ParseException {
            int sta = buffer.indexOf('[');
            int end = buffer.indexOf(']');
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

            private String actor;

            private String action;

        }
    }
}
