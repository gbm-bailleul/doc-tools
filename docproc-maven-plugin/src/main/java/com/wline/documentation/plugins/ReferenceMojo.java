package com.wline.documentation.plugins;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wline.documentation.ref.PageLink;

@Mojo(name = "reference")
public class ReferenceMojo extends AbstractMojo {

    @Parameter( property = "reference.source", required =  true)
    private File source;

    @Parameter( property = "reference.output", required =  true)
    private File output;

    @Parameter( property = "reference.references", required =  true)
    private File references;


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Map<String, List<PageLink>> links;

    private TreeMap<String,String> missingReferences = new TreeMap<>();

    private String currentSection = null;

    private void initialize () {
        try {
            this.links = PageLink.loadCsvFile(references);
        } catch (IOException e) {
            throw new IllegalArgumentException("Problem when reading csv file : " + e.getMessage());
        }
//        initialized = true;
    }


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        initialize(); // TODO better place ?
        File target = new File (output,source.getName());
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(source),"utf-8"));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(target),"utf-8"))
        ){
            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith("[reference=")) {
                    process(writer,line);
                } else {
                    writer.append(line);
                    writer.newLine();
                    if (line.startsWith("=")) {
                        currentSection = line.substring(line.indexOf(" "));
                    }
                }
                line = reader.readLine();
            }
            feedback(writer);
        } catch (IOException e) {
            throw new MojoFailureException("Failed to execute mojo",e);
        }
    }

    private void process(BufferedWriter writer, String line) throws IOException {
        String ref = getReference(line);
        logger.debug("Found reference: "+ref+" / "+line);
        List<PageLink> pageLinks = links.get(ref);
        if (pageLinks==null) {
            missingReferences.put(ref,currentSection);
            writer.write("WARNING: No values found for reference "+ref);
            writer.newLine();
        } else {
            writer.write("|===");
            writer.newLine();
            writer.write("|description |link");
            writer.newLine();
            for (PageLink link: pageLinks) {
                writer.write('|');
                writer.write(link.getDescription());
                writer.write('|');
                writer.write(link.getTarget());
                writer.newLine();
            }
            writer.write("|===");
            writer.newLine();
            writer.newLine();
        }
    }

    private String getReference (String line) {
        int sta = line.indexOf('"');
        int end = line.indexOf('"',sta+1);
        return line.substring(sta+1,end);
    }

    private void feedback(BufferedWriter writer) throws IOException {
        writer.newLine();
        writer.write("== Generation feedback");
        writer.newLine();
        writer.write("=== Missing references");
        writer.newLine();
        writer.write("|===");
        writer.newLine();
        writer.write("|ref |title");
        writer.newLine();
        for (Map.Entry<String,String> entry: missingReferences.entrySet()) {
            writer.write('|');
            writer.write(entry.getKey());
            writer.write("| <<");
            writer.write(entry.getValue().trim().replace("'","\'"));
            writer.write(">> ");
            writer.newLine();
        }
        writer.write("|===");
        writer.newLine();
        writer.newLine();

    }



}
