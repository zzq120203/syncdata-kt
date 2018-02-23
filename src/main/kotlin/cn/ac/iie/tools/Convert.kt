package cn.ac.iie.tools

import org.slf4j.LoggerFactory

class Convert {

    private var jc: JNIConvert = JNIConvert()

    @Throws(Exception::class)
    fun silk2wav(src: ByteArray): ByteArray? {
        val pcm = jc.silk2pcm(src)
        if (pcm.isEmpty()) return null
        val PCMSize = pcm.size

        // 填入参数，比特率等等。这里用的是16位单声道 8000 hz
        val header = WaveHeader()
        // 长度字段 = 内容的大小（PCMSize) + 头部字段的大小(不包括前面4字节的标识符RIFF以及fileLength本身的4字节)
        header.fileLength = PCMSize + (44 - 8)
        header.FmtHdrLeth = 16
        header.BitsPerSample = 16
        header.Channels = 1
        header.FormatTag = 0x0001
        header.SamplesPerSec = 8000
        header.BlockAlign = (header.Channels * header.BitsPerSample / 8).toShort()
        header.AvgBytesPerSec = header.BlockAlign * header.SamplesPerSec
        header.DataHdrLeth = PCMSize

        val h = header.header

        assert(h.size == 44) // WAV标准，头部应该是44字节

        val target = ByteArray(h.size + pcm.size)
        System.arraycopy(h, 0, target, 0, h.size)
        System.arraycopy(pcm, 0, target, h.size, pcm.size)
        log.debug("Convert OK!")
        return target
    }

    companion object {

        private val log = LoggerFactory.getLogger(Convert::class.java)
    }

}