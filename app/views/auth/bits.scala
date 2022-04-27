package views.html
package auth

import controllers.routes
import play.api.data.{ Field, Form }

import lila.api.Context
import lila.app.templating.Environment._
import lila.app.ui.ScalatagsTemplate._
import lila.security.HcaptchaForm
import lila.user.User


object bits {

  val choices = List(
      0 -> "Aucun",
      1 -> "Analytique, opérations et technologies de l'information",
      2 -> "Arts visuels et médiatiques",
      3 -> "Chimie",
      4 -> "Communication sociale et publique",
      5 -> "Danse",
      6 -> "Design",
      7 -> "Didactique",
      8 -> "Didactique des langues",
      9 -> "Éducation et formation spécialisées",
      10 -> "Éducation et pédagogie",
      11 -> "Études internationales de Montréal",
      12 -> "Études littéraires",
      13 -> "Études urbaines et touristiques",
      14 -> "Finance",
      15 -> "Géographie",
      16 -> "Histoire",
      17 -> "Histoire de l'art",
      18 -> "Informatique",
      19 -> "Langues",
      20 -> "Linguistique",
      21 -> "Management",
      22 -> "Marketing",
      23 -> "Mathématiques",
      24 -> "Media",
      25 -> "Mode",
      26 -> "Musique",
      27 -> "Organisation et ressources humaines",
      28 -> "Patrimoine",
      29 -> "Philosophie",
      30 -> "Psychologie",
      31 -> "Recherches et études féministes (IREF)",
      32 -> "Santé et société (ISS)",
      33 -> "Science politique",
      34 -> "Sciences biologiques",
      35 -> "Sciences cognitives (ISC)",
      36 -> "Sciences comptables",
      37 -> "Sciences de l'activité physique",
      38 -> "Sciences de l'environnement (ISE)",
      39 -> "Sciences de la Terre et de l'atmosphère",
      40 -> "Sciences des religions",
      41 -> "Sciences économiques",
      42 -> "Sciences juridiques",
      43 -> "Sexologie",
      44 -> "Sociologie",
      45 -> "Stratégie, responsabilité sociale et environnementale",
      46 -> "Théâtre",
      47 -> "Travail social"
      
    )

  def formFields(username: Field, password: Field, depcheck: Option[Field], departement: Option[Field], emailOption: Option[Field], register: Boolean)(implicit
      ctx: Context
  ) =
    frag(
      form3.group(username, if (register) trans.username() else trans.usernameOrEmail()) { f =>
        frag(
          form3.input(f)(autofocus, required, autocomplete := "username"),
          p(cls := "error username-exists none")(trans.usernameAlreadyUsed())
        )
      },
      form3.passwordModified(password, trans.password())(
        autocomplete := (if (register) "new-password" else "current-password")
      ),
      
      register option form3.passwordComplexityMeter(trans.newPasswordStrength()),
      emailOption.map { email =>
        form3.group(email, trans.email(), help = frag("We will only use it for password reset.").some)(
          form3.input(_, typ = "email")(required)
        )
      },
      //register option form3.checkbox(username, "Je suis membre de la communaute Uqam"),
      //register option form3.departement(username, "Departement")(),
      
      depcheck.map { check =>
        form3.checkbox(check, "Je suis membre de la communaute Uqam")
      },
      
      departement.map { dep =>
        form3.departement(dep, "Departement")()
      },
    )

  def formFields2(username: Field, password: Field, depcheck: Field, departement: Field, emailOption: Option[Field], register: Boolean)(implicit
      ctx: Context
  ) =
    frag(
      form3.group(username, if (register) trans.username() else trans.usernameOrEmail()) { f =>
        frag(
          form3.input(f)(autofocus, required, autocomplete := "username"),
          p(cls := "error username-exists none")(trans.usernameAlreadyUsed())
        )
      },
      form3.passwordModified(password, trans.password())(
        autocomplete := (if (register) "new-password" else "current-password")
      ),
      
      register option form3.passwordComplexityMeter(trans.newPasswordStrength()),
      emailOption.map { email =>
        form3.group(email, trans.email(), help = frag("We will only use it for password reset, if the email is not valid, password reset will not be possible.").some)(
          form3.input(_, typ = "email")(required)
        )
      },
      //register option form3.checkbox(username, "Je suis membre de la communaute Uqam"),
      //register option form3.departement(username, "Departement")(),

      form3.checkbox(depcheck, "Je suis membre de la communaute Uqam"),
      form3.group(departement, "Departement")(
        form3.select(_, choices)
      )
    )

  def passwordReset(form: HcaptchaForm[_], fail: Boolean)(implicit ctx: Context) =
    views.html.base.layout(
      title = trans.passwordReset.txt(),
      moreCss = cssTag("auth"),
      moreJs = views.html.base.hcaptcha.script(form),
      csp = defaultCsp.withHcaptcha.some
    ) {
      main(cls := "auth auth-signup box box-pad")(
        h1(
          fail option span(cls := "is-red", dataIcon := ""),
          trans.passwordReset()
        ),
        postForm(cls := "form3", action := routes.Auth.passwordResetApply)(
          form3.group(form("email"), trans.email())(
            form3.input(_, typ = "email")(autofocus, required, autocomplete := "email")
          ),
          views.html.base.hcaptcha.tag(form),
          form3.action(form3.submit(trans.emailMeALink()))
        )
      )
    }

  def passwordResetSent(email: String)(implicit ctx: Context) =
    views.html.base.layout(
      title = trans.passwordReset.txt()
    ) {
      main(cls := "page-small box box-pad")(
        h1(cls := "is-green text", dataIcon := "")(trans.checkYourEmail()),
        p(trans.weHaveSentYouAnEmailTo(email)),
        p(trans.ifYouDoNotSeeTheEmailCheckOtherPlaces())
      )
    }

  def passwordResetConfirm(u: User, token: String, form: Form[_], ok: Option[Boolean] = None)(implicit
      ctx: Context
  ) =
    views.html.base.layout(
      title = s"${u.username} - ${trans.changePassword.txt()}",
      moreCss = cssTag("form3"),
      moreJs = frag(
        embedJsUnsafeLoadThen("""
          lichess.loadModule('passwordComplexity').then(() =>
            passwordComplexity.addPasswordChangeListener('form3-newPasswd1')
          )""")
      )
    ) {
      main(cls := "page-small box box-pad")(
        (ok match {
          case Some(true)  => h1(cls := "is-green text", dataIcon := "")
          case Some(false) => h1(cls := "is-red text", dataIcon := "")
          case _           => h1
        })(
          userLink(u, withOnline = false),
          " - ",
          trans.changePassword()
        ),
        postForm(cls := "form3", action := routes.Auth.passwordResetConfirmApply(token))(
          form3.hidden(form("token")),
          form3.passwordModified(form("newPasswd1"), trans.newPassword())(
            autofocus,
            autocomplete := "new-password"
          ),
          form3.passwordComplexityMeter(trans.newPasswordStrength()),
          form3.passwordModified(form("newPasswd2"), trans.newPasswordAgain())(
            autocomplete := "new-password"
          ),
          form3.globalError(form),
          form3.action(form3.submit(trans.changePassword()))
        )
      )
    }

  def magicLink(form: HcaptchaForm[_], fail: Boolean)(implicit ctx: Context) =
    views.html.base.layout(
      title = "Log in by email",
      moreCss = cssTag("auth"),
      moreJs = views.html.base.hcaptcha.script(form),
      csp = defaultCsp.withHcaptcha.some
    ) {
      main(cls := "auth auth-signup box box-pad")(
        h1(
          fail option span(cls := "is-red", dataIcon := ""),
          "Log in by email"
        ),
        p("We will send you an email containing a link to log you in."),
        p("If the email adress is not valid, no email will be sent."),
        postForm(cls := "form3", action := routes.Auth.magicLinkApply)(
          form3.group(form("email"), trans.email())(
            form3.input(_, typ = "email")(autofocus, required, autocomplete := "email")
          ),
          views.html.base.hcaptcha.tag(form),
          form3.action(form3.submit(trans.emailMeALink()))
        )
      )
    }

  def magicLinkSent(implicit ctx: Context) =
    views.html.base.layout(
      title = "Log in by email"
    ) {
      main(cls := "page-small box box-pad")(
        h1(cls := "is-green text", dataIcon := "")(trans.checkYourEmail()),
        p("We've sent you an email with a link."),
        p(trans.ifYouDoNotSeeTheEmailCheckOtherPlaces())
      )
    }

  def tokenLoginConfirmation(user: User, token: String, referrer: Option[String])(implicit ctx: Context) =
    views.html.base.layout(
      title = s"Log in as ${user.username}",
      moreCss = cssTag("form3")
    ) {
      main(cls := "page-small box box-pad")(
        h1("Log in as ", userLink(user)),
        postForm(action := routes.Auth.loginWithTokenPost(token, referrer))(
          form3.actions(
            a(href := routes.Lobby.home)(trans.cancel()),
            submitButton(cls := "button")(s"${user.username} is my Lichess username, log me in")
          )
        )
      )
    }

  def checkYourEmailBanner(userEmail: lila.security.EmailConfirm.UserEmail) =
    frag(
      styleTag("""
body { margin-top: 45px; }
#email-confirm {
  height: 40px;
  background: #3893E8;
  color: #fff!important;
  font-size: 1.3em;
  display: flex;
  flex-flow: row nowrap;
  justify-content: center;
  align-items: center;
  border-bottom: 1px solid #666;
  box-shadow: 0 5px 6px rgba(0, 0, 0, 0.3);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 107;
}
#email-confirm a {
  color: #fff!important;
  text-decoration: underline;
  margin-left: 1em;
}
"""),
      div(id := "email-confirm")(
        s"Almost there, ${userEmail.username}! Now check your email (${userEmail.email.conceal}) for signup confirmation.",
        a(href := routes.Auth.checkYourEmail)("Click here for help")
      )
    )

  def tor()(implicit ctx: Context) =
    views.html.base.layout(
      title = "Tor exit node"
    ) {
      main(cls := "page-small box box-pad")(
        h1(cls := "text", dataIcon := "2")("Ooops"),
        p("Sorry, you can't signup to Lichess through Tor!"),
        p("You can play, train and use almost all Lichess features as an anonymous user.")
      )
    }

  def logout()(implicit ctx: Context) =
    views.html.base.layout(
      title = trans.logOut.txt()
    ) {
      main(cls := "page-small box box-pad")(
        h1(trans.logOut()),
        form(action := routes.Auth.logout, method := "post")(
          button(cls := "button button-red", tpe := "submit")(trans.logOut.txt())
        )
      )
    }
}
