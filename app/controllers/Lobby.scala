package controllers

import play.api.libs.json._
import play.api.mvc._
import views._

import lila.app._
import lila.i18n.I18nLangPicker
import lila.api.Context

final class Lobby(
    env: Env
) extends LilaController(env) {

  private lazy val lobbyJson = Json.obj(
    "lobby" -> Json.obj(
      "version" -> 0,
      //"pools"   -> lila.pool.PoolList.json
    ),
    "assets" -> Json.obj(
      "domain" -> env.net.assetDomain.value
    )
  )

  def home =
    Open { implicit ctx =>
      pageHit
      negotiate(
        html = serveHtmlHome,
        api = _ =>
          fuccess {
            val expiration = 60 * 60 * 24 * 7 // set to one hour, one week before changing the pool config
            Ok(lobbyJson).withHeaders(CACHE_CONTROL -> s"max-age=$expiration")
          }
      )
    }

  private def redirectWithQueryString(path: String)(implicit req: RequestHeader) =
    Redirect {
      if (req.target.uriString contains "?") s"$path?${req.target.queryString}" else path
    }

  private def serveHtmlHome(implicit ctx: Context) =
    env.pageCache { () =>
      keyPages.homeHtml.dmap { html =>
        NoCache(Ok(html))
      }
    } map env.lilaCookie.ensure(ctx.req)

  def homeLang(langCode: String) =
    Open { ctx =>
      if (ctx.isAuth) redirectWithQueryString("/")(ctx.req).fuccess
      else
        I18nLangPicker.byHref(langCode) match {
          case I18nLangPicker.NotFound    => notFound(ctx)
          case I18nLangPicker.Redir(code) => redirectWithQueryString(s"/$code")(ctx.req).fuccess
          case I18nLangPicker.Found(lang) =>
            implicit val langCtx = ctx withLang lang
            pageHit
            serveHtmlHome
        }
    }

  def handleStatus(req: RequestHeader, status: Results.Status): Fu[Result] =
    reqToCtx(req) flatMap { ctx =>
      keyPages.home(status)(ctx)
    }

  def seeks =
    Open { implicit ctx =>
      negotiate(
        html = fuccess(NotFound),
        api = _ =>
          ctx.me.fold(env.lobby.seekApi.forAnon)(env.lobby.seekApi.forUser) map { seeks =>
            Ok(JsArray(seeks.map(_.render))).withHeaders(CACHE_CONTROL -> s"max-age=10")
          }
      )
    }

  def timeline =
    Auth { implicit ctx => me =>
      env.timeline.entryApi.userEntries(me.id) map { entries =>
        Ok(html.timeline.entries(entries)).withHeaders(CACHE_CONTROL -> s"max-age=20")
      }
    }
}
