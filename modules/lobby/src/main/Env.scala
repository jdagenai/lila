package lila.lobby

import com.softwaremill.macwire._
import play.api.Configuration
import scala.concurrent.duration._
import lila.socket.Socket.{ GetVersion, SocketVersion }
import lila.common.config._

@Module
final class Env(
    appConfig: Configuration,
    db: lila.db.Db,
    onStart: lila.round.OnStart,
    relationApi: lila.relation.RelationApi,
    playbanApi: lila.playban.PlaybanApi,
    gameCache: lila.game.Cached,
    userRepo: lila.user.UserRepo,
    gameRepo: lila.game.GameRepo,
    poolApi: lila.pool.PoolApi,
    cacheApi: lila.memo.CacheApi,
    chatApi: lila.chat.ChatApi,
    remoteSocketApi: lila.socket.RemoteSocket
)(implicit
    ec: scala.concurrent.ExecutionContext,
    system: akka.actor.ActorSystem,
    idGenerator: lila.game.IdGenerator,
    mode: play.api.Mode
) {

  private lazy val seekApiConfig = new SeekApi.Config(
    coll = db(CollName("seek")),
    archiveColl = db(CollName("seek_archive")),
    maxPerPage = MaxPerPage(13),
    maxPerUser = Max(5)
  )

  lazy val seekApi = wire[SeekApi]

  lazy val boardApiHookStream = wire[BoardApiHookStream]

  private lazy val lobbySyncActor = LobbySyncActor.start(
    broomPeriod = 2 seconds,
    resyncIdsPeriod = 25 seconds
  ) { () =>
    wire[LobbySyncActor]
  }

  private lazy val abortListener = wire[AbortListener]

  private lazy val biter = wire[Biter]

  val socket = wire[LobbySocket]

  def version(id:String = "lobbyhome") = socket.rooms.ask[SocketVersion](id)(GetVersion)

  lila.common.Bus.subscribeFun("abortGame") { case lila.game.actorApi.AbortedBy(pov) =>
    abortListener(pov).unit
  }
}
