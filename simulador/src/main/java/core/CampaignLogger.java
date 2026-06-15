package core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Classe CampaignLogger — grava o histórico da campanha em um arquivo .txt
 * (campaign_log.txt), permitindo verificar depois se cada mapa terminou
 * em vitória, derrota ou empate.
 *
 * O arquivo é criado/atualizado na pasta de execução do programa
 * (mesmo local do .jar / projeto), usando Gdx.files.local().
 * Cada execução do programa acrescenta novas linhas ao final do arquivo
 * (não sobrescreve o histórico anterior).
 */
public class CampaignLogger {

    private static final String LOG_FILE = "campaign_log.txt";

    /** Escreve uma linha simples no log, com timestamp. */
    public static void log(String message) {
        try {
            FileHandle fh = Gdx.files.local(LOG_FILE);

            System.out.println("LOG PATH: " + fh.file().getAbsolutePath());

            String timestamp =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        fh.writeString(
                "[" + timestamp + "] " + message + "\n",
                true,
                "UTF-8"
        );

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    /** Registra o resultado de uma missão/mapa concluído. */
    public static void logMissionResult(int missionNumber, int totalMissions, String mapFile,
                                         MissionManager.MissionResult result, String description) {
        log("Mapa " + missionNumber + "/" + totalMissions + " (" + mapFile + ") -> "
            + resultLabel(result)
            + (description != null && !description.isEmpty() ? " | " + description : ""));
    }

    /** Registra o resumo final da campanha (chamado na tela de fim de campanha). */
    public static void logCampaignSummary(MissionManager missions) {
        log("==================================================");
        log("RESUMO DA CAMPANHA: " + missions.getCampaignName());
        for (int i = 0; i < missions.resultsHistory.size(); i++) {
            log("  Execução " + (i + 1) + ": " + resultLabel(missions.resultsHistory.get(i)));
        }
        log("  Total -> Vitórias: " + missions.victories
            + " | Derrotas: " + missions.defeats
            + " | Empates: " + missions.draws);
        log("  Veredito final: " + missions.finalVerdict());
        log("==================================================");
    }

    /** Converte o resultado em um rótulo de texto (PT-BR) para o log. */
    public static String resultLabel(MissionManager.MissionResult result) {
        switch (result) {
            case VICTORY: return "VITORIA";
            case DEFEAT:  return "DERROTA";
            default:      return "EMPATE";
        }
    }
}