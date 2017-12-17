package util

import java.io.File
import javax.swing.JFileChooser

/**
 * Created by yuu on 2016/12/18.
 */

object Util {
    /** 最小値と最大値の間に収める*/
    inline fun maxmin(min: Int, max: Int, value: Int) = Math.min(max, Math.max(min, value))

    /** 安全にディレクトリを取得する*/
    inline fun getDir(path: String) : File {
        File(path).mkdir()
        return File(path)
    }

    /** 安全にファイル選択ダイアログを取得する*/
    inline fun getChooser(path: String) : JFileChooser {
        File(path).mkdir()
        return JFileChooser(path)
    }
}