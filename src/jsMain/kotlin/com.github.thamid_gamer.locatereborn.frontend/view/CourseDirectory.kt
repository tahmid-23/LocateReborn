package com.github.thamid_gamer.locatereborn.frontend.view

import com.github.thamid_gamer.locatereborn.frontend.helmet.helmet
import com.github.thamid_gamer.locatereborn.shared.api.data.CourseData
import com.github.thamid_gamer.locatereborn.shared.api.data.DayGroupData
import csstype.ClassName
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import react.*
import react.dom.html.InputType
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.link
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.title
import react.dom.html.ReactHTML.ul
import react.router.Route
import react.router.dom.Link
import react.router.useNavigate

external interface CourseDirectoryProps : Props {
    var courses: Map<CourseData, Collection<Pair<Int, DayGroupData>>>
}

val courseDirectory = FC<CourseDirectoryProps> { props ->
    var prefix by useState("")
    var sortDescending by useState(false)

    val coursesCopy = props.courses.toMutableMap()
    val filteredCourseCopy = coursesCopy.filterKeys {
        prefix in it.simpleCourseName.lowercase()
    }
        .toList()
        .sortedBy(sortDescending) {
            it.first.simpleCourseName
        }

    val matches = filteredCourseCopy.size

    helmet {
        title {
            +"Courses"
        }
        link {
            rel = "stylesheet"
            type = "text/css"
            href = "static/css/link-styles.css"
        }
    }
    h1 {
        +"Courses"
    }
    Link {
        className = ClassName("visible-link")
        to = "/students"
        +"Go to Students"
    }
    span {
        className = ClassName("aligned-row horizontally-spaced-row")
        label {
            input {
                type = InputType.text
                id = "filter-text"
                name = "Filter Text"
                placeholder = "Filter..."
                onInput = {
                    prefix = it.currentTarget.value.lowercase()
                }
            }
        }
        span {
            className = ClassName("aligned-row")
            +"Matches: $matches"
        }
        span {
            className = ClassName("aligned-row")
            label {
                htmlFor = "sort-descending"
                +"Sort Descending: "
            }
            input {
                type = InputType.checkbox
                id = "sort-descending"
                name = "Sort Descending"
                onChange = {
                    sortDescending = it.currentTarget.checked
                }
            }
        }
    }
    ul {
        for (course in filteredCourseCopy) {
            for (dayGroup in course.second) {
                li {
                    span {
                        className = ClassName("aligned-row")

                    }
                    Link {
                        className = ClassName("directory-link")
                        to = "/course?groupId=${dayGroup.first}"
                        +buildString {
                            append("${course.first.simpleCourseName}: ${dayGroup.second.period}(")
                            val alphabet = "ABCDE"
                            var anyPrevious = false
                            var hasDay = false
                            for (day in 0 until 5) {
                                if (day in dayGroup.second.days) {
                                    if (!hasDay) {
                                        if (anyPrevious) {
                                            append(",")
                                        }
                                        append(alphabet[day])
                                        hasDay = true
                                        anyPrevious = true
                                    }
                                    else if (day == 4) {
                                        append("-E")
                                    }
                                } else if (hasDay) {
                                    append("-")
                                    append(alphabet[day - 1])
                                    hasDay = false
                                }
                            }
                            append(")")
                        }
                    }
                }
            }
        }
    }
}

private inline fun <T, R : Comparable<R>> Iterable<T>.sortedBy(
    descending: Boolean,
    crossinline selector: (T) -> R?
): List<T> {
    return if (descending) {
        sortedByDescending(selector)
    }
    else {
        sortedBy(selector)
    }
}

fun ChildrenBuilder.courseDirectoryRoute(client: HttpClient, scope: CoroutineScope) {
    Route {
        path = "/courses"
        element = FC<Props> {
            val navigate = useNavigate()

            var courses by useState<Map<CourseData, Collection<Pair<Int, DayGroupData>>>>(emptyMap())

            courseDirectory {
                this.courses = courses
            }

            useEffectOnce {
                scope.launch {
                    try {
                        val origin = window.location.origin

                        val studentsReqDataResponse = client.get("$origin/api/courses")
                        if (studentsReqDataResponse.status == HttpStatusCode.Unauthorized) {
                            navigate("/")
                        }

                        courses = studentsReqDataResponse.body()
                    }
                    catch (ignored: Exception) {}
                }
            }
        }.create()
    }
}