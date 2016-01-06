package de.test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class TestVelocity {

	public static void main(String[] args) {
		VelocityEngine ve = new VelocityEngine();
		ve.init();

		// create some test data
		ArrayList<Map> carList = new ArrayList<Map>();
		Map<String, String> map = new HashMap<String, String>();
		map.put("name", "BMW");
		map.put("type", "3er");
		carList.add(map);
		map = new HashMap<String, String>();
		map.put("name", "VW");
		map.put("type", "Golf");
		carList.add(map);
		map = new HashMap<String, String>();
		map.put("name", "Audi");
		map.put("type", "A6");
		carList.add(map);
		
		// create a pie chart
		DefaultPieDataset pieDataset = new DefaultPieDataset();
		pieDataset.setValue("A", 20);
		pieDataset.setValue("B", 40);
		pieDataset.setValue("C", 30);
        JFreeChart chart = ChartFactory.createPieChart("Test Chart", pieDataset, true, true, false);
        String encodedChart = encodeImageToBase64(chart.createBufferedImage(200, 200));

        // set context for later replacement
		VelocityContext context = new VelocityContext();
		context.put("title", "My own title");
		context.put("text", "This is my dynamic text");
		context.put("carList", carList);
		context.put("pieChart", encodedChart);
		context.put("pieChartAlt", "Test Chart Alt Text");

		Template t = ve.getTemplate("./src/test/resources/templates/TestTemplate.vm");

		// merge template with context data and write result to HTML file
		FileWriter writer = null;
		try {
			writer = new FileWriter("./target/TestVelocityOutput.html");
			t.merge(context, writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String encodeImageToBase64(BufferedImage image) {
		String imageString = null;
		
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(image, "PNG", bos);
			byte[] imageBytes = bos.toByteArray();
			
			// Java 8
			//imageString = Base64.getEncoder().encodeToString(imageBytes);
			
			// Java 6 & 7
			imageString = DatatypeConverter.printBase64Binary(imageBytes);
			
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return imageString;
	}
}
