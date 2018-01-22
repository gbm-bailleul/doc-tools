package com.wline.documentation.canvas;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class BuildCanvas {

	public void parse (File fn,File out) throws IOException {
		try (Workbook wb = new XSSFWorkbook(new FileInputStream(fn))) {
			Sheet sheet = 	wb.getSheet("Feuil1");
			Iterator<Row> rows = sheet.iterator();

			List<List<Row>> bags = new ArrayList<>();
			List<Row> bag =  null;
			// skip headers
			rows.next();
			while (rows.hasNext()) {
				Row row = rows.next();
				if (row.cellIterator().next().getStringCellValue().startsWith("§")) {
					if (bag!=null)  bags.add(bag);
					bag = new ArrayList<>();
				}
				bag.add(row);

			}
			bags.add(bag);

			PrintStream output = new PrintStream(new FileOutputStream(out));
			for (List<Row> current: bags) {
				display(current,output);
			}

			IOUtils.closeQuietly(output);
		}

	}

	public void display (List<Row> bag, PrintStream output ) throws IOException {
		// block title
		output.println("[reference=\""+getBagId(bag)+"\"]");
		output.print("=== Chapitre ");
		output.println(getBagTitle(bag));
		output.println("Reference: "+getBagId(bag));
		// link with GA Z42-019
		output.println("....");
		for (Row current: bag) {
			Iterator<Cell> cells = current.cellIterator();
			cells.next(); // skip column 1
			String content = cells.next().getStringCellValue().trim();
			if (content.length()>0) {
				output.println(content);
			}
		}
		output.println("....");
		output.println();
		// exigence
		output.println("....");
		output.println("Exigences");
		for (Row current: bag) {
			Iterator<Cell> cells = current.cellIterator();
			cells.next(); // skip column 1
			if (cells.hasNext()) {
				cells.next(); // skip column 2
				if (cells.hasNext()) {
					String content = cells.next().getStringCellValue().trim();
					if (content.length()>0) {
						output.println(content);
					}
				}
			}
		}
		output.println("....");
		output.println();
		// controle
		output.println("....");
		output.println("Modalité de controle");
		for (Row current: bag) {
			Iterator<Cell> cells = current.cellIterator();
			cells.next(); // skip column 1
			if (cells.hasNext())
				cells.next(); // skip column 2
			else
				continue;
			if (cells.hasNext())
				cells.next(); // skip column 3
			else
				continue;
			if (!cells.hasNext())
				continue;
			String content = cells.next().getStringCellValue().trim();
			if (content.length()>0) {
				output.println(content);
			}
		}
		output.println("....");
		output.println();

	}

	public String getBagTitle (List<Row> bag) {
		StringBuilder sb = new StringBuilder ();
		for (Row current : bag) {
			sb.append(current.cellIterator().next().getStringCellValue());
		}
		return sb.toString().substring(2);
	}

	public String getBagId (List<Row> bag) {
		// tmp = first line without the first character
		String tmp = bag.get(0).cellIterator().next().getStringCellValue().substring(2);
		if (tmp.indexOf(' ')<0)
			return tmp; // there is only the value
		else
			return tmp.substring(0,tmp.indexOf(' ')); // remove text after index
	}





}
