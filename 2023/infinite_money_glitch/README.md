# Infinite money glich

> Ce super site permet de devenir riche en regardant des vidéos !
> 
> Comme toutes les bonnes choses, il doit bien y avoir un moyen d'en abuser...

Le site lié, "money grabber" propose après création d'un compte de regarder une vidéo sur laquelle est indiqué un code.  Ce code, entré suite à la vidéo, augmente le crédit de 0.1€.  Au final, on pourra acheter le Flag pour 500€.  
5000 vidéos de 30 secondes à regarder, ca fait 41 heures.  C'est jouable à la main, mais on va faire autrement, hein :-)

J'ai fait un [petit script python](soft.py) à base de tesseract pour faire de la reconnaissance de caractères et ainsi valider les vidéos. En lancant plusieurs fois ce script en parallèle, on obtient ainsi assez facilement le flag. 

Ce que je n'ai pas vu, c'est que le nombre de vidéos était très limité, et qu'il était possible de faire un hash de chacune pour les retrouver dans une short list.
