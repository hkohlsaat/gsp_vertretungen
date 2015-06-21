package org.aweture.wonk.background;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aweture.wonk.log.LogUtil;
import org.aweture.wonk.models.Class;
import org.aweture.wonk.models.Date;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.models.Subjects.Subject;
import org.aweture.wonk.models.Substitution;
import org.aweture.wonk.models.Teachers.Teacher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility class to transform the at times messy html returned
 * by the server into an array of {@link Plan}s.
 * 
 * @author Hannes Kohlsaat
 *
 */
public class IServHtmlUtil {
	
	private String plan;
	private List<Plan> plans;
	
	public IServHtmlUtil(String plan) {
		this.plan = plan;
	}
	
	public List<Plan> toPlans() throws UnsupportedEncodingException, SAXException, IOException, ParserConfigurationException {
		prepareForParsing();
		Document document = parseToDocument();
		Element rootElement = (Element) document.getFirstChild();
		NodeList nodeList = rootElement.getElementsByTagName("font");
		readPlans(nodeList);
		return plans;
	}
	
	/**
	 * Remove unnecessary/unused/wrong tags.
	 */
	private void prepareForParsing() {
		// Remove the head section.
		StringBuilder builder = new StringBuilder();
		builder.append("<root>" + plan);
		int headStart = builder.indexOf("<head>");
		int headEnd = builder.indexOf("</head>") + 7;
		builder.delete(headStart, headEnd);
		builder.append("</root>");
		plan = builder.toString();
		
		// Remove all html/body/p/br/center tags.
		plan = plan.replaceAll("(</?html>)|(</?body>)|(</?p>)|(</?br>)|(</?CENTER>)", "");
	}
	
	private Document parseToDocument() throws UnsupportedEncodingException, SAXException, IOException, ParserConfigurationException {
		// Parse the data
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(new InputSource(new ByteArrayInputStream(plan.getBytes("utf-8"))));
	}
	
	private void readPlans(NodeList nodeList) {
		List<Plan> plans = new ArrayList<Plan>();
		int nodeLength = nodeList.getLength();
		for (int i = 1; i < nodeLength; i += 2) {
			// Get as of time.
			Node asOfTimeNode = nodeList.item(i - 1);
			String creationTime = getCreationTime(asOfTimeNode);
			
			// Get regarding date.
			Element currentElement = (Element) nodeList.item(i);
			String dateString = getDate(currentElement.getElementsByTagName("div").item(0));
			
			// Create Plan and set data.
			Plan plan = new Plan();
			Date creation = Date.fromStringDateTime(creationTime);
			plan.setCreated(creation);
			Date date = Date.fromStringDate(dateString);
			plan.setDate(date);
			plan.setQueried(new Date());
			transferSubstitutions(plan, currentElement.getElementsByTagName("table"));
			plans.add(plan);
		}
		this.plans = plans;
	}
	
	private String getCreationTime(Node node) {
		String rawCreationTime = node.getTextContent();
		return rawCreationTime.substring(7);
	}
	
	private String getDate(Node currentPlan) {
		String rawDate = currentPlan.getTextContent();
		String[] parts = rawDate.split(" ");
		String[] digits = parts[0].split("\\.");
		StringBuilder builder = new StringBuilder();
		if (digits[0].length() == 1) {
			builder.append(0);
		}
		builder.append(digits[0] + ".");
		if (digits[1].length() == 1) {
			builder.append(0);
		}
		builder.append(digits[1] + ".");
		builder.append(digits[2]);
		return builder.toString();
	}
	
	private void transferSubstitutions(Plan plan, NodeList currentPart) {
		Node currentPlan = currentPart.item(2);
		NodeList rows = ((Element) currentPlan).getElementsByTagName("tr");
		int rowCount = rows.getLength();
		for (int row = 2; row < rowCount; row++) {
			// Transfer each row of the table to a Substitution object.
			transferSubstitution(plan, rows.item(row));
		}
	}
	
	private void transferSubstitution(Plan plan, Node row) {
		NodeList cells = ((Element) row).getElementsByTagName("td");
		
		String compactClassName = cells.item(0).getTextContent();
		if (compactClassName != null && !compactClassName.matches("(SLR)|(Ltg)")) {
			String[] classNames = filterClassNames(compactClassName);
			compactClassName = classNames[0];
			for (int i = 1; i < classNames.length; i++) {
				compactClassName += "," + classNames[i];
			}
			String periodsString = cells.item(1).getTextContent();
			int[] periods = filterPeriods(periodsString);
			String substTeacher = cells.item(2).getTextContent();
			if (substTeacher.matches("(---)|\\?+|\\+"))
				substTeacher = "";
			String instdTeacher = cells.item(3).getTextContent();
			String instdSubject = cells.item(4).getTextContent();
			// Avoid occurrences of "Rel b" and similar.
			int firstPart = instdSubject.indexOf(" ");
			firstPart = firstPart == -1 ? instdSubject.length() : firstPart;
			instdSubject = instdSubject.substring(0, firstPart);
			String kind = removeNull(cells.item(5).getTextContent());
			if (kind.equals("Statt-Vertretung"))
				kind = "Vertretung";
			String text = removeNull(cells.item(7).getTextContent());
			text = text.replaceAll("regul.r", "regulär");
			Pattern taskPattern = Pattern.compile("Aufg(\\.|(abe)) [A-Za-z]{2,3}");
			Matcher taskMatcher = taskPattern.matcher(text);
			String taskProvider = "";
			while (taskMatcher.find()) {
				int end = taskMatcher.end();
				if (end != text.length() && text.charAt(end) != ' ')
					continue;
				int start = end - 3;
				taskProvider += "," + text.substring(start, end).trim();
			}
			
			Class currentClass = new Class();
			currentClass.baseUppon(compactClassName);
			List<Substitution> substitutions = plan.get(currentClass);
			if (substitutions == null) {
				substitutions = new ArrayList<Substitution>();
				plan.put(currentClass, substitutions);
			}
			for (int period : periods) {
				Substitution substitution = new Substitution();
				substitution.setPeriodNumber(period);
				substitution.setSubstTeacher(new Teacher(substTeacher));
				substitution.setInstdTeacher(new Teacher(instdTeacher));
				substitution.setInstdSubject(new Subject(instdSubject));
				substitution.setKind(kind);
				substitution.setText(text);
				substitution.setClassName(compactClassName);
				substitution.setTaskProvider(new Teacher(taskProvider));
				substitutions.add(substitution);
				LogUtil.d(compactClassName + "\t| " + period + "\t| " + substTeacher + "\t| " + instdTeacher + "\t| " + instdSubject + "\t| " + kind  + "\t| " + text  + "\t| " + taskProvider);
			}
		}
	}
	
	private String[] filterClassNames(String compactClassName) {
		List<String> classNames = new ArrayList<String>();
		compactClassName = filterClassNames(classNames, compactClassName, new String[]{"5", "6", "7", "8", "9", "10"}, "a?b?c?d?e?");
		compactClassName = filterClassNames(classNames, compactClassName, new String[]{"11", "12", "13"}, "g?n?s?");
		compactClassName = filterClassNames(classNames, compactClassName, new String[]{"E", "Q1", "Q2", "Q3", "Q4"}, "(Bi)?(Ch)?F?G?N?S?L?W?");
		if (!compactClassName.isEmpty()) {
			LogUtil.w("Class names not fully solved: " + compactClassName);
		}
		return classNames.toArray(new String[classNames.size()]);
	}
	
	private String filterClassNames(List<String> classNames, String compactClassName, String[] prefixes, String sufixes) {
		for (String prefix : prefixes) {

			Pattern pattern = Pattern.compile(prefix + sufixes);
			Matcher matcher = pattern.matcher(compactClassName);
			if (matcher.find()) {
				int start = matcher.start() + prefix.length();
				int end = matcher.end();
				String sufixQueue = compactClassName.substring(start, end);
				for (String sufix : sufixes.split("\\?")) {
					
					sufix = sufix.replaceAll("\\(|\\)", "");
					if (sufixQueue.contains(sufix)) {
						classNames.add(prefix + sufix);
					}
				}
				compactClassName = compactClassName.substring(end);
			}
		}
		return compactClassName;
	}
	
	private int[] filterPeriods(String periodsString) {
		String[] periodStrings = periodsString.split("-");
		int start = Integer.parseInt(periodStrings[0].trim());
		int end = start;
		if (periodStrings.length == 2) {
			end = Integer.parseInt(periodStrings[1].trim());
		}
		int[] periods = new int[1 + end - start];
		for (int i = start; i <= end; i++) {
			periods[i - start] = i;
		}
		return periods;
	}
	
	private String removeNull(String string) {
		if (string == null) {
			string = "";
		}
		return string;
	}
}
