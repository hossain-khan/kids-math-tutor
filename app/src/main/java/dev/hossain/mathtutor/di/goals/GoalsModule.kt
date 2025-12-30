package dev.hossain.mathtutor.di.goals

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import dev.hossain.mathtutor.di.AppScope
import dev.hossain.mathtutor.domain.repository.GoalRepository
import dev.hossain.mathtutor.domain.usecase.goals.ActivateGoalUseCase
import dev.hossain.mathtutor.domain.usecase.goals.CompleteGoalUseCase
import dev.hossain.mathtutor.domain.usecase.goals.CreateGoalUseCase
import dev.hossain.mathtutor.domain.usecase.goals.GetGoalAnalyticsUseCase
import dev.hossain.mathtutor.domain.usecase.goals.ResumeGoalUseCase
import dev.hossain.mathtutor.domain.usecase.goals.UpdateGoalProgressUseCase
import dev.zacsweers.metro.SingleIn
import javax.inject.Inject

/**
 * Metro DI module for Goals feature.
 * Provides bindings for repository and use case dependencies.
 *
 * This module is automatically included by the Metro framework
 * and provides all goal-related dependencies to the application.
 *
 * Note: GoalRepository binding is provided via @ContributesBinding on GoalRepositoryImpl
 */
@Module
@ContributesTo(AppScope::class)
object GoalsModule {
    /**
     * Provides the CreateGoalUseCase.
     *
     * @param repository The goal repository
     * @return A new instance of CreateGoalUseCase
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideCreateGoalUseCase(repository: GoalRepository): CreateGoalUseCase = CreateGoalUseCase(repository)

    /**
     * Provides the ActivateGoalUseCase.
     *
     * @param repository The goal repository
     * @return A new instance of ActivateGoalUseCase
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideActivateGoalUseCase(repository: GoalRepository): ActivateGoalUseCase = ActivateGoalUseCase(repository)

    /**
     * Provides the UpdateGoalProgressUseCase.
     *
     * @param repository The goal repository
     * @return A new instance of UpdateGoalProgressUseCase
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideUpdateGoalProgressUseCase(repository: GoalRepository): UpdateGoalProgressUseCase = UpdateGoalProgressUseCase(repository)

    /**
     * Provides the CompleteGoalUseCase.
     *
     * @param repository The goal repository
     * @return A new instance of CompleteGoalUseCase
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideCompleteGoalUseCase(repository: GoalRepository): CompleteGoalUseCase = CompleteGoalUseCase(repository)

    /**
     * Provides the ResumeGoalUseCase.
     *
     * @param repository The goal repository
     * @return A new instance of ResumeGoalUseCase
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideResumeGoalUseCase(repository: GoalRepository): ResumeGoalUseCase = ResumeGoalUseCase(repository)

    /**
     * Provides the GetGoalAnalyticsUseCase.
     *
     * @param repository The goal repository
     * @return A new instance of GetGoalAnalyticsUseCase
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideGetGoalAnalyticsUseCase(repository: GoalRepository): GetGoalAnalyticsUseCase = GetGoalAnalyticsUseCase(repository)
}
