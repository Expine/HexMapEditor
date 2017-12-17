package main

import layer.Chip
import layer.Layer
import util.Sprite
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.io.OutputStreamWriter
import java.util.*
import javax.swing.JPanel

/**
* Created by yuu on 2016/12/16.
*/

/**
 * Map Editorのパネルクラス
 * 描画処理及び入力処理を行う
 */
class MainPanel(val parent: MapEditor) : JPanel() {
    /** レイヤー*/
    var layers = kotlin.arrayOfNulls<Layer>(3)
    /** マウスに追従する選択画像を表示するレイヤー*/
    var selLayer: SelectLayer
    /** アンドゥ情報のリスト*/
    var undoList = ArrayList<ArrayList<undoItem>>()

    /** 初期化*/
    init {
        //マウスリスナーを追加
        val listener = MainPanelMouseListener(this)
        addMouseListener(listener)
        addMouseMotionListener(listener)
        addMouseWheelListener(listener)

        //選択画像表示レイヤーを追加
        selLayer = SelectLayer(this)
        selLayer.setLocation(0, 0)
        selLayer.isOpaque = false
        add(selLayer)

        //基本情報を設定
        isOpaque = false
        layout = null
    }

    /** レイヤー情報を設定しなおす*/
    fun setLayerData(width: Int, height: Int, base: Sprite?, size: Dimension) {
        layers[0] = Layer(width, height, base, size, parent.chipType, parent.gap)
        layers[1] = Layer(width, height, base, size, parent.chipType, parent.gap)
        layers[2] = Layer(width, height, base, size, parent.chipType, parent.gap)
    }

    /** このパネルの描画処理*/
    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        render(g)
    }

    /** 描画処理*/
    fun render(g2: Graphics?) {
        if (!parent.isMapExist())
            return

        //マップの実サイズと同じ領域のバッファを生成
        val buffer = BufferedImage(parent.getMapWidth(), parent.getMapHeight(), BufferedImage.TYPE_INT_ARGB)
        val area = parent.getRenderingArea()
        val g = buffer.graphics
        g.color = Color.BLACK
        //背景は真っ黒
        g.fillRect(0, 0, buffer.width, buffer.height)
        for ((index, layer) in layers.withIndex()) {
            if (index != parent.layerNumber)
                (g as Graphics2D).composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)
            layer?.render(g, area)
            if (index != parent.layerNumber)
                (g as Graphics2D).composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
        }
        g2?.drawImage(buffer, 0, 0, preferredSize.width, preferredSize.height, 0, 0, buffer.width, buffer.height, null)
    }

    /** 選択開始位置を設定*/
    fun selectStart(x: Int, y: Int) {
        selLayer.size = preferredSize
        selLayer.startPos.location = parent.getTile(x,y)
    }

    /** 選択終了位置を設定*/
    fun selectEnd(x: Int, y: Int) {
        selLayer.endPos.location = parent.getTile(x,y)
        selLayer.repaint()
    }

    /** 選択開始位置を取得*/
    fun getSelectStart() = selLayer.startPos
    /** 選択終了位置を取得*/
    fun getSelectEnd() = selLayer.endPos

    /** チップデータを書き込む*/
    fun writeData(out: OutputStreamWriter) = layers.forEach { it?.chips?.forEach { it.forEach { out.write("${it.id},") } } }

    /** セーブデータからレイヤーデータにパースする*/
    fun parseData(line: String) {
        //レイヤーを初期化
        layers[0] = Layer(parent.mapSize.width, parent.mapSize.height, parent.tileSet, parent.chipSize, parent.chipType, parent.gap)
        layers[1] = Layer(parent.mapSize.width, parent.mapSize.height, parent.tileSet, parent.chipSize, parent.chipType, parent.gap)
        layers[2] = Layer(parent.mapSize.width, parent.mapSize.height, parent.tileSet, parent.chipSize, parent.chipType, parent.gap)

        //チップ情報を更新
        line.split(",").dropLastWhile { it == "" }.forEachIndexed { i, s ->
            layers[i / (parent.mapSize.width * parent.mapSize.height)]?.replace(
                    (i % (parent.mapSize.width * parent.mapSize.height)) / parent.mapSize.height,
                    i % parent.mapSize.height,
                    s.toInt()
            )
        }

        //アンドゥリストを初期化
        undoList.clear()
    }

    /** アンドゥ処理*/
    fun undo() {
        //アンドゥリストが空ならば何もしない
        if (undoList.isEmpty())
            return

        //最後の項目を処理
        val items = undoList.last()
        items.forEach { it.chip.id = it.id }
        undoList.remove(items)
    }
}

/**
 * パネル中の入力処理を請け負うクラス
 */
class MainPanelMouseListener(val parent: MainPanel) : MouseAdapter() {
    /** アンドゥリストに追加する処理群*/
    var undoItems = ArrayList<undoItem>()

    /** */
    var dropping = false

    /** マウスを押した時の処理*/
    override fun mousePressed(e: MouseEvent?) {
        //マップが存在しない場合は何もしない
        if (!parent.parent.isMapExist())
            return

        //右クリック
        if(e?.button == 3) {
            parent.selectStart(e?.x!!, e?.y!!)
            parent.selectEnd(e?.x!! + parent.parent.getSelectAreaWidth().toInt(), e?.y!! + parent.parent.getSelectAreaHeight().toInt())
            dropping = true
            return
        }

        //アンドゥリストに追加する処理群を再定義（クリアだと同じオブジェクトを追加することになるから使えない）
        undoItems = ArrayList<undoItem>()

        //押した時の処理
        when (parent.parent.tool) {
            MapTool.PENCIL -> {
                parent.selectStart(e?.x!!, e?.y!!)
                parent.selectEnd(e?.x!! + parent.parent.getSelectAreaWidth().toInt(), e?.y!! + parent.parent.getSelectAreaHeight().toInt())
                pencil(e?.x!!, e?.y!!)
            }
            MapTool.SQUARE -> {
                parent.selectStart(e?.x!!, e?.y!!)
                parent.selectEnd(e?.x!!, e?.y!!)
            }
            MapTool.PAINT -> {
                parent.selectStart(e?.x!!, e?.y!!)
                parent.selectEnd(e?.x!! + parent.parent.getSelectAreaWidth().toInt(), e?.y!! + parent.parent.getSelectAreaHeight().toInt())
                paint(e?.x!!, e?.y!!)
            }
        }
    }

    /** マウスを離した時の処理*/
    override fun mouseReleased(e: MouseEvent?) {
        //マップが存在しない場合は何もしない
        if (!parent.parent.isMapExist())
            return

        //右クリック処理
        if(e?.button == 3) {
            dropper()
            dropping = false
            return
        }

        //四角形ツールならば、四角形描画を行う
        if (parent.parent.tool == MapTool.SQUARE)
            square(e?.x!!, e?.y!!)

        //アンドゥリストに処理群を追加
        parent.undoList.add(undoItems)
    }

    /** マウスを動かした時の処理*/
    override fun mouseMoved(e: MouseEvent?) {
        //マップが存在しない場合は何もしない
        if (!parent.parent.isMapExist())
            return

        //動かしているだけの場合は、既定の大きさの四角形範囲のみを描画する
        parent.selectStart(e?.x!!, e?.y!!)
        parent.selectEnd(e?.x!! + parent.parent.getSelectAreaWidth().toInt(), e?.y!! + parent.parent.getSelectAreaHeight().toInt())
    }

    /** マウスをドラッグした時の処理*/
    override fun mouseDragged(e: MouseEvent?) {
        //マップが存在しない場合は何もしない
        if (!parent.parent.isMapExist())
            return

        //右クリック
        if(dropping) {
            parent.selectEnd(e?.x!!, e?.y!!)
            return
        }

        when (parent.parent.tool) {
            MapTool.PENCIL -> {
                parent.selectStart(e?.x!!, e?.y!!)
                parent.selectEnd(e?.x!! + parent.parent.getSelectAreaWidth().toInt(), e?.y!! + parent.parent.getSelectAreaHeight().toInt())
                pencil(e?.x!!, e?.y!!)
            }
            MapTool.SQUARE -> {
                parent.selectEnd(e?.x!!, e?.y!!)
            }
            MapTool.PAINT -> {
                parent.selectStart(e?.x!!, e?.y!!)
                parent.selectEnd(e?.x!! + parent.parent.getSelectAreaWidth().toInt(), e?.y!! + parent.parent.getSelectAreaHeight().toInt())
                paint(e?.x!!, e?.y!!)
            }
        }
    }

    /** スポイト処理*/
    fun dropper() {
        parent.parent.selectTile.clear()
        val sx = Math.min(parent.getSelectStart().x, parent.getSelectEnd().x)
        val sy = Math.min(parent.getSelectStart().y, parent.getSelectEnd().y)
        val ex = Math.max(parent.getSelectStart().x, parent.getSelectEnd().x)
        val ey = Math.max(parent.getSelectStart().y, parent.getSelectEnd().y)
        for(x in sx..ex)
            for(y in sy..ey)
                parent.parent.selectTile.add(parent.layers[parent.parent.layerNumber]?.getChip(x, y)?.id!!)
        parent.parent.selectAreaSize.setSize(ex-sx+1, ey-sy+1)
    }

    /** 置き換え処理(範囲外判定、アンドゥリスト追加付き)。最後の引数の値を返す*/
    fun replace(x: Int, y: Int, l: Layer, id: Int, ret: Boolean = false) : Boolean {
        if (x < 0 || y < 0 || x > parent.parent.mapSize.width - 1 || y > parent.parent.mapSize.height - 1)
            return ret
        if (l.getID(x, y) != id)
            if (undoItems.filter { it.chip == l.getChip(x, y) }.isEmpty())
                undoItems.add(undoItem(l.getChip(x, y), l.getID(x, y)))
        l.replace(x, y, id)
        return ret
    }

    /** 鉛筆ツールの処理*/
    fun pencil(ex: Int, ey: Int) {
        //描画開始位置、レイヤーを取得
        val p = parent.parent.getTile(ex, ey)
        val l = parent.layers[parent.parent.layerNumber]!!

        //置換
        for (ax in 0..parent.parent.selectAreaSize.width - 1)
            for (ay in 0..parent.parent.selectAreaSize.height - 1)
                replace(p.x + ax, p.y + ay, l, parent.parent.selectTile[ax * parent.parent.selectAreaSize.height + ay])

        //セーブ済みを解除し、再描画
        parent.parent.saved = false
        parent.parent.repaint()
    }

    /** 四角形ツールの処理*/
    fun square(ex: Int, ey: Int) {
        //描画開始位置、レイヤーを取得
        val p = parent.parent.getTile(ex, ey)
        val l = parent.layers[parent.parent.layerNumber]!!

        //開始位置及び終了位置を最適化
        val startX = Math.min(p.x, parent.getSelectStart().x)
        val startY = Math.min(p.y, parent.getSelectStart().y)
        val endX = Math.max(p.x, parent.getSelectStart().x)
        val endY = Math.max(p.y, parent.getSelectStart().y)

        //置換
        for (tx in startX..endX)
            for (ty in startY..endY)
                replace(tx, ty, l, parent.parent.selectTile[((tx - startX) % parent.parent.selectAreaSize.width) * parent.parent.selectAreaSize.height + (ty - startY) % parent.parent.selectAreaSize.height])

        //セーブ済みを解除し、再描画
        parent.parent.saved = false
        parent.parent.repaint()
    }

    /** ペイントツールの処理*/
    fun paint(ex: Int, ey: Int) {
        //描画開始位置、レイヤーを取得
        val p = parent.parent.getTile(ex, ey)
        val l = parent.layers[parent.parent.layerNumber]!!

        //開始位置が範囲外ならば何もしない
        if(p.x < 0 || p.y < 0 || p.x > parent.parent.mapSize.width - 1 || p.y > parent.parent.mapSize.height - 1)
            return

        //再帰的に必要領域を -1 で塗りつぶす
        recursivePaint(p.x, p.y, l, l.getID(p.x, p.y))

        //-1で塗りつぶされた領域をタイルによって塗りつぶす
        val w = parent.parent.selectAreaSize.width
        val h = parent.parent.selectAreaSize.height
        (0..parent.parent.mapSize.width - 1).forEachIndexed { i, x ->
            (0..parent.parent.mapSize.height - 1).filter { l.getID(x, it) == -1 }.forEach {
                l.replace(x, it, parent.parent.selectTile[((x - p.x + parent.parent.mapSize.width * w) % w) * h + (it - p.y + parent.parent.mapSize.height * h) % h])
            }
        }

        //セーブ済みを解除し、再描画
        parent.parent.saved = false
        parent.parent.repaint()
    }

    /** 再帰的な塗りつぶしの処理*/
    fun recursivePaint(x: Int, y: Int, l: Layer, oriID: Int) {
        //まずそのマス自身を塗りつぶす
        replace(x, y, l, -1)

        //右端および左端
        var rx = parent.parent.mapSize.width
        var lx = -1
        //最左端を見つけつつ、それまでの道筋を塗りつぶす
        if (x > 0)
            (x - 1 downTo  0).find { if (l.getID(it, y) != oriID) true else replace(it, y, l, -1); }?.let { lx = it }
        //最右端を見つけつつ、それまでの道筋を塗りつぶす
        if (x < parent.parent.mapSize.width - 1)
            (x + 1..parent.parent.mapSize.width - 1).find { if (l.getID(it, y) != oriID) true else replace(it, y, l, -1); }?.let { rx = it }

        //上および下のマスでも同様の処理を行う
        //対象となるマスは、左端であるか左に異なるチップを持つ、同じチップである
        if (y > 0)
            (lx + 1..rx - 1).filter { (it == lx + 1 || l.getID(it - 1, y - 1) != oriID) && l.getID(it, y - 1) == oriID  }.forEach { recursivePaint(it, y - 1, l, oriID) }
        if (y < parent.parent.mapSize.height - 1)
            (lx + 1..rx - 1).filter { (it == lx + 1 || l.getID(it - 1, y + 1) != oriID) && l.getID(it, y + 1) == oriID  }.forEach { recursivePaint(it, y + 1, l, oriID) }
    }
}

/**
 * 選択範囲表示用のレイヤー
 */
class SelectLayer(var parent: MainPanel) : JPanel() {
    /** 選択範囲の開始地点、終了地点*/
    var startPos = Point(0, 0)
    var endPos = Point(0, 0)

    /** 描画処理*/
    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        //マップが存在しない場合は何もしない
        if (!parent.parent.isMapExist())
            return

        //破線属性を設定
        (g as Graphics2D).stroke = BasicStroke(
                (1.0 * parent.parent.mult).toFloat(),
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10.0f,
                FloatArray(2, { i -> ((4.0f - i * 3.0f) * parent.parent.mult).toFloat() }),
                0.0f
        )
        g.color = Color(30, 30, 255)
        //開始位置と終了位置を最適化
        val s = parent.parent.getCoordinateByTile(startPos.x, startPos.y)
        val e = parent.parent.getCoordinateByTile(endPos.x, endPos.y)
        val w = (parent.parent.chipSize.width * parent.parent.mult).toInt()
        val h = (parent.parent.chipSize.height * parent.parent.mult).toInt()
        val startX = Math.min(s.x, e.x)
        val startY = Math.min(s.y, e.y)
        val endX = Math.max(s.x, e.x) + w
        val endY = Math.max(s.y, e.y) + h
        //線を引く
        g.drawLine(startX, startY, endX, startY)
        g.drawLine(endX, startY, endX, endY)
        g.drawLine(startX, endY, endX, endY)
        g.drawLine(startX, startY, startX, endY)
    }
}

/** アンドゥリストに格納されるデータ型*/
data class undoItem(val chip: Chip, val id: Int)