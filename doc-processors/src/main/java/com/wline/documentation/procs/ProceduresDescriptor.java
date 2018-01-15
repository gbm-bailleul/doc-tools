package com.wline.documentation.procs;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ProceduresDescriptor {

	private Map<String,Object> content;

	public ProceduresDescriptor(Map<String, Object> content) {
		this.content = content;
	}

	public static ProceduresDescriptor load (File description ) throws IOException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		Map<String,Object> content = mapper.readValue(description, Map.class);
		return new ProceduresDescriptor(content);
	}


	public static void main (String [] args) throws Exception {
		ProceduresDescriptor descriptor = ProceduresDescriptor.load(new File("sample/data/main.yml"));
		System.out.println(descriptor.content);
		for (String docid : descriptor.getDocumentsKey()) {
			System.out.println("Template: "+descriptor.getTemplate(docid));
			for (Map.Entry<String,Object> entry: descriptor.getAttributes(docid,true).entrySet()) {
				System.out.println("  "+entry.getKey()+" = "+entry.getValue());
			}
			System.out.println();
		}
	}


	private Map<String,Object> getDocuments () {
		return (Map<String,Object>)content.get("documents");
	}

	public Set<String> getDocumentsKey () {
		return getDocuments().keySet();
	}

	public String getTemplate (String document) {
		return ((Map<String,Object>)getDocuments().get(document)).get("template").toString();
	}

	public Map<String,Object> getCommonAttributes() {
		return (Map<String,Object>)content.get("globals");
	}

	public Map<String,Object> getAttributes (String document,boolean addMainAttributes) {
		// TODO handle addMainAttributes
		Map<String,Object> res =  (Map<String,Object>)((Map<String,Object>)getDocuments().get(document)).get("attributes");
		if (addMainAttributes) {
			res.putAll(getCommonAttributes());
		}
		return res;
	}

}
