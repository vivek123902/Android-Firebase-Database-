package com.radhe.developers.addModule

data class InquiryModel (
    var inquiryId: String = "",
    var clientName: String = "",
    var clientNumber: String = "",
    var clientEmail: String = "",
    var cast: String = "",
    var clientReference: String = "",
    var clientRMedia: String = "",
    var categories: List<String> = emptyList(),
    var categoryName: String = "",
    var address: String = "",
    var followupDetails: List<FollowupData> = emptyList(),
)

data class FollowupData(
    var details: String = "",
    var followUpStatus: String = "",
    var followUpDate: String = "",
    var inquiryBy: String = "",
    var followupNote: String = "",
    var ratting: String = ""
)