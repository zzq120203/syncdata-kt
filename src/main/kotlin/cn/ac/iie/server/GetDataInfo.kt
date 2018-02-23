package cn.ac.iie.server

import cn.ac.iie.data.MetaData
import com.lmax.disruptor.RingBuffer
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis
import java.util.concurrent.atomic.AtomicBoolean

class GetDataInfo(private val ringBuffer: RingBuffer<MetaData>) : Runnable {

    override fun run() {
        var seq: Long = 0
        var mmd: MetaData
        do {
            val jedis = MMSyncServer.mainClient.pc.rpL1.resource
            try {
                jedis?.let {
                    val skeys = it.hgetAll("ds.set")
                    if (skeys.isNotEmpty()) {
                        log.info(" ds.set size :" + skeys.size)
                        for ((key, value) in skeys) {
                            try {
                                log.debug("get key:{}; value:{}", key, value)
                                seq = ringBuffer.next()
                                mmd = ringBuffer.get(seq)
                                mmd.key = key
                                mmd.value = value
                                mmd.type = mmd.key!![0].toString()
                                mmd.isSilk2wav = false

                                //hdelKey(jedis, "ds.set", key)
                            } finally {
                                ringBuffer.publish(seq)
                            }
                            if (!startGetDataInfo.get()) {
                                log.info("GetDataInfo break")
                                break
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                log.error(e.message)
            } finally {
                MMSyncServer.mainClient.pc.rpL1.putInstance(jedis)
            }

            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                log.error(e.message)
            }

        } while (startGetDataInfo.get())

    }

    private fun hdelKey(jedis: Jedis, vararg arg: String) {
        if (shaDel == null) {
            val script = "redis.call('hdel', KEYS[1], ARGV[1]);"
            shaDel = jedis.scriptLoad(script)
        }
        jedis.evalsha(shaDel, 1, arg[0], arg[1])
    }

    companion object {
        private val log = LoggerFactory.getLogger(GetDataInfo::class.java)
        var startGetDataInfo = AtomicBoolean(true)
        private var shaDel: String? = null
    }
}