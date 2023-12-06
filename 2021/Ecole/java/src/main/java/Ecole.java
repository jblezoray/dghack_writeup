import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ecole {
    private List<Integer> dnaList;
    private static final Random RANDOM = new Random(0);

    public Ecole(List<Integer> dnaList) {
        this.dnaList = dnaList;
    }

    public List<Integer> getDnaList() {
        return dnaList;
    }

    public Ecole copy() {
        var dnaListCopy = new ArrayList<Integer>(this.dnaList.size());
        dnaListCopy.addAll(this.dnaList);
        return new Ecole(dnaListCopy);
    }

    public void permutate() {
        int a = RANDOM.nextInt(90);
        int b = RANDOM.nextInt(90);
        var aVal = this.dnaList.get(a);
        var bVal = this.dnaList.get(b);
        this.dnaList.set(b, aVal);
        this.dnaList.set(a, bVal);
    }



    @Override
    public String toString() {
        var classes = new JSONArray();
        for (int i=0; i<3; i++) {
            var classe = new JSONArray();
            for (int p = i*30; p < i*30 + 30; p++) {
                int eleve = this.dnaList.get(p);
                classe.put(eleve);
            }
            classes.put(classe);
        }
        var json = classes.toString();
        return json;
    }

}
