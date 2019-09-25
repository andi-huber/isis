package org.ro.handler

import org.ro.org.ro.core.aggregator.UndefinedAggregator

class DefaultHandler : BaseHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        return true
    }

    override fun doHandle() {
        logEntry.aggregator = UndefinedAggregator()
        update()
//        console.log("[DefaultHandler.doHandle] no handler for $logEntry")
    }

}
