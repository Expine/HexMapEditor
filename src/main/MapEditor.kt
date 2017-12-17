package main

import layer.Chip
import util.Sprite
import util.Util
import java.awt.*
import java.awt.event.*
import java.io.*
import java.util.*
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

/**
* Created by yuu on 2016/12/16.
*/

/**
 * Map Editorの本体フレーム
 * Map Editorの描画、入力以外の処理を行う
 */
class MapEditor(title: String) : JFrame(title) {
    /** MainPanelのインスタンス*/
    var child: MainPanel? = null
    var scroll: JScrollPane? = null

    /** ファイルの名前(拡張子を含む)*/
    var _fileName = ""
    var fileName: String
        get() = _fileName
        set(value) {
            _fileName = value
            this.title = "MapFile/" + fileName + ".mps - " + title.substring(title.lastIndexOf("Map Editor - "))
        }

    /** マップファイルが含むマップ情報*/
    var backGround = ""
    var BGM = ""
    var tileSet: Sprite? = null
    var mapSize = Dimension(0, 0)
    var chipType = ChipType.SQUARE
    var chipSize = Dimension(16, 16)
    var gap = 10

    /** editorファイルが持つMap Editorの情報*/
    var mult = 3.0
    var layerNumber = 0
    var tool = MapTool.PENCIL

    /** 塗りの領域およびチップ情報*/
    var selectAreaSize = Dimension(1, 1)
    var selectTile = ArrayList<Int>()

    /** セーブ済みかどうかの情報*/
    var _saved = true
    var saved: Boolean
        get() = _saved
        set(value) {
            _saved = value
            if (title.endsWith('*')) {
                if (_saved)
                    this.title = this.title.substring(0, title.lastIndexOf("Map Editor - ") + 13) + "セーブしました"
            } else {
                if (!_saved)
                    this.title = this.title.substring(0, title.lastIndexOf("Map Editor - ") + 13) + "*"
            }
        }

    /** リサイズしてrepaintさせるための変数*/
    var extraResize = 1

    /** ボタンのグループ*/
    var layerMenuItem: ButtonGroup = ButtonGroup()
    var layerToolItem: ButtonGroup = ButtonGroup()
    var toolMenuItem: ButtonGroup = ButtonGroup()
    var toolToolItem: ButtonGroup = ButtonGroup()
    var resoToolItem: ButtonGroup = ButtonGroup()

    /** 初期化*/
    init {
        setTitle("Map Editor - マップを開いてください")
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) = end()
        })
        setMenuBar()
        setToolBar()

        selectTile.add(0)
    }

    /** メニューバーを設定する*/
    fun setMenuBar() {
        //メニューおよびメニューバー
        val menuBar = JMenuBar()
        val menus: Array<JMenu?> = kotlin.arrayOfNulls<JMenu?>(7)

        //メニューの名前を設定
        menus[0] = JMenu("ファイル(F)")
        menus[1] = JMenu("編集(E)")
        menus[2] = JMenu("表示(S)")
        menus[3] = JMenu("レイヤー(L)")
        menus[4] = JMenu("ツール(T)")
        menus[5] = JMenu("オプション(O)")
        menus[6] = JMenu("ヘルプ(H)")

        //ニーモニックを設定
        menus[0]?.setMnemonic('F')
        menus[1]?.setMnemonic('E')
        menus[2]?.setMnemonic('S')
        menus[3]?.setMnemonic('L')
        menus[4]?.setMnemonic('T')
        menus[5]?.setMnemonic('O')
        menus[6]?.setMnemonic('H')

        //メニューバーにメニューを追加
        for (menu in menus) {
            menu?.font = Font("MS 明朝", Font.PLAIN, 12)
            menuBar.add(menu)
            menuBar.add(Box.createRigidArea(Dimension(5, 1)))
        }

        //メニューの項目
        val item = kotlin.arrayOfNulls<Array<JMenuItem?>?>(7)
        item[0] = kotlin.arrayOfNulls<JMenuItem>(5)
        item[1] = kotlin.arrayOfNulls<JMenuItem>(6)
        item[2] = kotlin.arrayOfNulls<JMenuItem>(4)
        item[3] = kotlin.arrayOfNulls<JMenuItem>(4)
        item[4] = kotlin.arrayOfNulls<JMenuItem>(3)
        item[5] = kotlin.arrayOfNulls<JMenuItem>(1)
        item[6] = kotlin.arrayOfNulls<JMenuItem>(1)

        //ファイル直下の項目
        item[0]!![0] = JMenuItem("マップ新規作成")
        item[0]!![1] = JMenuItem("マップ読み込み")
        item[0]!![2] = JMenuItem("マップ上書き保存")
        item[0]!![3] = JMenuItem("名前を付けて保存")
        item[0]!![4] = JMenuItem("エディターの終了")

        //編集直下の項目
        item[1]!![0] = JMenuItem("イベント切り取り")
        item[1]!![1] = JMenuItem("イベントコピー")
        item[1]!![2] = JMenuItem("イベント貼り付け")
        item[1]!![3] = JMenuItem("イベント削除")
        item[1]!![4] = JMenuItem("アンドゥ")
        item[1]!![5] = JMenuItem("マップ基本設定")

        //表示直下の項目
        item[2]!![0] = JCheckBoxMenuItem("ツールバー")
        item[2]!![0]?.isSelected = true
        item[2]!![1] = JMenuItem("タイルセットウィンドウ")
        item[2]!![2] = JMenuItem("マップ選択ウィンドウ")
        item[2]!![3] = JMenuItem("タイル設定")

        //レイヤー直下の項目
        item[3]!![0] = JRadioButtonMenuItem("レイヤー１")
        item[3]!![0]?.isSelected = true
        item[3]!![1] = JRadioButtonMenuItem("レイヤー２")
        item[3]!![2] = JRadioButtonMenuItem("レイヤー３")
        item[3]!![3] = JRadioButtonMenuItem("レイヤーイベント")
        for (i in item[3]!!)
            layerMenuItem.add(i)

        //レイヤー直下の項目
        item[4]!![0] = JRadioButtonMenuItem("鉛筆ツール")
        item[4]!![0]?.isSelected = true
        item[4]!![1] = JRadioButtonMenuItem("四角形ツール")
        item[4]!![2] = JRadioButtonMenuItem("塗りつぶしツール")
        for (i in item[4]!!)
            toolMenuItem.add(i)

        //オプション直下の項目
        item[5]!![0] = JMenuItem("エディターオプション")
        item[6]!![0] = JMenuItem("ヘルプ")

        //キーアクセラレーションの設定
        item[0]!![0]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK)
        item[0]!![1]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK)
        item[0]!![2]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK)
        item[0]!![3]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK)

        item[1]!![0]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK)
        item[1]!![1]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK)
        item[1]!![2]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK)
        item[1]!![3]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE.toChar())
        item[1]!![4]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK)
        item[1]!![5]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK)

        item[2]!![0]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK)
        item[2]!![1]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_DOWN_MASK)
        item[2]!![2]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK)
        item[2]!![3]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK)

        item[3]!![0]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK)
        item[3]!![1]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK)
        item[3]!![2]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK)
        item[3]!![3]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_DOWN_MASK)

        item[4]!![0]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.CTRL_DOWN_MASK)
        item[4]!![1]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.CTRL_DOWN_MASK)
        item[4]!![2]?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK)

        //メニューのアクションを設定
        item[0]!![0]?.addActionListener { newMap() }
        item[0]!![1]?.addActionListener { openMap() }
        item[0]!![2]?.addActionListener { saveMap() }
        item[0]!![3]?.addActionListener { newSaveMap() }
        item[0]!![4]?.addActionListener { end() }

        item[1]!![0]?.addActionListener { cut() }
        item[1]!![1]?.addActionListener { copy() }
        item[1]!![2]?.addActionListener { paste() }
        item[1]!![3]?.addActionListener { delete() }
        item[1]!![4]?.addActionListener { undo() }

        item[2]!![1]?.addActionListener { showTileSetWindow() }
        item[2]!![2]?.addActionListener { showMapSelectWindow() }
        item[2]!![3]?.addActionListener { showTileSettingWindow() }

        item[3]!![0]?.addActionListener { setLayer(0) }
        item[3]!![1]?.addActionListener { setLayer(1) }
        item[3]!![2]?.addActionListener { setLayer(2) }
        item[3]!![3]?.addActionListener { setLayer(3) }

        item[4]!![0]?.addActionListener { setMapTool(MapTool.PENCIL) }
        item[4]!![1]?.addActionListener { setMapTool(MapTool.SQUARE) }
        item[4]!![2]?.addActionListener { setMapTool(MapTool.PAINT) }

        item[5]!![0]?.addActionListener { option() }
        item[6]!![0]?.addActionListener { help() }

        //メニューの項目をメニューに追加
        for ((index, items) in item.withIndex()) {
            for ((n, i) in items!!.withIndex()) {
                menus[index]?.add(i)
                i?.font = Font("MS 明朝", Font.PLAIN, 12)
                if ((index == 0 && n == 3) || (index == 1 && n == 4))
                    menus[index]?.addSeparator()
            }
        }

        jMenuBar = menuBar
    }

    /** ツールバーを設定する*/
    fun setToolBar() {
        //ツールバー
        val tool = JToolBar()
        tool.isFloatable = true
        tool.layout = FlowLayout(FlowLayout.LEFT)

        //ツールバーに載せるボタン
        val buttons = kotlin.arrayOfNulls<AbstractButton?>(14)

        //新規・セーブボタン
        buttons[0] = JButton(ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_new.png")))
        buttons[1] = JButton(ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_save.png")))

        //レイヤーボタン
        buttons[2] = JRadioButton(ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_frame1.png")))
        buttons[2]?.selectedIcon = ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_frame1_s.png"))
        buttons[3] = JRadioButton(ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_frame2.png")))
        buttons[3]?.selectedIcon = ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_frame2_s.png"))
        buttons[4] = JRadioButton(ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_frame3.png")))
        buttons[4]?.selectedIcon = ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_frame3_s.png"))
        buttons[5] = JRadioButton(ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_frame_event.png")))
        buttons[5]?.selectedIcon = ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_frame_event_s.png"))
        buttons[2]?.isSelected = true
        for(i in 2..5)
            layerToolItem.add(buttons[i])

        //拡大率ボタン
        buttons[6] = JRadioButton(ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_reso1.png")))
        buttons[6]?.selectedIcon = ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_reso1_s.png"))
        buttons[7] = JRadioButton(ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_reso2.png")))
        buttons[7]?.selectedIcon = ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_reso2_s.png"))
        buttons[8] = JRadioButton(ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_reso3.png")))
        buttons[8]?.selectedIcon = ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_reso3_s.png"))
        buttons[9] = JRadioButton(ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_reso4.png")))
        buttons[9]?.selectedIcon = ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_reso4_s.png"))
        buttons[6]?.isSelected = true
        for(i in 6..9)
            resoToolItem.add(buttons[i])

        //ツールボタン
        buttons[10] = JRadioButton(ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_pencil.png")))
        buttons[10]?.selectedIcon = ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_pencil_s.png"))
        buttons[11] = JRadioButton(ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_square.png")))
        buttons[11]?.selectedIcon = ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_square_s.png"))
        buttons[12] = JRadioButton(ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_paint.png")))
        buttons[12]?.selectedIcon = ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_paint_s.png"))
        buttons[10]?.isSelected = true
        for(i in 10..12)
            toolToolItem.add(buttons[i])

        //アンドゥボタン
        buttons[13] = JButton(ImageIcon(javaClass.classLoader.getResource("resources/icon/tool_back.png")))

        //ボタンのアクションを設定
        buttons[0]?.addActionListener { newMap() }
        buttons[1]?.addActionListener { saveMap() }

        buttons[2]?.addActionListener { setLayer(0) }
        buttons[3]?.addActionListener { setLayer(1) }
        buttons[4]?.addActionListener { setLayer(2) }
        buttons[5]?.addActionListener { setLayer(3) }

        buttons[6]?.addActionListener { setMultiple(Resolution.ONE) }
        buttons[7]?.addActionListener { setMultiple(Resolution.TWO) }
        buttons[8]?.addActionListener { setMultiple(Resolution.FOUR) }
        buttons[9]?.addActionListener { setMultiple(Resolution.EIGHT) }

        buttons[10]?.addActionListener { setMapTool(MapTool.PENCIL) }
        buttons[11]?.addActionListener { setMapTool(MapTool.SQUARE) }
        buttons[12]?.addActionListener { setMapTool(MapTool.PAINT) }

        buttons[13]?.addActionListener { undo() }


        //ボタンをツールバーに追加
        for ((i, item) in buttons.withIndex()) {
            tool.add(item)
            if (i == 1 || i == 5 || i == 9)
                tool.addSeparator()
        }

        contentPane.add(tool, BorderLayout.NORTH)
    }

    /** 新規マップを作成する*/
    fun newMap() {
        //最も小さいmap$numを探し出してファイル名とする
        var num = 1
        while (Util.getDir("./MapFile").listFiles({ file, s -> s == "map$num.mps" }).isNotEmpty()) num++
        NewMapDialog(this, "map$num")
    }

    /** マップファイルを開く（警告、ファイル選択ダイアログが出る）*/
    fun openMap() {
        //セーブ済みでない場合は警告を出す
        if (!saved) {
            val judge = JOptionPane.showOptionDialog(this,
                    "現在のマップは未保存です。保存しますか？",
                    "未保存です",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
                    arrayOf("保存して開く", "保存せずに開く", "キャンセル"),
                    "新規作成をキャンセル")
            when (judge) {
                0 -> saveMap()
                2 -> return
            }
        }

        //開くファイルを選ぶダイアログを出す
        val chooser = Util.getChooser("./MapFile")
        chooser.addChoosableFileFilter(FileNameExtensionFilter("mpsファイル", "mps"))
        chooser.isAcceptAllFileFilterUsed = false
        when (chooser.showOpenDialog(this)) {
            JFileChooser.APPROVE_OPTION -> {
                fileName = chooser.selectedFile.name
                openFile()
            }
        }
    }

    /** 現在のマップをマップファイルにセーブする*/
    fun saveMap() {
        File("./MapFile").mkdir()
        val out = OutputStreamWriter(FileOutputStream("./MapFile/$fileName"))
        out.write("${mapSize.width},${mapSize.height},${chipSize.width},${chipSize.height},$gap,$backGround,$BGM,${tileSet?.fileName},$chipType\n")
        child?.writeData(out)
        out.close()
        //セーブして、マップツリーに反映させる
        saved = true
        MapTreeOperator.update()
    }

    /** 現在のマップを新しいマップファイルにセーブする*/
    fun newSaveMap() {
        val chooser = Util.getChooser("./MapFile")
        chooser.addChoosableFileFilter(FileNameExtensionFilter("mpsファイル", "mps"))
        chooser.isAcceptAllFileFilterUsed = false
        when (chooser.showSaveDialog(this)) {
            JFileChooser.APPROVE_OPTION -> {
                File("./MapFile/${chooser.selectedFile.name}.mps").createNewFile()
                fileName = chooser.selectedFile.name
                saveMap()
            }
        }
    }

    /** Map Editorを終了する*/
    fun end() {
        //セーブ済みでないなら、終了に警告を出す
        if (!saved) {
            val judge = JOptionPane.showOptionDialog(this,
                    "未保存です。終了しますか？",
                    "未保存です",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
                    arrayOf("保存して終了", "保存せずに終了", "キャンセル"),
                    "キャンセル")
            when (judge) {
                0 -> saveMap()
                2 -> return
            }
        }

        //Map Editorの情報を書き込む
        val out = OutputStreamWriter(FileOutputStream("./.editor"))
        out.write("$fileName,${location.x},${location.y},${size.width},${size.height},$layerNumber,${tool.ordinal},$mult\n")
        out.write("${MapTreeOperator.isShowing()}")
        if (MapTreeOperator.isShowing()) {
            val dialog = MapTreeOperator.dialog!!
            out.write(",${dialog.location.x},${dialog.location.y},${dialog.size.width},${dialog.size.height}")
        }
        out.write("\n")
        out.write("${TileSetOperator.isShowing()}")
        if (TileSetOperator.isShowing()) {
            val dialog = TileSetOperator.dialog!!
            out.write(",${dialog.location.x},${dialog.location.y},${dialog.size.width},${dialog.size.height}")
        }
        out.close()
        this.dispose()
    }

    /** イベントのカット(未実装)*/
    fun cut() {}
    /** イベントのコピー(未実装)*/
    fun copy() {}
    /** イベントの貼り付け(未実装)*/
    fun paste() {}
    /** イベントの削除(未実装)*/
    fun delete() {}

    /** アンドゥ処理*/
    fun undo() {
        child?.undo()
        repaint()
    }

    /** タイルセットウィンドウを表示する*/
    fun showTileSetWindow() {
        TileSetOperator.openTileSet(this)
    }

    /** マップ選択ウィンドウを表示する*/
    fun showMapSelectWindow() {
        MapTreeOperator.openMapTree(this)
    }

    /** タイル設定ウィンドウを表示する（未実装）*/
    fun showTileSettingWindow() {}

    /** レイヤーを設定する*/
    fun setLayer(number: Int) {
        //レイヤー3は未実装
        if (number == 3)
            JOptionPane.showMessageDialog(this, "未実装です", "お詫び", JOptionPane.INFORMATION_MESSAGE)
        else
            layerNumber = number

        //レイヤーボタングループを同期
        layerMenuItem.elements.toList().forEachIndexed { i, item -> item.isSelected = i == layerNumber }
        layerToolItem.elements.toList().forEachIndexed { i, item -> item.isSelected = i == layerNumber }
        repaint()
    }

    /** ツールを設定する*/
    fun setMapTool(tool: MapTool) {
        this.tool = tool
        //ツールボタンを同期する
        toolMenuItem.elements.toList().forEachIndexed { i, item -> item.isSelected = i == tool.ordinal }
        toolToolItem.elements.toList().forEachIndexed { i, item -> item.isSelected = i == tool.ordinal }
    }

    /** 拡大率を設定する*/
    fun setMultiple(mult: Resolution) {
        this.mult = mult.resolution
        //拡大率ボタンを同期しておく
        resoToolItem.elements.toList().forEachIndexed { i, item ->item.isSelected = i == mult.ordinal  }

        //マップが存在するならば、パネルの適切なサイズを変更し、再描画させる
        if (isMapExist()) {
            child?.preferredSize = Dimension((getMapWidth() * this.mult).toInt(), (getMapHeight() * this.mult).toInt())
            repaintByResize()
        }
    }

    /** チップタイプに依存してマップの実横幅を取得（拡大率は反映しない）*/
    fun getMapWidth() = when(chipType) {
        ChipType.SQUARE -> mapSize.width * chipSize.width
        ChipType.HEX -> mapSize.width * (chipSize.width + gap) + (chipSize.width - gap) / 2
    }

    /** チップタイプに依存してマップの実縦幅を取得（拡大率は反映しない）*/
    fun getMapHeight() = when(chipType) {
        ChipType.SQUARE -> mapSize.height * chipSize.height
        ChipType.HEX -> mapSize.height * chipSize.height / 2 + chipSize.height / 2
    }

    /** エディターオプション(未実装)*/
    fun option() {}

    /** ヘルプ(未実装)*/
    fun help() {}

    /** マップの基本データを設定する*/
    fun setBaseData(name: String, width: Int, height: Int, back: String, bgm: String, tile: String, type: ChipType, chipSize: Dimension, gap: Int) {
        fileName = name
        mapSize.setSize(width, height)
        backGround = back
        BGM = bgm
        chipType = type
        this.chipSize = chipSize
        this.gap = gap
        //取得した情報をもとに、タイルセットの画像を作成
        tileSet = Sprite(tile, chipSize.width, chipSize.height)

        //取得した情報をもとに、レイヤー情報を構成
        child?.setLayerData(width, height, tileSet, chipSize)

        //同じ拡大率で再描画させる
        setMultiple(Resolution.values().filter { it.resolution == mult }.first())

        //タイルセットウィンドウを表示
        showTileSetWindow()
    }

    /** マップファイルが存在しているかどうかの判定*/
    fun isMapExist() = mapSize.width != 0

    /** リサイズを用いて再描画する*/
    fun repaintByResize() {
        size = Dimension(width + extraResize, height + extraResize)
        extraResize *= -1
    }

    /** ファイルを開き、マップファイルにパースする*/
    fun openFile() {
        //そのファイルが存在しない場合は何もしない
        if (!File("./MapFile/$fileName").exists())
            return
        //マップデータをパースする
        val br = BufferedReader(FileReader("./MapFile/$fileName"))
        //基本情報をパース
        parseData(br.readLine())
        //チップ情報をパース
        child?.parseData(br.readLine())
        br.close()

        //再描画
        setMultiple(Resolution.values().filter { it.resolution == mult }.first())
        repaintByResize()

        saved = true
        TileSetOperator.update()
    }
    /** マップファイルから基本情報をパースする*/
    fun parseData(line: String) {
        line.split(",").forEachIndexed { i, s -> when(i){
            0 -> mapSize.width = s.toInt()
            1 -> mapSize.height = s.toInt()
            2 -> chipSize.width = s.toInt()
            3 -> chipSize.height = s.toInt()
            4 -> gap = s.toInt()
            5 -> backGround = s
            6 -> BGM = s
            7 -> tileSet = Sprite(s, chipSize.width, chipSize.height)
            8 -> chipType = ChipType.valueOf(s)
        } }
        //選択中のタイルセットを初期化
        selectAreaSize.setSize(1, 1)
        selectTile.clear()
        selectTile.add(0)
        TileSetOperator.initialize()
    }

    /** editorファイルからMap Editorの情報をパースする*/
    fun parseBaseData() {
        //editorファイルがなければ処理しない
        if (!File("./.editor").exists())
            return
        val br = BufferedReader(FileReader("./.editor"))
        var preValue = 0

        //Map Editor本体の情報をパース
        br.readLine().split(",").forEachIndexed { i, s -> when(i) {
            0 -> { fileName = s; openFile() }
            1 -> preValue = s.toInt()
            2 -> setLocation(preValue, s.toInt())
            3 -> preValue = s.toInt()
            4 -> setSize(preValue, s.toInt())
            5 -> setLayer(s.toInt())
            6 -> setMapTool(MapTool.values()[s.toInt()])
            7 -> setMultiple(Resolution.values().find { it.resolution == s.toDouble() }!!)
        } }

        //マップ選択ウィンドウの情報をパース
        br.readLine().split(",").forEachIndexed { i, s -> when(i) {
            0 -> if(s.toBoolean()) { MapTreeOperator.openMapTree(this) }
            1 -> preValue = s.toInt()
            2 -> MapTreeOperator.dialog?.setLocation(preValue, s.toInt())
            3 -> preValue = s.toInt()
            4 -> MapTreeOperator.dialog?.setSize(preValue, s.toInt())
        } }

        br.readLine().split(",").forEachIndexed { i, s -> when(i) {
            0 -> if(s.toBoolean()) { TileSetOperator.openTileSet(this) }
            1 -> preValue = s.toInt()
            2 -> TileSetOperator.dialog?.setLocation(preValue, s.toInt())
            3 -> preValue = s.toInt()
            4 -> TileSetOperator.dialog?.setSize(preValue, s.toInt())
        } }
    }

    /** タイルセットの横のチップの数を取得*/
    fun getChipWNumber() = tileSet?.base?.width!! / chipSize.width
    fun getChipHNumber() = tileSet?.base?.height!! / chipSize.height

    /** 指定座標からタイル座標を取得*/
    fun getTile(x: Int, y: Int) =
            when(chipType) {
                ChipType.SQUARE -> Point((x / (chipSize.width * mult)).toInt(), (y / (chipSize.height * mult)).toInt())
                ChipType.HEX -> getHexTile(x, y)
            }
    fun getHexTile(x:Int, y:Int) : Point{
        val ex = Util.maxmin(0, mapSize.width - 1, getHexTileXEasy(x).toInt())
        val ey = Util.maxmin(0, mapSize.height - 1, getHexTileYEasy(x, y).toInt())
        return Point(ex, ey)
    }
    fun getHexTileXEasy(x: Int) = x.toDouble() / (chipSize.width + gap).toDouble() / mult
    fun getHexTileYEasy(x: Int, y: Int) : Int{
        val even = (x.toDouble() % ((chipSize.width + gap) * mult)) <= (chipSize.width + gap).toDouble() * mult / 2
        return ((if(even) y.toDouble() else (y.toDouble() - (chipSize.height * mult / 2))) / chipSize.height / mult).toInt() * 2 + if(even) 0 else 1
    }

    /** 指定タイル座標から座標を取得*/
    fun getCoordinateByTile(x: Int, y: Int) =
            when(chipType) {
                ChipType.SQUARE -> Point((x * chipSize.width * mult).toInt(), (y * chipSize.height * mult).toInt())
                ChipType.HEX -> Point((x * (chipSize.width + gap) * mult + (y % 2) * (chipSize.width + gap) * mult / 2).toInt(), (y * chipSize.height * mult / 2).toInt())
            }

    /** 選択領域の実サイズを返す*/
    fun getSelectAreaWidth() =
            when(chipType) {
                ChipType.SQUARE -> (selectAreaSize.width -1) * chipSize.width * mult
                ChipType.HEX -> (selectAreaSize.width - 1) * (chipSize.width + gap) * mult
            }
    fun getSelectAreaHeight() =
            when(chipType) {
                ChipType.SQUARE -> (selectAreaSize.height - 1) * chipSize.height * mult
                ChipType.HEX -> (selectAreaSize.height- 1) * chipSize.height * mult / 2
            }

    /** 描画領域を返す*/
    fun getRenderingArea() = RenderingArea(this, Dimension(scroll?.viewport?.x!!, scroll?.viewport?.y!!), Dimension(scroll?.viewport?.x!! + (width * mult).toInt(), scroll?.viewport?.y!! + (height * mult).toInt()))
}
class RenderingArea(val parent: MapEditor, val start: Dimension, val end: Dimension) {
    fun inArea(x: Int, y: Int, w: Int, h: Int) = x + w > start.width && y +  h > start.height && x < end.width && y < end.height
    fun getMultipleStartPosX() = start.width / parent.mult
    fun getMultipleStartPosY() = start.height / parent.mult
}


/** チップの種別*/
enum class ChipType {
    SQUARE, HEX
}

/** 解像度の種別*/
enum class Resolution(val resolution: Double) {
    ONE(3.0), TWO(1.5), FOUR(0.75), EIGHT(0.375)
}

enum class MapTool {
    PENCIL, SQUARE, PAINT
}