package cn.ac.iie.server

import cn.ac.iie.client.ClientAPI
import cn.ac.iie.configs.ConfLoading
import cn.ac.iie.configs.config
import cn.ac.iie.data.MetaData
import cn.ac.iie.handler.DataSyncHandler
import cn.ac.iie.handler.EngineInterface
import com.lmax.disruptor.BlockingWaitStrategy
import com.lmax.disruptor.EventFactory
import com.lmax.disruptor.dsl.Disruptor
import com.lmax.disruptor.dsl.ProducerType
import com.lmax.disruptor.util.DaemonThreadFactory
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.slf4j.LoggerFactory
import java.io.File

object MMSyncServer {

    private val log = LoggerFactory.getLogger(MMSyncServer::class.java)

    var mainClient = ClientAPI()
    var newClient = ClientAPI()

    init {
        val logContext = LogManager.getContext(false) as LoggerContext
        val log4jFile = File(System.getProperty("log4j.configuration"))
        logContext.configLocation = log4jFile.toURI()
        logContext.reconfigure()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            log.info("MMHandler starting....")

            ConfLoading.init(System.getProperty("config"))

            mainClient.init(config().mainClientUrl, "TEST")
            newClient.init(config().newClientUrl, "TEST")


            val disruptor = Disruptor(EventFactory<MetaData> { MetaData() }, 512, DaemonThreadFactory.INSTANCE, ProducerType.MULTI, BlockingWaitStrategy())
            disruptor.handleEventsWithWorkerPool(DataSyncHandler()).handleEventsWithWorkerPool(EngineInterface())

            val ringBuffer = disruptor.start()

            val thread = Thread(GetDataInfo(ringBuffer), "MainMMKeyThread")
            thread.start()

            Runtime.getRuntime().addShutdownHook(Thread {
                log.info("Execute shutdown Hook.....")
                try {
                    GetDataInfo.startGetDataInfo.set(false)

                    disruptor.shutdown()
                    mainClient.quit()
                    newClient.quit()

                    EngineInterface.bp.stop()
                } catch (e: Exception) {
                    log.error(e.message)
                }

                log.info("MMHandler exiting......")


            })

        } catch (e: Exception) {
            log.error(e.message)
        }

    }
}
