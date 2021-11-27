import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class EcoleFitnessNative {

    private List<Integer[]> student;
    private static final String JSON_RESOURCE = "dghack2021-ecole-repartition.json";

    public EcoleFitnessNative() {
        setStudents(loadResource(JSON_RESOURCE));
    }

    private void setStudents(String jsonStr) {
        this.student = new ArrayList<>(90);
        // 90 students + the 0th one that stays null for ever.
        for (int i=0; i<=90; i++) {
            this.student.add(i, null);
        }
        var mainArray = new JSONArray(jsonStr);
        this.student.set(0, null);
        for (var subObj : mainArray) {
            Integer[] friends = new Integer[4];
            Integer idx = ((JSONObject)subObj).getInt("idx");
            JSONArray fri = ((JSONObject)subObj).getJSONArray("friends");
            for (int j=0; j<4; j++) {
                friends[j] = fri.getInt(j);
            }
            this.student.set(idx, friends);
        }
    }

    private String loadResource(String resourceName) {
        var classLoader = getClass().getClassLoader();
        var textBuilder = new StringBuilder();
        try (var resourceAsStream = classLoader.getResourceAsStream(resourceName);
             var streamReader = new InputStreamReader(resourceAsStream);
             var bufferedReader = new BufferedReader(streamReader)) {
            int c = 0;
            while ((c = bufferedReader.read()) != -1) {
                textBuilder.append((char) c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return textBuilder.toString();
    }

    public double computeFitnessOf(Ecole e) {
        int score = 0;
        for (var i=1; i<=90; i++) {
            var studentClass = getClassOf(i, e);
            var studentsFriends = this.student.get(i);

            if (studentClass == getClassOf(studentsFriends[0], e)) {
                score += 20;
            }
            if (studentClass == getClassOf(studentsFriends[1], e)) {
                score += 15;
            }
            if (studentClass == getClassOf(studentsFriends[2], e)) {
                score += 10;
            }
            if (studentClass == getClassOf(studentsFriends[3], e)) {
                score += 5;
            }
        }

        return score;
    }

    private int getClassOf(int student, Ecole e) {
        List<Integer> school = e.getDnaList();
        var idx = school.indexOf(student);
        return (idx<30) ? 0 : (idx<60) ? 1 : 2;
    }
}
