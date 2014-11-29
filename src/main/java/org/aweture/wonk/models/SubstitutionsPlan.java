package org.aweture.wonk.models;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aweture.wonk.storage.SimpleData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Log;

public class SubstitutionsPlan {
	private static final String TAG = SubstitutionsPlan.class.getSimpleName();
	private static final boolean DEBUG = false;
	
	private String plan;
	
	public SubstitutionsPlan(String plan) {
		this.plan = plan;
	}
	
	public void save(Context context) throws IOException, SAXException, ParserConfigurationException {
		// Parse the data
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dom = db.parse(new InputSource(new ByteArrayInputStream(plan.getBytes("utf-8"))));
		
		// Get the creation time
		Element creationTimeElement = (Element) dom.getElementsByTagName("font").item(0);
		String creationTime = creationTimeElement.getTextContent().substring(7);
		
		// Save time of plan creation time.
		SimpleData data = SimpleData.getInstance(context);
		data.setTimeOfSubstitutionsPlanCreation(creationTime);
		
		// Search the dates of the days the plans concern.
		String[] planDates = getPlanDates(dom);
		// Search the substitution plans.
		Node[] plans = getPlans(dom);
		
		// Save the plans with the dates they concern.
		for (int i = 0; i < plans.length; i++) {
			savePlan(context, planDates[i], plans[i]);
		}
	}
	
	/**
	 * Search the dates of the days the plans concern.
	 * 
	 * @param dom Document which contains the information
	 * @return String[] with the two dates
	 */
	private String[] getPlanDates(Document dom) {
		String[] planDates = new String[2];
		
		// Run through all divs
		NodeList divs = dom.getElementsByTagName("div");
		for (int i = 0, datesCount = 0; i < divs.getLength() && datesCount < 2; i++) {
			
			// Check the class attribute to be existent.
			Node classAttr = divs.item(i).getAttributes().getNamedItem("class");
			if (classAttr != null) {
				
				// Check the class value to be == "mon_title", which is what we are looking for.
				String classValue = classAttr.getNodeValue();
				if (classValue.equals("mon_title")) {
					
					// Save the date of the day the plan concerns.
					planDates[datesCount] = divs.item(i).getTextContent().substring(0, 10);
					datesCount++;
				}
			}
		}
		return planDates;
	}
	
	/**
	 * Search the substitution plans.
	 * 
	 * @param dom Document which contains the information
	 * @return Node[] with the two plans
	 */
	private Node[] getPlans(Document dom) {
		Node[] plans = new Node[2];
		
		// Run through all contained tables
		NodeList tables = dom.getElementsByTagName("table");
		for (int i = 0, planCount = 0; i < tables.getLength() && planCount < 2; i++) {

			// Check the class attribute to be existent.
			Node classAttr = tables.item(i).getAttributes().getNamedItem("class");
			if (classAttr != null) {

				// Check the class value to be == "mon_list",
				// wich is what we are looking for.
				String classValue = classAttr.getNodeValue();
				if (classValue.equals("mon_list")) {
					
					// Save the table to the array to be returned.
					plans[planCount] = tables.item(i);
					planCount++;
				}
			}
		}
		return plans;
	}
	
	private void savePlan(Context context, String planDate, Node plan) {
		
		if (DEBUG) Log.d(TAG, "Datum\t\tKlasse\tStunde\tVertreter\tstatt Lehrer\tstatt Fach\tArt\tText\n");
		
		// Get all rows of the plan and run through them.
		NodeList rows = ((Element) plan).getElementsByTagName("tr");
		// Start at i == 2 due to the first two rows containing meta information.
		for (int i = 2; i < rows.getLength(); i++) {
			
			// Get the cells of the current row.
			Node row = rows.item(i);
			NodeList cells = ((Element) row).getElementsByTagName("td");
			
			// Check for the class information, which may be null.
			// Those "null substitutes" are useless.
			String className = cells.item(0).getTextContent();
			if (className == null || className.equals("SLR") || className.equals("Ltg")) {
				// Skip this row and get the next one.
				continue;
			}

			String periodContent = cells.item(1).getTextContent();
			
			Substitution substitution = new Substitution();
			
			for (String period : getPeriods(periodContent)) {
				int periodNr = Integer.parseInt(period);
				String substTeacher = cells.item(2).getTextContent();
				String instdTeacher = cells.item(3).getTextContent();
				String instdSubject = cells.item(4).getTextContent();
				String kind = cells.item(5).getTextContent();
				String text = cells.item(7).getTextContent();
				
				substitution.setDate(planDate);
				substitution.setClassName(className);
				substitution.setPeriod(periodNr);
				substitution.setSubstTeacher(substTeacher);
				substitution.setInstdTeacher(instdTeacher);
				substitution.setInstdSubject(instdSubject);
				substitution.setKind(kind);
				substitution.setText(text);
				substitution.save(context);
				
				if (DEBUG) Log.d(TAG, planDate + "\t" + className + "\t" + periodNr + "\t" + substTeacher + "\t\t" + instdTeacher + "\t\t" + instdSubject + "\t\t" + kind + "\t" + text);
				
			}
		}
	}
	
	private String[] getPeriods(String periodContent) {
		String[] periods = periodContent.split("-");
		for (int i = 0; i < periods.length; i++) {
			periods[i] = periods[i].trim();
		}
		return periods;
	}
}
