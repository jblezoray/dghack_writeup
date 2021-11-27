import com.google.common.primitives.Bytes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import model.Packet;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

public class Main {

    @AllArgsConstructor
    @Getter
    private enum Message {
        RsaKeyMessage (78),
        PingMessage (10),
        AuthMessage (4444),
        GetFileMessage (666),
        GetFilesMessage (45),
        ErrorMessage (1),
        ConnectMessage (1921),
        SessionKeyMessage (1337),
        RsaKeyReply (98),
        ConnectReply (4875),
        PingReply (11),
        SessionKeyReply (1338),
        AuthReply (6789),
        GetFileReply (7331),
        GetFilesReply (46);
        private int id;
    }

    public static final String IDENTIFIANT = "GUEST_USER";
    public static final String MOT_DE_PASSE = "GUEST_PASSWORD";

    public static final int TIMEOUT = 2_000;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.setSecurityManager(null);

        var client = new Client("secure-ftp.dghack.fr", 4445);

        // ConnectMessage / ConnectReply
        //- Envoi d'un message `ConnectMessage`. Ce message doit avoir la chaîne de caractère
        //  `CONNECT` dans son attribut `data`.
        //- Le serveur répondra à ce message en envoyant une réponse `ConnectReply` contenant
        //  votre identifiant de session et le premier flag.
        String sessionID;
        {
            var connect = ContenuFactory.mkString("CONNECT");
            var connectMessage = PacketFactory.buildFrom(Message.ConnectMessage.getId(), connect);
            System.out.println("id=" + connectMessage.getId());
            System.out.println("contenu=" + ContenuFactory.readStrings(connectMessage.getContenu()));
            var connectReply = client.send(connectMessage, 4, TIMEOUT);
            var connectReplyContent = ContenuFactory.readStrings(connectReply.getContenu());
            sessionID = connectReplyContent.get(0);
            var flag = connectReplyContent.get(1);
            if (connectReply.getId() != Message.ConnectReply.getId()) {
                System.out.println("bad id=" + connectReply.getId());
            }
            System.out.println("sessionID=" + sessionID);
            System.out.println("flag=" + flag);
        }
        System.out.println("========================================================================");

        // RsaKeyMessage / RsaKeyReply
        //- Envoi d'un message `RsaKeyMessage` avec comme argument `sessionId` votre identifiant de session.
        //- Le serveur vous répondra avec la réponse `RsaKeyReply` contenant sa clé publique
        //  RSA (`servPubKey`). Cette clé est chiffrée avec l'algorithme XOR et la clé
        //  `ThisIsNotSoSecretPleaseChangeIt`, puis encodée en Base64.
        byte[] servPubKeyRsa;
        {
            var sessionIdBytes = ContenuFactory.mkString(sessionID);
            var rsaKeyMessage = PacketFactory.buildFrom(Message.RsaKeyMessage.getId(), sessionIdBytes);
            System.out.println("contenu=" + ContenuFactory.readStrings(rsaKeyMessage.getContenu()));

            var rsaKeyReply = client.send(rsaKeyMessage, 4, TIMEOUT);
            if (rsaKeyReply.getId() != Message.RsaKeyReply.getId()) {
                System.out.println("bad id=" + rsaKeyReply.getId());
            }
            var servPubKeyRsa_xor_base64 = ContenuFactory.readStrings(rsaKeyReply.getContenu()).get(0);
            var servPubKeyRsa_xor = Base64.getDecoder().decode(servPubKeyRsa_xor_base64);
            servPubKeyRsa = XorCoder.apply(servPubKeyRsa_xor, "ThisIsNotSoSecretPleaseChangeIt".getBytes(StandardCharsets.UTF_8));
            System.out.println("servPubKeyRsa=" + Hex.fromBytes(servPubKeyRsa) + " ("+servPubKeyRsa.length+" bytes)");
        }
        System.out.println("========================================================================");

        // SessionKeyMessage / SessionKeyReply
        //- Envoi d'un message `SessionKeyMessage`. Ce message contient votre identifiant de session et
        //une clé AES 256 bits (algorithme `AES/CBC/PKCS5Padding`) que vous avez générée. Cette clé
        //doit être chiffrée avec `servPubKey` et encodée en Base64.
        //- Le serveur vous répondra avec la réponse `SessionKeyReply`. Ce message contient un sel
        //de 10 octets sous forme d'un tableau d'octet chiffré avec votre clé AES encodé en Base64.
        byte[] iv = AES.generateIv();
        byte[] aeskey = AES.generateKey();
        byte[] salt;
        {
            var aeskey_rsa = RSA.encrypt(servPubKeyRsa, aeskey);
            var aeskey_rsa_base64 = Base64.getEncoder().encodeToString(aeskey_rsa);
            var payload = ContenuFactory.mkString(sessionID, aeskey_rsa_base64);
            var sessionKeyMessage = PacketFactory.buildFrom(Message.SessionKeyMessage.getId(), payload);
            System.out.println("contenu=" + ContenuFactory.readStrings(sessionKeyMessage.getContenu()));

            var sessionKeyReply = client.send(sessionKeyMessage, 4, TIMEOUT);
            if (sessionKeyReply.getId() != Message.SessionKeyReply.getId()) {
                System.out.println("bad id=" + sessionKeyReply.getId());
            }

            System.out.println("raw response=" + Hex.fromBytes(sessionKeyReply.getContenu()));
            var salt_aes_base64 = ContenuFactory.readStrings(sessionKeyReply.getContenu()).get(0);
            System.out.println("salt_aes_base64=" + salt_aes_base64);
            var salt_aes = Base64.getDecoder().decode(salt_aes_base64);
            System.out.println("salt_aes=" + Hex.fromBytes(salt_aes) + " (" + salt_aes.length + " bytes)");
            salt = AES.decrypt(aeskey, iv, salt_aes);
            System.out.println("salt=" + Hex.fromBytes(salt) + " (" + salt.length + " bytes)");
        }
        System.out.println("========================================================================");

        // AuthMessage / AuthReply
        //- Envoi d'un message `AuthMessage`. Ce message contient :
        //  - Votre identifiant de session ;
        //  - Le sel             chiffré avec la clé AES encodé en Base64 ;
        //  - Votre identifiant  chiffré avec la clé AES encodé en Base64 ;
        //  - Votre mot de passe chiffré avec la clé AES encodé en Base64.
        //- Le serveur répondra avec une réponse `AuthReply` contenant le message `AUTH_OK` si
        //l'authentification a réussi et le deuxième flag.
        //
        // Toutes les messages chiffrées en AES doivent commencer par le vecteur d'initialisation (IV) utilisé.
        {
            // aeskey = AES.generateKey(); // alter key --> results in "Invalid salt" --> key is valid.
            var salt_aes = AES.encrypt(aeskey, iv, salt);
            var salt_aes_base64 = Base64.getEncoder().encodeToString(salt_aes);
            var identifiant = IDENTIFIANT.getBytes(StandardCharsets.UTF_8);
            var identifiant_aes = AES.encrypt(aeskey, iv, identifiant);
            var identifiant_aes_base64 = Base64.getEncoder().encodeToString(Bytes.concat(iv, identifiant_aes));
            var password = MOT_DE_PASSE.getBytes(StandardCharsets.UTF_8);
            var password_aes = AES.encrypt(aeskey, iv, password);
            var password_aes_base64 = Base64.getEncoder().encodeToString(Bytes.concat(iv, password_aes));
            var payload = ContenuFactory.mkString(
                    sessionID,
                    salt_aes_base64,
                    identifiant_aes_base64,
                    password_aes_base64
            );
            var authMessage = PacketFactory.buildFrom(Message.AuthMessage.getId(), payload);
            System.out.println("authMessage=" + ContenuFactory.readStrings(authMessage.getContenu()));

            var authReply = client.send(authMessage, 4, TIMEOUT);
            if (authReply.getId() != Message.AuthReply.getId()) {
                System.out.println("bad id=" + authReply.getId());
                var status = ContenuFactory.readStrings(authReply.getContenu()).get(0);
                System.out.println("status=" + status);
            }
            var status = ContenuFactory.readStrings(authReply.getContenu()).get(0);

            System.out.println("status=" + status);
            var flag = ContenuFactory.readStrings(authReply.getContenu()).get(1);
            System.out.println("flag=" + flag);
        }

        System.out.println("========================================================================");

        // GetFilesMessage / GetFilesReply
        // * Envoi d'un message `GetFilesMessage` pour lister les fichiers d'un répertoire. Ce
        //   message contient votre identifiant de session et le chemin à lister chiffré avec
        //   votre clé AES et encodé en Base64.
        //    - Le serveur répondra avec la réponse `GetFilesReply` qui contient les noms
        //    des fichiers du répertoire sous forme d'un tableau de chaîne de caractère
        //    chiffré avec votre clé AES et encodé en Base64.
        String filename;
        {
            var chemin = "/opt/dga2021".getBytes(StandardCharsets.UTF_8);
            var chemin_aes = AES.encrypt(aeskey, iv, chemin);
            var chemin_aes_base64 = Base64.getEncoder().encodeToString(Bytes.concat(iv, chemin_aes));
            var payload = ContenuFactory.mkString(
                    sessionID,
                    chemin_aes_base64
            );
            var getFilesMessage = PacketFactory.buildFrom(Message.GetFilesMessage.getId(), payload);
            System.out.println("getFilesMessage=" + ContenuFactory.readStrings(getFilesMessage.getContenu()));

            var getFilesReply = client.send(getFilesMessage, 4, TIMEOUT);
            if (getFilesReply.getId() != Message.GetFilesReply.getId()) {
                System.out.println("bad id=" + getFilesReply.getId());
                var status = ContenuFactory.readStrings(getFilesReply.getContenu()).get(0);
                System.out.println("status=" + status);
            }
            var files_array_aes_base64 = ContenuFactory.readStrings(getFilesReply.getContenu()).get(0);
            var files_array_aes = Base64.getDecoder().decode(files_array_aes_base64);
            var files_array = AES.decrypt(aeskey, iv, files_array_aes);
            System.out.println("files_array=" + Hex.fromBytes(files_array));
            var files_array_no_iv = Arrays.copyOfRange(files_array, iv.length, files_array.length);
            var files = ContenuFactory.readBytesArray(files_array_no_iv);
            System.out.println("files=" + files + " ("+files.size()+" elements)");
            var filenames_no_iv = new ArrayList<String>();
            for (int i=0; i<files.size(); i++) {
                var filenameBytes = files.get(i);
                filenames_no_iv.add(new String(filenameBytes, StandardCharsets.UTF_8));
            }
            System.out.println("filenames_no_iv=" + filenames_no_iv);
            filename = filenames_no_iv.get(0);
            System.out.println("filename=" + filename);
        }

        System.out.println("========================================================================");

        // GetFileMessage / GetFileReply
        //  * Envoi d'un message `GetFileMessage` pour récupérer le contenu d'un fichier. Ce
        //    message contient votre identifiant de session et le chemin du fichier à récupérer
        //    chiffré avec votre clé AES et encodé en Base64.
        //    - Le serveur répondra avec la réponse `GetFileReply` qui contient le contenu
        //      du fichier chiffré avec votre clé AES et encodé en Base64.
        {
            var chemin = ("/opt/dga2021/"+filename).getBytes(StandardCharsets.UTF_8);
            var chemin_aes = AES.encrypt(aeskey, iv, chemin);
            var chemin_aes_base64 = Base64.getEncoder().encodeToString(Bytes.concat(iv, chemin_aes));
            var payload = ContenuFactory.mkString(
                    sessionID,
                    chemin_aes_base64
            );
            var getFileMessage = PacketFactory.buildFrom(Message.GetFileMessage.getId(), payload);
            System.out.println("getFileMessage=" + ContenuFactory.readStrings(getFileMessage.getContenu()));

            var getFileReply = client.send(getFileMessage, 4, TIMEOUT);
            if (getFileReply.getId() != Message.GetFileReply.getId()) {
                System.out.println("bad id=" + getFileReply.getId());
                var status = ContenuFactory.readStrings(getFileReply.getContenu()).get(0);
                System.out.println("status=" + status);
            }
            System.out.println("raw response=" + Hex.fromBytes(getFileReply.getContenu()));
            var filecontent_aes_base64 = ContenuFactory.readStrings(getFileReply.getContenu()).get(0);
            System.out.println("filecontent_aes_base64=" + filecontent_aes_base64);
            var filecontent_aes = Base64.getDecoder().decode(filecontent_aes_base64);
            System.out.println("filecontent_aes=" + Hex.fromBytes(filecontent_aes));
            var filecontent = AES.decrypt(aeskey, iv, filecontent_aes);
            System.out.println("filecontent=" + Hex.fromBytes(filecontent));
            System.out.println("filecontent=" + new String(filecontent, StandardCharsets.UTF_8));
        }

        System.out.println("Yeah this is fine...");
    }
}
