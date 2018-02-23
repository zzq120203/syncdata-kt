package cn.ac.iie.handler

import cn.ac.iie.configs.config
import cn.ac.iie.data.MetaData
import cn.ac.iie.data.SendData
import cn.ac.iie.mq.BroadcastProducer
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.lmax.disruptor.LifecycleAware
import com.lmax.disruptor.WorkHandler
import org.apache.rocketmq.common.message.Message
import org.apache.rocketmq.remoting.common.RemotingHelper
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class EngineInterface : WorkHandler<MetaData>, LifecycleAware {

    private var oldName: String? = null
    private val name = "DataSyncThread-"
    private val messages = ArrayList<Message>()

    init {
        try {
            bp.start()
        } catch (e: Exception) {
            log.error(e.message)
        }

    }

    override fun onStart() {
        val currentThread = Thread.currentThread()
        oldName = currentThread.name
        currentThread.name = name + threadId.addAndGet(1)
    }

    override fun onShutdown() {
        Thread.currentThread().name = oldName!!
    }

    @Throws(Exception::class)
    override fun onEvent(mmd: MetaData) {
        try {
            val msg = Message()
            msg.topic = config().topic
            when (mmd.type) {
                "a" -> msg.tags = "a"
                "v" -> msg.tags = "v"
                "i" -> msg.tags = "i"
                "t" -> msg.tags = "t"
                "o" -> msg.tags = "o"
            }

            val se = SendData()
            val jsonObj = parser.parse(mmd.value!!).asJsonObject
            se.table = jsonObj.get("table").asString
            se.u_ch_id = jsonObj.get("u_ch_id").asString
            se.m_ch_id = jsonObj.get("m_ch_id").asString
            se.m_chat_room = jsonObj.get("m_chat_room").asString
            se.key = mmd.key

            val json = gson.toJson(se)

            msg.body = json.toByteArray(charset(RemotingHelper.DEFAULT_CHARSET))

            messages.add(msg)

            if (messages.size > 10) {
                bp.send(messages)
                messages.clear()
            }
        } catch (e: Exception) {
            log.error(e.message)
        }

    }

    companion object {
        val bp: BroadcastProducer = BroadcastProducer(config().groupName, config().address)
        private val log = LoggerFactory.getLogger(EngineInterface::class.java)
        private val threadId = AtomicInteger(0)
        private val parser = JsonParser()
        private val gson = Gson()
    }
}
