package cn.ac.iie.tools

class JNIConvert {
    init {
        System.loadLibrary("silk")
    }

    external fun silk2pcm(inSilk: ByteArray): ByteArray

}
