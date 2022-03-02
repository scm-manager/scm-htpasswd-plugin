---
title: Usage
---
Once the plugin is installed, there is an additional item named "htpasswd" in the settings. There, you can configure the paths of the files that contain the users passwords (.htpasswd), groups (.htgroup) and complementary info (.htmeta).

## File Formats

##### .htpasswd (users/passwords; described [here](https://httpd.apache.org/docs/2.4/misc/password_encryptions.html))

    #username:encripted-password (test123)
    arthur:$apr1$dummy$aVxoIgJn.JnWLU9GBijfj.
    prefect:$apr1$dummy$aVxoIgJn.JnWLU9GBijfj.
    trillian:$apr1$dummy$aVxoIgJn.JnWLU9GBijfj.

##### .htgroup (groups/users; described [here](https://httpd.apache.org/docs/2.4/mod/mod_authz_groupfile.html))

    #group: user1 user2 userN ...
    RestaurantAtTheEndOfTheUniverse: trillian
    RestaurantsAtEarth: arthur
    HeartOfGold: arthur prefect trillian

##### .htmeta (users/metadata: email, display-name)

    #username:email:display-name
    arthur:arthur.dent@hitchhiker.com:Arthur Dent
    prefect:ford.prefect@hitchhiker.com:Ford Prefect
    trillian:tricia.mcmillan@hitchhiker.com:Tricia McMillan

Note: For users and groups only basic characters are allowed (0-9, a-z, A-Z)
