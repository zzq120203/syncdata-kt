package cn.ac.iie.data

data class MetaData (
        var key: String? = null,
        var value: String? = null,
        var type: String? = null,
        var isSilk2wav: Boolean = false
)

data class SendData (
        var key: String? = null,
        var m_chat_room: String? = null,
        var u_ch_id: String? = null,
        var m_ch_id: String? = null,
        var table: String? = null
)
