package de.test;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class TestVelocity {

	public static void main(String[] args) {
		VelocityEngine ve = new VelocityEngine();
		ve.init();

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

		VelocityContext context = new VelocityContext();
		context.put("title", "My own title");
		context.put("text", "This is my dynamic text");
		context.put("carList", carList);

		Template t = ve.getTemplate("./templates/TestTemplate.vm");

		StringWriter writer = new StringWriter();
		t.merge(context, writer);
		System.out.println(writer.toString());
	}

}
