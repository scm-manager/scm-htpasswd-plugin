---
title: Nutzung
---
Sobald das Plugin installiert ist, gibt es ein zusätzliches Element namens „Htpasswd“ in den Einstellungen. Sie können dort die Pfade der Dateien einstellen, welche die Benutzerpasswörter (.htpasswd), Gruppen (.htgroup) und begleitende Informationen (.htmeta) enthalten.

## Dateiformate

##### .htpasswd (Benutzer/Passwörter; wird [hier](https://httpd.apache.org/docs/2.4/misc/password_encryptions.html) beschrieben)

    #username:encrypted-password (test123)
    arthur:$apr1$dummy$aVxoIgJn.JnWLU9GBijfj.
    prefect:$apr1$dummy$aVxoIgJn.JnWLU9GBijfj.
    trillian:$apr1$dummy$aVxoIgJn.JnWLU9GBijfj.

##### .htgroup (Gruppen/Benutzer; wird [hier](https://httpd.apache.org/docs/2.4/mod/mod_authz_groupfile.html) beschrieben)

    #group: user1 user2 userN ...
    RestaurantAtTheEndOfTheUniverse: trillian
    RestaurantsAtEarth: arthur
    HeartOfGold: arthur prefect trillian

##### .htmeta (Benutzer/Metadaten: E-Mail, Anzeigename)

    #username:email:display-name
    arthur:arthur.dent@hitchhiker.com:Arthur Dent
    prefect:ford.prefect@hitchhiker.com:Ford Prefect
    trillian:tricia.mcmillan@hitchhiker.com:Tricia McMillan

*Anmerkung:* Für Benutzer und Gruppen ist nur ein einfacher Zeichensatz (0-9, a-z, A-Z) zulässig.
