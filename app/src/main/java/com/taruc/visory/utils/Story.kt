package com.taruc.visory.utils

class Story{
    var uid: String? = null
    var imageUrl: String? = null
    var sTitle: String? = null
    var sBody: String? = null
    var sDate: String? = null

    constructor(){

    }

    constructor(uid: String?, imageUrl: String?, sTitle: String?, sBody: String?, sDate: String?) {
        this.uid = uid
        this.imageUrl = imageUrl
        this.sTitle = sTitle
        this.sBody = sBody
        this.sDate = sDate
    }





}