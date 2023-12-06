import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EcoleFactory {

    public Ecole createRandomIndividual() {
        List<Integer> values = IntStream.rangeClosed(1, 90).boxed().collect(Collectors.toList());
        Collections.shuffle(values);
        return new Ecole(values);
    }

    public Ecole createFrom(int[][] ecole) {
        List<Integer> values = new ArrayList<>();

        for (int i=0; i<3; i++) {
            for (int j=0; j<30; j++) {
                values.add(ecole[i][j]);
            }
        }

        return new Ecole(values);
    }

}