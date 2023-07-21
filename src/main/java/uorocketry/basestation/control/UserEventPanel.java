package uorocketry.basestation.control;

import uorocketry.basestation.config.Config;
import uorocketry.basestation.data.DataHolder;
import uorocketry.basestation.data.DataPointHolder;
import uorocketry.basestation.data.UserEvent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserEventPanel implements ActionListener {
    private JPanel panel;
    private JButton recordEventButton;

    private List<UserEvent> events = new ArrayList<>();

    private Config mainConfig;
    private DataPointHolder dataPointHolder;

    public UserEventPanel(Config mainConfig) {
        this.mainConfig = mainConfig;
        panel = new JPanel();

        recordEventButton = new JButton(getButtonTitle(0));
        recordEventButton.addActionListener(this);
        panel.add(recordEventButton);
    }

    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == recordEventButton) {
            List<List<DataHolder>> allData = dataPointHolder.getAllReceivedData();
            long[] datasetsTimestamp = new long[allData.size()];
            for (int i = 0; i < allData.size(); i++) {
                Integer timestampIndex = mainConfig.getDataSet(i).getIndex("timestamp");
                List<DataHolder> dataHolders = allData.get(i);
                if (timestampIndex != null && dataHolders != null && dataHolders.size() > 0) {
                    DataHolder currentData = dataHolders.get(dataHolders.size() - 1);
                    datasetsTimestamp[i] = currentData != null ?
                            Optional.ofNullable(currentData.data[timestampIndex].getLongValue()).orElse(-1L)
                            : -1L;
                } else {
                    datasetsTimestamp[i] = -1;
                }
            }

            events.add(new UserEvent("", events.size(), System.currentTimeMillis(), datasetsTimestamp));
            recordEventButton.setText(getButtonTitle(events.size()));

            System.out.println(events.size() + "\t" + System.currentTimeMillis() + "\t" + datasetsTimestamp);
        }
    }

    private String getButtonTitle(int index) {
        return "Record Event (" + index + ")";
    }

    public void setDataPointHolder(DataPointHolder dataPointHolder) {
        this.dataPointHolder = dataPointHolder;
    }
}
