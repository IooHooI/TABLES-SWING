package org.swing;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Created by bender on 01.10.17.
 */
public class FileTransferHandler extends TransferHandler {
    /**
     * We only support importing strings.
     */
    public boolean canImport(TransferHandler.TransferSupport info) {
        // Check for String flavor
        return info.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    /**
     * We support both copy and move actions.
     */
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY_OR_MOVE;
    }

    /**
     * Perform the actual import.  This demo only supports drag and drop.
     */
    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }
        SomeForm frame = (SomeForm)info.getComponent();
        Transferable t = info.getTransferable();
        java.util.List<File> l;
        try {
            l = (java.util.List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
            if (l.size() == 1) {
                frame.outputExelData(l.get(0).getAbsolutePath());
            }
            if (l.size() == 2) {
                frame.outputComparisonExelData(l.stream().map(File::getAbsolutePath).collect(Collectors.toList()));
            }
        } catch (UnsupportedFlavorException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}