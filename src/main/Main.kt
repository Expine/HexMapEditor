package main

import util.MouseWheelTransferListener
import java.awt.Dimension
import java.io.File
import javax.swing.JScrollPane
import javax.swing.WindowConstants

/**
* Created by yuu on 2016/12/16.
*/

val FRAME_DEFAULT_WIDTH = 800
val FRAME_DEFAULT_HEIGHT = 600

/** 最初の処理*/
fun main(args: Array<String>) {
    //必要なディレクトリを作成
    File("./BackGround").mkdir()
    File("./BGM").mkdir()
    File("./TileSet").mkdir()
    File("./MapFile").mkdir()

    //フレームを生成
    val frame = MapEditor("Map Editor")
    frame.defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE

    //パネルを生成
    val panel = MainPanel(frame)
    panel.preferredSize = Dimension(FRAME_DEFAULT_WIDTH, FRAME_DEFAULT_HEIGHT)

    //スクロールバーを生成
    val scroll = JScrollPane(panel)
    scroll.isOpaque = false
    scroll.verticalScrollBar.unitIncrement = 25
    scroll.horizontalScrollBar.unitIncrement = 25
    frame.contentPane.add(scroll)
    frame.addMouseWheelListener(MouseWheelTransferListener(scroll))

    //フレームにパネルを登録し、初期設定を行う
    frame.child = panel
    frame.scroll = scroll
    frame.pack()
    frame.setLocationRelativeTo(null)

    //フレームの情報を構成し、可視化する
    frame.parseBaseData()
    frame.isVisible = true
}

