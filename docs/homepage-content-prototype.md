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

Zones libres, actualités et dernières publications sont des instances identifiées, ajoutables sans limite
fixée à trois. Elles partagent `Widget`, `Content` et `Layout` : type, titre,
colonne `main`/`right`, ordre et visibilité. Le registre `widgets` de
`page.properties` contient les trois types. Les réglages propres aux actualités
(source, limite, importantes uniquement, liste/carrousel) utilisent le même
préfixe `widget.<id>`. La révision couvre ensemble le placement, le titre et ces
réglages. Les actualités sont communes aux langues ; le HTML reste multilingue.

`HomepageWidgets` assemble la liste commune triée par ordre puis identifiant et
prépare les actualités visibles avec le helper existant, qui filtre les droits
du lecteur. Le tag `displayHomepageWidgets` distribue les trois types dans chaque
colonne. L’ordre est partagé entre types, dans le groupe de widgets placé après
les autres blocs existants de chaque colonne. Les carrousels sont initialisés
indépendamment, une seule fois chacun, après construction du DOM.

La gestion propose « Ajouter des actualités », « Ajouter les dernières publications » et « Ajouter une zone libre »,
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
Deux options indépendantes contrôlent les liens « + d’actualités » (applications et
plan de classement) et « Voir les actualités ». Elles valent vrai par défaut, y
compris pour les configurations antérieures ; les anciennes révisions restent valides.
Dimensions des images et contenu des boutons du plan de classement restent hérités du thème
(bloc principal pour une nouvelle instance). Les abonnements restent indépendants.
Sauvegarde réservée aux administrateurs avec protections CSRF et révision (HTTP 409
en cas de conflit). Le type d’une instance existante ne peut pas être changé.


Les dernières publications partagent titre, colonne, ordre et visibilité avec les
zones libres et les actualités. Leur configuration comporte un espace source
facultatif (identifiant numérique ou WA suivi de chiffres ; vide pour tous les
espaces accessibles), un nombre maximal de 1 à 100 et la visibilité du lien vers
la liste complète. Cette liste complète reste globale, comme auparavant.
La récupération passe par `LookAuroraHelper.getLatestPublications(spaceId, size)` :
les droits du lecteur et exclusions du thème (applications, espaces, espace personnel
et actualités) sont conservés. Les blocs vides ne sont pas affichés.
L’ancien bloc est exposé comme instance virtuelle `publications-legacy`, initialisée
avec les réglages `home.publications.spaceid/nb`. Il rejoint le groupe commun à
l’ordre 40 par défaut et n’est plus rendu séparément. Aucune écriture au chargement ;
l’enregistrement remplace cette instance sans doublon. Plusieurs nouvelles instances
peuvent être ajoutées. Paramètres communs aux langues, mêmes droits admin/CSRF et
contrôle de révision que les autres widgets.


Les éphémérides utilisent le type `ephemeris` et les mêmes titre, colonne, ordre,
visibilité et contrôle de révision. Leur contenu reste la date (format du thème)
et le nom du saint du jour issu du calendrier JavaScript existant. Plusieurs
instances peuvent être ajoutées ; chacune est initialisée après le DOM, sans
identifiant HTML partagé. L’instance virtuelle `ephemeris-legacy` reprend
`home.ephemeris`, colonne droite, ordre 0, sans écriture. L’enregistrement la
remplace dans le registre commun, sans doublon. L’ancien affichage des éphémérides
est retiré du bloc météo sur la home générale ; la météo reste à sa place avec
ses réglages et sa condition historique d’affichage. Les autres usages du tag
météo gardent leur comportement. Aucun service externe supplémentaire.


Les prochains événements (`events`) et les flux RSS (`rss`) rejoignent le registre
commun avec titre, colonne, ordre et visibilité, et plusieurs instances possibles.
Les événements proposent des agendas sources (`*`, identifiants `almanach` séparés
par des espaces, ou vide), de 1 à 100 jours comportant des événements, l’inclusion
d’aujourd’hui et de l’agenda personnel du lecteur, le filtre des événements importants
et les liens « Voir l’agenda ». Le nombre porte sur les jours qui contiennent des
événements, conformément au helper historique ; la limite de récupération du calendrier
reste celle de la plateforme.
Les RSS utilisent une application `rssAgregator` accessible au lecteur (vide : aucun
flux) et les modes `aggregate`, `tabs`, `carrousel`. Les liens RSS s’ouvrent dans un
nouvel onglet. Les onglets, filtres, descriptions et carrousels sont isolés par instance.

Les instances virtuelles `events-legacy` (droite, ordre 10) et `rss-legacy` (principale,
ordre 50) reprennent les réglages `home.events.*` et `home.rss.*` sans écrire de données.
La sauvegarde remplace ces instances dans le registre sans doublon. Les blocs historiques
séparés sont retirés de Main.jsp, mais leurs fragments restent disponibles pour les
autres usages. Le rendu commun utilise `action=renderFeed` avec authentification et
contrôle du skin actif, puis les helpers filtrent les sources selon les droits du lecteur.
Les blocs masqués ne récupèrent aucune donnée ; les blocs vides ne sont pas affichés.
Le chargement reste asynchrone, avec initialisation RSS après insertion dans le DOM.
Les éditeurs conservent les protections administrateur/CSRF et le contrôle de révision.


La FAQ (`faq`) et les nouveaux utilisateurs (`users`) utilisent également le registre
commun, avec plusieurs instances indépendantes et les mêmes protections de sauvegarde.
La FAQ configure une application `questionReply<id>` (vide : aucune question), de 1 à
100 questions, et une sélection aléatoire sans répétition ou les premières questions
renvoyées par l’application. L’accès à l’application et le droit de poser une question
restent évalués pour le lecteur. La sélection aléatoire inclut désormais toutes les
questions, y compris la dernière de la liste.

Les nouveaux utilisateurs configurent un groupe numérique (vide ou `-1` : tous),
un nombre de 0 à 100, les avatars et les identifiants des champs de profil séparés
par des espaces. Les comptes bloqués, désactivés et supprimés sont exclus ; le tri
reste la date de création décroissante. Une liste vide ou plus courte que la limite
est correctement rendue sans perdre son dernier utilisateur. 0 désactive la récupération.

Les instances virtuelles `faq-legacy` (droite, ordre 20) et `users-legacy` (principale,
ordre 60) reprennent `home.faq.*` et `home.users.*` sans écriture. `users-legacy` est
masquée lorsque `home.users.nb` vaut 0. La sauvegarde remplace leur instance virtuelle
sans doublon. Le rendu passe par le tag commun ; les blocs masqués ne récupèrent
aucune donnée et les blocs vides ne s’affichent pas. Les cartes utilisateurs restent
rendues avec le DOM initial pour conserver leur initialisation native. Les styles des
quatre skins couvrent les classes des nouvelles instances sans identifiant HTML partagé.


Les actualités des abonnements (`subscriptions`) rejoignent le registre commun :
plusieurs instances, titre, colonne, ordre, visibilité, limite de 1 à 100 actualités
et option du lien vers la liste complète. La source reste toujours les abonnements
du lecteur aux applications QuickInfo, sans identifiant de source imposé par l’admin.
Les applications devenues inaccessibles sont exclues avant toute récupération des
actualités visibles ; les abonnements à la même application sont dédupliqués. Les
résultats restent triés par date de publication décroissante avant la limite globale.

L’instance virtuelle `subscriptions-legacy` (principale, ordre 70) conserve
`home.subscription.news` et `home.subscription.news.max`, sans écriture au chargement.
Sa sauvegarde remplace le bloc historique sans doublon. Le nombre et le lien sont
inclus dans la révision ; la sauvegarde garde les protections admin/CSRF. Les blocs
masqués ne récupèrent rien, et une liste vide ne s’affiche pas. Le tag conserve ses
attributs existants pour les autres usages, ajoute titre/identifiant uniques et le
lien complet reste activé par défaut. Le lien utilise le contexte de l’application.
Le réglage `subscription.news.maxbycompoments` de la page complète reste distinct.


Les raccourcis de la home (`shortcuts`) sont administrables par instance : titre,
colonne, ordre et visibilité, et liste ordonnée de 0 à 100 liens. Chaque lien a un
libellé (200 caractères maximum), une URL (2048), une icône facultative et une cible
(vide, `_self`, `_blank`, `_parent`, `_top` ou nom de fenêtre comme `bottomFrame`).
L’éditeur permet l’ajout, la suppression et le déplacement des liens. Les URL
relatives et les ancres sont conservées ; HTTP/HTTPS et mailto sont acceptés pour
les liens, HTTP/HTTPS pour les icônes. Les schémas exécutables/data et les caractères
de contrôle sont rejetés. Les valeurs affichées sont échappées. La protection
noopener s’applique aux nouvelles fenêtres `_blank`, en conservant les cibles nommées.

`shortcuts-legacy` (principale, ordre 0) reprend uniquement `Shortcut.home.*` via
`getMainShortcuts()`, sans écrire avant enregistrement ; la sauvegarde le remplace
sans doublon. Les raccourcis `Shortcut.tool.*`, `getToolsShortcuts()` et TopBar.jsp
restent inchangés. Les raccourcis des homes d’espace gardent leur configuration.
Les widgets vides et masqués ne sont pas affichés. Les classes et identifiants du
rendu administré sont propres à chaque instance : placement dans le flux des deux
colonnes, sans les positions absolues du bloc historique. Les révisions incluent
l’ordre et tous les champs des liens ; contrôle admin, CSRF et conflits 409.


### Recherche

Un seul type `search`, avec modes `taxonomy` (plan de classement) ou `advanced`
(formulaire XML). Éditeur unique : titre, colonne, ordre, visibilité, libellés dans les
sélecteurs du plan de classement, fichier de modèle XML, critères période/espace.
Le modèle est un nom de fichier local `.xml` sans chemin ; il doit exister et définir
un formulaire de recherche. S’il est indisponible, le lecteur voit un message explicite.
Les résultats et leurs droits restent traités par les routes Core AdvancedSearch/XMLSearch.

L’instance virtuelle `search-legacy` reprend `home.search.*`, sans écriture. Si un modèle
est configuré, elle choisit le mode avancé ; sinon le plan de classement suit `home.search`.
Cela remplace les deux rendus historiques potentiellement simultanés par un seul widget.
Sa sauvegarde le remplace sans doublon. Les nouvelles instances sont indépendantes.
Les champs générés par Core ayant des identifiants fixes, chaque instance possède un
document iframe de même origine, redimensionné automatiquement. La soumission cible
la page parente : les résultats ne restent pas enfermés dans le widget. Les anciens
tags utilisés par les homes d’espace restent inchangés.


### Favoris personnels

Le type `bookmarks` propose titre, colonne, ordre, visibilité, affichage même si la
liste est vide et URL facultative du fragment affiché à la place de la liste vide.
Les URL relatives et HTTP/HTTPS sont autorisées, les schémas exécutables/data et les
caractères de contrôle sont rejetés. Tous ces paramètres font partie de la révision.
La sauvegarde reste réservée aux administrateurs, avec CSRF et contrôle de conflit.

`bookmarks-legacy` (droite, ordre 40) reprend `home.bookmarks.empty.show` et
`home.bookmarks.empty.fragment`, sans écriture au chargement. Son enregistrement
remplace le bloc historique ; les nouvelles instances ont leurs propres paramètres.
Chaque lecteur retrouve ses favoris visibles via `LookAuroraHelper.getBookmarks()` :
aucun lien personnel n’est copié dans la configuration partagée. Les widgets masqués
ne récupèrent aucun lien. Les visiteurs anonymes ne voient pas de bloc vide.

Le composant Core conserve catégories et bouton « voir plus ». Ses identifiants et
sélecteurs globaux sont isolés par instance dans un document iframe de même origine,
redimensionné automatiquement. Le bloc vide masqué ne laisse pas de cadre visible.
Le lien de gestion cible la page parente, les liens internes utilisent la navigation
Core et les liens externes conservent leur comportement existant. L’ancien tag et
les favoris de la barre supérieure restent inchangés.

### Météo

Le type `weather` propose les réglages communs (titre, colonne, ordre, visibilité).
Les villes, le fournisseur et les clés restent exclusivement dans `weather.properties`
(ou le bundle météo personnalisé déjà sélectionné par l’instance).
Aucune ville n’est copiée dans les paramètres des widgets. Les anciennes valeurs locales
éventuellement enregistrées sont ignorées et supprimées au prochain enregistrement.
Une liste de villes vide dans le fichier n’affiche aucun bloc.

L’instance virtuelle `weather-legacy` reprend la visibilité historique sans écriture.
Une fois enregistrée, sa visibilité devient indépendante des éphémérides et de `home.weather`.
Chaque instance retient séparément la dernière ville du lecteur dans un cookie ; le cookie
historique est repris pour `weather-legacy`. RWeatherService, son cache et les traducteurs
existants sont réutilisés. L’ancien tag météo reste disponible pour les autres pages.

### Suppression des widgets

L’écran « Modifier la page d’accueil » propose « Supprimer » sur chaque ligne, avec
confirmation. La suppression concerne le widget dans toutes les langues de cette page.
Un marqueur persistant empêche la réapparition des blocs historiques. Les contenus et
réglages sont conservés sur disque ; les données personnelles et les applications
sources ne sont pas supprimées. Une ancienne fenêtre d’édition ne peut pas rétablir
le widget supprimé. L’action POST applique les mêmes droits et protections CSRF que
l’enregistrement et vérifie la révision avant de retirer le widget.

### Navigation des espaces

Le type `navigation` est réservé aux pages d’accueil d’espace, dans le catalogue et
sur les actions serveur. Il propose deux options indépendantes : applications et
sous-espaces. Les éléments proviennent exclusivement de l’espace courant, filtrés
par les droits du lecteur ; les applications masquées sont exclues. Un bloc masqué
ne charge aucune liste et une liste vide n’affiche aucun bloc. Les libellés et
descriptions sont traduits dans la langue du lecteur et échappés au rendu.

La configuration (titre, placement, visibilité, `apps`, `subspaces`) est enregistrée
dans `homepages/spaces/WA<id>.properties`, avec contrôle de révision et protection
CSRF. Aucune source d’un autre espace n’est sélectionnable. Les liens utilisent la
navigation Silverpeas et conservent des permaliens pour l’ouverture dans un nouvel onglet.
