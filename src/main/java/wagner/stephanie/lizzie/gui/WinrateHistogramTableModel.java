package wagner.stephanie.lizzie.gui;

import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import wagner.stephanie.lizzie.Lizzie;
import wagner.stephanie.lizzie.analysis.BestMoveObserver;
import wagner.stephanie.lizzie.analysis.MoveData;
import wagner.stephanie.lizzie.rules.Board;
import wagner.stephanie.lizzie.rules.BoardData;
import wagner.stephanie.lizzie.rules.BoardHistoryNode;
import wagner.stephanie.lizzie.rules.BoardStateChangeObserver;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WinrateHistogramTableModel extends AbstractTableModel {
    private ArrayList<WinrateHistogramEntry> histogramEntryList;
    private ArrayList<WinrateHistogramEntry> histogramEntryFilteredList;
    private boolean provideFilteredData;
    private Consumer<WinrateHistogramTableModel> refreshObserver;
    private double significantOscillationThreshould;

    public WinrateHistogramTableModel() {
        histogramEntryList = new ArrayList<>();
        histogramEntryFilteredList = new ArrayList<>();
        provideFilteredData = true;
        significantOscillationThreshould = 15.0;

        Lizzie.board.registerBoardStateChangeObserver(new BoardStateChangeObserver() {
            @Override
            public void mainStreamAppended(BoardHistoryNode newNodeBegin, BoardHistoryNode head) {
            }

            @Override
            public void mainStreamCut(BoardHistoryNode nodeBeforeCutPoint, BoardHistoryNode head) {
            }

            @Override
            public void headMoved(BoardHistoryNode oldHead, BoardHistoryNode newHead) {
                int lastMoveNumberInTable = histogramEntryList.get(histogramEntryList.size() - 1).getMoveNumber();
                if (newHead.getData().getMoveNumber() > lastMoveNumberInTable) {
                    BoardHistoryNode node;
                    do {
                        node = oldHead.getNext();
                        addHistogramData(node.getData());
                    } while (node != newHead);
                } else {
                    histogramEntryList.removeIf(entry -> entry.getMoveNumber() > newHead.getData().getMoveNumber());
                }

                rebuildFilteredHistogramData();
                if (refreshObserver != null) {
                    Lizzie.miscExecutor.execute(() -> refreshObserver.accept(WinrateHistogramTableModel.this));
                }
            }

            @Override
            public void boardCleared(BoardHistoryNode initialNode, BoardHistoryNode initialHead) {
                histogramEntryList.clear();
                histogramEntryFilteredList.clear();

                initialNode.forEach(WinrateHistogramTableModel.this::addHistogramData);
                rebuildFilteredHistogramData();

                fireTableDataChanged();
                if (refreshObserver != null) {
                    Lizzie.miscExecutor.execute(() -> refreshObserver.accept(WinrateHistogramTableModel.this));
                }
            }
        });

        Lizzie.leelaz.registerBestMoveObserver(new BestMoveObserver() {
            @Override
            public void bestMovesUpdated(int boardStateCount, List<MoveData> newBestMoves) {
                if (boardStateCount < histogramEntryList.size() && CollectionUtils.isNotEmpty(newBestMoves)) {
                    MoveData moveData = newBestMoves.get(0);
                    WinrateHistogramEntry histogramEntry = histogramEntryList.get(boardStateCount);
                    if (Objects.equals(histogramEntry.getColor(), WinrateHistogramEntry.COLOR_BLACK)) {
                        histogramEntry.setWhiteWinrate(moveData.getWinrate());
                    } else {
                        histogramEntry.setBlackWinrate(moveData.getWinrate());
                    }

                    if (boardStateCount > 0) {
                        histogramEntry.setBlackWindiff(histogramEntry.getBlackWinrate() - histogramEntryList.get(boardStateCount - 1).getBlackWinrate());
                    }

                    rebuildFilteredHistogramData();
                    fireTableDataChanged();
                    if (refreshObserver != null) {
                        Lizzie.miscExecutor.execute(() -> refreshObserver.accept(WinrateHistogramTableModel.this));
                    }
                }
            }

            @Override
            public void engineRestarted() {
            }
        });
    }

    public Consumer<WinrateHistogramTableModel> getRefreshObserver() {
        return refreshObserver;
    }

    public void setRefreshObserver(Consumer<WinrateHistogramTableModel> refreshObserver) {
        this.refreshObserver = refreshObserver;
    }

    public boolean isProvideFilteredData() {
        return provideFilteredData;
    }

    public void setProvideFilteredData(boolean provideFilteredData) {
        this.provideFilteredData = provideFilteredData;
        fireTableDataChanged();
    }

    public ArrayList<WinrateHistogramEntry> getHistogramEntryList() {
        return histogramEntryList;
    }

    public ArrayList<WinrateHistogramEntry> getHistogramEntryFilteredList() {
        return histogramEntryFilteredList;
    }

    public void setSignificantOscillationThreshould(double significantOscillationThreshould) {
        this.significantOscillationThreshould = significantOscillationThreshould;

        rebuildFilteredHistogramData();
        fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
        return WinrateHistogramEntry.COLUMN_NAMES.size();
    }

    @Override
    public String getColumnName(int column) {
        return WinrateHistogramEntry.COLUMN_NAMES.get(column);
    }

    @Override
    public int findColumn(String columnName) {
        return WinrateHistogramEntry.COLUMN_NAMES.indexOf(columnName);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return WinrateHistogramEntry.COLUMN_CLASSES.get(columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public int getRowCount() {
        if (isProvideFilteredData()) {
            return histogramEntryFilteredList.size();
        } else {
            return histogramEntryList.size();
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (isProvideFilteredData()) {
            return histogramEntryFilteredList.get(rowIndex).getDataByIndex(columnIndex);
        } else {
            return histogramEntryList.get(rowIndex).getDataByIndex(columnIndex);
        }
    }

    public void rebuildHistogramData() {
        histogramEntryList.clear();
        histogramEntryFilteredList.clear();
        Lizzie.board.getHistory().forEach(boardData -> {
            if (boardData.getMoveNumber() >= 0) {
                addHistogramData(boardData);
            }
        });
    }

    public void rebuildFilteredHistogramData() {
        // FIXME: This class has thread sync problems. This method updates the data list, while GUI thread reads it.
        // Exception in thread "AWT-EventQueue-0" java.lang.IndexOutOfBoundsException: Index: 8, Size: 10
        //	at java.util.ArrayList.rangeCheck(ArrayList.java:657)
        //	at java.util.ArrayList.get(ArrayList.java:433)
        //	at wagner.stephanie.lizzie.gui.WinrateHistogramTableModel.getValueAt(WinrateHistogramTableModel.java:179)
        //	at javax.swing.JTable.getValueAt(JTable.java:2717)
        //	at javax.swing.JTable.prepareRenderer(JTable.java:5706)
        // 	at javax.swing.plaf.synth.SynthTableUI.paintCell(SynthTableUI.java:683)
        //	at javax.swing.plaf.synth.SynthTableUI.paintCells(SynthTableUI.java:580)
        // ...

        // This s only a relief. Other methods should be changed too.
        // FIXME: The update operation should be called in SwingUtilities.invokeLater() to prevent thread problems
        histogramEntryFilteredList = histogramEntryList.stream()
                .filter(entry -> Math.abs(entry.getBlackWindiff()) >= significantOscillationThreshould)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void addHistogramData(BoardData boardData) {
        String color = getCurrentDisplayColor(boardData);
        String move = getCurrentDisplayMove(boardData);

        WinrateHistogramEntry entry;
        if (histogramEntryList.isEmpty()) {
            entry = new WinrateHistogramEntry(boardData.getMoveNumber(), color, move, boardData.getBlackWinrate(), 0.0);
        } else {
            double lastBlackWinrate = histogramEntryList.get(histogramEntryList.size() - 1).getBlackWinrate();
            entry = new WinrateHistogramEntry(boardData.getMoveNumber(), color, move, boardData.getBlackWinrate(), boardData.getBlackWinrate() - lastBlackWinrate);
        }
        histogramEntryList.add(entry);
    }

    @NotNull
    private String getCurrentDisplayMove(BoardData boardData) {
        String move;
        int[] lastMove = boardData.getLastMove();
        if (lastMove != null && Board.isValid(lastMove[0], lastMove[1])) {
            move = Board.convertCoordinatesToDisplayName(lastMove[0], lastMove[1]);
        } else {
            move = "Pass";
        }
        return move;
    }

    @NotNull
    private String getCurrentDisplayColor(BoardData boardData) {
        String color;
        if (boardData.isBlack().isPresent()) {
            if (boardData.isBlack().get()) {
                color = WinrateHistogramEntry.COLOR_BLACK;
            } else {
                color = WinrateHistogramEntry.COLOR_WHITE;
            }

        } else {
            color = WinrateHistogramEntry.COLOR_NONE;
        }
        return color;
    }
}
