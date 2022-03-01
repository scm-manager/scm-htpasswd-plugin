<p align="center">
  <a href="https://scm-manager.org/">
    <img alt="SCM-Manager" src="https://download.scm-manager.org/images/logo/scm-manager_logo.png" width="500" />
  </a>
</p>
<h1 align="center">
  scm-htpasswd-plugin
</h1>

This plugin provides an Authentication for SCM-Manager using htpasswd.

## Usage

Find out how this plugin works on the [plugin documentation page](https://scm-manager.org/plugins/scm-htpasswd-plugin/docs/).

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

###### For users and groups only basic characters are allowed (0-9, a-z, A-Z)

## Build and testing

The plugin can be compiled and packaged with the following tasks:

* clean - `gradle clean` - deletes the build directory
* run - `gradle run` - starts an SCM-Manager with the plugin pre-installed and with livereload for the ui
* build - `gradle build` - executes all checks, tests and builds the smp inclusive javadoc and source jar
* test - `gradle test` - run all java tests
* ui-test - `gradle ui-test` - run all ui tests
* check - `gradle check` - executes all registered checks and tests (java and ui)
* fix - `gradle fix` - fixes all fixable findings of the check task
* smp - `gradle smp` - Builds the smp file, without the execution of checks and tests

For the development and testing the `run` task of the plugin can be used:

* run - `gradle run` - starts scm-manager with the plugin pre-installed.

If the plugin was started with `gradle run`, the default browser of the os should be automatically opened.
If the browser does not start automatically, start it manually and go to [http://localhost:8081/scm](http://localhost:8081/scm).

In this mode each change to web files (src/main/js or src/main/webapp), should trigger reload of the browser with the made changes.

## Setup Test Environment

You can use a single `curl` request for this configuration:

```bash
# Configure Plugin
curl -u scmadmin:scmadmin \
     --data '{"htpasswdFilepath":"/etc/scm/.htpasswd.test","htgroupFilepath":"/etc/scm/.htgroup.test","htmetaFilepath":"/etc/scm/.htmeta.test","enabled":true,"showTestDialog":false}' \
     -H "Content-Type: application/json" \
     -X PUT \
     http://localhost:8081/scm/api/v2/config/htpasswd
# Create Test users
tee    /etc/scm/.htpasswd.test <<<'testuser:$apr1$dummy$aVxoIgJn.JnWLU9GBijfj.'
tee -a /etc/scm/.htpasswd.test <<<'otheruser:$apr1$dummy$aVxoIgJn.JnWLU9GBijfj.'
tee    /etc/scm/.htmeta.test <<<'testuser:testuser@acme.org:Friendly User'
tee -a /etc/scm/.htmeta.test <<<'otheruser:otheruser@acme.org:Happy User'
tee    /etc/scm/.htgroup.test <<<'testgroup: testuser otheruser'
```

Now you can test the authentication with username `testuser` and password `test123`.

## Directory & File structure

A quick look at the files and directories you'll see in an SCM-Manager project.

    .
    ├── node_modules/
    ├── src/
    |   ├── main/
    |   |   ├── java/
    |   |   ├── js/
    |   |   └── resources/
    |   └── test/
    |       ├── java/
    |       └── resources/
    ├── .editorconfig
    ├── .gitignore
    ├── build.gradle
    ├── CHANGELOG.md
    ├── gradle.properties
    ├── gradlew
    ├── LICENSE.txt
    ├── package.json
    ├── README.md
    ├── settings.gradle
    ├── tsconfig.json
    └── yarn.lock

1.  **`node_modules/`**: This directory contains all modules of code that your project depends on (npm packages) are automatically installed.

2.  **`src/`**: This directory will contain all code related to what you see or not. `src` is a convention for “source code”.
    1. **`main/`**
        1. **`java/`**: This directory contains the Java code.
        2. **`js/`**: This directory contains the JavaScript code for the web ui, inclusive unit tests: suffixed with `.test.ts`
        3. **`resources/`**: This directory contains the classpath resources.
    2. **`test/`**
        1. **`java/`**: This directory contains the Java unit tests.
        2. **`resources/`**: This directory contains classpath resources for unit tests.

3.  **`.editorconfig`**: This is a configuration file for your editor using [EditorConfig](https://editorconfig.org/). The file specifies a style that IDEs use for code.

4.  **`.gitignore`**: This file tells git which files it should not track / not maintain a version history for.

5.  **`build.gradle`**: Gradle build configuration, which also includes things like metadata.

6.  **`CHANGELOG.md`**: All notable changes to this project will be documented in this file.

7.  **`gradle.properties`**: Defines the module version.

8.  **`gradlew`**: Bundled gradle wrapper if you don't have gradle installed.

9.  **`LICENSE.txt`**: This project is licensed under the MIT license.

10.  **`package.json`**: Here you can find the dependency/build configuration and dependencies for the frontend.

11.  **`README.md`**: This file, containing useful reference information about the project.

12.  **`settings.gradle`**: Gradle settings configuration.

13. **`tsconfig.json`** This is the typescript configuration file.

14. **`yarn.lock`**: This is the ui dependency configuration.

## Need help?

Looking for more guidance? Full documentation lives on our [homepage](https://scm-manager.org/docs/) or the dedicated pages for our [plugins](https://scm-manager.org/plugins/). Do you have further ideas or need support?

- **Community Support** - Contact the SCM-Manager support team for questions about SCM-Manager, to report bugs or to request features through the official channels. [Find more about this here](https://scm-manager.org/support/).

- **Enterprise Support** - Do you require support with the integration of SCM-Manager into your processes, with the customization of the tool or simply a service level agreement (SLA)? **Contact our development partner Cloudogu! Their team is looking forward to discussing your individual requirements with you and will be more than happy to give you a quote.** [Request Enterprise Support](https://cloudogu.com/en/scm-manager-enterprise/).

---
Inspired in [scm-htpasswd-plugin for scm-manager 1.x](https://bitbucket.org/triologygmbh/scm-htpasswd-plugin/) and [scm-ldap-plugin for scm-manager 2.x](https://github.com/scm-manager/scm-ldap-plugin), this code is Java-minimalistic version.
