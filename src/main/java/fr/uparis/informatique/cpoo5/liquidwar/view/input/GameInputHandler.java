package fr.uparis.informatique.cpoo5.liquidwar.view.input;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;
import fr.uparis.informatique.cpoo5.liquidwar.controller.command.CommandHistory;
import fr.uparis.informatique.cpoo5.liquidwar.controller.command.MoveCursorCommand;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;
import fr.uparis.informatique.cpoo5.liquidwar.util.GameLogger;
import fr.uparis.informatique.cpoo5.liquidwar.view.WaterDistortionEffect;

/**
 * Gestionnaire des événements d'entrée (clavier et souris) pour GameCanvas.
 * 
 * Responsabilités :
 * - Gestion des touches clavier (flèches, ZQSD, raccourcis)
 * - Gestion des événements souris
 * - Délégation des actions au contrôleur approprié
 */
public class GameInputHandler implements KeyListener, MouseListener, MouseMotionListener {

    // Interface pour notifier les actions
    public interface InputListener {
        void onCursorMoveRequested(int team, int x, int y);

        void onPauseRequested();

        void onWaterEffectToggle();

        void onOptimizedEngineToggle();
    }

    private InputListener listener;
    private CommandHistory commandHistory;
    private WaterDistortionEffect waterFX;

    // État des touches flèches
    private boolean keyUp = false;
    private boolean keyDown = false;
    private boolean keyLeft = false;
    private boolean keyRight = false;

    // État des touches ZQSD
    private boolean keyZ = false;
    private boolean keyQ = false;
    private boolean keyS = false;
    private boolean keyD = false;

    // Configuration
    private String[] teamControlTypes;
    private String[] teamTypes;
    private int activeTeams;
    private int playerTeam = 0;
    private boolean networkMode = false;
    private Cursor[] cursors;
    private int canvasWidth;
    private int canvasHeight;

    public GameInputHandler(InputListener listener, CommandHistory commandHistory) {
        this.listener = listener;
        this.commandHistory = commandHistory;
    }

    public void setWaterEffect(WaterDistortionEffect waterFX) {
        this.waterFX = waterFX;
    }

    public void setConfiguration(String[] teamControlTypes, String[] teamTypes,
            int activeTeams, int playerTeam, boolean networkMode,
            Cursor[] cursors, int canvasWidth, int canvasHeight) {
        this.teamControlTypes = teamControlTypes;
        this.teamTypes = teamTypes;
        this.activeTeams = activeTeams;
        this.playerTeam = playerTeam;
        this.networkMode = networkMode;
        this.cursors = cursors;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Raccourcis undo/redo (Ctrl+Z, Ctrl+Y)
        if (e.isControlDown()) {
            if (e.getKeyCode() == KeyEvent.VK_Z && !isZQSDKey(e.getKeyCode())) {
                if (commandHistory != null && commandHistory.canUndo()) {
                    commandHistory.undo();
                    GameLogger.getInstance().debug("Undo: Curseur déplacé");
                }
                return;
            } else if (e.getKeyCode() == KeyEvent.VK_Y) {
                if (commandHistory != null && commandHistory.canRedo()) {
                    commandHistory.redo();
                    GameLogger.getInstance().debug("Redo: Curseur déplacé");
                }
                return;
            }
        }

        // Enregistrer l'état des touches FLÈCHES
        if (e.getKeyCode() == KeyEvent.VK_UP)
            keyUp = true;
        if (e.getKeyCode() == KeyEvent.VK_DOWN)
            keyDown = true;
        if (e.getKeyCode() == KeyEvent.VK_LEFT)
            keyLeft = true;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT)
            keyRight = true;

        // Enregistrer l'état des touches ZQSD
        if (e.getKeyCode() == KeyEvent.VK_Z)
            keyZ = true;
        if (e.getKeyCode() == KeyEvent.VK_Q)
            keyQ = true;
        if (e.getKeyCode() == KeyEvent.VK_S)
            keyS = true;
        if (e.getKeyCode() == KeyEvent.VK_D)
            keyD = true;

        // Touche W : activer/désactiver l'effet d'eau
        if (e.getKeyCode() == KeyEvent.VK_W && waterFX != null && listener != null) {
            listener.onWaterEffectToggle();
        }

        // Touche O : activer/désactiver le moteur optimisé
        if (e.getKeyCode() == KeyEvent.VK_O && listener != null) {
            listener.onOptimizedEngineToggle();
        }

        // Touche Échap : pause
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && listener != null) {
            listener.onPauseRequested();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Libérer l'état des touches FLÈCHES
        if (e.getKeyCode() == KeyEvent.VK_UP)
            keyUp = false;
        if (e.getKeyCode() == KeyEvent.VK_DOWN)
            keyDown = false;
        if (e.getKeyCode() == KeyEvent.VK_LEFT)
            keyLeft = false;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT)
            keyRight = false;

        // Libérer l'état des touches ZQSD
        if (e.getKeyCode() == KeyEvent.VK_Z)
            keyZ = false;
        if (e.getKeyCode() == KeyEvent.VK_Q)
            keyQ = false;
        if (e.getKeyCode() == KeyEvent.VK_S)
            keyS = false;
        if (e.getKeyCode() == KeyEvent.VK_D)
            keyD = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Non utilisé
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (listener == null || cursors == null || teamControlTypes == null) {
            return;
        }

        // Trouver quelle équipe HUMAINE utilise la souris
        int mouseTeam = -1;
        for (int i = 0; i < teamControlTypes.length && i < activeTeams; i++) {
            if (isTeamHuman(i) && "Souris".equals(teamControlTypes[i])) {
                mouseTeam = i;
                break;
            }
        }

        if (mouseTeam < 0 || cursors[mouseTeam] == null) {
            return;
        }

        // Calculer la position dans l'espace du jeu
        double scaleX = (double) GameConfig.MAP_WIDTH / canvasWidth;
        double scaleY = (double) GameConfig.MAP_HEIGHT / canvasHeight;
        int newX = (int) (e.getX() * scaleX);
        int newY = (int) (e.getY() * scaleY);

        // Limiter aux bords
        newX = Math.max(10, Math.min(GameConfig.MAP_WIDTH - 10, newX));
        newY = Math.max(10, Math.min(GameConfig.MAP_HEIGHT - 10, newY));

        // Utiliser Command Pattern pour undo/redo (si équipe du joueur principal)
        if (mouseTeam == playerTeam && commandHistory != null && cursors[mouseTeam] != null) {
            Point newPos = new Point(newX, newY);
            MoveCursorCommand cmd = new MoveCursorCommand(cursors[mouseTeam], newPos);
            commandHistory.execute(cmd);
        }

        // Notifier le listener
        listener.onCursorMoveRequested(mouseTeam, newX, newY);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Non utilisé
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Non utilisé
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Non utilisé
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Non utilisé
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Non utilisé
    }

    /**
     * Vérifie si une touche fait partie des contrôles ZQSD.
     */
    private boolean isZQSDKey(int keyCode) {
        return keyCode == KeyEvent.VK_Z || keyCode == KeyEvent.VK_Q ||
                keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_D;
    }

    /**
     * Vérifie si une équipe est contrôlée par un humain.
     */
    private boolean isTeamHuman(int team) {
        if (teamTypes == null || team < 0 || team >= teamTypes.length) {
            return false;
        }
        return "Humain".equals(teamTypes[team]);
    }

    /**
     * Obtient l'état des touches pour le calcul du mouvement.
     */
    public KeyState getKeyState() {
        return new KeyState(keyUp, keyDown, keyLeft, keyRight, keyZ, keyQ, keyS, keyD);
    }

    /**
     * État des touches pour le mouvement.
     */
    public static class KeyState {
        public final boolean up, down, left, right;
        public final boolean z, q, s, d;

        public KeyState(boolean up, boolean down, boolean left, boolean right,
                boolean z, boolean q, boolean s, boolean d) {
            this.up = up;
            this.down = down;
            this.left = left;
            this.right = right;
            this.z = z;
            this.q = q;
            this.s = s;
            this.d = d;
        }
    }
}
