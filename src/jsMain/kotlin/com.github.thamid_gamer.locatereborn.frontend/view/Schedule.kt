package com.github.thamid_gamer.locatereborn.frontend.view

import com.github.thamid_gamer.locatereborn.frontend.data.BasicCourseTypeColorizer
import com.github.thamid_gamer.locatereborn.frontend.data.CourseTypeColorizer
import com.github.thamid_gamer.locatereborn.frontend.helmet.helmet
import com.github.thamid_gamer.locatereborn.shared.api.data.CourseData
import com.github.thamid_gamer.locatereborn.shared.api.data.StudentData
import com.github.thamid_gamer.locatereborn.shared.api.data.StudentPeriodData
import csstype.ClassName
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import react.*
import react.dom.html.InputType
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.link
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.title
import react.dom.html.ReactHTML.tr
import react.router.Route
import react.router.dom.Link
import react.router.dom.useSearchParams
import react.router.useNavigate
import kotlin.math.max

external interface ScheduleProps : Props {
    var studentData: StudentData
    var courseData: Map<CourseData, Collection<StudentPeriodData>>
    var courseTypeColorizer: CourseTypeColorizer
}

val schedule = FC<ScheduleProps> { props ->
    val navigate = useNavigate()
    var hideLunch by useState(false)
    var hideStudyHalls by useState(false)

    val periodMap = buildMap<Int, MutableMap<Int, CourseData>> {
        for (course in props.courseData) {
            for (period in course.value) {
                getOrPut(period.period, ::mutableMapOf)[period.day] = course.key
            }
        }
    }
    val maxPeriod = max(props.courseData.values.flatten().maxOfOrNull {
        it.period
    } ?: 10, 10)
    val maxDay = max(props.courseData.values.flatten().maxOfOrNull {
        it.day
    } ?: 5, 5)

    helmet {
        title {
            +"${props.studentData.firstName} ${props.studentData.lastName}"
        }
        link {
            rel = "stylesheet"
            type = "text/css"
            href = "static/css/schedule-styles.css"
        }
        link {
            rel = "stylesheet"
            type = "text/css"
            href = "static/css/link-styles.css"
        }
    }
    h1 {
        +buildString {
            append("${props.studentData.firstName} ${props.studentData.lastName}")
            if (props.studentData.roomNumber != null) {
                append(" (${props.studentData.roomNumber})")
            }
        }
    }
    table {
        tbody {
            tr {
                th { +"Period" }
                th { +"Monday" }
                th { +"Tuesday" }
                th { +"Wednesday" }
                th { +"Thursday" }
                th { +"Friday" }
            }

            for (period in 1..maxPeriod) {
                tr {
                    th { +period.toString() }

                    val days = periodMap[period]
                    for (day in 1..maxDay) {
                        val course = days?.get(day)
                        if ((course == null) ||
                            (hideLunch && "lunch" in course.simpleCourseName.lowercase()) ||
                            (hideStudyHalls && "study hall" in course.simpleCourseName.lowercase())) {
                            td {}
                        }
                        else {
                            td {
                                className = ClassName(props.courseTypeColorizer.colorize(course.courseType))
                                Link {
                                    className = ClassName("directory-link")
                                    to = course.getRoute(period)
                                    +course.simpleCourseName
                                }
                            }
                        }
                    }
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
        input {
            type = InputType.checkbox
            id = "toggle-lunch"
            name = "Toggle Lunch"
            onChange = {
                hideLunch = it.currentTarget.checked
            }
        }
        label {
            htmlFor = "toggle-lunch"
            +"Toggle Lunch"
        }
        input {
            type = InputType.checkbox
            id = "toggle-study-halls"
            name = "Toggle Study Halls"
            onChange = {
                hideStudyHalls = it.currentTarget.checked
            }
        }
        label {
            htmlFor = "toggle-study-halls"
            +"Toggle Study Halls"
        }
    }
}

private fun CourseData.getRoute(period: Int) = "/course" +
        "?schoologyCourseId=${schoologyCourseId}" +
        "&period=${period}"

fun ChildrenBuilder.scheduleRoute(client: HttpClient,
                                  scope: CoroutineScope,
                                  courseTypeColorizer: CourseTypeColorizer = BasicCourseTypeColorizer()) {
    Route {
        path = "/student"
        element = FC<Props> {
            val navigate = useNavigate()

            val searchParams by useSearchParams()
            var loaded by useState(false)
            var data by useState<Pair<StudentData, Map<CourseData, Collection<StudentPeriodData>>>>()

            val id = searchParams.get("id")
            if (id == null) {
                p {
                    className = ClassName("error-message")
                    +"No id query parameter set."
                }
                return@FC
            }

            if (loaded) {
                val propData = data
                if (propData == null) {
                    p {
                        className = ClassName("error-message")
                        +"No student has id $id."
                    }
                }
                else {
                    schedule {
                        this.studentData = propData.first
                        this.courseData = propData.second
                        this.courseTypeColorizer = courseTypeColorizer
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

                        val dataReqResponse = client.submitForm(
                            "$origin/api/student-courses",
                            parametersOf("id" to listOf(id)),
                            true)
                        if (dataReqResponse.status == HttpStatusCode.Unauthorized) {
                            navigate("/")
                        }

                        data = dataReqResponse.body()
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