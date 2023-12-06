# Cryptoneat


> Une information tres secrète est dissimulée dans cette page web.
> 
> Saurez-vous la trouver ?

Avec un lien vers [cette page](page.html).

Je ne suis pas trop crypto, mais j'ai tenté.  Et je me suis cassé les dents :-) J'étais pourtant sur la bonne piste.

On a donc deux chiffrés dans la page, ainsi que des fonctions de chiffrement / déchiffrement.  Chaque message est la concaténation d'un IV et d'un chiffré. 

```
const encryptedMsg = '34aff6de8f8c01b25c56c52261e49cbddQsBGjy+uKhZ7z3+zPhswKWQHMYJpz7wffAe4Es/bwrJmMo99Kv7XJ8P63TbN/8XvvLH8F1NwLyPnJ4q044jQ9+zgWPoOkYW0McgFdNDaZnfdqgHEsa+b8FfzTS2ECa5cs9rlri61ybC+SMIA7aPJXgj6TGBMFN0OySQRmA3Nc38cN.............................................................OHFzqkuluVfUkKOq3FS13AbmDLHD6I39l5DUMSodKagH5Ivn+X6IT5eL1u46hHG/asZnuDWHtSGBRDu5OeBRpVBZqQQZwwbK4DwlfV3cuCyapfTuWjWOzMKUv8FK8SKbBpt6120HA0/SQBvbLUSaWtPZEIfhE1LTNCqJiMUFaXg=';
const encryptedMsg2 = '34aff6de8f8c01b25c56c52261e49cbdC19FW3jqqqxd6G/z0fcpnOSIBsUSvD+jZ7E9/VkscwDMrdk9i9efIvJw1Fj6Fs0R';
``` 


Le chiffrement est un AES en mode CTR. 
``` 
  var encrypted = CryptoJS.AES.encrypt(msg, key, {
    iv: iv,
    padding: CryptoJS.pad.Pkcs7,
    mode: CryptoJS.mode.CTR,
  });
``` 

Or, [wikipedia](https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation#CTR) nous apprend que le mode CTR interdit la réutilisation du IV. Ici, les deux messages partagent le même IV `34aff6de8f8c01b25c56c52261e49cbd`.  En XORant les deux chiffrés, nous pouvons donc annuler l'effet du chiffrement par bloc et obtenir un XOR des deux clairs:

```
AES(IV + cpt, Kpri) XOR Plaintext = Ciphertext
==> AES(IV + cpt, Kpri) = Plaintext XOR Ciphertext
==> Plaintext1 XOR Ciphertext1 = Plaintext2 XOR Ciphertext2
==> Plaintext1 XOR Plaintext2 = Ciphertext1 XOR Ciphertext2
```

Donc si on connait un des deux clairs, on connaitra le second également. Il nous faut donc un des deux clairs.

C'est à partir de ce moment que je suis parti sur une mauvaise piste.  Dans le JS on a un `document.write(plainHTML);` qui est fait dès que le mot de passe est entré, pour afficher le résultat. J'ai donc pensé qu'il pouvait s'agir de HTML, et j'ai essayé beaucoup de combinaisons de `<!DOCTYPE html>` et de code HTML plus ou moins valide, plus ou moins récent, avec des combinaisons de majuscules, minuscules, etc, sans réussir. 

Or, semble t'il, il fallait se baser sur la chaine suivante, incluse dans la page, pour obtenir un clair: 
```
exports.cryptoThanks = "Build with love, kitties and flowers";
```

Donc fail. 