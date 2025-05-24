package com.example.foodordering.Model // Hoặc package của app Admin: com.example.adminfoodordering.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable
import java.util.ArrayList // Cần thiết cho Parcelable lists

// Chuyển thành data class
data class OrderDetails(
    var userUid: String? = null,
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
    var currentTime: Long = 0,
    var orderDispatched: Boolean = false
) : Serializable, Parcelable {


    constructor() : this(
        null, null, null, null, null, null, null, null, null,
        false, false, null, 0L, false
    )

    // Constructor cho Parcelable
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList(),
        mutableListOf<Int>().apply {
            val size = parcel.readInt()
            if (size != -1) {
                for (i in 0 until size) {
                    add(parcel.readInt())
                }
            } else {

            }
        },
        parcel.createStringArrayList(),
        parcel.createStringArrayList(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userUid)
        parcel.writeString(userName)
        parcel.writeStringList(foodNames)

        // Ghi MutableList<Int> (foodQuantities)
        if (foodQuantities != null) {
            parcel.writeInt(foodQuantities!!.size)
            for (quantity in foodQuantities!!) {
                parcel.writeInt(quantity)
            }
        } else {
            parcel.writeInt(-1)
        }

        parcel.writeStringList(foodPrices)
        parcel.writeStringList(foodImages)
        parcel.writeString(address)
        parcel.writeString(totalPrice)
        parcel.writeString(phoneNumber)
        parcel.writeByte(if (orderAccepted) 1 else 0)
        parcel.writeByte(if (paymentReceived) 1 else 0)
        parcel.writeString(itemPushKey)
        parcel.writeLong(currentTime)
        parcel.writeByte(if (orderDispatched) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OrderDetails> {
        override fun createFromParcel(parcel: Parcel): OrderDetails {
            return OrderDetails(parcel)
        }

        override fun newArray(size: Int): Array<OrderDetails?> {
            return arrayOfNulls(size)
        }
    }
}
