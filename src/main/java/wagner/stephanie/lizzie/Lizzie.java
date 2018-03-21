package wagner.stephanie.lizzie;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import wagner.stephanie.lizzie.analysis.Leelaz;
import wagner.stephanie.lizzie.gui.AnalysisFrame;
import wagner.stephanie.lizzie.gui.LizzieFrame;
import wagner.stephanie.lizzie.gui.OptionDialog;
import wagner.stephanie.lizzie.gui.OptionSetting;
import wagner.stephanie.lizzie.rules.Board;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.io.*;

/**
 * Main class.
 */
public class Lizzie {
    public static final String SETTING_FILE = "mylizzie.json";
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static LizzieFrame frame;
    public static JDialog analysisDialog;
    public static AnalysisFrame analysisFrame;
    public static Leelaz leelaz;
    public static Board board;
    public static OptionDialog optionDialog;
    public static OptionSetting optionSetting;

    static {
        readSettingFile();
        // Sometimes gson will fail to parse the file
        if (optionSetting == null || optionSetting.getBoardColor() == null) {
            optionSetting = new OptionSetting();
        }
    }

    /**
     * Launches the game window, and runs the game.
     */
    public static void main(String[] args) throws IOException {
        // Use Nimbus look and feel which looks better
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }

        leelaz = new Leelaz();
        leelaz.ponder();

        board = new Board();
        frame = new LizzieFrame();

        analysisDialog = AnalysisFrame.createAnalysisDialog(frame);
        analysisFrame = (AnalysisFrame) analysisDialog.getContentPane();

        optionDialog = new OptionDialog(frame);
        optionDialog.setDialogSetting(optionSetting);

        setGuiPosition();

        analysisDialog.setVisible(optionSetting.isAnalysisModeOn());
    }

    public static void readGuiPosition() {
        readMainFramePosition();
        readAnalysisWindowPosition();
    }

    public static void readAnalysisWindowPosition() {
        optionSetting.setAnalysisWindowPosX(analysisDialog.getX());
        optionSetting.setAnalysisWindowPosY(analysisDialog.getY());
        optionSetting.setAnalysisWindowWidth(analysisDialog.getWidth());
        optionSetting.setAnalysisWindowHeight(analysisDialog.getHeight());
    }

    public static void readMainFramePosition() {
        optionSetting.setMainWindowPosX(frame.getX());
        optionSetting.setMainWindowPosY(frame.getY());
        optionSetting.setMainWindowWidth(frame.getWidth());
        optionSetting.setMainWindowHeight(frame.getHeight());
    }

    public static void setGuiPosition() {
        setMainWindowPositino();
        setAnalysisWindowPosition();
    }

    public static void setAnalysisWindowPosition() {
        if (optionSetting.getAnalysisWindowPosX() >= 10 && optionSetting.getAnalysisWindowPosY() >= 10) {
            analysisDialog.setLocation(optionSetting.getAnalysisWindowPosX(), optionSetting.getAnalysisWindowPosY());
        }
        if (optionSetting.getAnalysisWindowWidth() >= 10 && optionSetting.getAnalysisWindowHeight() >= 10) {
            analysisDialog.setSize(optionSetting.getAnalysisWindowWidth(), optionSetting.getAnalysisWindowHeight());
        }
    }

    public static void setMainWindowPositino() {
        if (optionSetting.getMainWindowPosX() >= 10 && optionSetting.getMainWindowPosY() >= 10) {
            frame.setLocation(optionSetting.getMainWindowPosX(), optionSetting.getMainWindowPosY());
        }
        if (optionSetting.getMainWindowWidth() >= 10 && optionSetting.getMainWindowHeight() >= 10) {
            frame.setSize(optionSetting.getMainWindowWidth(), optionSetting.getMainWindowHeight());
        }
    }

    public static void readSettingFile() {
        try (Reader reader = new FileReader(SETTING_FILE)) {
            optionSetting = gson.fromJson(reader, OptionSetting.class);
        } catch (FileNotFoundException e) {
            // Do nothing
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeSettingFile() {
        try (Writer writer = new FileWriter(SETTING_FILE)) {
            writer.write(gson.toJson(optionSetting));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
