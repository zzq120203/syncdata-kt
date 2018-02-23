package cn.ac.iie.mq

import org.apache.rocketmq.client.producer.DefaultMQProducer
import org.apache.rocketmq.common.message.Message

class BroadcastProducer(groupName: String, address: String) {

    private var producer: DefaultMQProducer = DefaultMQProducer(groupName)

    init {
        producer.namesrvAddr = address
    }

    @Throws(Exception::class)
    fun start() {
        producer.start()
    }

    @Throws(Exception::class)
    fun send(message: Message) {
        producer.send(message)
    }

    @Throws(Exception::class)
    fun send(messages: List<Message>) {
        val splitter = ListSplitter(messages)
        while (splitter.hasNext()) {
            val listItem = splitter.next()
            producer.send(listItem)
        }
    }

    fun stop() {
        producer.shutdown()
    }

}