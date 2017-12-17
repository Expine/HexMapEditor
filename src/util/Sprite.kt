package util

import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

/**
 * Created by yuu on 2016/12/17.
 */

class Sprite(var fileName: String, chipw: Int, chiph: Int) {
    /** 元の画像*/
    val base: BufferedImage
    /** 分離した画像*/
    val sprites = ArrayList<BufferedImage>()

    /** 初期化処理*/
    init {
        base = ImageIO.read(File("./TileSet/" + fileName))
        val wn = base.width / chipw
        val hn = base.height / chiph
        (0..hn - 1).forEachIndexed { index, y -> (0..wn - 1).forEach { sprites.add(base.getSubimage(it * chipw, y * chiph, chipw, chiph)) } }
    }

    /** 描画処理*/
    fun render(g: Graphics, id: Int, x: Int, y: Int) = g.drawImage(sprites[id], x, y, null)
}