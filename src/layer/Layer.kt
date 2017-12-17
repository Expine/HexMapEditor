package layer

import main.ChipType
import main.MapEditor
import main.RenderingArea
import util.Sprite
import java.awt.Dimension
import java.awt.Graphics
import java.util.*

/**
* Created by yuu on 2016/12/16.
*/

/**
 * レイヤー情報
 */
class Layer(width: Int, height: Int, var baseChip: Sprite?, var baseChipSize: Dimension, val type: ChipType, var gap: Int) {
    /** レイヤー内にあるチップの配列*/
    var chips: ArrayList<ArrayList<Chip>> = ArrayList()

    /** 初期化*/
    init {
        //チップを初期化
        (0..width - 1).forEach { chips.add(ArrayList<Chip>((0..height - 1).map { Chip(0, type) })) }
    }

    /** 描画処理*/
    fun render(g: Graphics, area: RenderingArea) {
        chips.forEachIndexed { x, list -> list.forEachIndexed { y, chip -> chip.render(g, baseChip, x, y, baseChipSize.width, baseChipSize.height, gap, area) } }
    }

    /** チップを取得*/
    fun getChip(x: Int, y: Int) = chips[x][y]
    /** チップIDを取得*/
    fun getID(x: Int, y: Int) = chips[x][y].id

    /** チップIDを置き換える*/
    fun replace(x: Int, y: Int, id: Int) {
        chips[x][y].id = id
    }
}