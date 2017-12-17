package main

import util.Util
import java.awt.Dimension
import java.io.File
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

/**
* Created by yuu on 2016/12/17.
*/


class NewMapDialog(val owner: MapEditor, val defaultName: String) : JDialog(owner, true) {
    /** ダイアログの大きさ*/
    val NEWMAP_WIDTH = 500
    val NEWMAP_HEIGHT = 550

    /** マップ情報を設定するテキストフィールドおよびスピナー*/
    var mapNameField = JTextField()
    var mapw = JSpinner()
    var maph = JSpinner()
    var backField = JTextField()
    var bgmField = JTextField()
    var tileField = JTextField()
    var mapTypeSelector = JComboBox<String>()
    var chipw = JSpinner()
    var chiph = JSpinner()
    var gap = JSpinner()

    /** 初期化処理*/
    init {
        setMenu(setPanel())

        //基本情報の設定
        title = "マップの新規作成"
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        setLocationRelativeTo(null)
        isVisible = true
        isResizable = false
        pack()
    }

    /** パネルを設置する*/
    fun setPanel(): JPanel {
        val panel = JPanel()
        preferredSize = Dimension(NEWMAP_WIDTH, NEWMAP_HEIGHT)
        contentPane.add(panel)
        pack()
        return panel
    }

    /** 各項目を設置する*/
    fun setMenu(panel: JPanel) {
        //マップファイル名
        val mapNameLabel = JLabel("マップのファイル名を入力してください")
        mapNameField.text = defaultName
        val mapNameExtensionLabel = JLabel(".mps")

        //マップサイズ
        val mapSizeLabel = JLabel("マップサイズ")
        val mapwLabel = JLabel("横")
        val maphLabel = JLabel("縦")
        mapw.model = SpinnerNumberModel(20, 20, 1000, 1)
        maph.model = SpinnerNumberModel(15, 15, 1000, 1)
        mapw.value = 20
        maph.value = 15

        //遠景ファイル
        val backLabel = JLabel("遠景ファイル")
        val backFileLabel = JLabel("ファイル")
        val backButton = JButton("File")

        //BGMファイル
        val bgmLabel = JLabel("再生BGMファイル")
        val bgmFileLabel = JLabel("ファイル")
        val bgmButton = JButton("File")

        //タイルファイル
        val tileLabel = JLabel("使用タイル")
        val tileFileLabel = JLabel("ファイル")
        val tileButton = JButton("File")

        //タイル設定
        val chipSizeLabel = JLabel("タイルサイズ")
        val chipWLabel = JLabel("横")
        val chipHLabel = JLabel("縦")
        var gapLabel = JLabel("ギャップ(HexTypeのみ)")
        chipw.model = SpinnerNumberModel(16, 1, 1000, 1)
        chiph.model = SpinnerNumberModel(16, 1, 1000, 1)
        gap.model = SpinnerNumberModel(10, 1, 1000, 1)
        chipw.value = 16
        chiph.value = 16
        gap.value = 10

        //マップタイプ設定
        val mapTypeLabel = JLabel("マップタイプ")
        mapTypeSelector.addItem("SquareType(Default)")
        mapTypeSelector.addItem("HexType")

        //決定、キャンセルボタン
        val ok = JButton("OK")
        val cancel = JButton("キャンセル")

        //アクションを設定
        backButton.addActionListener { setBack() }
        bgmButton.addActionListener { setBGM() }
        tileButton.addActionListener { setTile() }
        ok.addActionListener { ok() }
        cancel.addActionListener { end() }

        //以下、レイアウトを設定
        val lay = GroupLayout(panel)
        lay.autoCreateGaps = true
        lay.autoCreateContainerGaps = true
        panel.layout = lay
        val hGroup = lay.createSequentialGroup()
        val vGroup = lay.createSequentialGroup()

        //水平方向での設置方針
        hGroup.addGroup(lay.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(mapNameLabel)
                .addGroup(lay.createSequentialGroup()
                        .addComponent(mapNameField)
                        .addComponent(mapNameExtensionLabel)
                )
                .addComponent(mapSizeLabel)
                .addGroup(lay.createSequentialGroup()
                        .addGap(100)
                        .addComponent(mapwLabel)
                        .addComponent(mapw)
                        .addGap(20)
                        .addComponent(maphLabel)
                        .addComponent(maph)
                        .addGap(100)
                )
                .addComponent(backLabel)
                .addGroup(lay.createSequentialGroup()
                        .addComponent(backFileLabel)
                        .addComponent(backField)
                        .addComponent(backButton)
                )
                .addComponent(bgmLabel)
                .addGroup(lay.createSequentialGroup()
                        .addComponent(bgmFileLabel)
                        .addComponent(bgmField)
                        .addComponent(bgmButton)
                )
                .addComponent(tileLabel)
                .addGroup(lay.createSequentialGroup()
                        .addComponent(tileFileLabel)
                        .addComponent(tileField)
                        .addComponent(tileButton)
                )
                .addComponent(chipSizeLabel)
                .addGroup(lay.createSequentialGroup()
                        .addGap(100)
                        .addComponent(chipWLabel)
                        .addComponent(chipw)
                        .addGap(20)
                        .addComponent(chipHLabel)
                        .addComponent(chiph)
                        .addGap(100)
                )
                .addComponent(gapLabel)
                .addGroup(lay.createSequentialGroup()
                        .addGap(180)
                        .addComponent(gap)
                        .addGap(180)
                )
                .addComponent(mapTypeLabel)
                .addGroup(lay.createSequentialGroup()
                        .addGap(50)
                        .addComponent(mapTypeSelector)
                        .addGap(50)
                )
                .addGroup(lay.createSequentialGroup()
                        .addComponent(ok)
                        .addGap(100)
                        .addComponent(cancel)
                )
        )
        lay.setHorizontalGroup(hGroup)

        //垂直方向での設置方針
        vGroup.addComponent(mapNameLabel)
        vGroup.addGroup(lay.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(mapNameField).addComponent(mapNameExtensionLabel))
        vGroup.addGap(10)
        vGroup.addComponent(mapSizeLabel)
        vGroup.addGroup(lay.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(mapwLabel).addComponent(mapw).addComponent(maphLabel).addComponent(maph))
        vGroup.addGap(10)
        vGroup.addComponent(backLabel)
        vGroup.addGroup(lay.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(backFileLabel).addComponent(backField).addComponent(backButton))
        vGroup.addGap(10)
        vGroup.addComponent(bgmLabel)
        vGroup.addGroup(lay.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(bgmFileLabel).addComponent(bgmField).addComponent(bgmButton))
        vGroup.addGap(10)
        vGroup.addComponent(tileLabel)
        vGroup.addGroup(lay.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(tileFileLabel).addComponent(tileField).addComponent(tileButton))
        vGroup.addGap(10)
        vGroup.addComponent(chipSizeLabel)
        vGroup.addGroup(lay.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(chipWLabel).addComponent(chipw).addComponent(chipHLabel).addComponent(chiph))
        vGroup.addGap(10)
        vGroup.addComponent(gapLabel)
        vGroup.addComponent(gap)
        vGroup.addComponent(mapTypeLabel)
        vGroup.addComponent(mapTypeSelector)
        vGroup.addGap(30)
        vGroup.addGroup(lay.createParallelGroup().addComponent(ok).addComponent(cancel))
        lay.setVerticalGroup(vGroup)
    }

    /** 遠景画像を設定させる*/
    fun setBack() {
        val backChooser = Util.getChooser("./BackGround")
        backChooser.addChoosableFileFilter(FileNameExtensionFilter("png, gif, jpgファイル", "gif", "png", "jpg"))
        backChooser.isAcceptAllFileFilterUsed = false
        when (backChooser.showOpenDialog(this)) {
            JFileChooser.APPROVE_OPTION -> backField.text = backChooser.selectedFile.name
        }
    }

    /** BGMを設定させる*/
    fun setBGM() {
        val bgmChooser = Util.getChooser("./BGM")
        bgmChooser.addChoosableFileFilter(FileNameExtensionFilter("wav, mid, mp3ファイル", "wav", "mid", "mp3"))
        bgmChooser.isAcceptAllFileFilterUsed = false
        when (bgmChooser.showOpenDialog(this)) {
            JFileChooser.APPROVE_OPTION -> bgmField.text = bgmChooser.selectedFile.name
        }
    }

    /** タイル情報を設定させる*/
    fun setTile() {
        val tileChooser = Util.getChooser("./TileSet")
        tileChooser.addChoosableFileFilter(FileNameExtensionFilter("png, gif, jpgファイル", "gif", "png", "jpg"))
        tileChooser.isAcceptAllFileFilterUsed = false
        when (tileChooser.showOpenDialog(this)) {
            JFileChooser.APPROVE_OPTION -> tileField.text = tileChooser.selectedFile.name
        }
    }

    /** 決定処理*/
    fun ok() {
        //入力すべき項目が空欄の場合は警告
        if (mapNameField.text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "マップのファイル名を入力してください", "エラー", JOptionPane.ERROR_MESSAGE)
            return
        }
        if (tileField.text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "使用するタイルを入力してください", "エラー", JOptionPane.ERROR_MESSAGE)
            return
        }

        //入力したファイルが存在しなければ警告
        if (!checkFile(backField, "./BackGround", "その遠景ファイルは存在しません"))
            return
        if (!checkFile(bgmField, "./BGM", "そのBGMファイルは存在しません"))
            return
        if (!checkFile(tileField, "./TileSet", "そのタイルは存在しません"))
            return

        //同一の名前のマップファイルがある場合は警告
        if (Util.getDir("./MapFile").listFiles({ file, s -> s == mapNameField.text }).isNotEmpty()) {
            JOptionPane.showMessageDialog(this, "同じ名前のファイルがあります", "エラー", JOptionPane.ERROR_MESSAGE)
            return
        }

        //Map Editorに情報を通達して、セーブする
        owner.setBaseData(
                mapNameField.text + ".mps",
                mapw.value as Int,
                maph.value as Int,
                backField.text,
                bgmField.text,
                tileField.text,
                ChipType.values()[mapTypeSelector.selectedIndex],
                Dimension(chipw.value as Int, chiph.value as Int),
                gap.value as Int
        )
        owner.saveMap()
        end()
    }

    /**
     * ファイルの存在を判定する。
     * そのファイルがない場合は、falseを返す。
     * フィールドが空欄ならばtrueを返す
     */
    fun checkFile(field: JTextField, dir: String, message: String): Boolean {
        if (field.text.isEmpty())
            return true
        val hasTileSet = File(dir).listFiles({ file, s -> s == field.text }).isNotEmpty()
        if (!hasTileSet)
            JOptionPane.showMessageDialog(this, message, "エラー", JOptionPane.ERROR_MESSAGE)
        return hasTileSet
    }

    fun end() = this.dispose()
}
