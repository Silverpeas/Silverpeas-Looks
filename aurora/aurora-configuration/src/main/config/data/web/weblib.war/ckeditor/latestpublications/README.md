# CKEditor 4 : dernières publications

Plugin externe livré par Aurora sous `/weblib/ckeditor/latestpublications/`.
Il insère un widget modifiable par double-clic. Les champs numériques doivent
être des entiers strictement positifs. Valeurs initiales : maximum 5,
sous-espaces exclus. Le texte de prévisualisation est retiré à la sauvegarde.
Le plugin génère uniquement le conteneur enregistré. À la consultation, le script
`silverpeas-latest-publications.js` appelle `/LatestPublications` avec la session
du lecteur. Le serveur utilise les publications autorisées, les exclusions Aurora
et le rendu `displayPublications` des pages d’accueil. Le lien « plus » global
est omis pour ces blocs ciblés. Plusieurs blocs peuvent cohabiter.

Le script est chargé par le layout Aurora et les accueils ; il observe aussi les
iframes de même origine et les contenus ajoutés dynamiquement, en ignorant CKEditor.
Pour une page autonome hors layout, inclure le script et les feuilles du thème Aurora.
La liste est calculée à chaque chargement de page ; aucun contenu chargé n’est enregistré
dans le widget CKEditor.

## Activation

Dans la configuration CKEditor personnalisée de l’installation, enregistrer
le chemin du plugin, puis l’ajouter à la liste existante et aux barres voulues :

```javascript
CKEDITOR.plugins.addExternal('latestpublications', '/weblib/ckeditor/latestpublications/', 'plugin.js');
// Dans CKEDITOR.editorConfig, après la configuration existante :
config.extraPlugins += ',latestpublications';
config.toolbar_Default.find(function(group) {
  return group.name === 'insert';
}).items.push('Latestpublications');
```

Le nom du bouton est `Latestpublications` (majuscule initiale), tandis que le nom
du plugin et de la commande reste `latestpublications`.

Ne pas remplacer les autres plugins ou les barres existantes. Ajouter également
le bouton aux autres barres souhaitées (par exemple `toolbar_Light`).
Les plugins CKEditor `widget` et `dialog` sont requis. Pour un éditeur inline,
inclure aussi `styles/editor.css` dans la page hôte.

## Vérification

Depuis la racine du dépôt : `node --test aurora/aurora-configuration/src/test/javascript/latestpublications.test.cjs`.
En instance : insérer, annuler, modifier par double-clic, passer en mode source,
sauvegarder et recharger. Vérifier que la div reste vide et conserve les trois
attributs, notamment `data-include-subspaces="false"` lorsque la case est décochée.
