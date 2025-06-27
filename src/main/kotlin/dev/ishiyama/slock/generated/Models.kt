@file:Suppress("ktlint")

package dev.ishiyama.slock.generated

import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable

public object Models {
  @Serializable
  public data class Channel(
    public val id: String,
    public val name: String,
    public val description: String,
    public val isDirect: Boolean,
  )

  @Serializable
  public data class CreateChannel(
    public val name: String,
    public val description: String,
    public val isDirect: Boolean,
  )

  @Serializable
  public data class CreateChannelRequest(
    public val channel: CreateChannel,
  )

  @Serializable
  public data class CreateChannelResponse(
    public val channel: Channel,
  )

  @Serializable
  public data class ListChannelsResponse(
    public val items: List<Channel>,
  )

  @Serializable
  public data class ListMessagesResponse(
    public val items: List<Message>,
    public val topCursor: String,
    public val bottomCursor: String,
  )

  @Serializable
  public data class LoginRequest(
    public val name: String,
    public val password: String,
  )

  @Serializable
  public data class LoginResponse(
    public val user: User,
    public val token: String,
  )

  @Serializable
  public data class Message(
    public val id: String,
    public val userId: String,
    public val channelId: String,
    public val threadId: String,
    public val content: String,
  )

  @Serializable
  public data class User(
    public val id: String,
    public val name: String,
  )
}
