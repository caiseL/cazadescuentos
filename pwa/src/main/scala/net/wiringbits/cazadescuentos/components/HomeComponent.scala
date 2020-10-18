package net.wiringbits.cazadescuentos.components

import net.wiringbits.cazadescuentos.API
import net.wiringbits.cazadescuentos.api.storage.models.StoredProduct
import net.wiringbits.cazadescuentos.components.models.DataState
import net.wiringbits.cazadescuentos.models.AppInfo
import org.scalablytyped.runtime.StringDictionary
import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.core.facade.Hooks
import slinky.web.html._
import typings.csstype.csstypeStrings.auto
import typings.detectBrowser.mod.Browser
import typings.materialUiCore.createMuiThemeMod.Theme
import typings.materialUiCore.mod.PropTypes
import typings.materialUiCore.{typographyTypographyMod, components => mui}
import typings.materialUiIcons.{components => muiIcons}
import typings.materialUiStyles.makeStylesMod.StylesHook
import typings.materialUiStyles.mod.makeStyles
import typings.materialUiStyles.withStylesMod.{CSSProperties, StyleRulesCallback, Styles, WithStylesOptions}

@react object HomeComponent {

  private lazy val useStyles: StylesHook[Styles[Theme, Unit, String]] = {
    /* If you don't need direct access to theme, this could be `StyleRules[Props, String]` */
    val stylesCallback: StyleRulesCallback[Theme, Unit, String] = theme =>
      StringDictionary(
        "root" -> CSSProperties().setWidth("100%").setOverflowX(auto)
      )

    makeStyles(stylesCallback, WithStylesOptions())
  }

  type Data = List[StoredProduct]
  case class Props(api: API, appInfo: AppInfo, notifications: List[String])

  val component: FunctionalComponent[Props] = FunctionalComponent[Props] { props =>
    val (dataState, setDataState) = Hooks.useState(DataState.loading[Data])

    def refreshData(): Unit = {
      setDataState(DataState.loading[Data])
      val data = props.api.storageService.load().map(_.products).toList.flatten
      setDataState(_.loaded(data))
    }

    def delete(item: StoredProduct): Unit = {
      props.api.storageService.delete(item.storeProduct)
      refreshData()
    }

    Hooks.useEffect(refreshData, "")

    val classes = useStyles(())
    val notifications = if (props.notifications.isEmpty) {
      div()
    } else {
      div(
        props.notifications.map { text =>
          div(
            muiIcons.ThumbUp().color(PropTypes.Color.primary),
            mui.Typography().component("h3").variant(typographyTypographyMod.Style.h5)(text)
          )
        }
      )
    }

    val data = dataState match {
      case DataState.Loading() =>
        div(
          mui.CircularProgress()
        )

      case DataState.Loaded(data) =>
        div(
          AddNewItemFloatingButton.component(AddNewItemFloatingButton.Props(props.api, refreshData)),
          MyProductsSummaryComponent.component(MyProductsSummaryComponent.Props(data, delete))
        )
    }

    val installButton = if (props.appInfo.isAndroidApp) {
      div()
    } else {
      div(
        InstallButton.component(InstallButton.Props(props.api))
      )
    }

    val notificationsUnsupported = props.appInfo.browser.exists { browser =>
      List(Browser.safari, Browser.ios, Browser.`ios-webview`).contains(browser.name)
    }
    val disclaimer = if (notificationsUnsupported) {
      """Desafortunadamente las notificaciones no funcionan en iPhone/Safari todavia.
        |
        |Agradecemos tu paciencia, recuerda que si quieres usar la versión completa de la aplicación,
        |esta puede ser instalada en tu computadora desde https://cazadescuentos.net
        |""".stripMargin
    } else {
      """Recuerda que si quieres usar la versión completa de la aplicación,
        |esta puede ser instalada en tu computadora desde https://cazadescuentos.net
        |""".stripMargin
    }

    mui.Paper.className(classes("root"))(
      notifications,
      data,
      installButton,
      mui
        .Typography()
        .component("p")
        .variant(typographyTypographyMod.Style.h6)(disclaimer)
    )
  }
}
