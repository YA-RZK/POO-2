package core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe MissionManager — gerencia a campanha como uma sequência de mapas.
 *
 * Lê o arquivo campaign.json que lista os arquivos de mapa em ordem.
 * Se campaign.json não existir, usa uma campanha padrão embutida.
 *
 * Estrutura de campaign.json:
 * <pre>
 * {
 *   "campaignName": "Operação Resistência",
 *   "maps": [
 *     "map_missao_1.json",
 *     "map_missao_2.json",
 *     "map_missao_3.json"
 *   ]
 * }
 * </pre>
 *
 * Cada arquivo de mapa é um map_config.json normal, com os campos opcionais
 * "missionTitle" e "roundLimit" no nível raiz.
 */
public class MissionManager {

    // -------------------------------------------------------------------------
    // Estado
    // -------------------------------------------------------------------------

    private final String       campaignName;
    private final List<String> mapFiles;
    private int                currentIndex = 0;

    /** Placar acumulado da campanha. */
    public int victories = 0;
    public int defeats   = 0;
    public int draws     = 0;

    /**
     * Histórico ordenado dos resultados de cada execução do mapa
     * (1 entrada por mapa concluído, na ordem em que foram jogados).
     * Usado para exibir os resultados individuais na tela final
     * e para o log em arquivo .txt.
     */
    public final List<MissionResult> resultsHistory = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Enum de resultado de missão
    // -------------------------------------------------------------------------

    public enum MissionResult { VICTORY, DEFEAT, DRAW }

    // -------------------------------------------------------------------------
    // Construtor privado
    // -------------------------------------------------------------------------

    private MissionManager(String campaignName, List<String> mapFiles) {
        this.campaignName = campaignName;
        this.mapFiles     = mapFiles;
    }

    // -------------------------------------------------------------------------
    // Fábrica
    // -------------------------------------------------------------------------

    /**
     * Carrega campaign.json (pasta local ou interna).
     * Cai para a campanha padrão embutida se não encontrar.
     */
    public static MissionManager load() {
        try {
            FileHandle fh = Gdx.files.local("campaign.json");
            if (!fh.exists()) fh = Gdx.files.internal("campaign.json");
            if (fh.exists()) {
                MissionManager mm = parse(fh.readString("UTF-8"));
                if (mm != null) return mm;
            }
        } catch (Exception e) {
            Gdx.app.error("MissionManager", "Erro ao ler campaign.json: " + e.getMessage());
        }
        Gdx.app.log("MissionManager", "campaign.json não encontrado — usando campanha padrão.");
        return loadDefault();
    }

    private static MissionManager parse(String json) {
        try {
            JsonValue root = new JsonReader().parse(json);
            String name = root.getString("campaignName", "Campanha");

            List<String> files = new ArrayList<>();
            JsonValue mapsV = root.get("maps");
            if (mapsV != null) {
                for (JsonValue mv : mapsV) files.add(mv.asString());
            }

            if (files.isEmpty()) return null;
            return new MissionManager(name, files);
        } catch (Exception e) {
            Gdx.app.error("MissionManager", "Erro ao parsear campaign.json: " + e.getMessage());
            return null;
        }
    }

    /** Campanha padrão embutida — joga o mapa 1 três vezes seguidas. */
    public static MissionManager loadDefault() {
        List<String> files = new ArrayList<>();
        files.add("map_missao_1.json");
        files.add("map_missao_1.json");
        files.add("map_missao_1.json");
        return new MissionManager("Campanha Padrão - 3 Execuções", files);
    }

    // -------------------------------------------------------------------------
    // Navegação
    // -------------------------------------------------------------------------

    /** Retorna o MapConfig do mapa atual (usa loadDefault se o arquivo não existir). */
    public MapConfig currentConfig() {
        if (isFinished()) return MapConfig.loadDefault();
        return MapConfig.loadFile(mapFiles.get(currentIndex));
    }

    public void advance(MissionResult result) {
        advance(result, "");
    }

    /**
     * Avança para o próximo mapa registrando o resultado do atual.
     * Também grava o resultado no arquivo campaign_log.txt.
     *
     * @param result      resultado da missão concluída
     * @param description mensagem descritiva do resultado (opcional, vai para o log)
     */
    public void advance(MissionResult result, String description) {
        String mapFile   = currentFileName();
        int    missionNo = currentMissionNumber();

        switch (result) {
            case VICTORY: victories++; break;
            case DEFEAT:  defeats++;   break;
            case DRAW:    draws++;     break;
        }

        resultsHistory.add(result);
        CampaignLogger.logMissionResult(missionNo, totalMissions(), mapFile, result, description);

        currentIndex++;
    }

    /** Número do mapa atual (base-1). */
    public int currentMissionNumber() { return currentIndex + 1; }

    /** Total de missões na campanha. */
    public int totalMissions() { return mapFiles.size(); }

    /** Nome do arquivo do mapa atual. */
    public String currentFileName() {
        return isFinished() ? "(fim)" : mapFiles.get(currentIndex);
    }

    /** True se todos os mapas já foram jogados. */
    public boolean isFinished() { return currentIndex >= mapFiles.size(); }

    public String getCampaignName() { return campaignName; }

    /** Resumo do placar da campanha. */
    public String scoreString() {
        return "Vitórias: " + victories + "   Derrotas: " + defeats + "   Empates: " + draws;
    }

    /** Resumo final com resultado global. */
    public String finalVerdict() {
        if (victories > defeats) return "CAMPANHA CONCLUÍDA — VITÓRIA GERAL!";
        if (defeats > victories) return "CAMPANHA CONCLUÍDA — DERROTA GERAL.";
        return "CAMPANHA CONCLUÍDA — EMPATE.";
    }
}
