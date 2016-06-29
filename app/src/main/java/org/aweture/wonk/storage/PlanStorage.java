package org.aweture.wonk.storage;

import android.content.Context;
import android.util.JsonReader;

import org.aweture.wonk.log.LogUtil;
import org.aweture.wonk.models.Date;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.models.Subject;
import org.aweture.wonk.models.Substitution;
import org.aweture.wonk.models.Teacher;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlanStorage {

    private static final String FILENAME = "plan.json";

    private Context context;

    public PlanStorage(Context context) {
        this.context = context;
    }

    public void savePlan(String planJSON) {
        synchronized (FILENAME) {
            try {
                FileOutputStream outputStream = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
                outputStream.write(planJSON.getBytes("UTF-8"));
                outputStream.close();
            } catch (Exception e) {
                LogUtil.e(e);
            }
        }
    }

    public Plan readPlan() throws IOException {
        synchronized (FILENAME) {
            FileInputStream inputStream = context.openFileInput(FILENAME);
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
            try {
                Plan plan = readPlan(reader);
                for (Plan.Part part : plan.parts) {
                    Arrays.sort(part.substitutions);
                }
                return plan;
            } finally {
                reader.close();
            }
        }
    }

    private Plan readPlan(JsonReader reader) throws IOException {
        reader.beginObject();

        Plan plan = new Plan();

        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("Created")) {
                String created = reader.nextString();
                plan.created = Date.fromStringTimestamp(created);
            } else if (name.equals("Parts")) {
                plan.parts = readPartsArray(reader);
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();
        return plan;
    }

    private Plan.Part[] readPartsArray(JsonReader reader) throws IOException {
        reader.beginArray();

        ArrayList<Plan.Part> parts = new ArrayList<Plan.Part>();

        while (reader.hasNext()) {
            parts.add(readPart(reader));
        }

        reader.endArray();
        return (Plan.Part[]) parts.toArray(new Plan.Part[parts.size()]);
    }

    private Plan.Part readPart(JsonReader reader) throws IOException {
        reader.beginObject();

        Plan.Part part = new Plan.Part();

        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("Day")) {
                String day = reader.nextString();
                part.day = Date.fromStringTimestamp(day);
            } else if (name.equals("Substitutions")) {
                part.substitutions = readSubstitutionsArray(reader);
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();
        return part;
    }

    private Substitution[] readSubstitutionsArray(JsonReader reader) throws IOException {
        reader.beginArray();

        ArrayList<Substitution> substitutions = new ArrayList<Substitution>();

        while (reader.hasNext()) {
            Substitution[] s = readSubstitution(reader);
            for (int i = 0; i < s.length; i++) {
                substitutions.add(s[i]);
            }
        }

        reader.endArray();
        return (Substitution[]) substitutions.toArray(new Substitution[substitutions.size()]);
    }

    private Substitution[] readSubstitution(JsonReader reader) throws IOException {
        reader.beginObject();

        Substitution substitution = new Substitution();

        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("Period")) {
                substitution.period = reader.nextString();
            } else if (name.equals("Class")) {
                substitution.className = reader.nextString();
            } else if (name.equals("SubstTeacher")) {
                substitution.substTeacher = readTeacher(reader);
            } else if (name.equals("InstdTeacher")) {
                substitution.instdTeacher = readTeacher(reader);
            } else if (name.equals("InstdSubject")) {
                substitution.instdSubject = readSubject(reader);
            } else if (name.equals("Kind")) {
                substitution.kind = reader.nextString();
            } else if (name.equals("Text")) {
                substitution.text = reader.nextString();
            } else if (name.equals("TaskProvider")) {
                substitution.taskProvider = readTeacher(reader);
            } else {
                reader.skipValue();
            }
        }

        String[] classNames = decomposeClassNames(substitution.className);
        Substitution[] substitutions = new Substitution[classNames.length];
        for (int i = 0; i < substitutions.length; i++) {
            Substitution s = substitution.copy();
            s.className = classNames[i];
            substitutions[i] = s;
        }

        reader.endObject();
        return substitutions;
    }

    private Teacher readTeacher(JsonReader reader) throws IOException {
        reader.beginObject();

        String teacherAbbr = "";
        String teacherName = "";
        String teacherSex = "";

        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("Short")) {
                teacherAbbr = reader.nextString();
            } else if (name.equals("Name")) {
                teacherName = reader.nextString();
            } else if (name.equals("Sex")) {
                teacherSex = reader.nextString();
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();
        return new Teacher(teacherAbbr, teacherName, teacherSex);
    }

    private Subject readSubject(JsonReader reader) throws IOException {
        reader.beginObject();

        String subjectAbbr = "";
        String subjectName = "";
        boolean splitClass = false;

        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("Short")) {
                subjectAbbr = reader.nextString();
            } else if (name.equals("Name")) {
                subjectName = reader.nextString();
            } else if (name.equals("SplitClass")) {
                splitClass = reader.nextBoolean();
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();
        return new Subject(subjectAbbr, subjectName, splitClass);
    }

    private String[] decomposeClassNames(String compactClassName) {
        List<String> classNames = new ArrayList<String>();
        compactClassName = decomposeClassNames(classNames, compactClassName, new String[]{"5", "6", "7", "8", "9", "10"}, "a?b?c?d?e?");
        compactClassName = decomposeClassNames(classNames, compactClassName, new String[]{"11", "12", "13"}, "g?n?s?");
        compactClassName = decomposeClassNames(classNames, compactClassName, new String[]{"E", "Q1", "Q2", "Q3", "Q4"}, "(Bi)?(Ch)?F?G?N?S?L?W?g?n?s?");
        if (!compactClassName.isEmpty()) {
            LogUtil.w("Class names not fully solved: " + compactClassName);
        }
        return classNames.toArray(new String[classNames.size()]);
    }

    private String decomposeClassNames(List<String> classNames, String compactClassName, String[] prefixes, String sufixes) {
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
}
