# 📂 Resources / Maps (Ressources / Cartes)

## 📋 Description
Le dossier **maps** contient les fichiers de cartes du jeu Liquid War. Chaque carte est composée de deux fichiers : un fichier image BMP et un fichier texte pour les métadonnées.

---

## 📄 Fichiers

### 1️⃣ `2d.bmp` ⭐ (CARTE PRINCIPALE)

**Rôle** : Fichier image bitmap qui définit la topologie de la carte (obstacles et zones libres).

#### 📊 Caractéristiques techniques

| Propriété | Valeur |
|-----------|--------|
| **Format** | BMP (Bitmap) non compressé |
| **Dimensions** | 281×240 pixels |
| **Profondeur** | 24 bits (RGB) ou 8 bits (niveaux de gris) |
| **Taille fichier** | ~67 Ko |
| **Total pixels** | 67 440 |
| **Pixels noirs** | 36 761 (54,5%) = Obstacles |
| **Pixels blancs** | 30 679 (45,5%) = Zones libres |

#### 🎨 Convention des couleurs

Le fichier BMP utilise un système simple de 2 couleurs :

| Couleur | Valeur RGB | Niveau de gris | Signification | Valeur dans la grille |
|---------|------------|----------------|---------------|----------------------|
| **Noir** | (0, 0, 0) | < 128 | **Obstacle** (mur) | `-1` |
| **Blanc** | (255, 255, 255) | ≥ 128 | **Zone libre** | `0` |

**Note** : Le seuil de 128 est utilisé pour déterminer si un pixel est noir ou blanc :
```java
int gray = (R + G + B) / 3;
if (gray < 128) {
    // Pixel noir → Obstacle (-1)
} else {
    // Pixel blanc → Zone libre (0)
}
```

#### 🗺️ Structure de la carte

La carte `2d.bmp` représente un labyrinthe en 2D avec :
- **Murs extérieurs** : Délimitent la zone de jeu
- **Obstacles intérieurs** : Créent des couloirs et des passages
- **Zones ouvertes** : Permettent les batailles entre armées

**Exemple visuel** (simplifié) :
```
████████████████████████████████
█                              █
█  ████  ████████  ████  ████  █
█  ████      ██      ██  ████  █
█        ██      ██            █
█  ████  ██████████  ██  ████  █
█  ████      ██      ██  ████  █
█                              █
████████████████████████████████
```
- `█` = Obstacle (pixel noir)
- ` ` = Zone libre (pixel blanc)

#### 📐 Dimensions et zoom

Les dimensions de la carte affectent la taille de la fenêtre selon le facteur de zoom :

| Zoom | Largeur fenêtre | Hauteur fenêtre | Résolution totale |
|------|-----------------|-----------------|-------------------|
| x1 | 281 | 240 | Trop petit |
| x2 | 562 | 480 | Compact |
| x3 | 843 | 720 | Moyen |
| x4 | 1124 | 960 | Grand |
| x5 | 1405 | 1200 | Très grand |
| **x6** | **1686** | **1440** | **Actuel** ⭐ |

**Configuration actuelle** : Zoom x6 pour une fenêtre de ~1700×1440 pixels.

#### 🔧 Comment charger la carte

La carte est chargée par `MapLoader.loadMapFromBMP()` :
```java
int[][] map = MapLoader.loadMapFromBMP(
    "src/main/resources/maps/2d.bmp",
    281,  // Largeur
    240   // Hauteur
);
```

**Retour** :
- Grille 2D `int[240][281]`
- `map[y][x] = -1` pour obstacles
- `map[y][x] = 0` pour zones libres

---

### 2️⃣ `2d.txt` (MÉTADONNÉES)

**Rôle** : Fichier texte qui contient le nom de la carte et éventuellement d'autres informations.

#### 📄 Contenu

```
Kasper - 2d
```

**Structure** :
- **Ligne 1** : Nom de la carte (obligatoire)
- **Lignes suivantes** : Réservées pour futures métadonnées (auteur, difficulté, etc.)

#### 🔧 Comment lire le nom

Le nom est lu par `MapLoader.readMapName()` :
```java
String name = MapLoader.readMapName("src/main/resources/maps/2d.txt");
// Retourne "Kasper - 2d"
```

---

## 📂 Structure des fichiers

```
src/main/resources/maps/
├── 2d.bmp          ← Image de la carte (281×240 pixels)
└── 2d.txt          ← Nom de la carte ("Kasper - 2d")
```

---

## 🎯 Utilisation dans le jeu

### 1. Au démarrage du jeu

```java
// Dans LiquidWarGame.initializeGame()
map = MapLoader.loadMapFromBMP("src/main/resources/maps/2d.bmp", 281, 240);
String mapName = MapLoader.readMapName("src/main/resources/maps/2d.txt");
```

### 2. Pendant le jeu

```java
// Vérifier si une position est un obstacle
if (map[y][x] == -1) {
    // Obstacle : les combattants ne peuvent pas passer
} else {
    // Zone libre : les combattants peuvent se déplacer
}
```

### 3. Lors du rendu

```java
// Dans GameArea.drawStaticBuffer()
if (map[y][x] == -1) {
    staticBufferPixels[idx] = obstacleColor;  // Gris foncé
} else {
    staticBufferPixels[idx] = backgroundColor;  // Bleu foncé
}
```

---

## 🗺️ Ajouter une nouvelle carte

Pour ajouter une nouvelle carte au jeu :

### Étape 1 : Créer l'image BMP

1. **Ouvrir un éditeur d'images** (GIMP, Photoshop, Paint.NET, etc.)
2. **Créer une nouvelle image** :
   - Dimensions : largeur et hauteur au choix (ex : 400×300)
   - Mode couleur : RVB ou Niveaux de gris
3. **Dessiner la carte** :
   - Utiliser **noir** pour les obstacles (murs)
   - Utiliser **blanc** pour les zones libres
4. **Enregistrer au format BMP** :
   - Fichier → Enregistrer sous → Choisir "BMP"
   - Nom : `ma_carte.bmp`
   - Emplacement : `src/main/resources/maps/`

### Étape 2 : Créer le fichier TXT

1. **Créer un fichier texte** : `ma_carte.txt`
2. **Écrire le nom de la carte** sur la première ligne :
   ```
   Ma Carte Personnalisée
   ```
3. **Enregistrer** dans `src/main/resources/maps/`

### Étape 3 : Charger la carte dans le jeu

Modifier `LiquidWarGame.java` pour utiliser la nouvelle carte :
```java
map = MapLoader.loadMapFromBMP(
    "src/main/resources/maps/ma_carte.bmp",
    400,  // Nouvelle largeur
    300   // Nouvelle hauteur
);
```

---

## 🎨 Conseils de conception

### 1. Dimensions recommandées

| Taille | Dimensions | Usage |
|--------|------------|-------|
| **Petite** | 200×150 | Batailles rapides |
| **Moyenne** | 300×250 | Équilibré |
| **Grande** | 400×300 | Batailles épiques |
| **Très grande** | 500×400 | Mode stratégique |

**Attention** : Des cartes trop grandes peuvent ralentir le jeu (calcul de gradient).

### 2. Ratio d'obstacles

| Pourcentage | Effet |
|-------------|-------|
| **30-40%** | Carte ouverte, combat direct |
| **40-50%** | Équilibré, couloirs larges |
| **50-60%** | Carte dense, stratégie importante (comme `2d.bmp`) |
| **60-70%** | Labyrinthe, mouvement limité |

**Optimal** : 50-60% d'obstacles pour un bon équilibre.

### 3. Motifs intéressants

#### Labyrinthe
```
█ █ █ █ █
█     █   █
█ █ █   █ █
█   █ █   █
█ █ █ █ █ █
```

#### Îles
```
      ███
  █   ███   █
      ███
███       ███
███   █   ███
███       ███
```

#### Spirale
```
███████████
█         █
█ ███████ █
█ █     █ █
█ █ ███ █ █
█ █     █ █
█ ███████ █
█         █
███████████
```

### 4. Zones de départ

Laisser **des espaces ouverts** pour les zones de départ des joueurs :
- Coin supérieur gauche (joueur 1)
- Coin supérieur droit (joueur 2)
- Coins inférieurs (joueurs 3-4)
- Centre haut/bas (joueurs 5-6)

---

## 🐛 Résolution de problèmes

### ❌ Carte non trouvée

**Symptôme** :
```
✗ Fichier NON TROUVÉ: src/main/resources/maps/2d.bmp
→ Création d'une carte par défaut...
```

**Causes possibles** :
1. Le fichier n'est pas dans le bon dossier
2. Le nom du fichier est incorrect
3. Problème de droits d'accès

**Solutions** :
```bash
# Vérifier l'emplacement
ls -la src/main/resources/maps/

# Vérifier les droits
chmod 644 src/main/resources/maps/2d.bmp

# Vérifier le nom exact (sensible à la casse)
```

---

### ❌ Image non chargée (null)

**Symptôme** :
```
✗ Erreur: ImageIO.read() a retourné null
```

**Causes possibles** :
1. Le fichier n'est pas un BMP valide
2. Le fichier est corrompu
3. Format BMP non supporté (ex : BMP compressé)

**Solutions** :
1. Réenregistrer l'image au format BMP non compressé
2. Utiliser GIMP : Fichier → Exporter → BMP → Décocher "Compression RLE"

---

### ❌ Dimensions incorrectes

**Symptôme** :
- La carte est coupée
- Des zones noires apparaissent

**Causes** :
- Les dimensions passées à `loadMapFromBMP()` ne correspondent pas aux dimensions réelles de l'image

**Solution** :
```java
// Vérifier les dimensions réelles de l'image
BufferedImage img = ImageIO.read(new File("2d.bmp"));
System.out.println("Dimensions: " + img.getWidth() + "x" + img.getHeight());

// Utiliser ces dimensions dans le code
map = MapLoader.loadMapFromBMP("2d.bmp", img.getWidth(), img.getHeight());
```

---

### ❌ Carte inversée (noir = libre, blanc = obstacle)

**Symptôme** :
- Les combattants se déplacent uniquement sur les zones noires
- Le jeu semble impossible

**Cause** :
- L'image a les couleurs inversées

**Solution** :
1. **Option 1** : Inverser les couleurs dans un éditeur d'images
   - GIMP : Couleurs → Inverser
2. **Option 2** : Modifier le code de `MapLoader.java` :
   ```java
   // Inverser la condition
   map[y][x] = (gray < 128) ? 0 : -1;  // Au lieu de (gray < 128) ? -1 : 0
   ```

---

## 📊 Statistiques de la carte actuelle (2d.bmp)

D'après les logs du chargement :

```
==========================================
Carte "Kasper - 2d"
==========================================
Dimensions      : 281×240 pixels
Total pixels    : 67 440
Murs (noir)     : 36 761 pixels (54,5%)
Zones libres    : 30 679 pixels (45,5%)
Ratio obstacles : 54,5% (équilibré)
Zone de jeu     : 10,8 m² (à l'échelle du jeu)
==========================================
```

---

## 🔧 Formats supportés et non supportés

### ✅ Formats supportés
- **BMP non compressé** (24 bits RGB, 8 bits niveaux de gris)
- **Dimensions** : Minimum 50×50, maximum 1000×1000 (recommandé)

### ❌ Formats non supportés
- **BMP compressé** (RLE)
- **PNG, JPEG, GIF** (non supportés actuellement, mais possibles avec modification du code)
- **Images avec plus de 2 couleurs** (seules noir et blanc sont utilisées)

---

## 🚀 Évolutions possibles

### Futures fonctionnalités

1. **Support de formats supplémentaires** :
   - PNG (avec transparence)
   - JPEG (avec seuil de gris)
   - SVG (vectoriel)

2. **Cartes multi-niveaux** :
   - Zones avec vitesse variable (eau, boue, etc.)
   - Téléporteurs (portails)
   - Zones de boost

3. **Métadonnées étendues** :
   ```
   Nom: Kasper - 2d
   Auteur: Kasper
   Difficulté: Moyenne
   Joueurs: 2-6
   Temps: 5-10 minutes
   Description: Labyrinthe classique avec couloirs étroits
   ```

4. **Éditeur de cartes intégré** :
   - Créer et modifier des cartes dans le jeu
   - Sauvegarder en BMP
   - Prévisualisation en temps réel

5. **Génération procédurale** :
   - Créer des cartes aléatoires
   - Algorithmes de labyrinthe (DFS, Prim, etc.)

---

## 📚 Références

Le format de carte BMP est inspiré du code C original de Liquid War :
- **Fichier C** : `model/map.c` (fonction `lw_map_create_from_bitmap`)
- **Différences** :
  - Le code C supporte plus de formats (Allegro datafiles)
  - Le code Java est simplifié (uniquement BMP)
  - Convention des valeurs inversée dans certaines versions C

---

## 📝 Notes importantes

1. **Convention des valeurs** :
   - `-1` = Obstacle (noir)
   - `0` = Libre (blanc)
   - **Ne jamais utiliser `1`** pour obstacles (erreur courante)

2. **Seuil de gris** :
   - `< 128` → noir → obstacle
   - `≥ 128` → blanc → libre
   - Les nuances de gris sont simplifiées en noir ou blanc

3. **Optimisation** :
   - Le chargement est fait **une seule fois** au démarrage
   - La carte est stockée en mémoire (67 440 entiers = ~270 Ko)
   - Pas de recharge pendant le jeu

4. **Compatibilité** :
   - Les cartes créées pour Liquid War 5 (C) sont **compatibles**
   - Il suffit de les copier dans `src/main/resources/maps/`

5. **Performance** :
   - Temps de chargement : **< 100ms** pour 2d.bmp
   - Très peu d'impact sur les performances du jeu

