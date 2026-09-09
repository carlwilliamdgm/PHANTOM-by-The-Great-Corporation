# PHANTOM by The Great Corporation

> *« The controller you don't hold, the power you command »*  
> **Système de Contrôleur Virtuel Ultime & Écosystème Multiplateforme**  
> *Crafted with excellence by The Great Corporation*

---

## Vue d'ensemble

**PHANTOM by The Great Corporation** transforme votre smartphone Android en un contrôleur de jeu invisible, ultra-rapide et ergonomique pour PC Windows, Smart TV et consoles. L'application allie immersion totale, réactivité extrême et personnalisation visuelle poussée (fonds d'écran, GIFs animés, packs de skins).

Le système propose deux modes d'utilisation distincts :

### 1. Mode « The Great » (Avancé & Sur-Mesure)
* **Application Serveur Windows (« Phantom Server » - `gui_app.py`)** : Dashboard avec détection automatique de l'IP du PC (Auto-Discovery), copie en 1 clic, visualiseur de manette interactif en temps réel et console d'événements.
* **Multi-connexion Zéro-Friction** : 
  * **Wi-Fi local (UDP)** : Détection automatique sans saisie manuelle d'IP et calcul de latence RTT en temps réel.
  * **Câble USB (ADB Reverse)** : Latence strictement nulle (`adb reverse tcp:8890 tcp:8890`).
  * **Wi-Fi (WebSocket)** : Mode de secours fiable.
* **Émulation Matérielle** : Émulation native Xbox 360 et PlayStation DualShock 4 via le pilote ViGEmBus.
* **Mode Hybride Clavier/Souris** : Contrôle des jeux PC non compatibles manette.

### 2. Mode « Plug & Play » (Universel Natif)
* **100 % Autonome** : Aucun serveur ni logiciel tiers requis sur l'appareil récepteur.
* **Diffusion Bluetooth sous le nom « Phantom »** : Émulation d'une manette matérielle standard reconnue instantanément par n'importe quel hôte (PC, Mac, Smart TV, console).

---

## Architecture UX à 3 Écrans

1. **Écran 1 — Accueil & Prestige** :
   * Branding officiel : **« PHANTOM by The Great Corporation »**.
   * Slogan en légende discrète : *« The controller you don't hold, the power you command »*.
   * Détection automatique instantanée du serveur PC en Wi-Fi.
   * Sélecteur de mode immédiat (*The Great* / *Plug & Play*).
2. **Écran 2 — Studio de Configuration & Customisation Intégrale** :
   * **Arrière-plans & GIFs animés** : Importez n'importe quelle image ou GIF animé depuis votre galerie via le sélecteur Android officiel avec persistance permanente et curseur d'assombrissement.
   * **Skins des Touches** : Choix instantané entre les thèmes *Xbox*, *PlayStation*, *Nintendo*, *Néon Cyberpunk* et *Ghost (Minimaliste)*.
   * **Calibration Fine** : Réglage de la sensibilité des joysticks et de la zone morte (deadzone).
   * **Paramètres Réseau Zéro-Friction** : Badge de détection automatique en premier plan et accordéon escamotable pour les réglages manuels.
3. **Écran 3 — Manette Immersive In-Game & Quick Settings** :
   * 100 % plein écran dédié au jeu (barres système masquées).
   * Pastille discrète de statut (latence en ms et batterie).
   * **Onglet Escamotable (« Quick Settings Drawer »)** : Un volet discret semi-transparent s'ouvrant d'un coup de pouce pour ajuster la sensibilité, la deadzone ou forcer une reconnexion sans jamais quitter la partie.

---

## Démarrage Rapide

### 1. Côté PC Windows (Serveur Phantom)

1. Installez les dépendances requises :
   ```bash
   cd server
   pip install -r requirements.txt
   ```
2. Installez le pilote ViGEmBus : [ViGEmBus GitHub Releases](https://github.com/ViGEm/ViGEmBus/releases).
3. Lancez l'application graphique :
   * Double-cliquez sur `Lancer_Serveur_Phantom_TGC.bat` à la racine, **ou**
   * Exécutez : `python server/gui_app.py`
4. Cliquez sur **« DÉMARRER LE SERVEUR PHANTOM »**.

### 2. Côté Smartphone Android

1. Ouvrez le projet dans **Android Studio** (Dossier `android/`).
2. Branchez votre smartphone en mode débogage USB et lancez l'application (`Run 'app'`).
3. Dès l'ouverture, **votre PC est détecté automatiquement en Wi-Fi** : le badge vert s'affiche. Cliquez simplement sur **▶ JOUER**.

---
*Propriété exclusive de The Great Corporation — Tous droits réservés.*
