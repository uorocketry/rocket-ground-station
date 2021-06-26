package uorocketry.basestation.panel;

import javax.swing.*;

public class TableHolder {
    private JTable receivedDataTable, connectionInfoTable;

    public TableHolder(JTable receivedDataTable, JTable connectionInfoTable) {
        this.receivedDataTable = receivedDataTable;
        this.connectionInfoTable = connectionInfoTable;
    }

    public JTable getReceivedDataTable() {
        return receivedDataTable;
    }

    public void setReceivedDataTable(JTable receivedDataTable) {
        this.receivedDataTable = receivedDataTable;
    }

    public JTable getConnectionInfoTable() {
        return connectionInfoTable;
    }

    public void setConnectionInfoTable(JTable connectionInfoTable) {
        this.connectionInfoTable = connectionInfoTable;
    }
}
