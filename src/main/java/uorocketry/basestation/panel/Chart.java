package uorocketry.basestation.panel;

import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import uorocketry.basestation.data.DataPointHolder;
import uorocketry.basestation.data.DataType;

public interface Chart {
    void update(
            DataPointHolder dataPointHolder,
            int minDataIndex, int maxDataIndex,
            boolean onlyShowLatestData,
            int maxDataPointsDisplayed);

    JPanel getPanel();

    SnapPanel getSpanPanel();

    DataType[] getXTypes();

    void setXTypes(DataType[] xTypes);

    DataType getYType();

    void setYType(DataType yTypes);

    /**
     *
     * @param e
     * @return if the drag event has been handled. If true, the snap panel will not
     *         process window resizes.
     */
    default boolean mouseDragged(MouseEvent e) {
        return false;
    }
}
