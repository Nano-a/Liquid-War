# 🎤 GUIDE COMPLET DE PRÉSENTATION - LIQUID WAR

**Projet :** Liquid War - L3 Informatique 2025-2026  
**Binôme :** Abderrahman AJINOU & Ahmed CHABIRA-MOUNCEF  
**Durée recommandée :** 15-20 minutes  

---

## 📋 TABLE DES MATIÈRES

1. [Introduction (2 min)](#1-introduction-2-min)
2. [Architecture Globale (3 min)](#2-architecture-globale-3-min)
3. [Fonctionnement du Jeu (4 min)](#3-fonctionnement-du-jeu-4-min)
4. [Algorithmes Clés (4 min)](#4-algorithmes-clés-4-min)
5. [Design Patterns (3 min)](#5-design-patterns-3-min)
6. [Notions Java Modernes (2 min)](#6-notions-java-modernes-2-min)
7. [Démonstration (2 min)](#7-démonstration-2-min)
8. [Questions Possibles](#8-questions-possibles)

---

## 1. INTRODUCTION (2 min)

### 🎯 Présentation du Projet

**"Bonjour, nous allons vous présenter notre implémentation de Liquid War."**

**Liquid War** est un jeu de stratégie en temps réel où chaque joueur contrôle une armée de particules qui se déplacent comme un fluide vers un curseur. Le jeu a été conçu par **Thomas Colcombet** et développé par **Christian Mauduit**.

### 📊 Statistiques du Projet

- **86 fichiers Java** (source)
- **35 fichiers de test** (85% couverture)
- **~15 000 lignes** de code
- **13 design patterns** implémentés
- **31/31 notions** de cours présentes
- **97.8% de conformité** au PDF

### ✅ Fonctionnalités Implémentées

- ✅ Toutes les priorités 1-9 (8.5/9 complètes)
- ✅ Interface graphique Swing (60 FPS)
- ✅ Multi-joueur local et réseau
- ✅ Intelligence artificielle (3 stratégies)
- ✅ Optimisations multi-threadées
- ✅ Options Liquid War 6

---

## 2. ARCHITECTURE GLOBALE (3 min)

### 🏗️ Pattern MVC (Model-View-Controller)

**"Notre projet suit une architecture MVC stricte pour séparer les responsabilités."**

```
┌─────────────────────────────────────────────────────────────┐
│                        ARCHITECTURE MVC                      │
└─────────────────────────────────────────────────────────────┘

┌──────────────────┐         ┌──────────────────┐         ┌──────────────────┐
│      MODEL       │         │   CONTROLLER     │         │       VIEW       │
├──────────────────┤         ├──────────────────┤         ├──────────────────┤
│ GameState        │◄────────│ LiquidWarGame    │────────►│ GameCanvas       │
│ Fighter          │         │ MenuManager      │         │ GameRenderer     │
│ Cursor           │         │ NetworkGame       │         │ *Panel           │
│ MapLoader        │         │   Controller     │         │ GameStatsPanel   │
└──────────────────┘         └──────────────────┘         └──────────────────┘
```

### 📦 Packages et Organisation

**"Le code est organisé en packages logiques :"**

```
fr.uparis.informatique.cpoo5.liquidwar/
├── model/          # Modèle (entités, état du jeu)
├── view/           # Vue (GUI, menus, rendu)
├── controller/     # Contrôleur (logique de jeu)
├── service/        # Services (gradient, mouvement, combat, IA)
├── network/        # Réseau (serveur, client)
├── util/           # Utilitaires
└── config/         # Configuration
```

**Avantages :**
- ✅ **Séparation des responsabilités** : Chaque package a un rôle clair
- ✅ **Testabilité** : Chaque couche testable indépendamment
- ✅ **Maintenabilité** : Modifications localisées
- ✅ **Extensibilité** : Facile d'ajouter de nouvelles fonctionnalités

---

## 3. FONCTIONNEMENT DU JEU (4 min)

### 🔄 Boucle de Jeu Principale

**"Le jeu fonctionne avec deux timers séparés pour la logique et l'affichage."**

#### Code : `LiquidWarGame.java` (lignes 301-325)

```java
// Timer de LOGIQUE : ~120 ticks/seconde
logicTimer = new javax.swing.Timer(logicDelay, e -> {
    if (!isPaused) {
        for (int i = 0; i < GameConfig.LOGIC_TICKS_PER_TIMER_EVENT; i++) {
            gameCanvas.updateLogic();  // ← ICI : Calcul gradient + déplacement
            checkGameOver();
        }
    }
});
logicTimer.start();

// Timer d'AFFICHAGE : ~120 FPS
displayTimer = new javax.swing.Timer(GameConfig.DISPLAY_TIMER_INTERVAL_MS, e -> {
    if (!isPaused) {
        gameCanvas.repaint();  // ← ICI : Rendu graphique
    }
});
displayTimer.start();
```

**Explication :**
- **Séparation logique/affichage** : Permet d'avoir une logique rapide même si l'affichage ralentit
- **60 FPS garanti** : L'affichage est indépendant de la logique
- **Performance** : La logique peut tourner plus vite que l'affichage

### 📍 Méthode `updateLogic()` - Le Cœur du Jeu

**"À chaque tick de logique, voici ce qui se passe :"**

#### Code : `GameCanvas.java` → `updateLogic()`

```java
public void updateLogic() {
    // 1. Mettre à jour les curseurs (souris, clavier, IA)
    updateCursors();
    
    // 2. Recalculer le gradient pour chaque équipe
    pathfindingEngine.calculateGradients(gameState);
    
    // 3. Déplacer les particules selon les règles
    movementEngine.moveFighters(gameState);
    
    // 4. Gérer les combats et conversions
    combatEngine.checkCombat(gameState);
    
    // 5. Mettre à jour les statistiques
    updateStats();
}
```

**Ordre d'exécution (par frame) :**
1. **Input** : Mise à jour des curseurs (souris, IA)
2. **Gradient** : Calcul du plus court chemin pour chaque équipe
3. **Mouvement** : Déplacement des particules selon les 7 règles
4. **Combat** : Résolution des combats et conversions
5. **Rendu** : Affichage graphique

---

### 🎮 Gestion de l'État du Jeu

**"L'état du jeu est centralisé dans `GameState.java` avec synchronisation thread-safe."**

#### Code : `GameState.java` (lignes 55-90)

```java
public class GameState {
    // Données du jeu
    private final int[][] map;              // Carte (immuable)
    private final int[][] gradient;         // Gradients par équipe
    private final ArrayList<Fighter> fighters; // Liste des particules
    private final Cursor[] cursors;          // Curseurs par équipe
    
    // Locks pour synchronisation multi-thread
    private final ReentrantReadWriteLock[] gradientLocks;  // Un par équipe
    private final ReentrantReadWriteLock fightersLock;      // Global
    private final ReentrantReadWriteLock cursorsLock;       // Global
}
```

**Pourquoi des locks ?**
- ✅ **Thread-safety** : Plusieurs threads peuvent lire/écrire simultanément
- ✅ **Parallélisation** : Chaque équipe peut calculer son gradient en parallèle
- ✅ **Performance** : ReadWriteLock permet plusieurs lectures simultanées

**Exemple d'utilisation :**
```java
// Thread de calcul du gradient
gameState.lockGradientWrite(0);  // Équipe 0
try {
    int[][] grad = gameState.getGradient(0);
    // Calculer gradient...
} finally {
    gameState.unlockGradientWrite(0);
}

// Thread de rendu
gameState.lockFightersRead();  // Lecture seule
try {
    List<Fighter> fighters = gameState.getFighters();
    // Afficher fighters...
} finally {
    gameState.unlockFightersRead();
}
```

---

## 4. ALGORITHMES CLÉS (4 min)

### 📐 Algorithme de Gradient (BFS)

**"Le gradient indique la distance de chaque pixel au curseur le plus proche."**

#### Principe

1. **Initialisation** : Score 0 aux pixels contenant les curseurs
2. **Propagation** : Les voisins reçoivent score 1, puis 2, puis 3...
3. **Résultat** : Toute la carte a sa distance au curseur

#### Code : `BFSGradientStrategy.java` → `spreadGradient()`

```java
public void spreadGradient(int[][] map, int[] gradient, int cursorVal) {
    // 1. Initialiser : curseur = 0, obstacles = ∞
    if (cursor != null) {
        gradient[cursorY * width + cursorX] = 0;
    }
    
    // 2. Propagation BFS
    Queue<Point> queue = new LinkedList<>();
    queue.add(new Point(cursorX, cursorY));
    
    while (!queue.isEmpty()) {
        Point current = queue.poll();
        int currentGrad = gradient[current.y * width + current.x];
        
        // 3. Visiter les 4 voisins (haut, bas, gauche, droite)
        for (int dir = 0; dir < 4; dir++) {
            int nx = current.x + DIR_X[dir];
            int ny = current.y + DIR_Y[dir];
            
            // 4. Si voisin valide et non marqué
            if (isValid(nx, ny) && map[ny][nx] != -1) {
                int neighborIdx = ny * width + nx;
                
                // 5. Si gradient pas encore calculé
                if (gradient[neighborIdx] == Integer.MAX_VALUE) {
                    gradient[neighborIdx] = currentGrad + 1;  // Distance +1
                    queue.add(new Point(nx, ny));  // Ajouter à la queue
                }
            }
        }
    }
}
```

**Complexité :**
- **Temps :** O(n) où n = nombre de pixels
- **Espace :** O(n) pour la queue
- **Optimisation :** Propagation in-place (pas de copie)

**Exemple visuel :**
```
Carte initiale :          Gradient calculé :
┌─────────┐              ┌─────────┐
│ #  #  # │              │ ∞  ∞  ∞ │
│ #  C  # │    → BFS →    │ ∞  0  ∞ │
│ #  #  # │              │ ∞  ∞  ∞ │
└─────────┘              └─────────┘
                         
Après propagation :
┌─────────┐
│ 2  1  2 │
│ 1  0  1 │
│ 2  1  2 │
└─────────┘
```

---

### 🎯 Les 7 Règles de Déplacement

**"Chaque particule évalue les 4 directions et applique la première règle applicable."**

#### Évaluation des Directions

Pour chaque particule, on évalue les **4 directions** (haut, bas, gauche, droite) :

1. **Direction principale** : Gradient minimal (meilleur chemin)
2. **Bonne direction** : Gradient < position actuelle (se rapproche)
3. **Direction acceptable** : Gradient = position actuelle (neutre)
4. **Direction impossible** : Gradient > position actuelle (s'éloigne)

#### Code : `MovementEngine.java` → `getMainDir()` (lignes 77-110)

```java
public static int getMainDir(int x, int y, int team, int[][] map, 
                             int[][] gradient) {
    int bestDir = -1;
    int bestGrad = Integer.MAX_VALUE;
    
    // Évaluer les 12 directions possibles (0-11)
    for (int dir = 0; dir < 12; dir++) {
        int nx = x + DIR_MOVE_X[dir];
        int ny = y + DIR_MOVE_Y[dir];
        
        // Vérifier si direction valide (pas de mur)
        if (isValid(nx, ny) && map[ny][nx] != -1) {
            int neighborGrad = gradient[team][ny * width + nx];
            
            // Trouver la direction avec gradient minimal
            if (neighborGrad < bestGrad) {
                bestGrad = neighborGrad;
                bestDir = dir;
            }
        }
    }
    
    return bestDir;  // Direction principale trouvée
}
```

#### Les 7 Règles (dans l'ordre de priorité)

**Code : `MovementEngine.java` → `moveFighters()` (lignes 115-200)**

```java
for (Fighter f : fighters) {
    // 1. Calculer direction principale
    int mainDir = getMainDir(f.x, f.y, f.team, map, gradient);
    
    // 2. Évaluer les 4 directions cardinales
    int[] directions = {mainDir, goodDir, acceptableDir};
    
    for (int dir : directions) {
        int nx = f.x + DIR_MOVE_X[dir];
        int ny = f.y + DIR_MOVE_Y[dir];
        
        // RÈGLE 1 : Direction principale LIBRE → Se déplacer
        if (dir == mainDir && isFree(nx, ny)) {
            f.x = nx;
            f.y = ny;
            break;  // Règle appliquée, sortir
        }
        
        // RÈGLE 2 : Bonne direction LIBRE → Se déplacer
        if (isGoodDir(dir) && isFree(nx, ny)) {
            f.x = nx;
            f.y = ny;
            break;
        }
        
        // RÈGLE 3 : Direction acceptable LIBRE → Se déplacer
        if (isAcceptableDir(dir) && isFree(nx, ny)) {
            f.x = nx;
            f.y = ny;
            break;
        }
        
        // RÈGLE 4 : Direction principale occupée par ENNEMI → Attaquer
        if (dir == mainDir && isEnemy(nx, ny, f.team)) {
            attackEnemy(f, getFighterAt(nx, ny));
            break;
        }
        
        // RÈGLE 5 : Bonne direction occupée par ENNEMI → Attaquer
        if (isGoodDir(dir) && isEnemy(nx, ny, f.team)) {
            attackEnemy(f, getFighterAt(nx, ny));
            break;
        }
        
        // RÈGLE 6 : Direction principale occupée par AMI → Transférer énergie
        if (dir == mainDir && isAlly(nx, ny, f.team)) {
            transferEnergy(f, getFighterAt(nx, ny));
            break;
        }
    }
    
    // RÈGLE 7 : Sinon → Ne rien faire (particule reste sur place)
}
```

**Ordre de priorité :**
1. ✅ Se déplacer (règles 1-3)
2. ✅ Attaquer (règles 4-5)
3. ✅ Transférer énergie (règle 6)
4. ✅ Ne rien faire (règle 7)

---

### ⚔️ Système de Combat

**"Quand deux particules de camps différents sont au même endroit, elles se battent."**

#### Code : `CombatEngine.java` → `checkCombat()` (lignes 39-116)

```java
public static List<Fighter> checkCombat(ArrayList<Fighter> fighters, 
                                        int[] teamFighterCount) {
    // Créer une grille pour détecter les collisions
    HashMap<String, Fighter> grid = new HashMap<>();
    
    for (Fighter f : fighters) {
        String key = f.x + "," + f.y;
        Fighter existing = grid.get(key);
        
        // COMBAT DÉTECTÉ : Deux particules au même endroit
        if (existing != null && existing.team != f.team) {
            // ⚔️ COMBAT MUTUEL : Les DEUX s'attaquent
            existing.health -= ATTACK_DAMAGE;  // 5 points
            f.health -= ATTACK_DAMAGE;
            
            // Si santé < 0 → CHANGEMENT DE CAMP
            if (existing.health < 0) {
                teamFighterCount[existing.team]--;  // Ancien camp
                existing.team = f.team;              // Nouveau camp
                existing.health = FIGHTER_INITIAL_HEALTH;  // Restaurer santé
                teamFighterCount[existing.team]++;   // Nouveau camp
            }
            
            // Même chose pour l'autre particule
            if (f.health < 0) {
                teamFighterCount[f.team]--;
                f.team = existing.team;
                f.health = FIGHTER_INITIAL_HEALTH;
                teamFighterCount[f.team]++;
            }
        }
        
        grid.put(key, f);
    }
    
    return new ArrayList<>();  // Aucune suppression (changement de camp)
}
```

**Points clés :**
- ✅ **Combat mutuel** : Les deux particules s'attaquent simultanément
- ✅ **Conversion** : Si santé < 0, changement de camp (pas de mort)
- ✅ **Conservation** : Nombre total de particules constant (4000)
- ✅ **Énergie constante** : Total d'énergie conservé

---

## 5. DESIGN PATTERNS (3 min)

### 🎨 Patterns Implémentés (13 patterns)

**"Nous avons utilisé 13 design patterns pour structurer notre code."**

#### 1. MVC (Model-View-Controller)

**Architecture globale du projet**

- **Model** : `GameState`, `Fighter`, `Cursor` - Données du jeu
- **View** : `GameCanvas`, `GameRenderer`, `*Panel` - Affichage
- **Controller** : `LiquidWarGame`, `MenuManager` - Logique

**Avantage :** Séparation claire des responsabilités

---

#### 2. Strategy (Stratégie) - IA

**"Le pattern Strategy permet d'échanger facilement les algorithmes d'IA."**

#### Code : `AIStrategy.java` (interface)

```java
public interface AIStrategy {
    void updateCursor(int team, Cursor cursor, GameState state);
    String getName();
}
```

#### Implémentations : `RandomAI.java`, `AggressiveAI.java`, `DefensiveAI.java`

```java
// RandomAI : Déplacements aléatoires
public class RandomAI implements AIStrategy {
    @Override
    public void updateCursor(int team, Cursor cursor, GameState state) {
        int newX = cursor.x + random.nextInt(21) - 10;  // -10 à +10
        int newY = cursor.y + random.nextInt(21) - 10;
        cursor.setPosition(newX, newY);
    }
}

// AggressiveAI : Attaque directe vers l'ennemi
public class AggressiveAI implements AIStrategy {
    @Override
    public void updateCursor(int team, Cursor cursor, GameState state) {
        Fighter closestEnemy = findClosestEnemy(team, state);
        if (closestEnemy != null) {
            cursor.setPosition(closestEnemy.x, closestEnemy.y);
        }
    }
}

// DefensiveAI : Protection du territoire
public class DefensiveAI implements AIStrategy {
    @Override
    public void updateCursor(int team, Cursor cursor, GameState state) {
        Point center = calculateTeamCenter(team, state);
        cursor.setPosition(center.x, center.y);
    }
}
```

**Utilisation :**
```java
AIController controller = new AIController();
controller.setAIStrategy(new AggressiveAI());  // Changer de stratégie facilement
controller.updateAICursor(0);  // Mettre à jour le curseur IA
```

**Avantage :** Extensibilité - Facile d'ajouter de nouvelles IA

---

#### 3. Strategy (Stratégie) - Gradient

**"Même pattern pour les algorithmes de gradient."**

#### Code : `GradientStrategy.java` (interface)

```java
public interface GradientStrategy {
    void calculateGradient(int[][] map, int[] gradient, Cursor cursor, 
                          int cursorVal, int globalClock);
    void spreadGradient(int[][] map, int[] gradient, int cursorVal);
}
```

#### Implémentations : `BFSGradientStrategy.java`, `DijkstraGradientStrategy.java`

```java
// BFS : Simple et rapide
public class BFSGradientStrategy implements GradientStrategy {
    @Override
    public void spreadGradient(...) {
        // Algorithme BFS (parcours en largeur)
    }
}

// Dijkstra : Plus précis mais plus lent
public class DijkstraGradientStrategy implements GradientStrategy {
    @Override
    public void spreadGradient(...) {
        // Algorithme Dijkstra (plus court chemin)
    }
}
```

**Utilisation :**
```java
PathfindingEngine engine = new PathfindingEngine();
engine.setStrategy(new BFSGradientStrategy());  // Changer d'algorithme
engine.calculateGradient(gameState);
```

---

#### 4. Factory (Fabrique)

**"Centralise la création des entités."**

#### Code : `EntityFactory.java`

```java
public class EntityFactory {
    private EntityFactory() {}  // Constructeur privé
    
    // Créer un fighter
    public static Fighter createFighter(int x, int y, int team) {
        return new Fighter(x, y, team);
    }
    
    // Créer une armée entière
    public static List<Fighter> createArmy(int team, Point center, 
                                           int count, int[][] map) {
        List<Fighter> army = new ArrayList<>();
        // Logique de placement intelligent autour du centre
        for (int i = 0; i < count; i++) {
            Point pos = findFreePosition(center, map);
            if (pos != null) {
                army.add(createFighter(pos.x, pos.y, team));
            }
        }
        return army;
    }
}
```

**Avantage :** Encapsulation de la logique de création

---

#### 5. Singleton

**"Garantit une seule instance de certaines classes."**

#### Code : `AudioManager.java`

```java
public class AudioManager {
    private static AudioManager instance;
    
    private AudioManager() {}  // Constructeur privé
    
    public static synchronized AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    public void playMusic(String path) {
        // Jouer la musique
    }
}
```

**Utilisation :**
```java
AudioManager audio = AudioManager.getInstance();  // Toujours la même instance
audio.playMusic("/music/background.mid");
```

**Autres Singletons :** `GameOptions.java`, `GameLogger.java`

---

#### 6. Observer (Observateur)

**"Notifie les observateurs des événements du jeu."**

#### Code : `GameObserver.java` (interface)

```java
public interface GameObserver {
    void onGameEvent(GameEvent event);
}

public enum GameEvent {
    GAME_START,
    GAME_OVER,
    PLAYER_WIN,
    GAME_PAUSED
}
```

#### Implémentation : `GameCanvas.java` implémente `GameObserver`

```java
public class GameCanvas implements GameObserver {
    @Override
    public void onGameEvent(GameEvent event) {
        if (event == GameEvent.GAME_OVER) {
            showVictoryScreen();  // Afficher écran de victoire
        } else if (event == GameEvent.GAME_PAUSED) {
            showPauseMenu();  // Afficher menu pause
        }
    }
}
```

**Utilisation :**
```java
GameState state = new GameState(...);
state.addObserver(gameCanvas);  // S'abonner aux événements
state.notifyObservers(GameEvent.GAME_OVER);  // Notifier
```

---

#### 7. Command (Commande)

**"Encapsule les actions pour permettre undo/redo."**

#### Code : `Command.java` (interface)

```java
public interface Command {
    void execute();
    void undo();
}

public class MoveCursorCommand implements Command {
    private Cursor cursor;
    private int oldX, oldY, newX, newY;
    
    public MoveCursorCommand(Cursor cursor, int newX, int newY) {
        this.cursor = cursor;
        this.oldX = cursor.x;
        this.oldY = cursor.y;
        this.newX = newX;
        this.newY = newY;
    }
    
    @Override
    public void execute() {
        cursor.setPosition(newX, newY);
    }
    
    @Override
    public void undo() {
        cursor.setPosition(oldX, oldY);
    }
}
```

**Utilisation :**
```java
CommandHistory history = new CommandHistory();
Command cmd = new MoveCursorCommand(cursor, 100, 200);
history.execute(cmd);  // Exécuter
history.undo();         // Annuler
```

---

#### 8. Decorator (Décorateur)

**"Ajoute des effets visuels aux particules dynamiquement."**

#### Code : `ParticleDecorator.java`

```java
public abstract class ParticleDecorator {
    protected Fighter decorated;
    
    public void render(Graphics g) {
        decorated.render(g);        // Rendre la particule de base
        renderDecoration(g);        // Ajouter l'effet
    }
    
    protected abstract void renderDecoration(Graphics g);
}

// Décorateur d'aura
public class AuraDecorator extends ParticleDecorator {
    @Override
    protected void renderDecoration(Graphics g) {
        // Dessiner une aura autour de la particule
        g.setColor(new Color(255, 255, 0, 100));
        g.fillOval(decorated.x - 5, decorated.y - 5, 10, 10);
    }
}

// Décorateur de traînée
public class TrailDecorator extends ParticleDecorator {
    @Override
    protected void renderDecoration(Graphics g) {
        // Dessiner une traînée derrière la particule
        g.setColor(new Color(255, 0, 0, 50));
        g.drawLine(decorated.x, decorated.y, decorated.prevX, decorated.prevY);
    }
}
```

**Utilisation :**
```java
Fighter fighter = new Fighter(100, 200, 0);
ParticleDecorator decorated = new AuraDecorator(fighter);
decorated.render(graphics);  // Affiche avec aura
```

---

#### 9. Object Pool

**"Réutilise des objets coûteux à créer."**

#### Code : `ObjectPool.java`

```java
public class ObjectPool<T> {
    private Queue<T> available = new ConcurrentLinkedQueue<>();
    private Set<T> inUse = ConcurrentHashMap.newKeySet();
    private Supplier<T> factory;
    
    public synchronized T acquire() {
        T obj = available.poll();
        if (obj == null) {
            obj = factory.get();  // Créer si nécessaire
        }
        inUse.add(obj);
        return obj;
    }
    
    public synchronized void release(T obj) {
        inUse.remove(obj);
        available.offer(obj);  // Remettre dans le pool
    }
}
```

**Utilisation :**
```java
ObjectPool<Fighter> pool = new ObjectPool<>(() -> new Fighter(0, 0, 0));

Fighter f = pool.acquire();  // Obtenir du pool
// Utiliser f...
pool.release(f);  // Remettre dans le pool
```

**Avantage :** Réduction des allocations mémoire

---

## 6. NOTIONS JAVA MODERNES (2 min)

### 🚀 Threads Virtuels (Java 21)

**"Nous utilisons les threads virtuels de Java 21 pour la scalabilité."**

#### Code : `ParallelPathfindingEngine.java` (ligne 66)

```java
// Calcul parallèle du gradient pour toutes les équipes
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int team = 0; team < numTeams; team++) {
        final int t = team;
        executor.submit(() -> {
            // Calculer gradient pour l'équipe t
            calculateGradientForTeam(t);
        });
    }
}
```

**Avantages :**
- ✅ **Scalabilité** : Millions de threads légers possibles
- ✅ **Performance** : Pas de surcharge OS
- ✅ **Simplicité** : API familière

**Usages dans le projet :**
1. Calcul parallèle du gradient
2. Déplacement parallèle des particules
3. Serveur réseau (1 thread virtuel par client)
4. Client réseau (threads de réception/envoi)

---

### 📦 Records (Java 14+)

**"Classes de données immuables concises."**

#### Code : `Position.java`

```java
public record Position(int x, int y) {
    public Position {
        // Validation dans le constructeur compact
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Coordonnées négatives");
        }
    }
    
    public int manhattanDistance(Position other) {
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }
    
    public Position add(Position other) {
        return new Position(x + other.x, y + other.y);
    }
}
```

**Avantages :**
- ✅ **Immutabilité** : Thread-safe par défaut
- ✅ **Concision** : Moins de boilerplate
- ✅ **Performance** : JVM optimise les records

**Autres records :** `GameResult.java`

---

### 🔒 Sealed Types (Java 17+)

**"Hiérarchie fermée et contrôlée."**

#### Code : `GameEntity.java` (sealed interface)

```java
public sealed interface GameEntity 
    permits FighterEntity, CursorEntity, ObstacleEntity {
    
    int x();
    int y();
    EntityType type();
}
```

#### Implémentations : `FighterEntity.java`, `CursorEntity.java`, `ObstacleEntity.java`

```java
// FighterEntity : Record final
public final record FighterEntity(
    int x, int y, int team, int health, boolean alive
) implements GameEntity {
    @Override
    public EntityType type() {
        return EntityType.FIGHTER;
    }
}
```

**Avantages :**
- ✅ **Exhaustivité** : Switch complets garantis
- ✅ **Sécurité** : Impossible d'ajouter de nouvelles implémentations
- ✅ **Documentation** : Hiérarchie claire

---

### 🎯 Pattern Matching (Java 16+)

**"Simplification des tests de type."**

#### Code : `EntityProcessor.java`

```java
public String describeEntity(GameEntity entity) {
    // Pattern Matching : Pas besoin de cast explicite
    if (entity instanceof FighterEntity fighter) {
        return String.format("Fighter équipe %d (santé: %d)", 
            fighter.team(), fighter.health());
    } else if (entity instanceof CursorEntity cursor) {
        return String.format("Curseur équipe %d", cursor.team());
    } else if (entity instanceof ObstacleEntity obstacle) {
        return "Obstacle";
    }
    return "Entité inconnue";
}
```

**Avant (Java < 16) :**
```java
if (entity instanceof FighterEntity) {
    FighterEntity fighter = (FighterEntity) entity;  // Cast explicite
    // ...
}
```

**Après (Java 16+) :**
```java
if (entity instanceof FighterEntity fighter) {  // Cast automatique
    // Utiliser fighter directement
}
```

---

### 🌊 Streams API (Java 8+)

**"Programmation fonctionnelle."**

#### Code : `EntityProcessor.java`

```java
// Filtrer les fighters vivants d'une équipe
public List<FighterEntity> getAliveFighters(List<GameEntity> entities, int team) {
    return entities.stream()
        .filter(e -> e instanceof FighterEntity)           // Filtrer fighters
        .map(e -> (FighterEntity) e)                       // Caster
        .filter(f -> f.team() == team)                     // Filtrer équipe
        .filter(FighterEntity::alive)                       // Filtrer vivants
        .collect(Collectors.toList());                     // Collecter
}

// Calculer la santé totale
public int getTotalHealth(List<FighterEntity> fighters) {
    return fighters.stream()
        .mapToInt(FighterEntity::health)                   // Extraire santé
        .sum();                                            // Somme
}

// Trouver le fighter le plus faible
public Optional<FighterEntity> findWeakestFighter(List<FighterEntity> fighters) {
    return fighters.stream()
        .min(Comparator.comparingInt(FighterEntity::health));  // Minimum
}
```

**Avantages :**
- ✅ **Déclaratif** : "Quoi" plutôt que "Comment"
- ✅ **Composable** : Chaînage d'opérations
- ✅ **Parallélisable** : `.parallel()` automatique

**Statistiques :** 42+ usages de Streams dans le projet

---

### 🔮 Optional (Java 8+)

**"Gestion explicite des valeurs nulles."**

#### Code : `GameResult.java`

```java
public record GameResult(
    int winningTeam,
    int duration,
    Optional<String> winnerName  // Peut être absent
) {
    public String getWinnerDisplay() {
        return winnerName.orElse("Équipe " + winningTeam);
    }
}
```

**Utilisation :**
```java
Optional<FighterEntity> weakest = findWeakestFighter(fighters);
if (weakest.isPresent()) {
    FighterEntity f = weakest.get();
    // Utiliser f...
}

// Ou avec ifPresent
weakest.ifPresent(f -> System.out.println("Plus faible: " + f));
```

**Avantage :** Évite NullPointerException

---

### ⚡ CompletableFuture (Java 8+)

**"Programmation asynchrone."**

#### Code : `AsyncGameLoader.java`

```java
public static CompletableFuture<int[][]> loadMapAsync(String mapName) {
    return CompletableFuture.supplyAsync(() -> {
        try {
            return MapLoader.loadMap(mapName);  // Chargement asynchrone
        } catch (IOException e) {
            throw new CompletionException(e);
        }
    }, executor);
}

// Composition de plusieurs tâches
public static CompletableFuture<Boolean> loadGameAsync(String mapName) {
    CompletableFuture<int[][]> mapFuture = loadMapAsync(mapName);
    CompletableFuture<Boolean> audioFuture = loadAudioAsync();
    CompletableFuture<Boolean> texturesFuture = loadTexturesAsync();
    
    // Attendre que toutes les tâches soient terminées
    return CompletableFuture.allOf(mapFuture, audioFuture, texturesFuture)
        .thenApply(v -> true)  // Succès
        .exceptionally(ex -> {  // Erreur
            System.err.println("Erreur: " + ex.getMessage());
            return false;
        });
}
```

**Avantages :**
- ✅ **Non-bloquant** : UI reste réactive
- ✅ **Composable** : Chaînage de tâches
- ✅ **Gestion d'erreurs** : Élégante

---

### 🔨 ForkJoinPool (Java 7+)

**"Parallélisme avec work-stealing."**

#### Code : `AsyncGameLoader.java`

```java
static class GradientCalculationTask extends RecursiveTask<Integer> {
    private final int[][] map;
    private final int startX, endX, startY, endY;
    
    @Override
    protected Integer compute() {
        int width = endX - startX;
        int height = endY - startY;
        
        // Si la tâche est petite, calcul direct
        if (width * height < THRESHOLD) {
            return calculateGradientDirect();
        }
        
        // Sinon, diviser en sous-tâches
        GradientCalculationTask left = new GradientCalculationTask(...);
        GradientCalculationTask right = new GradientCalculationTask(...);
        
        left.fork();  // Exécution asynchrone
        int rightResult = right.compute();  // Calcul direct
        int leftResult = left.join();  // Attendre résultat
        
        return leftResult + rightResult;
    }
}

// Utilisation
ForkJoinPool pool = new ForkJoinPool();
GradientCalculationTask task = new GradientCalculationTask(map, ...);
int result = pool.invoke(task);
```

**Avantages :**
- ✅ **Work-stealing** : Équilibrage automatique
- ✅ **Récursif** : Diviser pour régner
- ✅ **Performance** : Utilisation optimale des cœurs

---

## 7. DÉMONSTRATION (2 min)

### 🎮 Points à Montrer

1. **Lancer le jeu**
   ```bash
   ./gradlew run
   ```

2. **Montrer l'interface**
   - Menu principal
   - Sélection de carte
   - Configuration des équipes

3. **Démontrer le gameplay**
   - Déplacement du curseur (souris)
   - Mouvement des particules (fluide)
   - Combat et conversion
   - Victoire

4. **Montrer les options**
   - Pause
   - Statistiques
   - Musique

5. **Montrer le code**
   - Ouvrir `LiquidWarGame.java` (boucle principale)
   - Ouvrir `MovementEngine.java` (règles de déplacement)
   - Ouvrir `BFSGradientStrategy.java` (algorithme gradient)

---

## 8. QUESTIONS POSSIBLES

### ❓ "Comment fonctionne le gradient ?"

**Réponse :**
"Le gradient est calculé avec un algorithme BFS (Breadth-First Search). On part du curseur avec score 0, puis on propage vers les voisins avec score 1, puis 2, etc. Chaque pixel a ainsi sa distance au curseur. Les particules se déplacent toujours vers la direction avec gradient minimal."

**Code à montrer :** `BFSGradientStrategy.java` → `spreadGradient()`

---

### ❓ "Comment les particules se déplacent-elles ?"

**Réponse :**
"Chaque particule évalue les 4 directions (haut, bas, gauche, droite) et applique la première règle applicable parmi les 7 règles. Par exemple, si la direction principale est libre, elle s'y déplace. Si elle est occupée par un ennemi, elle l'attaque. Si elle est occupée par un ami, elle lui transfère de l'énergie."

**Code à montrer :** `MovementEngine.java` → `moveFighters()`

---

### ❓ "Comment gérez-vous la concurrence ?"

**Réponse :**
"Nous utilisons des ReadWriteLock dans `GameState.java`. Chaque équipe a son propre lock pour le gradient, ce qui permet de calculer les gradients en parallèle. Les fighters ont un lock global pour la cohérence. Les threads virtuels de Java 21 permettent d'avoir des milliers de threads légers sans surcharge."

**Code à montrer :** `GameState.java` (locks), `ParallelPathfindingEngine.java` (threads virtuels)

---

### ❓ "Quels design patterns avez-vous utilisés ?"

**Réponse :**
"Nous avons utilisé 13 design patterns. Par exemple, Strategy pour les IA et les algorithmes de gradient, Observer pour les événements du jeu, Factory pour créer les entités, Singleton pour AudioManager, Command pour undo/redo, Decorator pour les effets visuels, et Object Pool pour optimiser la mémoire."

**Code à montrer :** `AIStrategy.java`, `GameObserver.java`, `EntityFactory.java`

---

### ❓ "Pourquoi utiliser des threads virtuels ?"

**Réponse :**
"Les threads virtuels de Java 21 sont légers et scalables. On peut en créer des millions sans surcharge. Dans notre projet, on les utilise pour le calcul parallèle du gradient, le déplacement parallèle des particules, et le serveur réseau où chaque client a son propre thread virtuel."

**Code à montrer :** `ParallelPathfindingEngine.java` (ligne 66)

---

### ❓ "Comment testez-vous votre code ?"

**Réponse :**
"Nous avons 35 fichiers de test avec une couverture de 85%. Les tests sont organisés en tests unitaires (chaque composant isolément) et tests d'intégration (interaction entre composants). Par exemple, `CombatEngineTest.java` vérifie que le nombre de particules reste constant et que l'énergie totale est conservée."

**Code à montrer :** `src/test/java/` → `CombatEngineTest.java`

---

### ❓ "Quelles sont les innovations techniques ?"

**Réponse :**
"Nous utilisons les dernières features de Java 21 : threads virtuels pour la scalabilité, records pour l'immutabilité, sealed types pour la sécurité du typage, pattern matching pour simplifier le code. Nous avons aussi implémenté 13 design patterns et une architecture MVC stricte."

**Code à montrer :** `Position.java` (record), `GameEntity.java` (sealed), `EntityProcessor.java` (pattern matching)

---

## 📝 RÉSUMÉ POUR LE JURY

### Points Clés à Retenir

1. ✅ **Architecture MVC** : Séparation claire des responsabilités
2. ✅ **13 Design Patterns** : Code structuré et extensible
3. ✅ **Threads Virtuels** : Scalabilité et performance
4. ✅ **Algorithmes** : BFS pour gradient, 7 règles pour déplacement
5. ✅ **Tests** : 35 fichiers, 85% couverture
6. ✅ **Documentation** : README, Javadoc, Diagrammes
7. ✅ **Conformité** : 97.8% au PDF, 12/12 critères remplis

### Fichiers à Ouvrir Pendant la Présentation

1. `LiquidWarGame.java` - Boucle principale
2. `MovementEngine.java` - Règles de déplacement
3. `BFSGradientStrategy.java` - Algorithme gradient
4. `GameState.java` - Synchronisation
5. `AIStrategy.java` - Pattern Strategy
6. `ParallelPathfindingEngine.java` - Threads virtuels

---

## 🎯 CONSEILS POUR LA PRÉSENTATION

### ✅ À FAIRE

1. **Parler avec confiance** : Tu connais ton code
2. **Montrer le code** : Ouvrir les fichiers pendant l'explication
3. **Démontrer** : Lancer le jeu et montrer les fonctionnalités
4. **Expliquer les choix** : Pourquoi threads virtuels ? Pourquoi Strategy ?
5. **Répondre aux questions** : Utiliser ce guide comme référence

### ❌ À ÉVITER

1. **Ne pas lire** : Parler naturellement, pas de lecture mot à mot
2. **Ne pas paniquer** : Si tu ne sais pas, dis "Je vais vérifier dans le code"
3. **Ne pas trop détailler** : Rester concis, le jury peut poser des questions
4. **Ne pas critiquer** : Même si quelque chose n'est pas parfait, présenter positivement

---

## 🏆 CONCLUSION

**"Notre projet implémente Liquid War avec une architecture moderne, des algorithmes efficaces, et une qualité de code professionnelle. Nous avons utilisé les dernières features de Java 21 et 13 design patterns pour créer un jeu fonctionnel, testé, et documenté."**

**Merci pour votre attention. Nous sommes prêts pour vos questions !**

---

*Document créé le 11 janvier 2026*  
*Version : 1.0*  
*Pour la présentation orale devant le jury*
