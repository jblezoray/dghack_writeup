# aegissecureforge 

> La campagne de crowdfunding pour les robots AEgisSecureForge s'est terminée dans une grande déception. Les robots que nous avons finalement reçus, avec un an de retard, ne correspondent en rien à ce qui nous avait été promis. Il y a quelques jours, l'entreprise a réuni un groupe d'influenceurs pour dévoiler ses projets futurs, et il est devenu clair qu'ils prévoient de basculer vers un modèle B2B. Malheureusement, cela signifie la fin définitive du support pour nos robots.
> 
> Avant la réunion, j'avais déjà des doutes quant à leurs intentions. Les prétendus problèmes de stabilité des serveurs ne laissaient rien présager de bon. Même s'ils ont tenté de nous rassurer, j'ai eu l'impression d'entendre une conversation entre deux développeurs dans la salle de pause, évoquant une mise à jour visant à rendre inopérants les robots de génération précédente, probablement les nôtres. J'ai profité de l'occasion pour effectuer une capture réseau dans leurs locaux. Bien que je n'aie pas encore trouvé la dernière version du firmware qu'ils mentionnaient, je garde l'espoir de la récupérer.
> 
> Cependant, je ne peux pas y parvenir seul. C'est pourquoi je fais appel à vos compétences pour m'aider à récupérer le firmware, en utilisant la capture réseau que je mets à votre disposition ici. J'espère que nous pourrons trouver des preuves de cette manœuvre pour les diffuser publiquement, afin d'éviter à un maximum d'utilisateurs de voir leurs robots devenir inutilisables.

Le fichier joint est un `aegis.pcapng`.

# step 1

On analyse la capture dans Wireshark, et on trouve quelques trames HTTP en clair, vers un site `http://valence.mikeofp.free.fr`. 

```
POST /?960f29ac832bfe36 HTTP/1.1
[Full request URI: http://valence.mikeofp.free.fr/?960f29ac832bfe36]
X-Original-Url: http://valence.mikeofp.free.fr/?960f29ac832bfe36#2VPy/R1GCQubXa36xPmxB5TR7HfTchPQF9Wz+P+9Xqs=
R1GCQubXa36xPmxB5TR7HfTchPQF9WzP9Xqs
```

Il s'agit d'un site proposant du chiffrement et du partage de texte dans le navigateur, sur la base d'un mot de passe. 

Le lien est mort, mais nous pouvons reconstituer la page avec le payload depuis le dump. En lancant une version en local avec les bons parametres GET, on retrouve la page: 
```
file:///home/user/capture_privatebin/capture_msg_1448.html?960f29ac832bfe36#2VPy/R1GCQubXa36xPmxB5TR7HfTchPQF9Wz+P+9Xqs=
```

Il ne nous manque plus que le mot de passe ... qui se trouve être le nom du challenge `AEgisSecureForge`.  Et voici notre flag `DGHACK{ThisCodeSmellGood,No?}`.

## step 2

Le [contenu du message](message.txt) semble être le code d'un serveur `server.py`, qui répond à des commandes bas niveau.  On note surtout ce passage qui définit notre objectif: 

```
def _handle_healthcheck(self, check: bytes):
    res = b"HEALTH_OK"
    if check == b"\x2a":
        res = open("FLAG", "rb").read()

    logger.debug(f"|>({self.peername}) HEALTHCHECK OK")
    self.transport.write(self.encode_message_length(res))
    self.transport.write(res)
    self.transport.close()
```

Reste à forger et envoyer la bonne commande au serveur que nous avons trouvé dans la capture réseau.  Après un peu de rétroingénieurie du code, cela nous donne: 

```
> 4c041440010d0a
0000   4c 04 14 40 01 0d 0a                              L..@...
       4c 04                MAGIC_NUMBER
             14             COMMAND  PROTOCOL_CMD_GET_LATEST
                40 01       pkt_size 1 
                      0d 0a data

└─$ python send.py
received data: b"'\x00\x00\x00DGHACK{SeemsLike.YoureOnTheRightTrack!}"
```

