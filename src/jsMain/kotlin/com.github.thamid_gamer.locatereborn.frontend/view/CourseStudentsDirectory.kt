package com.github.thamid_gamer.locatereborn.frontend.view

import com.github.thamid_gamer.locatereborn.frontend.helmet.helmet
import com.github.thamid_gamer.locatereborn.shared.api.data.CourseType
import com.github.thamid_gamer.locatereborn.shared.api.data.PeriodData
import com.github.thamid_gamer.locatereborn.shared.api.data.StudentData
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
    var periodData: PeriodData
    var students: Map<String, StudentData>
}

val courseStudentsDirectory = FC<CourseStudentsDirectoryProps> { props ->
    val navigate = useNavigate()

    helmet {
        title {
            +props.periodData.simpleCourseName
        }
        link {
            rel = "stylesheet"
            type = "text/css"
            href = "static/css/link-styles.css"
        }
    }
    h1 {
        +props.periodData.simpleCourseName
    }
    ul {
        for (student in props.students) {
            li {
                Link {
                    className = "directory-link"
                    to = "/student?id=${student.key}"
                    +"${student.value.firstName} ${student.value.lastName}"
                }
            }
        }
    }
    br {}
    span {
        className = "aligned-row"
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
            var students by useState(emptyMap<String, StudentData>())

            val schoologyCourseId = searchParams.get("schoologyCourseId")
            if (schoologyCourseId == null) {
                p {
                    className = "error-message"
                    +"No schoologyCourseId query parameter set."
                }
                return@FC
            }

            val dayParam = searchParams.get("day")
            if (dayParam == null) {
                p {
                    className = "error-message"
                    +"No day query parameter set."
                }
                return@FC
            }
            val day = dayParam.toIntOrNull()
            if (day == null) {
                p {
                    className = "error-message"
                    +"day query parameter is not an integer."
                }
                return@FC
            }

            val periodParam = searchParams.get("period")
            if (periodParam == null) {
                p {
                    className = "error-message"
                    +"No periodParam query parameter set."
                }
                return@FC
            }
            val period = periodParam.toIntOrNull()
            if (period == null) {
                p {
                    className = "error-message"
                    +"period query parameter is not an integer."
                }
                return@FC
            }

            val fullCourseName = searchParams.get("fullCourseName") ?: "Unknown Course"
            val simpleCourseName = searchParams.get("simpleCourseName") ?: "Unknown Course"
            val courseTypeParam = searchParams.get("courseType")
            val courseType = if (courseTypeParam != null) {
                CourseType.courseTypeMap[courseTypeParam] ?: CourseType.UNKNOWN
            }
            else {
                CourseType.UNKNOWN
            }

            val periodData = PeriodData(
                schoologyCourseId,
                fullCourseName,
                simpleCourseName,
                courseType,
                day,
                period
            )

            if (loaded) {
                courseStudentsDirectory {
                    this.periodData = periodData
                    this.students = students
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
                                    "schoologyCourseId" to listOf(schoologyCourseId),
                                    "day" to listOf(dayParam),
                                    "period" to listOf(periodParam)
                                ),
                            true)
                        if (studentsReqDataResponse.status == HttpStatusCode.Unauthorized) {
                            navigate("/")
                        }

                        students = studentsReqDataResponse.body()
                    }
                    catch (ignored: Exception) {}

                    loaded = true
                }
            }
        }.create()
    }
}