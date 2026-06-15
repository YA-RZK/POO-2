package core;

import com.badlogic.gdx.Game;

/**
 * Classe MainGame — ponto de entrada da aplicação libGDX.
 *
 * Carrega o {@link MissionManager} (campaign.json) e abre a primeira missão.
 * O MissionManager é passado por toda a cadeia de telas para manter o placar.
 */
public class MainGame extends Game {

    @Override
    public void create() {
        MissionManager missions = MissionManager.load();
        setScreen(new GameScreen(this, missions));
    }
}
