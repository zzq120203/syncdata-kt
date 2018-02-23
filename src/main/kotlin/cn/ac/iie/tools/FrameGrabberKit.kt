package cn.ac.iie.tools

import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.imageio.ImageIO

object FrameGrabberKit {

    /**
     * 抓取setoff帧图片
     *
     * @return
     */
    @Throws(Exception::class)
    fun grabberFFmpegImage(filePath: String): ByteArray? {
        val ff = FFmpegFrameGrabber.createDefault(filePath)

        ff.start()
        val f = ff.grabImage()
        if (f?.image == null) {
            return null
        }
        val converter = Java2DFrameConverter()
        val bi = converter.getBufferedImage(f)
        val out = ByteArrayOutputStream()
        try {
            ImageIO.write(bi, "jpg", out)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val b = out.toByteArray()
        ff.stop()
        return b
    }

}
