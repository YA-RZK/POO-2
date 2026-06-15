package core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Tela final exibida após a conclusão de todas as missões da campanha.
 *
 * Mostra o placar completo, o veredicto geral e aguarda o fechamento da janela.
 */
public class CampaignEndScreen implements Screen {

    private final MainGame       game;
    private final MissionManager missions;

    private ShapeRenderer shape;
    private SpriteBatch   batch;
    private BitmapFont    fontBig;
    private BitmapFont    fontMed;
    private BitmapFont    fontSmall;

    private static final Color COL_BG      = new Color(0.04f, 0.04f, 0.08f, 1f);
    private static final Color COL_PANEL   = new Color(0.08f, 0.08f, 0.14f, 1f);
    private static final Color COL_VICTORY = new Color(0.20f, 0.90f, 0.40f, 1f);
    private static final Color COL_DEFEAT  = new Color(0.95f, 0.25f, 0.25f, 1f);
    private static final Color COL_DRAW    = new Color(0.90f, 0.80f, 0.20f, 1f);

    public CampaignEndScreen(MainGame game, MissionManager missions) {
        this.game     = game;
        this.missions = missions;
    }

    @Override
    public void show() {
        shape    = new ShapeRenderer();
        batch    = new SpriteBatch();
        fontBig  = new BitmapFont(); fontBig.getData().setScale(4.0f);
        fontMed  = new BitmapFont(); fontMed.getData().setScale(2.2f);
        fontSmall= new BitmapFont(); fontSmall.getData().setScale(1.5f);

        // Grava o resumo final da campanha em campaign_log.txt
        CampaignLogger.logCampaignSummary(missions);
    }

    @Override
    public void render(float delta) {
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        Gdx.gl.glClearColor(COL_BG.r, COL_BG.g, COL_BG.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int panelW = (int)(sw * 0.70f);
        int panelH = (int)(sh * 0.80f);
        int panelX = (sw - panelW) / 2;
        int panelY = (sh - panelH) / 2;

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(COL_PANEL);
        shape.rect(panelX, panelY, panelW, panelH);
        shape.setColor(verdictColor());
        shape.rect(panelX, panelY + panelH - 10, panelW, 10);
        shape.end();

        batch.begin();

        int cx    = panelX + 40;
        int topY  = panelY + panelH - 40;
        int lineH = 60;

        // Nome da campanha
        fontSmall.setColor(new Color(0.70f, 0.70f, 0.80f, 1f));
        fontSmall.draw(batch, missions.getCampaignName().toUpperCase(), cx, topY);

        // Veredito
        fontBig.setColor(verdictColor());
        fontBig.draw(batch, missions.finalVerdict(), cx, topY - lineH);

        // Placar
        fontMed.setColor(new Color(0.80f, 0.80f, 1.00f, 1f));
        fontMed.draw(batch, "Resultado final:", cx, topY - lineH * 2 - 10);

        fontMed.setColor(COL_VICTORY);
        fontMed.draw(batch, "Vitórias:  " + missions.victories, cx, topY - lineH * 3 - 10);
        fontMed.setColor(COL_DEFEAT);
        fontMed.draw(batch, "Derrotas:  " + missions.defeats,   cx, topY - lineH * 4 - 10);
        fontMed.setColor(COL_DRAW);
        fontMed.draw(batch, "Empates:   " + missions.draws,     cx, topY - lineH * 5 - 10);

        // ---- Resultados individuais de cada execução do mapa ----
        int listY = topY - lineH * 6 - 10;
        fontMed.setColor(new Color(0.80f, 0.80f, 1.00f, 1f));
        fontMed.draw(batch, "Resultados das " + missions.resultsHistory.size() + " execuções:", cx, listY);

        int lineHSmall = 36;
        for (int i = 0; i < missions.resultsHistory.size(); i++) {
            MissionManager.MissionResult r = missions.resultsHistory.get(i);
            fontSmall.setColor(colorFor(r));
            String label = (i + 1) + ") " + labelFor(r);
            fontSmall.draw(batch, label, cx, listY - lineHSmall * (i + 1) - 10);
        }

        // Instrução
        fontSmall.setColor(new Color(0.45f, 0.45f, 0.55f, 1f));
        fontSmall.draw(batch, "Feche a janela para sair.", cx, panelY + 30);

        batch.end();
    }

    private Color verdictColor() {
        if (missions.victories > missions.defeats) return COL_VICTORY;
        if (missions.defeats > missions.victories) return COL_DEFEAT;
        return COL_DRAW;
    }

    /** Cor correspondente ao resultado de uma execução individual. */
    private Color colorFor(MissionManager.MissionResult r) {
        switch (r) {
            case VICTORY: return COL_VICTORY;
            case DEFEAT:  return COL_DEFEAT;
            default:      return COL_DRAW;
        }
    }

    /** Rótulo em texto do resultado de uma execução individual. */
    private String labelFor(MissionManager.MissionResult r) {
        switch (r) {
            case VICTORY: return "VITÓRIA";
            case DEFEAT:  return "DERROTA";
            default:      return "EMPATE";
        }
    }

    @Override public void resize(int w, int h) {
        shape.getProjectionMatrix().setToOrtho2D(0,0,w,h);
        batch.getProjectionMatrix().setToOrtho2D(0,0,w,h);
    }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}
    @Override public void dispose() {
        shape.dispose();
        batch.dispose();
        fontBig.dispose();
        fontMed.dispose();
        fontSmall.dispose();
    }
}
