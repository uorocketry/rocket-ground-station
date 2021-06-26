package uorocketry.basestation.panel;

import uorocketry.basestation.data.DataHolder;
import uorocketry.basestation.data.DataPoint;
import uorocketry.basestation.data.DataPointHolder;
import uorocketry.basestation.data.DataType;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.List;

public interface Chart {
    void update(DataPointHolder dataPointHolder, int minDataIndex, int maxDataIndex, boolean onlyShowLatestData, int maxDataPointsDisplayed);

    JPanel getPanel();
    SnapPanel getSpanPanel();

    DataType[] getXTypes();
    void setXTypes(DataType[] xTypes);
    DataType getYType();
    void setYType(DataType yTypes);

    /**
     *
     * @param e
     * @return if the drag event has been handled. If true, the snap panel will not process window resizes.
     */
    default boolean mouseDragged(MouseEvent e) {
        return false;
    }
}
