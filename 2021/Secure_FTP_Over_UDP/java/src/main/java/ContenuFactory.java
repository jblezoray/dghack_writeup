import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContenuFactory {

    public static byte[] mkString(String... strings) {

        int globalSize = 0;
        for (String s : strings) {
            globalSize += s.getBytes(StandardCharsets.UTF_8).length + 2;
        }

        var output = new byte[globalSize];

        int i = 0;
        for (String s : strings) {
            var sBytes = s.getBytes(StandardCharsets.UTF_8);
            int size = sBytes.length;
            output[i++] = (byte) (size >> 8);
            output[i++] = (byte) size;
            for (int j=0; j<size; j++) {
                output[i++] = sBytes[j] ;
            }
        }
        return output;
    }
    //* Les chaînes de caractères et les tableaux 'simples' sont sérialisés de la forme suivante :
    //  - 2 octets contenant la longueur de la chaîne ou du tableau.
    //  - La chaine ou le tableau.
    public static List<String> readStrings(byte[] data) {
        var strings = new ArrayList<String>();
        int i = 0;
        while (i < data.length) {
            var stringSize = ((int)data[i] & 0xFF) <<8
                    | ((int)data[++i] & 0xFF);
            var stringBites = Arrays.copyOfRange(data, ++i, i+stringSize);
            i += stringSize;
            var string = new String(stringBites, StandardCharsets.UTF_8);
            strings.add(string);
        }
        return strings;
    }

    //* Un tableau de chaine de caractère est sérialisé de la forme suivante :
    //  * Le contenu du tableau est concaténé à la suite avec le caractère NULL (`\0`) comme délimiteur.
    //  * Le résultat de la concaténation est traîté comme une chaîne de caractère et est sérialisé comme détaillé plus haut.
    public static List<byte[]> readBytesArray(byte[] data) {
        var bytesArray = new ArrayList<byte[]>();
        int beginIndex = 0;
        for (int i=0; i<data.length; i++) {
            if (data[i] == 0x00) {
                var copy = Arrays.copyOfRange(data, beginIndex, i);
                bytesArray.add(copy);
                beginIndex = i+1;
            }
        }
        return bytesArray;
    }


}
