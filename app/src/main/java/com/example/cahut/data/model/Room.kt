package com.example.cahut.data.model

import com.google.gson.annotations.SerializedName

data class CreateRoomResponse(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("roomId")
    val roomId: String,
    
    @SerializedName("room")
    val room: Room
)

data class Room(
    @SerializedName("_id")
    val id: String? = null,
    
    @SerializedName("roomId")
    val roomId: String,
    
    @SerializedName("examId")
    val examId: String,
    
    @SerializedName("creatorId")
    val creatorId: String,
    
    @SerializedName("users")
    val users: List<String> = emptyList(),
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("__v")
    val version: Int? = null
) {
    override fun toString(): String {
        return "Room(id=$id, roomId=$roomId, examId=$examId, creatorId=$creatorId, users=$users, createdAt=$createdAt, version=$version)"
    }
}

data class Player(
    val userId: String,
    val username: String
) 