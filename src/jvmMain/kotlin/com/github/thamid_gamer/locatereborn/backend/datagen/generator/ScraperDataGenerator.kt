package com.github.thamid_gamer.locatereborn.backend.datagen.generator

import com.github.thamid_gamer.locatereborn.backend.datagen.classifier.CourseTypeClassifier
import com.github.thamid_gamer.locatereborn.backend.datagen.classifier.StudentTypeClassifier
import com.github.thamid_gamer.locatereborn.backend.datagen.exception.LocateDataGenerationException
import com.github.thamid_gamer.locatereborn.shared.api.data.CourseType
import com.github.thamid_gamer.locatereborn.shared.api.data.PeriodData
import com.github.thamid_gamer.locatereborn.shared.api.data.StudentData
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import kotlin.math.ceil

class ScraperDataGenerator(private val client: HttpClient,
                           private val studentTypeClassifier: StudentTypeClassifier,
                           private val courseTypeClassifier: CourseTypeClassifier) : LocateDataGenerator {

    companion object {

        private const val BCA_HOST = "https://bca.schoology.com"

        private const val SCHOOL_NID = "11897239"

        private const val LOGIN_URL = "$BCA_HOST/login/ldap"

        private const val STUDENTS_PER_PAGE = 30

        private const val DAYS_RANGE = "ABCDE"

    }

    override suspend fun generateData(request: GeneratorRequest): DataGenerationResult {
        if (!login(request)) {
            throw LocateDataGenerationException("Login failed!")
        }

        val studentMap = mutableMapOf<String, StudentData>()
        val studentPeriods = mutableMapOf<String, MutableCollection<PeriodData>>()

        for (groupId in request.groups) {
            scrapeGroup(groupId, studentMap, studentPeriods)
        }

        return DataGenerationResult(studentMap, studentPeriods)
    }

    private suspend fun login(request: GeneratorRequest): Boolean {
        client.config {
            followRedirects = false
        }

        val response = client.submitForm(
            LOGIN_URL, parametersOf(
            "mail" to listOf(request.username),
            "pass" to listOf(request.password),
            "school_nid" to listOf(SCHOOL_NID),
            "form_build_id" to listOf("71ae247-yQ74LLB-5MlUK435R985FXrajO8iDRgRwM4OoT_PGig"),
            "form_id" to listOf("s_user_login_form")
        )
        ) {
            handleTooManyRequests()

            parameter("school", SCHOOL_NID)

            expectSuccess = false
        }

        client.config {
            followRedirects = true
        }

        return response.status == HttpStatusCode.Found
    }

    private fun HttpRequestBuilder.handleTooManyRequests() {
        retry {
            constantDelay(millis = 5000, respectRetryAfterHeader = false) // schoology woozy, their Retry-Afters stay capping
            retryIf(15) { _, httpResponse ->
                httpResponse.status == HttpStatusCode.TooManyRequests
            }
        }
    }

    private suspend fun scrapeGroup(
        groupId: String,
        studentMap: MutableMap<String, StudentData>,
        studentPeriods: MutableMap<String, MutableCollection<PeriodData>>
    ) {
        val pageCount = getGroupPageCount(groupId) ?: return

        for (page in 1uL..pageCount) {
            val parameters = parametersOf("ss" to listOf(""), "p" to listOf(page.toString()))
            val response = client.submitForm(getGroupPageURL(groupId), parameters, true) {
                handleTooManyRequests()
            }

            val document = Jsoup.parse(response.bodyAsText())
            val students = document.getElementsByAttributeValue("role", "presentation").firstOrNull()
                ?.child(0) ?: continue

            for (student in students.children()) {
                scrapeStudentCourses(student, studentMap, studentPeriods)
            }
        }
    }

    private suspend fun getGroupPageCount(groupId: String): ULong? {
        val response = client.get(getGroupURL(groupId)) {
            handleTooManyRequests()
        }

        val document = Jsoup.parse(response.bodyAsText())
        val studentCount = document.getElementsByClass("total").firstOrNull()?.text()?.toDouble()
            ?.div(STUDENTS_PER_PAGE)
        return if (studentCount != null) ceil(studentCount).toULong() else null
    }

    private fun getGroupURL(groupId: String) = "$BCA_HOST/group/$groupId/members"

    private fun getGroupPageURL(groupId: String) = "$BCA_HOST/enrollments/edit/members/group/$groupId/ajax"

    private suspend fun scrapeStudentCourses(
        student: Element,
        studentMap: MutableMap<String, StudentData>,
        studentPeriods: MutableMap<String, MutableCollection<PeriodData>>
    ) {
        val studentLink = student.child(1).child(0)
        val studentId = studentLink.attr("href").substring("/user/".length)

        if (studentId in studentMap) { // may have been handled by a previous group
            return
        }

        val name = studentLink.text()
        val firstName = name.substringBeforeLast(' ')
        val lastName = name.substringAfterLast(' ')

        val isTeacher = student.child(2).childrenSize() == 1

        val response = client.get(getStudentCoursesURL(studentId)) {
            handleTooManyRequests()
        }

        val document = Jsoup.parse(response.bodyAsText())
        val courses = document.getElementsByClass("my-courses-item-list").firstOrNull() ?: return

        val created = buildList {
            for (course in courses.children()) {
                val parsed = parseCourse(course)
                addAll(parsed)

                for (parsedCourse in parsed) {
                    studentPeriods.getOrPut(studentId, ::mutableListOf).add(parsedCourse)
                }
            }
        }

        val studentType = studentTypeClassifier.classify(isTeacher, created)
        studentMap[studentId] = StudentData(firstName, lastName, isTeacher, studentType)
    }

    private fun getStudentCoursesURL(studentId: String) = "$BCA_HOST/user/$studentId/courses/list"

    private fun parseCourse(course: Element): Iterable<PeriodData> {
        val courseLink = course.child(0).child(1).child(0)
        val schoologyCourseId = courseLink.attr("href").substring("/course/".length)
        val fullCourseName = courseLink.ownText()

        val nameComponents = Regex("\\s*(.+)\\s*:\\s*(.+)\\s*").find(fullCourseName) ?: return emptyList()
        if (nameComponents.groupValues.size != 3) {
            return emptyList()
        }

        val simpleCourseName = nameComponents.groupValues[1]
        val courseType = courseTypeClassifier.classify(simpleCourseName)
        val timesComponent = nameComponents.groupValues[2].replace(" ", "")

        val times = Regex("(\\d+)\\(((?:[A-E](?:-[A-E])?)(?:,(?:[A-E](?:-[A-E])?))*)\\)").findAll(timesComponent)
        return buildList {
            for (time in times) {
                addAll(createPeriods(time, schoologyCourseId, fullCourseName, simpleCourseName, courseType))
            }
        }
    }

    private fun createPeriods(
        time: MatchResult,
        schoologyCourseId: String,
        fullCourseName: String,
        simpleCourseName: String,
        courseType: CourseType,
    ): Iterable<PeriodData> {
        if (time.groups.size != 3) {
            return emptyList()
        }

        val period = time.groupValues[1].toIntOrNull() ?: return emptyList()
        val daysData = time.groupValues[2]

        val days = Regex("(?:[A-E](?:-[A-E])?)").findAll(daysData)
        return buildList {
            for (day in days) {
                if (day.value.length == 1) {
                    val course = parseSingleDay(
                        day,
                        schoologyCourseId,
                        fullCourseName,
                        simpleCourseName,
                        courseType,
                        period
                    )

                    if (course != null) {
                        add(course)
                    }
                } else if (day.value.length == 3) {
                    addAll(parseDayRange(day, schoologyCourseId, fullCourseName, simpleCourseName, courseType, period))
                }
            }
        }
    }

    private fun parseSingleDay(
        day: MatchResult,
        schoologyCourseId: String,
        fullCourseName: String,
        simpleCourseName: String,
        courseType: CourseType,
        period: Int
    ): PeriodData? {
        val dayIndex = DAYS_RANGE.indexOf(day.value[0])
        if (dayIndex == -1) {
            return null
        }

        return PeriodData(
            schoologyCourseId,
            fullCourseName,
            simpleCourseName,
            courseType,
            dayIndex + 1,
            period
        )
    }

    private fun parseDayRange(
        day: MatchResult,
        schoologyCourseId: String,
        fullCourseName: String,
        simpleCourseName: String,
        courseType: CourseType,
        period: Int
    ): Iterable<PeriodData> {
        val startDayIndex = DAYS_RANGE.indexOf(day.value[0])
        if (startDayIndex == -1) {
            return emptyList()
        }

        val endDayIndex = DAYS_RANGE.indexOf(day.value[2])
        if (endDayIndex == -1) {
            return emptyList()
        }

        val range = if (startDayIndex <= endDayIndex) {
            (startDayIndex + 1)..(endDayIndex + 1)
        }
        else {
            (endDayIndex + 1) downTo (startDayIndex + 1)
        }

        return buildList {
            for (index in range) {
                add(PeriodData(
                    schoologyCourseId,
                    fullCourseName,
                    simpleCourseName,
                    courseType,
                    index,
                    period
                ))
            }
        }
    }

}