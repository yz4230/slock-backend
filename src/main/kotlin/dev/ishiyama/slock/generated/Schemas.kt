@file:Suppress("ktlint")

package dev.ishiyama.slock.generated

import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable

public object Schemas {
  @Serializable
  public data class Channel(
    public val id: String,
    public val name: String,
    public val description: String,
    public val isDirect: Boolean,
    public val createdAt: String,
    public val updatedAt: String,
  )

  @Serializable
  public data class CreateChannel(
    public val name: String,
    public val description: String,
    public val isDirect: Boolean,
  )

  @Serializable
  public data class CreateChannelRequest(
    public val channel: Schemas.CreateChannel,
  )

  @Serializable
  public data class CreateChannelResponse(
    public val channel: Schemas.Channel,
  )

  @Serializable
  public data class ListChannelsResponse(
    public val items: List<Schemas.Channel>,
  )

  @Serializable
  public data class ListMessagesResponse(
    public val items: List<Schemas.Message>,
    public val topCursor: String?,
    public val bottomCursor: String?,
  )

  @Serializable
  public data class LoginRequest(
    public val name: String,
    public val password: String,
  )

  @Serializable
  public data class LoginResponse(
    public val user: Schemas.User,
    public val token: String,
  )

  @Serializable
  public data class Message(
    public val id: String,
    public val userId: String,
    public val channelId: String,
    public val threadId: String?,
    public val content: String,
    public val createdAt: String,
    public val updatedAt: String,
  )

  @Serializable
  public data class RegisterRequest(
    public val name: String,
    public val displayName: String,
    public val email: String,
    public val password: String,
  )

  @Serializable
  public data class RegisterResponse(
    public val user: Schemas.User,
    public val token: String,
  )

  @Serializable
  public data class User(
    public val id: String,
    public val name: String,
    public val displayName: String,
    public val email: String,
    public val createdAt: String,
    public val updatedAt: String,
  )

  @Serializable
  public data class WithTimestamp(
    public val createdAt: String,
    public val updatedAt: String,
  )
}
