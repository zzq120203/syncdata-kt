package cn.ac.iie.handler

import cn.ac.iie.configs.config
import cn.ac.iie.data.MetaData
import cn.ac.iie.server.MMSyncServer
import cn.ac.iie.tools.Convert
import cn.ac.iie.tools.FrameGrabberKit
import com.lmax.disruptor.LifecycleAware
import com.lmax.disruptor.WorkHandler
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * 数据同步
 * silk转wav格式
 * 视频首帧提取
 */

class DataSyncHandler : WorkHandler<MetaData>, LifecycleAware {

    private var oldName: String? = null
    private val name = "DataSyncThread-"

    private val convert = Convert()

    override fun onStart() {
        val currentThread = Thread.currentThread()
        oldName = currentThread.name
        currentThread.name = name + threadId.addAndGet(1)
    }

    override fun onShutdown() {
        Thread.currentThread().name = oldName ?: "OldDataSyncThread"
    }

    @Throws(Exception::class)
    override fun onEvent(mmd: MetaData) {
        try {
            if (mmd.key != null) {
                var outContent: ByteArray? = MMSyncServer.mainClient.get(mmd.key!!)
                if (outContent == null || outContent.isEmpty()) {
                    log.error("Audio outContent:null; key:{}; value:{}", mmd.key, mmd.value)
                    return
                }
                if ("a" == mmd.type) {
                    outContent = convert.silk2wav(outContent)
                    if (null == outContent) {
                        log.error("Audio inContent:null; key:{}; value:{}", mmd.key, mmd.value)
                        return
                    }
                    mmd.isSilk2wav = true
                }
                MMSyncServer.newClient.put(mmd.key, outContent, false)
                if ("v" == mmd.type) {
                    val inContent = FrameGrabberKit.grabberFFmpegImage(
                            "http://" + config().ipArray[idx.incrementAndGet() % config().ipArray.size] +
                                    ":20099/get?key=" + mmd.key)

                    MMSyncServer.newClient.put("i" + mmd.key!!.substring(1), inContent, false)
                }
                log.info("MMData Sync key:{}", mmd.key)
            }
        } catch (e: Exception) {
            log.error(e.message)
        }

    }

    companion object {

        private val log = LoggerFactory.getLogger(DataSyncHandler::class.java)
        private val threadId = AtomicInteger(0)

        private val idx = AtomicInteger(0)
    }
}
