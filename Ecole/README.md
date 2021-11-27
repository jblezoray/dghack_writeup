
# Ecole

> Dans une ecole il y a 90 élèves que l'on doit repartir en 3 classes. Les enfants ont fait un classement de leurs quatre meilleurs copains. Vous devez trouver une repartition maximisant le plaisir des enfants :
> 
> Si un enfant est avec son meilleur copain cela rapporte 20 points, 15 points pour le 2ème copain, 10 points pour le 3e et 5 pour le 4e. Les vœux des élèves sont mentionnés dans le fichier dghack2021-ecole-repartition.json du toolkit fourni.
> 
> Pour passer le challenge, vous devez avoir un score superieur ou égale à 2950.

Avec cela nous est fourni [un fichier](dghack2021-ecole-toolkit.tar.gz) avec un ensemble de fichiers de tests, et une implémentation de l'algo présenté dans l'intitulé, en python. 

C'est donc un problème d'optimisation, que nous pouvons probablement résoudre avec un simple [Hill Climb](https://fr.wikipedia.org/wiki/M%C3%A9thode_hill-climbing), ce que j'ai implémenté en java.  

Nous avons déjà une fonction de fitness sous la forme du programme en python.  J'ai tout d'abord tenté de faire un appel `ProcessBuilder.command()` à chaque appel fitness de l'algo, mais c'était vraiment trop lent.  Je l'ai finalement réimplémenté en Java.

```
public class Main  {
  
  public static void main(String[] args) throws IOException {
    //var fit = new EcoleFitnessCallPython();
    var fit = new EcoleFitnessNative();
    
    // commencons par créer une école avec une répartition aléatoire des élève 
    // dans les trois classes. 
    Ecole e = new EcoleFactory().createRandomIndividual();

    double currentFit = 0.0;
    while (true) {
      
      // on travaille sur une copie de notre école de référence.  
      var copy = e.copy();
      
      // On va améliorer ce score de fitness. 
      var copyFit = fit.computeFitnessOf(e);
      
      // On tente une mutation aléatoire que l'on évalue, si elle est 
      // meilleure on la garde. Sinon tente d'ajouter une nouvelle mutation, 
      // cela 10 fois de suite. Si on dépasse 10 tours sans améliorer le 
      // résultat, c'est que notre copie ne va pas dans le bon sens, et on 
      // revient à notre exemplaire initial.
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
```

En quelques secondes, nous avons notre résultat. Cela ne converge pas systématiquement vers un score supérieur à 2950, mais au bout de quelques lancements, nous avons enfin un score honorable de 3040: 

```
3040.0(0) --> [[64,22,41,66,10,4,84,36,53,54,14,33,32,8,2,83,87,67,70,77,80,50,12,16,20,60,78,29,73,38],[56,25,58,44,45,75,47,89,21,63,1,35,65,59,81,86,18,90,48,27,30,40,5,69,9,46,34,23,76,49],[51,13,24,57,7,61,11,26,43,71,15,28,52,6,19,31,55,74,3,62,37,85,79,17,88,42,39,72,82,68]]
```

Le code source complet est disponible dans le répertoire de ce readme. 
