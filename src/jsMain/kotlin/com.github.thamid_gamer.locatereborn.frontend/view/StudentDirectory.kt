package com.github.thamid_gamer.locatereborn.frontend.view

import com.github.thamid_gamer.locatereborn.frontend.helmet.helmet
import com.github.thamid_gamer.locatereborn.frontend.view.FilterOrder.Companion.defaultOrder
import com.github.thamid_gamer.locatereborn.shared.api.data.StudentData
import com.github.thamid_gamer.locatereborn.shared.api.data.StudentType
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
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.title
import react.dom.html.ReactHTML.ul
import react.router.Route
import react.router.dom.Link
import react.router.useNavigate

external interface StudentDirectoryProps : Props {
    var students: Map<String, StudentData>
}

private enum class FilterOrder {
    ANY,
    FIRST,
    LAST;

    companion object {

        const val defaultOrder = "any"

        val filterOrderMap = buildMap {
            put("any", ANY)
            put("first", FIRST)
            put("last", LAST)
        }

    }

}

const val filterAny = "any"
const val filterAnyDisplay = "Any"

val studentDirectory = FC<StudentDirectoryProps> { props ->
    var prefix by useState("")
    var filterOrder by useState(FilterOrder.filterOrderMap[defaultOrder]!!)
    var sortDescending by useState(false)
    var gradeFilter by useState<StudentType?>(null)

    val studentsCopy = props.students.entries.toMutableList()

    if (gradeFilter != null) {
        studentsCopy.removeAll {
            it.value.studentType != gradeFilter
        }
    }

    val sortedStudentsCopy = when (filterOrder) {
        FilterOrder.FIRST -> studentsCopy.sortedBy(sortDescending) {
            it.value.firstName
        }
        FilterOrder.LAST -> studentsCopy.sortedBy(sortDescending) {
            it.value.lastName
        }
        FilterOrder.ANY -> studentsCopy
    }.toMutableList()

    when (filterOrder) {
        FilterOrder.FIRST -> {
            sortedStudentsCopy.removeAll {
                !"${it.value.firstName} ${it.value.lastName}".lowercase().startsWith(prefix)
            }
        }
        FilterOrder.LAST -> {
            sortedStudentsCopy.removeAll {
                !it.value.lastName.lowercase().startsWith(prefix)
            }
        }
        FilterOrder.ANY -> {
            sortedStudentsCopy.removeAll {
                prefix !in "${it.value.firstName} ${it.value.lastName}".lowercase()
            }
        }
    }

    val matches = sortedStudentsCopy.size

    helmet {
        title {
            +"Students and Teachers"
        }
        link {
            rel = "stylesheet"
            type = "text/css"
            href = "static/css/link-styles.css"
        }
    }
    h1 {
        +"Students and Teachers"
    }
    span {
        className = "aligned-row horizontally-spaced-row"
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
            className = "aligned-row"
            +"Matches: $matches"
        }
        span {
            className = "aligned-row"
            label {
                htmlFor = "filter-order"
                +"Filter By: "
            }
            select {
                id = "filter-order"
                name = "Filter Order"
                option {
                    value = filterAny
                    +filterAnyDisplay
                }
                option {
                    value = "first"
                    +"First Name"
                }
                option {
                    value = "last"
                    +"Last Name"
                }
                onChange = {
                    filterOrder = FilterOrder.filterOrderMap[it.currentTarget.value]
                        ?: FilterOrder.filterOrderMap[defaultOrder]!!
                }
            }
        }
        span {
            className = "aligned-row"
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
        span {
            className = "aligned-row"
            label {
                htmlFor = "grade-filter"
                +"Grade: "
            }
            select {
                id = "grade-filter"
                name = "Grade Filter"
                option {
                    value = filterAny
                    +filterAnyDisplay
                }
                option {
                    value = StudentType.FRESHMAN.name
                    +"Freshman"
                }
                option {
                    value = StudentType.SOPHOMORE.name
                    +"Sophomore"
                }
                option {
                    value = StudentType.JUNIOR.name
                    +"Junior"
                }
                option {
                    value = StudentType.SENIOR.name
                    +"Senior"
                }
                option {
                    value = StudentType.STAFF.name
                    +"Staff"
                }
                onChange = {
                    val gradeName = it.currentTarget.value
                    gradeFilter = if (gradeName == filterAny) {
                        null
                    } else {
                        StudentType.studentTypeMap[gradeName]
                    }
                }
            }
        }
    }
    ul {
        for (student in sortedStudentsCopy) {
            li {
                Link {
                    className = "directory-link"
                    to = "/student?id=${student.key}"
                    +"${student.value.firstName} ${student.value.lastName}"
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

fun ChildrenBuilder.studentDirectoryRoute(client: HttpClient, scope: CoroutineScope) {
    Route {
        path = "/students"
        element = FC<Props> {
            val navigate = useNavigate()

            var students by useState(emptyMap<String, StudentData>())

            studentDirectory {
                this.students = students
            }

            useEffectOnce {
                scope.launch {
                    try {
                        val origin = window.location.origin

                        val studentsReqDataResponse = client.get("$origin/api/students")
                        if (studentsReqDataResponse.status == HttpStatusCode.Unauthorized) {
                            navigate("/")
                        }

                        students = studentsReqDataResponse.body()
                    }
                    catch (ignored: Exception) {}
                }
            }
        }.create()
    }
}