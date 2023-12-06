package model;

import lombok.Data;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@Data
public class Packet {

    /**
     * L'entête est composé de deux octets.
     *
     * Les 14 bits les plus significatifs correspondent à l'ID du paquet.
     * Les 2 bits les moins significatifs correspondent à la taille en octet de la section `taille`.
     */
    private byte entete[];

    /**
     * La section taille a une longueur variable de 1 à 3 octets.
     * Elle définit la longueur de la section `contenu` en octets.
     */
    private byte taille[];

    /**
     * Le contenu du paquet. Celui-ci est spécifique à chaque paquet.
     */
    private byte contenu[];

    /**
     * Somme de contrôle stockée sur 4 octets permettant de détecter une
     * éventuelle modification du paquet pendant le transit.
     * L'algorithme utilisé est le même que celui présent dans Java
     * (`java.util.zip.CRC32`).
     * La somme est calculée avec la concaténation des sections entête,
     * taille et contenu.
     */
    private byte crc32[];

    public byte[] getBytes() {
        var bb = ByteBuffer.allocate(entete.length + taille.length + contenu.length + crc32.length);
        bb.put(entete);
        bb.put(taille);
        bb.put(contenu);
        bb.put(crc32);
        return bb.array();
    }

    public int getId() {
        return (
                (((int)entete[0]&0xFF) << 8) |
                ((int)entete[1]&0xFF)
        ) >> 2;
    }

    public String getContenuString() {
        return new String(this.contenu, Charset.forName("UTF-8"));
    }
}
