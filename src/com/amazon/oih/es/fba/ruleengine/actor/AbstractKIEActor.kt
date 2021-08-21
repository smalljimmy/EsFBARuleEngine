package com.amazon.oih.es.fba.ruleengine.actor

import com.amazon.oih.es.fba.ruleengine.entity.DroolsMessage
import kotlinx.coroutines.channels.SendChannel

abstract class AbstractKIEActor {
    abstract  var actor: SendChannel<DroolsMessage>
}
