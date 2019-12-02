package com.taruc.visory.utils

fun getLastName(name: String): String{
    val firstSpace: Int = name.indexOf(" ")
    return name.substring(firstSpace).trim()
}

fun getFirstName(name: String): String{
    val firstSpace: Int = name.indexOf(" ")
    return name.substring(0, firstSpace)
}