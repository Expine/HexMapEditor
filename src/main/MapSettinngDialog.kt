package main

import java.awt.Dialog
import javax.swing.JDialog

/**
 * Created by yuu on 2016/12/31.
 */
class MapSettingDialog(owner: MapEditor) : JDialog(owner, true) {
    init {
        title = "マップの設定"
        setLocationRelativeTo(null);
        isVisible = true;
        isResizable = false;
        pack();
    }

}
