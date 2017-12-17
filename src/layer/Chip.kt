package layer

import main.ChipType
import main.MapEditor
import main.RenderingArea
import util.Sprite
import java.awt.Graphics

/**
* Created by yuu on 2016/12/16.
*/

/**
 * チップ情報のクラス
 */
class Chip(var id: Int, var type: ChipType) {
    /** 描画処理*/
    fun render(g: Graphics, base: Sprite?, x: Int, y: Int, w: Int, h: Int, gap: Int, area: RenderingArea) {
        when (type) {
            ChipType.SQUARE -> if(area.inArea(x * w, y * h, w, h)) base?.render(g, id, x * w, y * h)
            ChipType.HEX -> if(area.inArea(x * (w + gap) + (y % 2) * (w + gap) / 2,y * h /2, w, h)) base?.render(g, id, x * (w + gap) + (y % 2) * (w + gap) / 2, y * h / 2)
        }
    }
}