package com.taruc.visory.utils

class Story{
    var uid: String? = null
    var imageUrl: String? = null
    var sTitle: String? = null
    var sBody: String? = null
    var sDate: String? = null
    var status: String? = null
    var username: String? = null

    constructor(uid: String?, imageUrl: String?, sTitle: String?, sBody: String?, sDate: String?, status: String?, username: String?) {
        this.uid = uid
        this.imageUrl = imageUrl
        this.sTitle = sTitle
        this.sBody = sBody
        this.sDate = sDate
        this.status = status
        this.username = username
    }

    constructor():this("","","","","", "", ""){

    }
}