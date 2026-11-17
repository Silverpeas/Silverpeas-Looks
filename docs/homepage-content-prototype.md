# Feature #15592 — Prototype des contenus libres Aurora

Ce prototype est réalisé uniquement dans Silverpeas-Looks. Il n'utilise ni instance WebPages,
ni identifiant de composant fictif, ni modification du Core. Il réutilise l'éditeur CKEditor
livré par Silverpeas et le filtrage HTML existant du Core.

## Essai sur une instance Silverpeas

Compiler et déployer le WAR Aurora selon la procédure habituelle. Le serveur doit définir la
propriété système `SILVERPEAS_DATA_HOME` ; le processus doit pouvoir y créer `homepages/`.
Sur une installation où elle vaut `/silverpeas/data`, les données sont stockées sous
`/silverpeas/data/homepages`.

Après connexion à Silverpeas, ouvrir dans le même navigateur :

- Pour un administrateur de plateforme :
  `/silverpeas/AuroraHomepageContent?action=editor&kind=skins&widget=bienvenue&language=fr`
- Pour l'administrateur de l'espace WA123 :
  `/silverpeas/AuroraHomepageContent?action=editor&kind=spaces&owner=WA123&widget=presentation&language=fr`

Remplacer le contexte `/silverpeas`, le nom du skin et l'identifiant d'espace si nécessaire.
Pour les skins, ne pas fixer le propriétaire dans le lien d’édition : Aurora le déduit du bundle
du skin réellement actif dans la session. Par exemple, l’alias `Initial` peut désigner
`Aurora.properties` et produit alors le dossier `skins/Aurora`. Un alias `Waves` associé à
`waves.properties` produit `skins/waves`. Les préférences utilisateur et le repli de configuration
sont donc ceux déjà résolus par Silverpeas. Une URL d’édition portant un autre propriétaire est
redirigée vers le skin actif ; les références explicites des images restent stables. Le nom du propriétaire et celui du widget sont des identifiants stables :
lettres, chiffres, tirets et underscores, 80 caractères maximum. La langue est explicitement choisie.

Saisir un titre et un texte, insérer une image avec le champ dédié, puis enregistrer. L'aperçu affiche
le contenu enregistré. Modifier `widget` dans l'URL pour créer une deuxième zone indépendante.
L'éditeur reste sur une page dédiée : aucune des deux homes existantes n'est modifiée automatiquement.

L'URL `action=view` rend un fragment HTML protégé ; l'URL `action=asset&name=...` rend une image protégée.
Elles portent le même contexte `kind/owner/widget` que l'éditeur.

## Stockage et ressources

```text
homepages/
  skins/waves/
    page.properties
    widgets/bienvenue/
      content-fr.html
      assets/<uuid>.png
  spaces/WA123/
    page.properties
    widgets/presentation/
      content-fr.html
      assets/<uuid>.png
```

`page.properties` contient la version du format, la liste des widgets et leurs paramètres
communs (type html, titre, zone main, activation, ordre initial). L'éditeur ne manipule pour
l'instant que le titre et le contenu. Le titre est commun aux langues ; le HTML est par langue.
Les properties sont lues et écrites en UTF-8, leurs autres entrées sont conservées.

Le répertoire est créé à la première écriture et n'est ni une ressource du WAR ni une ressource
à réinstaller par le paquet de configuration. Il doit être inclus dans les sauvegardes.
La résistance effective à une mise à jour doit encore être testée avec l'installateur.

## Contrôles implémentés

- Session Silverpeas obligatoire ; administration plateforme pour modifier un skin ; espaces
  administrables du demandeur pour modifier une home d'espace.
- Pour lire un contenu ou une image : administration plateforme, ou skin actif du demandeur,
  ou droit de consultation de l'espace. L'accès anonyme sans session Silverpeas n'est pas pris en charge.
- Jeton CSRF en session requis pour les sauvegardes et les images ; aucune écriture par GET.
  Les appels utilisent XMLHttpRequest pour recevoir également le jeton CSRF X-STKN du Core.
- Identifiants de chemin validés, liens symboliques refusés, noms d'images générés côté serveur.
- Téléversement limité à 5 Mio par fichier, requête multipart à 6 Mio ; dimensions limitées à
  16 millions de pixels avant décodage. Les images acceptées par ImageIO sont réencodées en PNG.
  SVG et autres fichiers non reconnus comme images sont refusés ; les images ne sont jamais exécutables.
- HTML filtré côté serveur par le sanitiseur du Core. L'aperçu est isolé par sandbox et CSP.
  L'origine est conservée pour accéder aux images protégées par la session, sans autoriser les scripts.
  Le filtre de CKEditor autorise explicitement les images insérées par le téléversement dédié.
  Les scripts et les iframes ne font pas partie du contenu pris en charge par ce prototype.
- Chaque fichier est remplacé atomiquement. Un jeton de révision refuse une sauvegarde devenue
  obsolète ; les accès sont sérialisés par une instance du service dans une JVM.
- Les contenus et les images portent des réponses sans cache et `nosniff`.

## Validation et limites

Tests automatisés : persistance après réouverture, séparation des propriétaires/widgets/langues,
conservation des autres widgets, conflits de sauvegarde, refus des traversées et liens symboliques,
accès à l'éditeur, droits des espaces et skins, CSRF, filtrage HTML et isolation de l'aperçu.

Les essais réels sur WildFly restent nécessaires : rendu JSP/CKEditor, téléversement et affichage
d'images, sessions et droits, redémarrage, puis mise à jour. Le prototype ne démontre pas encore
la migration des WebPages, l'intégration dans Main.jsp/spaceHomePage.jsp, la suppression/duplication,
le nettoyage des images orphelines ou la gestion des versions.

L'écriture du HTML et celle des properties sont deux remplacements de fichiers distincts : elles
ne constituent pas une transaction globale en cas de crash. Le verrou et la révision ne couvrent
pas plusieurs JVM ; ne pas activer l'édition sur plusieurs nœuds avant d'ajouter une coordination
distribuée. Ces points sont à résoudre avant une utilisation en production.

Tests du transport JavaScript : `node --test aurora/aurora-war/src/test/javascript/homepage-content-request.test.cjs`.

## Intégration à la home générale (#15592)

La home générale affiche les widgets HTML du bundle du skin actif, après les blocs
existants de chaque colonne. Le lien « Modifier la page d’accueil », visible pour
les administrateurs de plateforme, ouvre `action=manage&kind=skins` sans imposer de
propriétaire. Le servlet résout le bundle effectif avant d’afficher la gestion.

La gestion liste aussi les zones désactivées et propose d’ajouter une instance avec
un identifiant UUID stable. L’éditeur enregistre la colonne `main` ou `right`, un
ordre de 0 à 10000 et l’activation. À ordre égal, les identifiants départagent les
zones. Le titre, le placement et l’activation sont communs aux langues ; le HTML
reste distinct par langue. Une langue sans contenu ne reprend pas automatiquement
le contenu français. Les changements de placement participent au contrôle de
révision et empêchent un ancien éditeur de les écraser.

Les zones WebPages existantes sont conservées. L’ordre ne déplace pas les anciens
blocs. La gestion des homes d’espace reste une étape ultérieure.

### Interface de gestion

La gestion et l’éditeur utilisent le cadre, le fil d’Ariane et les panneaux
Silverpeas. La liste s’appuie sur un tableau Silverpeas paginé, sans tri manuel
pour conserver l’ordre configuré des zones. Elle distingue les zones visibles et
masquées et prévoit un état vide. Les titres sont échappés avant insertion dans
les cellules du tableau. L’éditeur regroupe les réglages d’affichage, le contenu,
l’insertion d’image et les actions ; l’aperçu enregistré est repliable.

### Base commune des widgets de la home générale

Zones libres et actualités sont des instances identifiées, ajoutables sans limite
fixée à trois. Elles partagent `Widget`, `Content` et `Layout` : type, titre,
colonne `main`/`right`, ordre et visibilité. Le registre `widgets` de
`page.properties` contient les deux types. Les réglages propres aux actualités
(source, limite, importantes uniquement, liste/carrousel) utilisent le même
préfixe `widget.<id>`. La révision couvre ensemble le placement, le titre et ces
réglages. Les actualités sont communes aux langues ; le HTML reste multilingue.

`HomepageWidgets` assemble la liste commune triée par ordre puis identifiant et
prépare les actualités visibles avec le helper existant, qui filtre les droits
du lecteur. Le tag `displayHomepageWidgets` distribue les deux types dans chaque
colonne. L’ordre est partagé entre types, dans le groupe de widgets placé après
les autres blocs existants de chaque colonne. Les carrousels sont initialisés
indépendamment, une seule fois chacun, après construction du DOM.

La gestion propose « Ajouter des actualités » et « Ajouter une zone libre »,
puis un tableau commun. Les trois anciens blocs d’actualités y sont exposés comme
instances `news-legacy-1..3`. Sans sauvegarde, leur contenu conserve les réglages
historiques du thème ou les anciennes configurations `news-<index>.properties`.
Aucun fichier n’est écrit à la simple consultation. Une modification explicite
enregistre le bloc dans le registre commun sans duplication ; les anciennes
données restent conservées. Les anciens endpoints par index restent compatibles,
et les anciens liens d’édition redirigent vers les instances correspondantes.

Les sources proposées restent toutes les applications autorisées, les actualités
déléguées, la position du plan de classement du thème ou une liste d’identifiants
quickinfo. La source toutes d’une instance configurée couvre toutes les applications
autorisées, sans exclusion automatique des autres instances. Le filtre importantes
ne concerne pas les actualités déléguées. La limite est de 1 à 100 ou -1 sans limite.
Dimensions des images et boutons du plan de classement restent hérités du thème
(bloc principal pour une nouvelle instance). Les abonnements restent indépendants.
Sauvegarde réservée aux administrateurs avec protections CSRF et révision (HTTP 409
en cas de conflit). Le type d’une instance existante ne peut pas être changé.
