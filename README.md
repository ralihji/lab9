# SEG3502 · Laboratoire 9 · Spring Security

API REST de conversion de température (code de départ du laboratoire 6) sécurisée avec **Spring Security** :
authentification **en mémoire** et **HTTP Basic** exigée pour toutes les requêtes.

**Équipe :** Hjiyej Andaloussi Elghali (300379897) et Brayan Adou (300433616)

## Comptes configurés

| Utilisateur | Mot de passe | Rôle |
|-------------|--------------|------|
| `user1`     | `pass1`      | USER |
| `user2`     | `pass2`      | USER |

Les mots de passe sont encodés avec `BCryptPasswordEncoder` dans `InMemoryUserDetailsManager`.

## Routes

| Méthode | Route | Exemple | Réponse |
|---------|-------|---------|---------|
| GET | `/temperature-converter/celsius-fahrenheit/{celsius}` | `/temperature-converter/celsius-fahrenheit/50` | `122.0` |
| GET | `/temperature-converter/fahrenheit-celsius/{fahrenheit}` | `/temperature-converter/fahrenheit-celsius/122` | `50.0` |
| GET | `/v3/api-docs`, `/swagger-ui.html` | documentation OpenAPI | JSON, Swagger UI |

| Situation | Code HTTP |
|-----------|-----------|
| Aucun identifiant | `401 Unauthorized` + `WWW-Authenticate: Basic realm="Realm"` |
| Utilisateur inconnu ou mauvais mot de passe | `401 Unauthorized` |
| `user1 / pass1` ou `user2 / pass2` | `200 OK` avec le résultat |
| Identifiants valides mais température invalide (`abc`) | `400 Bad Request`, `Unable to process request` |

La session est sans état (`STATELESS`) : chaque requête doit renvoyer ses identifiants.

## Prérequis

* **JDK 21** (vérifier avec `java -version`). Gradle 8.8 est téléchargé par `gradlew` au premier lancement.
* Un outil HTTP : curl, Postman, Insomnia, ou le client HTTP d'IntelliJ / VS Code.

## Installer et exécuter

```bash
git clone https://github.com/ralihji/lab9.git
cd lab9/SEG3502_Lab9
./gradlew bootRun          # Windows (PowerShell ou cmd) : .\gradlew.bat bootRun
```

L'API écoute sur `http://localhost:8080`. Pendant l'exécution, Gradle reste vers 80 % : c'est normal.
Arrêter avec `Ctrl + C`.

## Tester l'authentification

### curl

```bash
# 401 : aucun identifiant
curl -i http://localhost:8080/temperature-converter/celsius-fahrenheit/50

# 401 : mauvais mot de passe
curl -i -u user1:wrong http://localhost:8080/temperature-converter/celsius-fahrenheit/50

# 200 : 122.0
curl -i -u user1:pass1 http://localhost:8080/temperature-converter/celsius-fahrenheit/50

# 200 : 50.0
curl -i -u user2:pass2 http://localhost:8080/temperature-converter/fahrenheit-celsius/122
```

Sous Windows PowerShell, utiliser `curl.exe` (et non l'alias `curl`).

### Postman ou Insomnia

Importer `SEG3502_Lab9/requests/lab9.postman_collection.json` (Postman : *Import* ; Insomnia : *Import* puis
choisir le fichier). La collection contient 9 requêtes, chacune avec l'authentification Basic réglée et le résultat
attendu. Dans Postman, *Run collection* fait 13 vérifications automatiques (code HTTP, réponse, en-tête
`WWW-Authenticate`). En ligne de commande : `npx newman run SEG3502_Lab9/requests/lab9.postman_collection.json`.

### IntelliJ IDEA ou VS Code

Ouvrir `SEG3502_Lab9/requests/lab9.http` et cliquer sur *Send Request* au dessus de chaque requête
(VS Code : extension *REST Client*).

### Swagger UI

Ouvrir `http://localhost:8080/swagger-ui.html` : le navigateur demande un nom d'utilisateur et un mot de passe
(`user1` / `pass1`). Le bouton **Authorize** de Swagger UI correspond au schéma `basicAuth` déclaré dans la
documentation OpenAPI ; chaque route y affiche aussi la réponse 401.

## Tests automatisés

```bash
cd SEG3502_Lab9
./gradlew test             # Windows : .\gradlew.bat test
```

12 tests JUnit (MockMvc + `spring-security-test`) :

* `SecurityTests` (4) : routes protégées, accès des deux comptes, identifiants refusés, aucune session conservée ;
* `ApiBehaviourTests` (7) : valeurs de conversion, entrée invalide (400), 401 avant validation, mots de passe
  croisés refusés, sensibilité à la casse, documentation protégée, schéma Basic dans OpenAPI ;
* `TempConverterApiApplicationTests` (1) : chargement du contexte.

## Structure

```
SEG3502_Lab9/
├── build.gradle.kts                       spring-boot-starter-security, spring-security-test
├── requests/
│   ├── lab9.http                          requêtes IntelliJ / VS Code
│   └── lab9.postman_collection.json       collection Postman / Insomnia
└── src/
    ├── main/kotlin/seg3x02/tempconverterapi/
    │   ├── security/WebSecurityConfig.kt  comptes en mémoire, Basic Auth, STATELESS
    │   ├── config/OpenApiConfig.kt        schéma basicAuth et réponse 401 dans OpenAPI
    │   └── controller/                    routes de conversion et erreurs 400
    └── test/kotlin/seg3x02/tempconverterapi/
        ├── SecurityTests.kt
        ├── ApiBehaviourTests.kt
        └── TempConverterApiApplicationTests.kt
```
