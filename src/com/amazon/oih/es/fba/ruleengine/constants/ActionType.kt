package com.amazon.oih.es.fba.ruleengine.constants

enum class ActionType(val attributeValue: String) {
    EditListing("EditListing"),
    ImproveKeywords("ImproveKeywords"),
    AdvertiseListing("AdvertiseListing"),
    CreateASale("CreateASale"),
    RemovalOrder("RemovalOrder"),
    ImproveEconomicInputs("ImproveEconomicInputs"),
    OutletDeal("OutletDeal"),
    Unknown("Unknown"),
    Timeout("Timeout");
}
