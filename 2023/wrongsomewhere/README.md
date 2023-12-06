

# wrongsomewhere

> Un nouveau ransomware se propage sur internet.
> 
> Trop de vieilles dames se font arnaquer par celui-ci, il est temps d'agir !
> 
> Une des victimes nous a accordé l'accès à distance à sa machine, veuillez enquêter et trouver la clé pour déchiffrer les fichiers.

Avec ce petit warning pour rappeller que certains ont chiffré leur PCs les années passées: 
> Attention à ne pas lancer le ransomware sur une machine autre que celle fournie.

On a un fichier lié `wrongsomewhere.exe`. 

En le passant dans Ghidra, on voit des opérations sur le registre: 

```
local_c = RegOpenKeyExA((HKEY)0xffffffff80000001,&stda,0,0x20019,&local_18);
...
local_c = RegQueryValueExA(local_18,"error",(LPDWORD)0x0,&local_1c,local_128,&local_12c);
```

La doc de `RegQueryValueExA` qui est [sur le site de MS](https://learn.microsoft.com/en-us/windows/win32/api/winreg/nf-winreg-regqueryvalueexa) nous apprend que le 2nd parametre `error` correspond à une entrée du registre.  

Le code lit cette valeur, et s'il ne la trouve pas, la crée dans une fonction `first_pass`, et lui génère une valeur. 

En fouillant dans le Registre de la machine cible, la recherche ne donne pas grand chose (est-ce que ca cherche vraiment ??). Soit.  Si je supprime violement HKEY_CURRENT_USER et que je lance le prog, il ne fait rien (et calcule la clé).  Si je le lance à nouveau il redemande la clé.  En procédent récursivement dans les différent niveau du registre, on trouve finalement la clé ici : 
```
HKEY_CURRENT_USER/SOFTWARE/Microsoft/Windows/CurrentVersion/Run/OneDrive/error
```


Il n'y a plus qu'à lancer `wrongsomewhere.exe` sur la machine cible en lui passant le `flag.txt`, et à lui entrer la clé au prompt.  

Et hop : 
```
DGHACK{R4nS0mW4r3s_4r3_4_Cr1m3_D0_n0t_Us3_Th1s_0n3_F0r_3v1l}
```

