import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

public class EcoleFitnessCallPython  {

    public double computeFitnessOf(Ecole candidateToEvaluate) {

        var json = candidateToEvaluate.toString();

        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("ecole", "json");
            Files.write(tmpFile.toPath(), json.getBytes());

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.redirectErrorStream(true);
            processBuilder.command(
                    "/opt/local/bin/python3.8",
                    "score.py",
                    "-i", "dghack2021-ecole-repartition.json",
                    "-c", tmpFile.getAbsolutePath()
            );

            Process process = processBuilder.start();
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuilder output = new StringBuilder();
            String firstLine = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if (firstLine==null) {
                    firstLine = line;
                }
                output.append(line + "\n");
            }
            if (firstLine==null || !firstLine.startsWith("score total :")) {
                throw new RuntimeException("Error = " + output.toString());
            }

            Integer score = Integer.parseInt(firstLine.substring("score total :".length()).trim());
            return score.doubleValue();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);

        } finally {
            if (tmpFile!=null) {
                tmpFile.delete();
            }
        }
    }
}
