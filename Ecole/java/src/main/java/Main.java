import java.io.IOException;

public class Main  {

  public static void main(String[] args) throws IOException {
    var fit = new EcoleFitnessNative();
    EcoleFactory fac = new EcoleFactory();

    Ecole e = fac.createRandomIndividual();

    double currentFit = 0.0;
    while (true) {
      var copy = e.copy();
      var copyFit = fit.computeFitnessOf(e);
      int counter = 0;
      do {
        copy.permutate();
        copyFit = fit.computeFitnessOf(copy);
      } while(copyFit < currentFit && counter++ < 10);

      if (copyFit > currentFit) {
        currentFit = copyFit;
        e = copy;
        System.out.println(currentFit + "("+counter+") --> " + e.toString());
      }
    }
  }

}
