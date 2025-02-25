package ru.otus.homework.lintchecks


import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.android.tools.lint.checks.infrastructure.TestMode
import org.junit.Test

@Suppress("UnstableApiUsage")
class JobInBuilderRuleTest {

    @Test
    fun `detekt non cancellable Job`() {
        TestLintTask.lint()
            .allowMissingSdk()
            .files(
                LintDetectorTest.kotlin(
                    """
                    package ru.otus.homework.linthomework.jobinbuilderusage
                                        
                    import androidx.lifecycle.ViewModel
                    import androidx.lifecycle.viewModelScope
                    import kotlinx.coroutines.Job
                    import kotlinx.coroutines.NonCancellable
                    import kotlinx.coroutines.delay
                    import kotlinx.coroutines.launch
                    
                    class JobInBuilderTestCase(private val job: Job) : ViewModel() {
                    
                        fun case4() {
                            viewModelScope.launch(NonCancellable) {
                                delay(1000)
                                println("Hello World")
                            }
                        }
                    }
                """.trimIndent()
                ), kotlinxCoroutines, kotlinCoroutines, androidxLifecycle
            )
            .issues(JobInBuilderRule.ISSUE)
            .run()
            .expectErrorCount(1)
    }

    @Test
    fun `detekt SupervisorJob`() {
        TestLintTask.lint()
            .allowMissingSdk()
            .files(
                LintDetectorTest.kotlin(
                    """
                    package ru.otus.homework.linthomework.jobinbuilderusage
                                        
                    import androidx.lifecycle.ViewModel
                    import androidx.lifecycle.viewModelScope
                    import kotlinx.coroutines.Dispatchers
                    import kotlinx.coroutines.Job
                    import kotlinx.coroutines.SupervisorJob
                    import kotlinx.coroutines.delay
                    import kotlinx.coroutines.launch
                    
                    class JobInBuilderTestCase(private val job: Job) : ViewModel() {
                    
                        fun case1() {
                            viewModelScope.launch(SupervisorJob() + Dispatchers.IO) {
                                delay(1000)
                                println("Hello World")
                            }
                        }
                    }
                """.trimIndent()
                ), kotlinxCoroutines, kotlinCoroutines, androidxLifecycle
            )
            .issues(JobInBuilderRule.ISSUE)
            .testModes(TestMode.DEFAULT)
            .run()
            .expectErrorCount(1)
    }

    @Test
    fun `detekt Job`() {
        TestLintTask.lint()
            .allowMissingSdk()
            .files(
                LintDetectorTest.kotlin(
                    """
                    package ru.otus.homework.linthomework.jobinbuilderusage
                                        
                    import androidx.lifecycle.ViewModel
                    import androidx.lifecycle.viewModelScope
                    import kotlinx.coroutines.Job
                    import kotlinx.coroutines.delay
                    import kotlinx.coroutines.launch
                    
                    class JobInBuilderTestCase(private val job: Job) : ViewModel() {
                    
                        fun case2() {
                            viewModelScope.launch(Job()) {
                                delay(1000)
                                println("Hello World")
                            }
                        }
                    }
                """.trimIndent()
                ), kotlinxCoroutines, kotlinCoroutines, androidxLifecycle
            )
            .issues(JobInBuilderRule.ISSUE)
            .run()
            .expectErrorCount(1)
    }

    private companion object {

        val kotlinxCoroutines: TestFile = TestFiles.kotlin(
            """
        package kotlinx.coroutines
        
        import androidx.lifecycle.CoroutineContextStub
        import kotlin.coroutines.CoroutineContext
        
        interface Job : CoroutineContext
        interface CompletableJob : Job
        interface CoroutineScope {
            val coroutineContext: CoroutineContext
        }
        class JobStub: Job {
            override fun plus(context: CoroutineContext): CoroutineContext = CoroutineContextStub()
        }
        fun CoroutineScope.launch(
            context: CoroutineContext,
            block: suspend CoroutineScope.() -> Unit
        ): Job = JobStub()
        suspend fun delay(time: Long)
        fun SupervisorJob(parent: Job? = null) : CompletableJob = JobStub()
        fun Job(parent: Job? = null): Job = JobStub()
        object Dispatchers {
            val IO = CoroutineContextStub()
        }
        object GlobalScope : CoroutineScope
        object NonCancellable : Job {
            override fun plus(context: CoroutineContext): CoroutineContext = CoroutineContextStub()
        }
    """.trimIndent()
        )

        val kotlinCoroutines: TestFile = TestFiles.kotlin(
            """
        package kotlin.coroutines
        
        interface CoroutineContext {
            operator fun plus(context: CoroutineContext): CoroutineContext
        }
    """.trimIndent()
        )

        val androidxLifecycle: TestFile = TestFiles.kotlin(
            """
        package androidx.lifecycle
        
        import kotlinx.coroutines.CoroutineScope
        import kotlin.coroutines.CoroutineContext
        
        abstract class ViewModel
        class CoroutineScopeStub: CoroutineScope {
            override val coroutineContext: CoroutineContext
                get() = CoroutineContextStub()
        }
        class CoroutineContextStub: CoroutineContext {
            override fun plus(context: CoroutineContext): CoroutineContext = CoroutineContextStub()
        }
        val ViewModel.viewModelScope: CoroutineScope
            get() = CoroutineScopeStub()
    """.trimIndent()
        )
    }
}
