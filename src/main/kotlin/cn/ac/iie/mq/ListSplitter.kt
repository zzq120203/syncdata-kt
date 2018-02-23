package cn.ac.iie.mq

import org.apache.rocketmq.common.message.Message

class ListSplitter(private val messages: List<Message>) : Iterator<List<Message>> {
    private val SIZE_LIMIT = 1000 * 1000
    private var currIndex: Int = 0
    override fun hasNext(): Boolean {
        return currIndex < messages.size
    }

    override fun next(): List<Message> {
        var nextIndex = currIndex
        var totalSize = 0
        while (nextIndex < messages.size) {
            val message = messages[nextIndex]
            var tmpSize = message.topic.length + message.body.size
            val properties = message.properties
            for ((key, value) in properties) {
                tmpSize += key.length + value.length
            }
            tmpSize = tmpSize + 20 //for log overhead
            if (tmpSize > SIZE_LIMIT) {
                //it is unexpected that single message exceeds the SIZE_LIMIT
                //here just let it go, otherwise it will block the splitting process
                if (nextIndex - currIndex == 0) {
                    //if the next sublist has no element, add this one and then break, otherwise just break
                    nextIndex++
                }
                break
            }
            if (tmpSize + totalSize > SIZE_LIMIT) {
                break
            } else {
                totalSize += tmpSize
            }
            nextIndex++

        }
        val subList = messages.subList(currIndex, nextIndex)
        currIndex = nextIndex
        return subList
    }
}