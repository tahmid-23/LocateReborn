package com.github.thamid_gamer.locatereborn.frontend.view

import com.github.thamid_gamer.locatereborn.frontend.helmet.helmet
import com.github.thamid_gamer.locatereborn.shared.api.data.CourseType
import com.github.thamid_gamer.locatereborn.shared.api.data.CourseData
import com.github.thamid_gamer.locatereborn.shared.api.data.StudentData
import csstype.ClassName
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import react.*
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.link
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.title
import react.dom.html.ReactHTML.ul
import react.router.Route
import react.router.dom.Link
import react.router.dom.useSearchParams
import react.router.useNavigate

external interface CourseStudentsDirectoryProps : Props {
    var courseData: CourseData
    var studentData: Collection<StudentData>
}

val courseStudentsDirectory = FC<CourseStudentsDirectoryProps> { props ->
    val navigate = useNavigate()

    helmet {
        title {
            +props.courseData.simpleCourseName
        }
        link {
            rel = "stylesheet"
            type = "text/css"
            href = "static/css/link-styles.css"
        }
    }
    h1 {
        +props.courseData.simpleCourseName
    }
    ul {
        for (student in props.studentData) {
            li {
                Link {
                    className = ClassName("directory-link")
                    to = "/student?id=${student.studentId}"
                    +"${student.firstName} ${student.lastName}"
                }
            }
        }
    }
    br {}
    span {
        className = ClassName("aligned-row")
        button {
            onClick = {
                navigate("/students")
            }
            +"Home"
        }
        button {
            onClick = {
                navigate(-1)
            }
            +"Back"
        }
    }
}

fun ChildrenBuilder.courseStudentsDirectoryRoute(client: HttpClient, scope: CoroutineScope) {
    Route {
        path = "/course"
        element = FC<Props> {
            val navigate = useNavigate()

            val searchParams by useSearchParams()
            var loaded by useState(false)
            var data by useState<Pair<CourseData, Collection<StudentData>>>()

            val groupIdParam = searchParams.get("groupId")
            if (groupIdParam == null) {
                p {
                    className = ClassName("error-message")
                    +"No groupId query parameter set."
                }
                return@FC
            }

            val groupId = groupIdParam.toIntOrNull()
            if (groupId == null) {
                p {
                    className = ClassName("error-message")
                    +"groupId query parameter must be an integer."
                }
                return@FC
            }

            if (loaded) {
                val propData = data
                if (propData == null) {
                    p {
                        className = ClassName("error-message")
                        +"No course has group id $groupId."
                    }
                }
                else {
                    courseStudentsDirectory {
                        this.courseData = propData.first
                        this.studentData = propData.second
                    }
                }
            }
            else {
                p {
                    +"Loading..."
                }
            }

            useEffectOnce {
                scope.launch {
                    try {
                        val origin = window.location.origin

                        val studentsReqDataResponse = client.submitForm("$origin/api/course-students",
                                parametersOf(
                                    "groupId" to listOf(groupIdParam)
                                ),
                            true)
                        if (studentsReqDataResponse.status == HttpStatusCode.Unauthorized) {
                            navigate("/")
                        }

                        data = studentsReqDataResponse.body()
                    }
                    catch (e: Exception) {
                        e.printStackTrace()
                    }

                    loaded = true
                }
            }
        }.create()
    }
}