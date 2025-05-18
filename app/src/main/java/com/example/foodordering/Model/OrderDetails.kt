package com.example.foodordering.Model


import java.io.Serializable
import java.util.ArrayList

// Thêm annotation này cho kotlinx.serialization
class OrderDetails( var userUid: String? = null,
                    var userName: String? = null,
                    var foodNames: MutableList<String>? = null,
                    var foodQuantities: MutableList<Int>? = null,
                    var foodPrices: MutableList<String>? = null,
                    var foodImages: MutableList<String>? = null,
                    var address: String? = null,
                    var totalPrice: String? = null,
                    var phoneNumber: String? = null,
                    var orderAccepted: Boolean = false,
                    var paymentReceived: Boolean = false,
                    var itemPushKey: String? = null,
                    var currentTime: Long = 0): Serializable { // Khai báo rõ ràng việc implement Parcelable


    // Constructor chính là OrderDetails() (không tham số)

    // Constructor cho Parcelable - nhận Parcel
    // Tham số 'parcel' của constructor này khớp với 'android.os.Parcel' đã import.
    // Constructor này gọi this(), tức là gọi constructor chính.


    // Constructor tiện ích
    constructor(
        userId: String,
        name: String,
        foodItemName: ArrayList<String>,
        foodItemQuantities: ArrayList<Int>,
        foodItemPrice: ArrayList<String>,
        foodItemImage: ArrayList<String>,
        address: String,
        totalAmount: String,
        phone: String,
        b: Boolean,
        b1: Boolean,
        itemPushKey: String?,
        time: Long
    ) : this() {
        this.userUid = userId
        this.userName = name
        this.foodNames = foodItemName
        this.foodQuantities = foodItemQuantities
        this.foodPrices = foodItemPrice
        this.foodImages = foodItemImage
        this.address = address
        this.totalPrice = totalAmount
        this.phoneNumber = phone
        this.orderAccepted = b
        this.paymentReceived = b1
        this.itemPushKey = itemPushKey
        this.currentTime = time
    }


}