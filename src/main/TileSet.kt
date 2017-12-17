package main

import util.MouseWheelTransferListener
import util.Util
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.WindowConstants
import javax.tools.Tool

/**
 * Created by yuu on 2016/12/18.
 */

/**
 * タイルセットウィンドウを操作するシングルトンクラス
 */
object TileSetOperator {
    /** タイルセットのインスタンス*/
    var dialog: TileSet? = null

    /** タイルセットを開く*/
    fun openTileSet(parent: MapEditor) {
        if (dialog == null && parent.isMapExist())
            dialog = TileSet(parent)
    }

    /** タイルセットの更新処理*/
    fun update() = dialog?.repaint()

    /** タイルセットを閉じる*/
    fun closeMapTree() {
        dialog?.dispose()
        dialog = null
    }

    /** タイルセットを初期化*/
    fun initialize() = dialog?.initialize()

    /** タイルセットが表示中かを判定*/
    fun isShowing() = dialog != null

}

/**
 * タイルセットウィンドウのクラス
 */
class TileSet(val parent: MapEditor) : JDialog(parent) {
    /** タイルセットウィンドウの大きさ*/
    val TILESET_WIDTH = 400
    val TILESET_HEIGHT = 900
    /** タイルセットのパネル*/
    val panel: TileSetPanel

    /** 初期化処理*/
    init {
        //パネルとスクロールバーを設定
        panel = TileSetPanel(this)
        val scroll = JScrollPane(panel)
        contentPane.add(scroll)
        addMouseWheelListener(MouseWheelTransferListener(scroll))

        //基本情報を設定
        title = "タイルセット"
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        size = Dimension(TILESET_WIDTH, TILESET_HEIGHT)
        val x = parent.location.x + parent.width
        setLocation(if(x + width > Toolkit.getDefaultToolkit().screenSize.width) Toolkit.getDefaultToolkit().screenSize.width - width else x, parent.location.y)
        isVisible = true

        addWindowListener(object : WindowAdapter(){
            override fun windowClosing(e: WindowEvent?) {
                TileSetOperator.closeMapTree()
            }
        })
        //リスナーをとうろく
        val listener = TileSetMouseAdapter(panel)
        panel.addMouseListener(listener)
        panel.addMouseMotionListener(listener)
    }

    /** パネルを初期化する*/
    fun initialize() = panel.initialize()
}

/**
 * タイルセットのパネル
 */
class TileSetPanel(val parent: TileSet) : JPanel() {
    /** 選択開始位置と現在の位置*/
    var startPos = Point(0, 0)
    var nowPos = Point(0, 0)
    /** 選択済み開始位置と終了位置*/
    var selectedStartPos = Point(0, 0)
    var selectedEndPos = Point(0, 0)
    /** 拡大率*/
    val mult = 2.5
    var isSelecting = false

    /** 描画処理*/
    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)

        //タイルセットを描画
        val base = parent.parent.tileSet?.base!!
        preferredSize = Dimension((base.width * mult).toInt(), (base.height * mult).toInt())
        g?.drawImage(base, 0, 0, preferredSize.width, preferredSize.height, 0, 0, base.width, base.height, null)

        val w = (parent.parent.chipSize.width * mult).toInt()
        val h = (parent.parent.chipSize.height * mult).toInt()

        //選択中でない場合は、決定済みの囲みを描く
        if (!isSelecting) {
            val startX = Math.min(getXByTile(selectedStartPos.x), getXByTile(selectedEndPos.x))
            val startY = Math.min(getYByTile(selectedStartPos.y), getYByTile(selectedEndPos.y))
            val endX = Math.max(getXByTile(selectedStartPos.x), getXByTile(selectedEndPos.x)) + w
            val endY = Math.max(getYByTile(selectedStartPos.y), getYByTile(selectedEndPos.y)) + h
            (g as Graphics2D).stroke = BasicStroke(3.0f)
            g.color = Color.RED
            g.drawLine(startX, startY, endX, startY)
            g.drawLine(endX, startY, endX, endY)
            g.drawLine(startX, endY, endX, endY)
            g.drawLine(startX, startY, startX, endY)
        }

        //破線設定
        (g as Graphics2D).stroke = BasicStroke(
                (1.0 * mult).toFloat(),
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10.0f,
                FloatArray(2, { i -> ((4.0f - i * 3.0f) * mult).toFloat() }),
                0.0f
        )
        g.color = Color(30, 30, 255)

        //開始位置と終了位置の最適化
        val startX = Math.min(getXByTile(startPos.x), getXByTile(nowPos.x))
        val startY = Math.min(getYByTile(startPos.y), getYByTile(nowPos.y))
        val endX = Math.max(getXByTile(startPos.x), getXByTile(nowPos.x)) + w
        val endY = Math.max(getYByTile(startPos.y), getYByTile(nowPos.y)) + h
        g.drawLine(startX, startY, endX, startY)
        g.drawLine(endX, startY, endX, endY)
        g.drawLine(startX, endY, endX, endY)
        g.drawLine(startX, startY, startX, endY)
    }

    /** 座標から、タイル座標を取得*/
    fun getTileX(x: Int) = Util.maxmin(0, parent.parent.getChipWNumber() - 1, (x / (parent.parent.chipSize.width * mult)).toInt())
    fun getTileY(y: Int) = Util.maxmin(0, parent.parent.getChipHNumber() - 1, (y / (parent.parent.chipSize.height * mult)).toInt())
    /** タイル座標から、座標を取得*/
    fun getXByTile(x: Int) = (x * parent.parent.chipSize.width * mult).toInt()
    fun getYByTile(y: Int) = (y * parent.parent.chipSize.height * mult).toInt()

    /** 開始地点を設定*/
    fun setStartPoint(x: Int, y: Int) {
        startPos.setLocation(getTileX(x), getTileY(y))
        setPoint(x, y)
    }

    /** 現在地点を設定*/
    fun setPoint(x: Int, y: Int) {
        nowPos.setLocation(getTileX(x), getTileY(y))
        parent.repaint()
    }

    /** 選択済み位置などを初期化*/
    fun initialize() {
        startPos.setLocation(0, 0)
        nowPos.setLocation(0, 0)
        selectedStartPos.setLocation(0, 0)
        selectedEndPos.setLocation(0, 0)
    }
}

/**
 * タイルセットのマウス操作クラス
 */
class TileSetMouseAdapter(val parent: TileSetPanel) : MouseAdapter() {
    /** マウスを押したとき*/
    override fun mousePressed(e: MouseEvent?) {
        parent.selectedStartPos.setLocation(parent.getTileX(e?.x!!), parent.getTileY(e?.y!!))
        parent.isSelecting = true
    }

    /** マウスを離したとき*/
    override fun mouseReleased(e: MouseEvent?) {
        //終了位置を設定
        parent.selectedEndPos.setLocation(parent.getTileX(e?.x!!), parent.getTileY(e?.y!!))
        parent.isSelecting = false
        parent.setStartPoint(e?.x!!, e?.y!!)
        //開始位置と終了位置を最適化
        val startX = Math.min(parent.selectedStartPos.x, parent.selectedEndPos.x)
        val startY = Math.min(parent.selectedStartPos.y, parent.selectedEndPos.y)
        val endX = Math.max(parent.selectedStartPos.x, parent.selectedEndPos.x)
        val endY = Math.max(parent.selectedStartPos.y, parent.selectedEndPos.y)
        //選択タイルを設定
        parent.parent.parent.selectAreaSize = Dimension(endX - startX + 1, endY - startY + 1)
        parent.parent.parent.selectTile.clear()
        for (x in startX..endX)
            for (y in startY..endY)
                parent.parent.parent.selectTile.add(x + y * parent.parent.parent.getChipWNumber())
    }

    override fun mouseMoved(e: MouseEvent?) = parent.setStartPoint(e?.x!!, e?.y!!)

    override fun mouseDragged(e: MouseEvent?) = parent.setPoint(e?.x!!, e?.y!!)
}