package com.github.thamid_gamer.locatereborn.frontend.view

import com.github.thamid_gamer.locatereborn.frontend.helmet.helmet
import com.github.thamid_gamer.locatereborn.shared.google.clientId
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
                className = "container"
                header {
                    id = "locate-header"
                    className = "centered-section"
                    img {
                        id = "locate-header-logo"
                        src = "/static/image/monocle-cat.png"
                        alt = "Monocle Cat"
                    }
                    p {
                        id = "locate-header-title"
                        className = "header-text"
                        +"Locate Reborn"
                    }
                    p {
                        id = "locate-header-version"
                        className = "header-text"
                        +"v2.0 (not yet)"
                    }
                }
                div {
                    className = "centered-section"
                    div {
                        className = "centered-column"
                        p {
                            className = "locate-message centered-text"
                            +"This site is only for BCA students."
                        }
                        p {
                            className = "locate-message centered-text"
                            +"Use and share our vanity URL: "
                            a {
                                href = "bit.ly/locatebca"
                                +"bit.ly/locatebca"
                            }
                        }
                    }
                }
                div {
                    className = "centered-section"
                    div {
                        className = "centered-column"
                        div {
                            id = "sign-in-with-google-button"
                        }

                        if (loginFailed) {
                            p {
                                className = "error-message centered-text"
                                +"Failed to log in!"
                            }
                        }
                    }
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
                idConfig.login_uri = "$loginUriPrefix/google-login"
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