package org.http4s.client.tootallnate

import fs2.Pipe
import org.http4s.Response
import org.http4s.websocket.WebSocketFrame

object TooTallNateWebsocketClient {

}

trait WebsocketClient[F[_]] {
  def runSocket(pipe: Pipe[F, WebSocketFrame, WebSocketFrame]): F[Response[F]]

  def runSocket(): F[(Pipe[F, WebSocketFrame, WebSocketFrame], Response[F])]
}
