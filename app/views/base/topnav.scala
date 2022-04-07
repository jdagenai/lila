package views.html.base

import controllers.routes

import lila.api.Context
import lila.app.templating.Environment._
import lila.app.ui.ScalatagsTemplate._

object topnav {

  private def linkTitle(url: String, name: Frag)(implicit ctx: Context) =
    if (ctx.blind) h3(name) else a(href := url)(name)

  private def canSeeClasMenu(implicit ctx: Context) =
    ctx.hasClas || ctx.me.exists(u => u.hasTitle || u.roles.contains("ROLE_COACH"))

  def apply()(implicit ctx: Context) =
    st.nav(id := "topnav", cls := "hover")(
      st.section(
        linkTitle(
          "/",
          frag(
            span(cls := "play")(trans.play()),
            span(cls := "home")("lichess.org")
          )
        ),
        div(role := "group")(
          if(ctx.isAuth)
          {
          if (ctx.noBot) a(href := "/?any#hook")(trans.createAGame())
          else a(href := "/?any#friend")(trans.playWithAFriend())
          },
          ctx.noBot option frag(
            if(ctx.isAuth) a(href := routes.Tournament.home)(trans.arena.arenaTournaments())
            else a(href := "/login")(trans.arena.arenaTournaments()),
            if(ctx.isAuth) a(href := routes.Swiss.home)(trans.swiss.swissTournaments())
            else a(href := "/login")(trans.swiss.swissTournaments()),
            if(ctx.isAuth) a(href := routes.Simul.home)(trans.simultaneousExhibitions())
            else a(href := "/login")(trans.simultaneousExhibitions()),
            if(ctx.isAuth) ctx.pref.hasDgt option a(href := routes.DgtCtrl.index)("DGT board")
          )
        )
      ),
      ctx.noBot option st.section(
        linkTitle(routes.Puzzle.home.path, trans.puzzles()),
        div(role := "group")(
          if(ctx.isAuth) a(href := routes.Puzzle.home)(trans.puzzles())
          else a(href := "/login")(trans.puzzles()),
          if(ctx.isAuth) a(href := routes.Puzzle.dashboard(30, "home"))(trans.puzzle.puzzleDashboard())
          else a(href := "/login")(trans.puzzle.puzzleDashboard()),
          if(ctx.isAuth) a(href := routes.Puzzle.streak)("Puzzle Streak")
          else a(href := "/login")("Puzzle Streak"),
          if(ctx.isAuth) a(href := routes.Storm.home)("Puzzle Storm")
          else a(href := "/login")("Puzzle Storm"),
          if(ctx.isAuth) a(href := routes.Racer.home)("Puzzle Racer")
          else a(href := "/login")("Puzzle Racer")
        )
      ),
      st.section(
        linkTitle(routes.Practice.index.path, trans.learnMenu()),
        div(role := "group")(
          ctx.noBot option frag(
            if(ctx.isAuth) a(href := routes.Learn.index)(trans.chessBasics())
            else a(href := "/login")(trans.chessBasics()),
            if(ctx.isAuth) a(href := routes.Coordinate.home)(trans.coordinates.coordinates())
            else a(href := "/login")(trans.coordinates.coordinates())
          ),
          if(ctx.isAuth) a(href := routes.Study.allDefault(1))(trans.studyMenu())
          else a(href := "/login")(trans.studyMenu()),
          if(ctx.isAuth) ctx.noKid option a(href := routes.Coach.all(1))(trans.coaches()),
          if(ctx.isAuth) canSeeClasMenu option a(href := routes.Clas.index)(trans.clas.lichessClasses())
        )
      ),
      st.section(
        linkTitle(routes.Tv.index.path, trans.watch()),
        div(role := "group")(
          // Removed By Jean-Simon Dagenais
          //a(href := routes.Tv.index)("Lichess TV"),
          if(ctx.isAuth) a(href := routes.Tv.games)(trans.currentGames())
          else a(href := "/login")(trans.currentGames()),
          // Removed By Jean-Simon Dagenais
          //(ctx.noKid && ctx.noBot) option a(href := routes.Streamer.index())(trans.streamersMenu()),
          //a(href := routes.RelayTour.index())(trans.broadcast.broadcasts()),
          //ctx.noBot option a(href := routes.Video.index)(trans.videoLibrary())
        )
      ),
      st.section(
        linkTitle(routes.User.list.path, trans.community()),
        div(role := "group")(
          if(ctx.isAuth) a(href := routes.User.list)(trans.players())
          else a(href := "/login")(trans.players()),
          if(ctx.isAuth) a(href := routes.Team.home())(trans.team.teams())
          else a(href := "/login")(trans.team.teams()),
          if(ctx.isAuth) ctx.noKid option a(href := routes.ForumCateg.index)(trans.forum()),
          if(ctx.isAuth) ctx.noKid option a(href := routes.Ublog.community("all"))(trans.blog()),
          // Removed By Jean-Simon Dagenais
          //ctx.me.exists(!_.kid) option a(href := routes.Plan.index)(trans.patron.donate())
        )
      ),
      st.section(
        linkTitle(routes.UserAnalysis.index.path, trans.tools()),
        div(role := "group")(
          if(ctx.isAuth) a(href := routes.UserAnalysis.index)(trans.analysis())
          else a(href := "/login")(trans.analysis()),
          if(ctx.isAuth) a(href := s"${routes.UserAnalysis.index}#explorer")(trans.openingExplorer())
          else a(href := "/login")(trans.openingExplorer()),
          if(ctx.isAuth) a(href := routes.Editor.index)(trans.boardEditor())
          else a(href := "/login")(trans.boardEditor()),
          if(ctx.isAuth) a(href := routes.Importer.importGame)(trans.importGame())
          else a(href := "/login")(trans.importGame()),
          if(ctx.isAuth) a(href := routes.Search.index())(trans.search.advancedSearch())
          else a(href := "/login")(trans.search.advancedSearch())
          
        )
      )
    )
}
