# 📋 GUIDE POUR AHMED - PHASE 9

## 🎯 Commandes à exécuter DANS L'ORDRE

### ✅ ÉTAPE 1 : Se positionner correctement

```bash
# 1. Aller dans le dossier du projet
cd /home/ajinou/Bureau/Projet\ CPOO/liquid-war-upc

# 2. Se mettre sur develop
git checkout develop

# 3. Récupérer les dernières modifications (IMPORTANT!)
git pull origin develop

# 4. Vérifier que tu es bien sur le dernier commit
git log --oneline -1
# Tu DOIS voir : fd67134 - Merge feature/map-selection into develop
```

### ✅ ÉTAPE 2 : Créer ta branche pour Phase 9

```bash
# 5. Créer ta branche DEPUIS develop (là où tu es maintenant)
git checkout -b feature/time-mode

# 6. Vérifier que tu es sur la bonne branche
git branch --show-current
# Tu DOIS voir : feature/time-mode
```

### ✅ ÉTAPE 3 : Faire ton travail

```bash
# 7. Copier/Écrire ton code pour Phase 9
# (TimeModeMenuPanel.java, etc.)

# 8. Commit 1
git add <fichier1>
git commit -m "Feature: Menu sélection mode temps (Chrono/Minuterie)"
git push origin feature/time-mode

# 9. Commit 2 (si besoin)
git add <fichier2>
git commit -m "Feature: Intégration TimeModeMenuPanel dans flux menus"
git push origin feature/time-mode

# ... etc pour les autres commits
```

### ✅ ÉTAPE 4 : Merger dans develop

```bash
# 10. Revenir sur develop
git checkout develop

# 11. Récupérer les dernières modifs (au cas où)
git pull origin develop

# 12. Merger TA branche dans develop (avec --no-ff IMPORTANT!)
git merge --no-ff feature/time-mode

# 13. Pusher develop
git push origin develop
```

## ⚠️ ERREURS À ÉVITER

### ❌ NE PAS FAIRE :
```bash
# ❌ Créer la branche depuis un vieux commit
git checkout -b feature/time-mode 3dafdd9  # MAUVAIS!

# ❌ Créer la branche depuis main
git checkout main
git checkout -b feature/time-mode  # MAUVAIS!

# ❌ Oublier le pull avant de créer la branche
git checkout develop
git checkout -b feature/time-mode  # Sans "git pull" = MAUVAIS!
```

### ✅ TOUJOURS FAIRE :
```bash
# ✅ Toujours partir de develop À JOUR
git checkout develop
git pull origin develop  # ← IMPORTANT!
git checkout -b feature/time-mode  # ← Maintenant c'est BON!
```

## 🔍 Comment vérifier que c'est bon ?

Après avoir créé ta branche, vérifie :

```bash
# Afficher le graphe
git log --oneline --graph -5

# Tu DOIS voir quelque chose comme :
# * <ton-nouveau-commit> (HEAD -> feature/time-mode)
# *   fd67134 (origin/develop, develop) Merge feature/map-selection
# |\  
# | * 2198361 Fix: Cache images
# ...
```

Le commit `fd67134` (Merge feature/map-selection) **DOIT** apparaître juste avant ton premier commit.

## 📞 En cas de doute

Si tu n'es pas sûr, **AVANT** de pusher, montre-moi :

```bash
git log --oneline --graph -10
```

Et je te dirai si c'est bon ! 👍

---
*Guide créé pour éviter les erreurs de positionnement Git*
