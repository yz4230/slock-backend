@file:Suppress("ktlint")

package dev.ishiyama.slock.generated

import io.ktor.resources.Resource
import kotlin.String

public object Paths {
  @Resource(path = "/create-channel")
  public class CreateChannel()

  @Resource(path = "/list-channels")
  public class ListChannels()

  @Resource(path = "/list-messages")
  public class ListMessages(
    public val channelId: String,
    public val threadId: String?,
    public val topCursor: String?,
    public val bottomCursor: String?,
  )

  @Resource(path = "/login")
  public class Login()

  @Resource(path = "/me")
  public class Me()
}
