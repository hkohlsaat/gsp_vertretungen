package org.aweture.wonk.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aweture.wonk.models.Class;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.models.Substitution;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

public class SubstitutionsStore {
	public final String LOG_TAG = this.getClass().getSimpleName();
	
	private final String FILE_NAME = "substitutions.xml";

	private final String TAG_PLAN = "plan";
	private final String TAG_CLASS = "class";
	private final String TAG_SUBSTITUTION = "substitution";
	
	private final String ATTRIBUTE_DATE = "date";
	private final String ATTRIBUTE_NAME = "name";
	private final String ATTRIBUTE_PERIOD = "period";
	private final String ATTRIBUTE_INSTD_TEACHER = "instdTeacher";
	private final String ATTRIBUTE_INSTD_SUBJECT = "instdSubject";
	private final String ATTRIBUTE_SUBST_TEACHER = "substTeacher";
	private final String ATTRIBUTE_KIND = "kind";
	
	private static SubstitutionsStore instance;
	
	private Context context;
	private Plan[] plans;
	
	public static SubstitutionsStore getInstance(Context context) {
		if (instance == null) {
			instance = new SubstitutionsStore(context);
		}
		return instance;
	}
	
	private SubstitutionsStore(Context context) {
		this.context = context;
	}
	
	public synchronized Plan[] getCurrentPlans() {
		if (plans == null) {
			plans = readPlans();
		}
		return plans;
	}
	
	public synchronized Plan getPlanByDate(String date) {
		for (Plan plan : plans) {
			if (plan.getDate().toString().equals(date)) {
				return plan;
			}
		}
		String message = "There is no date = \"" + date + "\" in the available plans.";
		throw new IllegalArgumentException(message);
	}
	
	public synchronized void savePlans(Plan[] plans) {
		this.plans = plans;
		if (plans != null) {
			try (OutputStream outputStream = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {

		        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
		        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
				xml += plansToXml(plans);
				Log.d(LOG_TAG, xml);
				outputStreamWriter.write(xml);
				outputStreamWriter.close();
			} catch (IOException e) {
				Log.e(LOG_TAG, Log.getStackTraceString(e));
			}
		}
	}

	private Plan[] readPlans() {
		Plan[] plans = new Plan[0];
		try (InputStream inputStream = context.openFileInput(FILE_NAME)) {
			XmlPullParser parser = Xml.newPullParser();
	        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
	        parser.setInput(inputStream, null);
	        // Move to root element.
	        parser.nextTag();
	        
	        List<Plan> planList = new ArrayList<Plan>();
	        
	        while (parser.nextTag() == XmlPullParser.START_TAG){
	        	Plan plan = readPlan(parser);
	        	planList.add(plan);
	        }
	        plans = planList.toArray(new Plan[planList.size()]);
		} catch (IOException | XmlPullParserException e) {
			Log.e(LOG_TAG, Log.getStackTraceString(e));
		}
		return plans;
	}

	private Plan readPlan(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, null, TAG_PLAN);
		String date = parser.getAttributeValue(null, ATTRIBUTE_DATE);
		Plan plan = new Plan();
		plan.setDate(date);
		
		while (parser.nextTag() == XmlPullParser.START_TAG) {
			// Read for each class.
			fillPlan(plan, parser);
		}
		return plan;
	}

	private void fillPlan(Plan plan, XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, null, TAG_CLASS);
		String className = parser.getAttributeValue(null, ATTRIBUTE_NAME);
		Class currentClass = new Class();
		currentClass.setName(className);
		while(parser.nextTag() == XmlPullParser.START_TAG) {
			// Read each substitution of currentClass.
			Substitution substituion = readSubstitution(parser);
			List<Substitution> substitutions = plan.get(currentClass);
			if (substitutions == null) {
				substitutions = new ArrayList<Substitution>();
				plan.put(currentClass, substitutions);
			}
			substitutions.add(substituion);
		}
	}
	
	private Substitution readSubstitution(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, null, TAG_SUBSTITUTION);
		String period = parser.getAttributeValue(null, ATTRIBUTE_PERIOD);
		String instdTeacher = parser.getAttributeValue(null, ATTRIBUTE_INSTD_TEACHER);
		String instdSubject = parser.getAttributeValue(null, ATTRIBUTE_INSTD_SUBJECT);
		String substTeacher = parser.getAttributeValue(null, ATTRIBUTE_SUBST_TEACHER);
		String kind = parser.getAttributeValue(null, ATTRIBUTE_KIND);
		String text = parser.nextText().trim();
		
		Substitution substitution = new Substitution();
		substitution.setPeriodNumber(Integer.parseInt(period));
		substitution.setInstdTeacher(instdTeacher);
		substitution.setInstdSubject(instdSubject);
		substitution.setSubstTeacher(substTeacher);
		substitution.setKind(kind);
		substitution.setText(text);
		return substitution;
	}
	
	private String plansToXml(Plan[] plans) {
		StringBuilder builder = new StringBuilder();
		builder.append("<root>\n");
		for (Plan plan : plans) {
			builder.append(planToXml(plan));
		}
		builder.append("</root>");
		return builder.toString();
	}

	private String planToXml(Plan plan) {
		StringBuilder builder = new StringBuilder();
		String date = plan.getDate().toString();
		builder.append("<" + TAG_PLAN + attr(ATTRIBUTE_DATE, date) + ">\n");
		Set<Class> classes = plan.keySet();
		for (Class currentClass : classes) {
			List<Substitution> substitutions = plan.get(currentClass);
			builder.append(classesSubstitutionsToXml(currentClass, substitutions));
		}
		builder.append("</" + TAG_PLAN + ">\n");
		return builder.toString();
	}
	
	private String classesSubstitutionsToXml(Class currentClass, List<Substitution> substitutions) {
		StringBuilder builder = new StringBuilder();
		String className = currentClass.getName();
		builder.append("<" + TAG_CLASS + attr(ATTRIBUTE_NAME, className) + ">\n");
		for (Substitution substitution : substitutions) {
			builder.append(substitutionToXml(substitution));
		}
		builder.append("</" + TAG_CLASS + ">\n");
		return builder.toString();
	}

	private String substitutionToXml(Substitution substitution) {
		StringBuilder builder = new StringBuilder();
		String period = Integer.toString(substitution.getPeriodNumber());
		String instdTeacher = substitution.getInstdTeacher();
		String instdSubject = substitution.getInstdSubject();
		String substTeacher = substitution.getSubstTeacher();
		String kind = substitution.getKind();
		String text = substitution.getText();
		builder.append("<" + TAG_SUBSTITUTION);
		builder.append(attr(ATTRIBUTE_PERIOD, period));
		builder.append(attr(ATTRIBUTE_INSTD_TEACHER, instdTeacher));
		builder.append(attr(ATTRIBUTE_INSTD_SUBJECT, instdSubject));
		builder.append(attr(ATTRIBUTE_SUBST_TEACHER, substTeacher));
		builder.append(attr(ATTRIBUTE_KIND, kind) + ">");
		builder.append(text);
		builder.append("</" + TAG_SUBSTITUTION + ">\n");
		return builder.toString();
	}

	private String attr(String attrName, String attrValue) {
		return " " + attrName + "=\"" + attrValue + "\"";
	}
}
