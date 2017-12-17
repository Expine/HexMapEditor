package util

import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import javax.swing.JScrollPane

/**
 * Created by yuu on 2016/12/18.
 */

class MouseWheelTransferListener(val scroll: JScrollPane) : MouseWheelListener {
    override fun mouseWheelMoved(e: MouseWheelEvent?) {
        scroll.mouseWheelListeners.forEach { it.mouseWheelMoved(e) }
    }
}