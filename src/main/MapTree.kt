package main

import util.MouseWheelTransferListener
import util.Util
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

/**
* Created by yuu on 2016/12/18.
*/

/**
 * マップ選択ウィンドウをシングルトンに管理するためのクラス
 */
object MapTreeOperator {
    /** マップ選択ウィンドウのインスタンス*/
    var dialog: MapTree? = null

    /** マップ選択ウィンドウを開く(既に開いている場合は何もしない)*/
    fun openMapTree(parent: MapEditor) {
        if (dialog == null)
            dialog = MapTree(parent)
    }

    /** マップ選択ウィンドウを更新する*/
    fun update() = dialog?.update()

    /** マップ選択ウィンドウを閉じる*/
    fun closeMapTree() {
        dialog?.dispose()
        dialog = null
    }

    /** マップ選択ウィンドウを開いているかどうかを返す*/
    fun isShowing() = dialog != null
}

/**
 * マップ選択ウィンドウ
 */
class MapTree(var parent: MapEditor) : JDialog(parent) {
    /** マップツリー*/
    var tree = DefaultMutableTreeNode("MapFile")

    /** 初期化*/
    init {
        setTree()

        //基本情報を設定
        title = "マップツリー"
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        isVisible = true
        pack()

        //表示場所は左にくっつける
        val x = parent.location.x - width
        setLocation(if(x < 0) 0 else x, parent.location.y)

        addWindowListener(object :WindowAdapter(){
            override fun windowClosing(e: WindowEvent?) = MapTreeOperator.closeMapTree()
        })

        parent.requestFocus()
    }

    /** ツリー情報の初期設定(コンポーネントの設置などを含む)*/
    fun setTree() {
        update()
        //ツリーを設置
        val panel = JTree(DefaultTreeModel(tree))
        panel.addTreeSelectionListener(MapTreeSelectionListener(this))
        //スクロールバーを設置
        val scroll = JScrollPane(panel)
        contentPane.add(scroll)
        addMouseWheelListener(MouseWheelTransferListener(scroll))
    }

    /** ツリー情報を更新*/
    fun update() {
        tree.removeAllChildren()
        Util.getDir("./MapFile").listFiles({ file, s -> s.endsWith(".mps") }).forEach { tree.add(DefaultMutableTreeNode(it.name)) }
    }
}

/**
 * マップツリーの選択処理
 */
class MapTreeSelectionListener(val parent: MapTree) : TreeSelectionListener {
    override fun valueChanged(e: TreeSelectionEvent?) {
        //セーブ済みでないなら警告を出す
        if (!parent.parent.saved) {
            val judge = JOptionPane.showOptionDialog(parent,
                    "現在のマップは未保存です。保存しますか？",
                    "未保存です",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
                    arrayOf("保存して開く", "保存せずに開く", "キャンセル"),
                    "新規作成をキャンセル")
            when (judge) {
                0 -> parent.parent.saveMap()
                2 -> return
            }
        }
        //ファイルを開く
        parent.parent.fileName = e?.path?.lastPathComponent.toString()
        parent.parent.openFile()
    }
}