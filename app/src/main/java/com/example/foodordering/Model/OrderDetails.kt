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
    var orderDispatched: Boolean = false // TRƯỜNG MỚI THỰC SỰ
) : Serializable, Parcelable {

    // Constructor không tham số cho Firebase (data class tự tạo nếu tất cả thuộc tính có giá trị mặc định,
    // nhưng để rõ ràng và nếu có logic phức tạp hơn, bạn có thể định nghĩa tường minh)
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
                // Nếu size là -1, danh sách là null, nhưng vì foodQuantities là MutableList<Int>?
                // và chúng ta đã khởi tạo nó trong this(), chúng ta có thể để nó là emptyList()
                // hoặc xử lý null nếu logic của bạn cho phép foodQuantities là null hoàn toàn.
                // Để đơn giản, nếu size là -1, ta sẽ không thêm gì, nó sẽ là empty list.
                // Nếu bạn muốn nó thực sự là null, bạn cần thay đổi logic khởi tạo.
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
        parcel.readByte() != 0.toByte() // Đọc orderDispatched
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
            parcel.writeInt(-1) // Đánh dấu là null hoặc empty
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
        parcel.writeByte(if (orderDispatched) 1 else 0) // Ghi orderDispatched
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
