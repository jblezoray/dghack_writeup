## iDisk

https://www.dghack.fr/challenges/dghack/idisk/

> Il y a quelque temps, l'entreprise ECORP a constaté une intrusion au sein de son système d'information. Les équipes d'administrateurs système ont remarqué que des sauvegardes de la base de données "pre_prod" ont été effectuées à plusieurs reprises (et sans accord au préalable) aux alentours de 00h chaque jour. Après une longue enquête policière, un suspect (ex-employé d'ECORP) a été interpelé avec un ordinateur. Toutefois, la police étant en sous-effectif, nous avons besoin de votre aide afin de mener une investigation numérique sur la machine saisie. Êtes-vous prêt à accepter cette mission ?

Nous avons un fichier joints d'une dizaine de gigaoctets.

`cfdisk` nous indique sa nature : 

```
root@569154690cee:/workdir# cfdisk forensic.dd
Disk: forensic.dd
Size: 20 GiB, 21474836480 bytes, 41943040 sectors
Label: dos, identifier: 0x293532d6

    Device            Boot              Start           End       Sectors      Size     Id Type
    forensic.dd1      *                  2048       1126399       1124352      549M      7 HPFS/NTFS/exFAT
>>  forensic.dd2                      1126400      41938943      40812544     19.5G      7 HPFS/NTFS/exFAT
    Free space                       41938944      41943039          4096        2M
    
    ┌──────────────────────────────────────────────────────────────────────────────────────────────────────────┐
    │ Partition type: HPFS/NTFS/exFAT (7)                                                                      │
    │Filesystem UUID: 60E89466E8943C6A                                                                         │
    │     Filesystem: ntfs                                                                                     │
    └──────────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

Nous sommes en présence d'un disque NTFS.  