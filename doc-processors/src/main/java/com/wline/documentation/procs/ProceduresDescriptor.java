package com.wline.documentation.procs;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ProceduresDescriptor {

	private Map<String,Object> content;

	private static Logger logger = LoggerFactory.getLogger(ProceduresDescriptor.class);

	public ProceduresDescriptor(Map<String, Object> content) {
		this.content = content;
	}

	public static ProceduresDescriptor load (File description ) throws IOException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		Map<String,Object> content = mapper.readValue(description, Map.class);
		Map<String,Object> result = replaceVariables(content, content);
		System.out.flush();
		return new ProceduresDescriptor(result);
	}

	private static Map<String,Object> replaceVariables (Map<String,Object> root, Map<String,Object> content) {
		Map<String,Object> result = new HashMap<>();
		for (Map.Entry<String,Object> entry: content.entrySet()) {
			if (entry.getValue() instanceof Map) {
				result.put(entry.getKey(), replaceVariables(root, (Map<String,Object>)entry.getValue()));
			} else if (entry.getValue() instanceof String) {
				if (((String) entry.getValue()).startsWith("${")) {
					String value = ((String) entry.getValue()).substring(2, ((String) entry.getValue()).indexOf("}"));
					result.put(entry.getKey(),getGlobalValue(root, value));
				} else {
					result.put(entry.getKey(),entry.getValue());
				}
			} else {
				result.put(entry.getKey(),entry.getValue());
			}
		}
		return result;
	}

	private static String getGlobalValue (Map<String,Object> content, String key) {
		String value = (String)((Map<String,Object>)content.get("globals")).get(key);
		return value;
	}

	public static void main (String [] args) throws Exception {
		ProceduresDescriptor descriptor = ProceduresDescriptor.load(new File("sample/data/main.yml"));
		logger.info("{}",descriptor.content);
		for (String docid : descriptor.getDocumentsKey()) {
			logger.info("Template: "+descriptor.getTemplate(docid));
			for (Map.Entry<String,Object> entry: descriptor.getAttributes(docid,true).entrySet()) {
				logger.info("  "+entry.getKey()+" = "+entry.getValue());
			}
		}
	}

	public boolean isMapOfDocuments () {
		return content.get("documents") instanceof Map;
	}

	public boolean isListOfDocuments () {
		return content.get("documents") instanceof List;
	}

	public List<Object> getDocumentsAsList () {
		return (List<Object>)content.get("documents");
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

	public String getOutput (String document) {
		Object value = ((Map<String,Object>)getDocuments().get(document)).get("output");
		return value!=null?value.toString():null;
	}

	public String getOutput (Map<String,Object> content) {
		Object value = content.get("output");
		return value!=null?value.toString():null;
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
