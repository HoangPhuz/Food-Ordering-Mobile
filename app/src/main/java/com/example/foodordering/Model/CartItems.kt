package com.example.foodordering.Model

data class CartItems(
    val foodName: String?=null,
    val foodImage: String?=null,
    val foodPrice: String?=null,
    val foodDescription:String?=null,
    val foodIngredient: String?=null,
    val foodQuantity:Int?=null
)
