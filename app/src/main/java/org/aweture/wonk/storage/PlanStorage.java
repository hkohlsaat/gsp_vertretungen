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
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlanStorage {

    private static final String FILENAME = "plan.json";

    public static void savePlan(Context context, String planJSON) {
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

    public static Plan readPlan(Context context, boolean student) throws IOException {
        synchronized (FILENAME) {
            FileInputStream inputStream = context.openFileInput(FILENAME);
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
            try {
                Plan plan = readPlan(reader);
                if (student) {
                    prepareForStudent(plan);
                } else {
                    prepareForTeacher(plan);
                }
                return plan;
            } finally {
                reader.close();
            }
        }
    }

    public static Plan.Part readPlanPart(Context context, int partIndex, boolean student) throws IOException {
        synchronized (FILENAME) {
            FileInputStream inputStream = context.openFileInput(FILENAME);
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
            try {
                Plan.Part part = onlyReadPart(reader, partIndex);
                if (student) {
                    prepareForStudent(part);
                } else {
                    prepareForTeacher(part);
                }
                return part;
            } finally {
                reader.close();
            }
        }
    }

    private static void prepareForStudent(Plan plan) {
        for (int i = 0; i < plan.parts.length; i++) {
            prepareForStudent(plan.parts[i]);
        }
    }

    private static void prepareForStudent(Plan.Part part) {
        Comparator<Substitution> comparator = new Substitution.ClassComparator();
        ArrayList<Substitution> subs = new ArrayList<Substitution>(part.substitutions.length);
        for (int i = 0; i < part.substitutions.length; i++) {
            Substitution s = part.substitutions[i];
            String[] classNames = decomposeClassNames(s.className);
            for (int j = 0; j < classNames.length; j++) {
                Substitution copy = s.copy();
                copy.className = classNames[j];
                subs.add(copy);
            }
        }
        part.substitutions = subs.toArray(new Substitution[subs.size()]);
        Arrays.sort(part.substitutions, comparator);
    }

    private static void prepareForTeacher(Plan plan) {
        for (int i = 0; i < plan.parts.length; i++) {
            prepareForTeacher(plan.parts[i]);
        }
    }

    private static void prepareForTeacher(Plan.Part part) {
        Comparator<Substitution> comparator = new Substitution.TeacherComparator();
        ArrayList<Substitution> subs = new ArrayList<Substitution>(part.substitutions.length);
        for (int i = 0; i < part.substitutions.length; i++) {
            Substitution s = part.substitutions[i];
            if (s.substTeacher.abbr.length() > 0) {
                subs.add(s);

                if (s.taskProvider.getName().length() > 0 && !s.substTeacher.abbr.equals(s.taskProvider.abbr)) {
                    s = s.copy();
                    s.modeTaskProvider = true;
                    subs.add(s);
                }
            }
        }
        part.substitutions = subs.toArray(new Substitution[subs.size()]);
        Arrays.sort(part.substitutions, comparator);
    }

    private static Plan readPlan(JsonReader reader) throws IOException {
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

    private static Plan.Part onlyReadPart(JsonReader reader, int partIndex) throws IOException {
        reader.beginObject();

        Plan.Part part = null;

        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("Parts")) {
                part = readPartsArray(reader, partIndex);
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();
        return part;
    }

    private static Plan.Part[] readPartsArray(JsonReader reader) throws IOException {
        reader.beginArray();

        ArrayList<Plan.Part> parts = new ArrayList<Plan.Part>();

        while (reader.hasNext()) {
            parts.add(readPart(reader));
        }

        reader.endArray();
        return (Plan.Part[]) parts.toArray(new Plan.Part[parts.size()]);
    }

    private static Plan.Part readPartsArray(JsonReader reader, int partIndex) throws IOException {
        reader.beginArray();

        Plan.Part part = null;
        int i = 0;

        while (reader.hasNext()) {
            if (partIndex == i) {
                part = readPart(reader);
            } else {
                reader.beginObject();
                reader.endObject();
            }
            i++;
        }

        reader.endArray();
        return part;
    }

    private static Plan.Part readPart(JsonReader reader) throws IOException {
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

    private static Substitution[] readSubstitutionsArray(JsonReader reader) throws IOException {
        reader.beginArray();

        ArrayList<Substitution> substitutions = new ArrayList<Substitution>();

        while (reader.hasNext()) {
            substitutions.add(readSubstitution(reader));
        }

        reader.endArray();
        return (Substitution[]) substitutions.toArray(new Substitution[substitutions.size()]);
    }

    private static Substitution readSubstitution(JsonReader reader) throws IOException {
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

        reader.endObject();
        return substitution;
    }

    private static Teacher readTeacher(JsonReader reader) throws IOException {
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

    private static Subject readSubject(JsonReader reader) throws IOException {
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

    private static String[] decomposeClassNames(String compactClassName) {
        List<String> classNames = new ArrayList<String>();
        compactClassName = decomposeClassNames(classNames, compactClassName, new String[]{"5", "6", "7", "8", "9", "10"}, "a?b?c?d?e?");
        compactClassName = decomposeClassNames(classNames, compactClassName, new String[]{"11", "12", "13"}, "g?n?s?");
        compactClassName = decomposeClassNames(classNames, compactClassName, new String[]{"E", "Q1", "Q2", "Q3", "Q4"}, "(Bi)?(Ch)?F?G?N?S?L?W?g?n?s?");
        if (!compactClassName.isEmpty()) {
            LogUtil.w("Class names not fully solved: " + compactClassName);
        }
        return classNames.toArray(new String[classNames.size()]);
    }

    private static String decomposeClassNames(List<String> classNames, String compactClassName, String[] prefixes, String sufixes) {
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
