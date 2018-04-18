package wagner.stephanie.lizzie.gui;

import com.google.common.collect.ImmutableList;
import wagner.stephanie.lizzie.Lizzie;
import wagner.stephanie.lizzie.analysis.BestMoveObserver;
import wagner.stephanie.lizzie.analysis.MoveData;
import wagner.stephanie.lizzie.rules.Board;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ResourceBundle;

public class AnalysisTableModel extends AbstractTableModel {
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("wagner.stephanie.lizzie.i18n.GuiBundle");

    private static final List<String> COLUMN_NAMES = ImmutableList.of(
            resourceBundle.getString("AnalysisFrame.analysisTable.title.move")
            , resourceBundle.getString("AnalysisFrame.analysisTable.title.winrate")
            , resourceBundle.getString("AnalysisFrame.analysisTable.title.probability")
            , resourceBundle.getString("AnalysisFrame.analysisTable.title.calculation")
            , resourceBundle.getString("AnalysisFrame.analysisTable.title.variation")
    );
    private static final List<Class> COLUMN_CLASSES = ImmutableList.of(String.class, Double.class, Double.class, Integer.class, String.class);

    private JTable hostTable;
    private List<MoveData> bestMoves;
    private MoveData selectedMove;
    private BestMoveObserver bestMoveObserver;

    public AnalysisTableModel() {
        hostTable = null;
        bestMoves = null;
        selectedMove = null;

        bestMoveObserver = new BestMoveObserver() {
            @Override
            public void bestMovesUpdated(int boardStateCount, final List<MoveData> newBestMoves) {
                SwingUtilities.invokeLater(() -> {
                    bestMoves = newBestMoves;

                    refreshSelectedMove();
                    if (hostTable != null) {
                        fireTableDataChanged();
                        int selectedIndex = getSelectedMoveIndex();
                        if (selectedIndex >= 0) {
                            hostTable.setRowSelectionInterval(selectedIndex, selectedIndex);
                        }
                    }
                });
            }

            @Override
            public void engineRestarted() {

            }
        };

        Lizzie.leelaz.registerBestMoveObserver(bestMoveObserver);
    }

    public JTable getHostTable() {
        return hostTable;
    }

    public void setHostTable(JTable hostTable) {
        this.hostTable = hostTable;
    }

    public MoveData getSelectedMove() {
        return selectedMove;
    }

    public void setSelectedMove(MoveData selectedMove) {
        this.selectedMove = selectedMove;
    }

    public void setSelectedMoveByIndex(int index) {
        setSelectedMove(bestMoves.get(index));
    }

    public int getSelectedMoveIndex() {
        if (selectedMove == null) {
            return -1;
        }

        int index = 0;
        for (MoveData data : bestMoves) {
            if (data.getCoordinate().equals(selectedMove.getCoordinate())) {
                break;
            } else {
                ++index;
            }
        }

        return index >= bestMoves.size() ? -1 : index;
    }

    public void selectOrDeselectMoveByCoord(int[] mouseCoords) {
        MoveData mouseOnMove = null;
        if (mouseCoords != null) {
            for (MoveData data : bestMoves) {
                int[] coords = Board.convertNameToCoordinates(data.getCoordinate());
                if (coords[0] == mouseCoords[0] && coords[1] == mouseCoords[1]) {
                    mouseOnMove = data;
                    break;
                }
            }
        }

        selectedMove = mouseOnMove;
        int selectedIndex = getSelectedMoveIndex();
        if (selectedIndex >= 0) {
            SwingUtilities.invokeLater(() -> hostTable.setRowSelectionInterval(selectedIndex, selectedIndex));
        } else {
            SwingUtilities.invokeLater(() -> hostTable.clearSelection());
        }
    }

    public void clearSelectedMove() {
        selectedMove = null;
        SwingUtilities.invokeLater(() -> hostTable.clearSelection());
    }

    private void refreshSelectedMove() {
        if (selectedMove != null) {
            int index = getSelectedMoveIndex();
            if (index >= 0 && index < bestMoves.size()) {
                selectedMove = bestMoves.get(index);
            }
        }
    }

    @Override
    public int getRowCount() {
        if (bestMoves == null) {
            return 0;
        }

        return bestMoves.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.size();
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES.get(column);
    }

    @Override
    public int findColumn(String columnName) {
        return COLUMN_NAMES.indexOf(columnName);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return COLUMN_CLASSES.get(columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        MoveData data = bestMoves.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return data.getMoveDisplayString();
            case 1:
                return data.getWinrate();
            case 2:
                return data.getProbability();
            case 3:
                return data.getPlayouts();
            case 4:
                return data.getVariationDisplayString();
            default:
                return "";
        }
    }
}
