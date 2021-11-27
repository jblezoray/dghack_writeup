import model.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.zip.CRC32;

public class PacketFactory {

    public static Packet buildFrom(int id, byte[] contenu) {
        Packet p = new Packet();

        // contenu
        p.setContenu(contenu);

        // taille
        byte[] taille;
        if (contenu.length<=0xFF) {
            taille = new byte[1];
            taille[0] = (byte) contenu.length;
        } else if (contenu.length<=0xFFFF) {
            taille = new byte[2];
            taille[0] = (byte) (contenu.length >> 8);
            taille[1] = (byte) contenu.length;
        } else if (contenu.length<=0xFFFFFF) {
            taille = new byte[3];
            taille[0] = (byte) (contenu.length >> 16);
            taille[1] = (byte) (contenu.length >> 8);
            taille[2] = (byte) contenu.length;
        } else {
            throw new RuntimeException("taille du packet non représentable sur 3 octets.");
        }
        p.setTaille(taille);

        // entete
        int enteteInt = (id << 2) | (taille.length & 0b00000011);
        byte[] entete = new byte[2];
        entete[0] = (byte) (enteteInt >> 8);
        entete[1] = (byte) (enteteInt);
        p.setEntete(entete);

        // crc
        var crcCalculator = new CRC32();
        crcCalculator.update(entete);
        crcCalculator.update(taille);
        crcCalculator.update(contenu);
        long result = crcCalculator.getValue();
        byte[] crc = new byte[4];
        crc[0] = (byte) (result >> 24);
        crc[1] = (byte) (result >> 16);
        crc[2] = (byte) (result >> 8);
        crc[3] = (byte) (result);
        p.setCrc32(crc);

        return p;
    }

    public static Packet parse(byte[] bytes) throws InvalidCrcException {
        var bb = ByteBuffer.wrap(bytes);

        // entete
        var enteteb = new byte[] {bb.get(), bb.get()};
        int tailleTaille = enteteb[1] & 0b00000011;

        // taille
        int taille=0;
        byte[] tailleb = new byte[tailleTaille];
        for(int i=0; i<tailleTaille; i++) {
            // read value as bytes
            tailleb[i] = bb.get();
            // read value as int
            taille = taille | ((int)tailleb[i]&0xFF) << ((tailleTaille-i-1)*8);
        }

        // contenu
        byte[] contenu = new byte[taille];
        for (int i=0; i<taille; i++) {
            contenu[i] = bb.get();
        }

        // crc
        byte[] crcBytes = new byte[4];
        crcBytes[0] = bb.get();
        crcBytes[1] = bb.get();
        crcBytes[2] = bb.get();
        crcBytes[3] = bb.get();
        var crcCalculator = new CRC32();
        crcCalculator.update(enteteb);
        crcCalculator.update(tailleb);
        crcCalculator.update(contenu);
        long result = crcCalculator.getValue();
        byte[] resultByteArray = new byte[4];
        resultByteArray[0] = (byte) (result >> 24);
        resultByteArray[1] = (byte) (result >> 16);
        resultByteArray[2] = (byte) (result >> 8);
        resultByteArray[3] = (byte) (result);
        if (!Hex.fromBytes(resultByteArray).equalsIgnoreCase(Hex.fromBytes(crcBytes))) {
            throw new InvalidCrcException(crcBytes, resultByteArray);
        }

        Packet p = new Packet();
        p.setEntete(enteteb);
        p.setTaille(tailleb);
        p.setContenu(contenu);
        p.setCrc32(crcBytes);
        return p;
    }

    public static class InvalidCrcException extends Exception {
        public InvalidCrcException(byte[] crc, byte[] calculated) {
            super("Invalid : " + Hex.fromBytes(crc) + " (calculated: "+Hex.fromBytes(calculated)+")");
        }
    }

}
