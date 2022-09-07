package com.github.thamid_gamer.locatereborn.frontend.view

import com.github.thamid_gamer.locatereborn.frontend.helmet.helmet
import com.github.thamid_gamer.locatereborn.shared.google.clientId
import csstype.ClassName
import kotlinx.browser.document
import kotlinx.browser.window
import react.*
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.header
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.link
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.title
import react.router.Route
import react.router.dom.useSearchParams

@Suppress("unused")
fun ChildrenBuilder.loginRoute() {
    Route {
        index = true
        element = FC<Props> {
            val searchParams by useSearchParams()

            val loginFailed = searchParams.get("loginFailed") != null

            helmet {
                title {
                    +"Locate Reborn"
                }
                link {
                    rel = "stylesheet"
                    type = "text/css"
                    href = "/static/css/login.css"
                }
            }
            div {
                className = ClassName("container")
                header {
                    id = "locate-header"
                    className = ClassName("centered-section")
                    img {
                        id = "locate-header-logo"
                        src = "/static/image/monocle-cat.png"
                        alt = "Monocle Cat"
                    }
                    p {
                        id = "locate-header-title"
                        className = ClassName("header-text")
                        +"Locate Reborn"
                    }
                    p {
                        id = "locate-header-version"
                        className = ClassName("header-text")
                        +"v2.0 (not yet)"
                    }
                }
                div {
                    className = ClassName("centered-section")
                    div {
                        className = ClassName("centered-column")
                        p {
                            className = ClassName("locate-message centered-text")
                            +"This site is only for BCA students."
                        }
                        p {
                            className = ClassName("locate-message centered-text")
                            +"Use and share our vanity URL: "
                            a {
                                href = "bit.ly/locatebca"
                                +"bit.ly/locatebca"
                            }
                        }
                    }
                }
                div {
                    className = ClassName("centered-section")
                    div {
                        className = ClassName("centered-column")
                        div {
                            id = "sign-in-with-google-button"
                        }

                        if (loginFailed) {
                            p {
                                className = ClassName("error-message centered-text")
                                +"Failed to log in!"
                            }
                        }
                    }
                }
                p {
                    +"Where's Wang?"
                }
            }

            useEffectOnce {
                val google = window.asDynamic().google

                val loginUriPrefix = if (window.location.protocol == "https:" ||
                    window.location.hostname.lowercase() == "localhost") {
                    window.location.origin
                }
                else {
                    "https://${window.location.host}"
                }

                // TODO: find issues with the name mangling adding _1 when we use regular objects
                val idConfig = Any().asDynamic()
                idConfig.client_id = clientId
                idConfig.ux_mode = "redirect"
                idConfig.login_uri = "$loginUriPrefix/google-auth"
                google.accounts.id.initialize(idConfig)

                val buttonConfig = Any().asDynamic()
                buttonConfig.theme = "filled_blue"
                buttonConfig.size = "large"
                val renderTarget = document.getElementById("sign-in-with-google-button")
                google.accounts.id.renderButton(renderTarget, buttonConfig)

                Unit
            }
        }.create()
    }
}